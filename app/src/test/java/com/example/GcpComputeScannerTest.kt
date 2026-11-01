package com.example

import com.example.engine.GcpComputeScanner
import kotlinx.coroutines.runBlocking
import mockwebserver3.Dispatcher
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import mockwebserver3.RecordedRequest
import okhttp3.OkHttpClient
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.junit.Test

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class GcpComputeScannerTest {
    @Test
    fun aggregatedResponsesAreParsedPagedAndKeptReadOnly() = runBlocking {
        val server = MockWebServer()
        val seen = mutableListOf<RecordedRequest>()
        server.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                synchronized(seen) { seen += request }
                val path = request.url.encodedPath
                val response = when {
                    path.endsWith("/aggregated/disks") && request.url.queryParameter("pageToken") == null -> """
                        {"items":{"zones/us-central1-a":{"disks":[
                          {"name":"orphan-disk","sizeGb":"100","type":"zones/us-central1-a/diskTypes/pd-standard","status":"READY","users":[]},
                          {"name":"attached-disk","sizeGb":"40","type":"zones/us-central1-a/diskTypes/pd-balanced","status":"READY","users":["projects/fixture-guardian/zones/us-central1-a/instances/stopped-vm"]}
                        ]}},"nextPageToken":"next-page"}
                    """.trimIndent()
                    path.endsWith("/aggregated/disks") -> """
                        {"items":{"regions/us-central1":{"disks":[
                          {"name":"regional-orphan","sizeGb":"20","type":"regions/us-central1/diskTypes/pd-balanced","status":"READY","users":[]}
                        ]}}}
                    """.trimIndent()
                    path.endsWith("/aggregated/addresses") -> """
                        {"items":{"global":{"addresses":[
                          {"name":"unused-ip","address":"203.0.113.7","addressType":"EXTERNAL","status":"RESERVED","users":[]},
                          {"name":"active-ip","address":"203.0.113.8","addressType":"EXTERNAL","status":"IN_USE","users":["projects/fixture-guardian/global/forwardingRules/web"]}
                        ]}}}
                    """.trimIndent()
                    path.endsWith("/aggregated/instances") -> """
                        {"items":{"zones/us-central1-a":{"instances":[
                          {"name":"stopped-vm","status":"TERMINATED","disks":[
                            {"type":"PERSISTENT","source":"https://www.googleapis.com/compute/v1/projects/fixture-guardian/zones/us-central1-a/disks/attached-disk","diskSizeGb":"40"}
                          ]}
                        ]}}}
                    """.trimIndent()
                    else -> error("Unexpected request: ${request.url}")
                }
                return MockResponse.Builder()
                    .addHeader("Content-Type", "application/json")
                    .body(response)
                    .build()
            }
        }

        server.start()
        try {
            val baseUrl = server.url("/compute/v1/projects").toString().trimEnd('/')
            val (summary, findings) = GcpComputeScanner.runScan(
                projectId = "fixture-guardian",
                accessToken = "test-token",
                apiBaseUrl = baseUrl,
                httpClient = OkHttpClient()
            )

            assertEquals(4, summary.findingsCount)
            assertEquals(setOf("unattached_disk", "unused_ip", "stopped_vm"), findings.map { it.type }.toSet())
            assertTrue(findings.all { it.cloud == "gcp" })
            val stopped = findings.single { it.type == "stopped_vm" }
            assertEquals("1", stopped.evidence.metrics["attached_persistent_disks"])
            assertTrue(stopped.evidence.notes.any { it.contains("Billing data is not connected") })

            val requests = synchronized(seen) { seen.toList() }
            assertEquals(4, requests.size)
            assertTrue(requests.all { it.method == "GET" })
            assertTrue(requests.all { it.headers["Authorization"] == "Bearer test-token" })
            assertTrue(requests.any { it.url.encodedPath.endsWith("/aggregated/disks") && it.url.queryParameter("pageToken") == "next-page" })
            assertTrue(requests.all { it.url.encodedPath.contains("/projects/fixture-guardian/") })
        } finally {
            server.close()
        }
    }

    @Test
    fun permissionErrorsExplainTheRequiredSetup() = runBlocking {
        val server = MockWebServer()
        server.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse = MockResponse.Builder()
                .code(403)
                .addHeader("Content-Type", "application/json")
                .body("""{"error":{"message":"Required permission compute.instances.list"}}""")
                .build()
        }
        server.start()
        try {
            val error = runCatching {
                GcpComputeScanner.runScan(
                    projectId = "fixture-guardian",
                    accessToken = "test-token",
                    apiBaseUrl = server.url("/compute/v1/projects").toString().trimEnd('/'),
                    httpClient = OkHttpClient()
                )
            }.exceptionOrNull()
            assertNotNull(error)
            assertTrue(error?.message.orEmpty().contains("Compute Viewer"))
            assertTrue(error?.message.orEmpty().contains("compute.instances.list"))
            assertFalse(error?.message.orEmpty().contains("test-token"))
        } finally {
            server.close()
        }
    }
}


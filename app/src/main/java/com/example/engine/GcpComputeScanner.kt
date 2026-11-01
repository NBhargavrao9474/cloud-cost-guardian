package com.example.engine

import com.example.ai.ExplanationService
import com.example.data.models.Evidence
import com.example.data.models.Finding
import com.example.data.models.ScanSummary
import com.example.data.models.Severity
import com.example.data.models.SuggestedAction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import okhttp3.HttpUrl.Companion.toHttpUrl
import java.io.IOException
import java.util.UUID
import java.util.concurrent.TimeUnit

/** Read-only Compute Engine inventory checks. Access tokens are used in memory and never persisted. */
object GcpComputeScanner {
    private const val API_BASE = "https://compute.googleapis.com/compute/v1/projects"
    private const val PROJECT_ID_PATTERN = "[a-z][a-z0-9-]{4,28}[a-z0-9]"

    private val http = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .callTimeout(60, TimeUnit.SECONDS)
        .build()

    suspend fun runScan(
        projectId: String,
        accessToken: String,
        onLogMessage: (String) -> Unit = {},
        apiBaseUrl: String = API_BASE,
        httpClient: OkHttpClient = http
    ): Pair<ScanSummary, List<Finding>> = withContext(Dispatchers.IO) {
        require(Regex(PROJECT_ID_PATTERN).matches(projectId)) {
            "Enter a valid Google Cloud project ID (6–30 lowercase letters, digits, and hyphens)."
        }
        require(accessToken.isNotBlank()) { "Google authorization did not return an access token." }

        val startedAt = System.currentTimeMillis()
        onLogMessage("[GCP] Read-only authorization received. Listing Compute Engine inventory...")
        onLogMessage("[GCP] Checking disks, reserved external IP addresses, and instance states.")

        val inventories = coroutineScope {
            listOf("disks", "addresses", "instances").map { resource ->
                async { resource to listAggregated(projectId, resource, accessToken, apiBaseUrl, httpClient) }
            }.awaitAll().toMap()
        }
        val disks = inventories["disks"].orEmpty()
        val addresses = inventories["addresses"].orEmpty()
        val instances = inventories["instances"].orEmpty()
        onLogMessage("[GCP] Inventory received: ${disks.size} disks, ${addresses.size} addresses, ${instances.size} instances.")

        val findings = buildList {
            disks.forEach { scoped ->
                val disk = scoped.item
                val users = disk.optJSONArray("users")
                val sizeGb = disk.optString("sizeGb").toDoubleOrNull() ?: 0.0
                val isReady = disk.optString("status") == "READY"
                if (isReady && (users == null || users.length() == 0) && sizeGb > 0.0) {
                    val name = disk.optString("name")
                    val location = scoped.location
                    val diskType = disk.optString("type").substringAfterLast('/').ifBlank { "pd-standard" }
                    add(
                        liveFinding(
                            projectId = projectId,
                            type = "unattached_disk",
                            name = name,
                            location = location,
                            resourceUrl = "https://console.cloud.google.com/compute/disksDetail/${scoped.scopePath}/disks/$name?project=$projectId",
                            severity = Severity.MEDIUM,
                            evidence = Evidence(
                                metrics = mapOf("size_gb" to sizeGb.toInt().toString(), "disk_type" to diskType, "status" to disk.optString("status"), "users" to "0"),
                                notes = listOf("Live Compute Engine inventory reports no attached instances.", "Billing data is not connected, so no verified cost or savings amount is available.")
                            ),
                            action = SuggestedAction(
                                kind = "review_disk",
                                command = "gcloud compute disks describe $name ${locationArgument(scoped.scopePath, location)} --project=$projectId",
                                reversible = true,
                                risk = "Review disk contents, ownership, snapshots, and dependencies before taking any action. This scan never modifies cloud resources."
                            )
                        )
                    )
                }
            }

            addresses.forEach { scoped ->
                val address = scoped.item
                val isExternal = address.optString("addressType", "EXTERNAL") == "EXTERNAL"
                val unused = address.optString("status") == "RESERVED" &&
                    (address.optJSONArray("users")?.length() ?: 0) == 0
                if (isExternal && unused && address.optString("address").isNotBlank()) {
                    val name = address.optString("name")
                    val location = scoped.location
                    add(
                        liveFinding(
                            projectId = projectId,
                            type = "unused_ip",
                            name = "$name (${address.optString("address")})",
                            location = location,
                            resourceUrl = "https://console.cloud.google.com/networking/addresses/list?project=$projectId",
                            severity = Severity.LOW,
                            evidence = Evidence(
                                metrics = mapOf("address" to address.optString("address"), "status" to "RESERVED", "address_type" to "EXTERNAL", "attached_to" to "None"),
                                notes = listOf("Live Compute Engine inventory reports an unassigned external address.", "Confirm the address is not reserved for a planned service before releasing it.")
                            ),
                            action = SuggestedAction(
                                kind = "review_address",
                                command = "gcloud compute addresses describe $name ${locationArgument(scoped.scopePath, location)} --project=$projectId",
                                reversible = true,
                                risk = "Review ownership and dependencies first. Releasing an address can make its IP unavailable for reuse."
                            )
                        )
                    )
                }
            }

            instances.forEach { scoped ->
                val instance = scoped.item
                if (instance.optString("status") == "TERMINATED") {
                    val attachedDisks = instance.optJSONArray("disks") ?: JSONArray()
                    val attachedPersistentDisks = (0 until attachedDisks.length()).count { index ->
                        attachedDisks.optJSONObject(index)?.optString("type") == "PERSISTENT"
                    }
                    if (attachedPersistentDisks > 0) {
                        val name = instance.optString("name")
                        val location = scoped.location
                        add(
                            liveFinding(
                                projectId = projectId,
                                type = "stopped_vm",
                                name = name,
                                location = location,
                                resourceUrl = "https://console.cloud.google.com/compute/instancesDetail/zones/$location/instances/$name?project=$projectId",
                                severity = Severity.LOW,
                                evidence = Evidence(
                                    metrics = mapOf("status" to "TERMINATED", "attached_persistent_disks" to attachedPersistentDisks.toString()),
                                    notes = listOf("Live Compute Engine inventory reports this instance is terminated and still has persistent disks attached.", "Billing data is not connected; disk charges have not been measured.")
                                ),
                                action = SuggestedAction(
                                    kind = "review_stopped_instance",
                                    command = "gcloud compute instances describe $name --zone=$location --project=$projectId",
                                    reversible = true,
                                    risk = "The VM is already stopped. Review its disks and ownership before deleting or changing anything."
                                )
                            )
                        )
                    }
                }
            }
        }.sortedWith(compareBy<Finding> { it.severity.ordinal }.thenBy { it.resourceName })

        onLogMessage("[GCP] Metric-based idle and rightsizing checks were skipped; Monitoring metrics are not requested.")
        onLogMessage("[GCP:DONE] Read-only inventory scan completed with ${findings.size} actionable review findings.")

        val summary = ScanSummary(
            scanId = UUID.randomUUID().toString().take(8),
            projectName = projectId,
            status = "live_gcp_read_only",
            startedAt = startedAt,
            finishedAt = System.currentTimeMillis(),
            findingsCount = findings.size
        )
        summary to findings
    }

    private fun listAggregated(
        projectId: String,
        resource: String,
        accessToken: String,
        apiBaseUrl: String,
        httpClient: OkHttpClient
    ): List<ScopedResource> {
        val collected = mutableListOf<ScopedResource>()
        var pageToken: String? = null
        do {
            val urlBuilder = "$apiBaseUrl/$projectId/aggregated/$resource".toHttpUrl().newBuilder()
            if (!pageToken.isNullOrBlank()) urlBuilder.addQueryParameter("pageToken", pageToken)
            val url = urlBuilder.build()
            val request = Request.Builder()
                .url(url)
                .header("Authorization", "Bearer $accessToken")
                .header("Accept", "application/json")
                .get()
                .build()
            val response = try {
                httpClient.newCall(request).execute()
            } catch (e: IOException) {
                throw IOException("Could not reach the Google Cloud Compute API. Check the network and try again.", e)
            }
            response.use {
                val bodyText = it.body.string()
                if (!it.isSuccessful) {
                    val apiMessage = runCatching { JSONObject(bodyText).optJSONObject("error")?.optString("message") }.getOrNull()
                    val message = when (it.code) {
                        401 -> "Google authorization expired. Run the live scan again to reauthorize."
                        403 -> "Access denied. Enable the Compute Engine API and grant this account Compute Viewer access to the project. ${apiMessage.orEmpty()}".trim()
                        404 -> "Project or Compute Engine API was not found. Check the project ID and enable the API."
                        else -> "Google Cloud inventory request failed (${it.code}). ${apiMessage.orEmpty()}".trim()
                    }
                    throw IOException(message)
                }
                val json = JSONObject(bodyText)
                val scopes = json.optJSONObject("items") ?: JSONObject()
                val keys = scopes.keys()
                while (keys.hasNext()) {
                    val scopePath = keys.next()
                    val group = scopes.optJSONObject(scopePath) ?: continue
                    // Compute's aggregated-list response stores resources under the collection
                    // name (for example, `disks`), not under a generic `items` property.
                    val items = group.optJSONArray(resource) ?: continue
                    val location = scopePath.substringAfterLast('/').ifBlank { "global" }
                    for (index in 0 until items.length()) {
                        items.optJSONObject(index)?.let { collected += ScopedResource(scopePath, location, it) }
                    }
                }
                pageToken = json.optString("nextPageToken").takeIf { token -> token.isNotBlank() && token != "null" }
            }
        } while (pageToken != null)
        return collected
    }

    private fun liveFinding(
        projectId: String,
        type: String,
        name: String,
        location: String,
        resourceUrl: String,
        severity: Severity,
        evidence: Evidence,
        action: SuggestedAction
    ): Finding {
        val finding = Finding(
            id = UUID.randomUUID().toString(),
            cloud = "gcp",
            checkId = "gcp.$type",
            type = type,
            resourceName = name,
            resourceUrl = resourceUrl,
            project = projectId,
            location = location,
            severity = severity,
            evidence = evidence,
            action = action
        )
        return finding.copy(explanation = ExplanationService.generateDeterministicExplanation(finding))
    }

    private fun locationArgument(scopePath: String, location: String): String = when {
        scopePath == "global" -> "--global"
        scopePath.startsWith("regions/") -> "--region=$location"
        else -> "--zone=$location"
    }

    private data class ScopedResource(
        val scopePath: String,
        val location: String,
        val item: JSONObject
    )
}

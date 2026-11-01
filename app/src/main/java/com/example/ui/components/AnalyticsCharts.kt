package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.Finding
import com.example.data.models.Severity
import com.example.ui.theme.FinOpsAmberWarning
import com.example.ui.theme.FinOpsCyanSecondary
import com.example.ui.theme.FinOpsEmeraldPrimary
import com.example.ui.theme.FinOpsOutlineVariant
import com.example.ui.theme.FinOpsRubyCritical

data class CategorySlice(
    val label: String,
    val count: Int,
    val color: Color
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AnalyticsCharts(
    findings: List<Finding>,
    modifier: Modifier = Modifier
) {
    if (findings.isEmpty()) return

    val totalResources = findings.size

    // Group by category
    val slices = remember(findings) {
        val groups = findings.groupBy { it.type }
        listOf(
            CategorySlice("Unattached disks", groups["unattached_disk"]?.size ?: 0, FinOpsEmeraldPrimary),
            CategorySlice("Stopped instances", groups["stopped_vm"]?.size ?: 0, FinOpsAmberWarning),
            CategorySlice("Unassigned IPs", groups["unused_ip"]?.size ?: 0, FinOpsCyanSecondary)
        ).filter { it.count > 0 }
    }

    val highCount = findings.count { it.severity == Severity.HIGH }
    val medCount = findings.count { it.severity == Severity.MEDIUM }
    val lowCount = findings.count { it.severity == Severity.LOW }

    val animationProgress = remember { Animatable(0f) }
    LaunchedEffect(findings) {
        animationProgress.animateTo(1f, animationSpec = tween(durationMillis = 800))
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, FinOpsOutlineVariant, RoundedCornerShape(16.dp))
            .testTag("analytics_charts_card"),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "INVENTORY FINDINGS BY TYPE",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "${findings.size} Resources",
                    style = MaterialTheme.typography.labelSmall,
                    color = FinOpsCyanSecondary,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Donut Chart & Center Stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(130.dp)
                ) {
                    Canvas(modifier = Modifier.size(120.dp)) {
                        val strokeWidth = 18.dp.toPx()
                        val arcRadius = (size.minDimension - strokeWidth) / 2
                        val centerOffset = Offset(size.width / 2, size.height / 2)
                        val arcSize = Size(arcRadius * 2, arcRadius * 2)
                        val topLeft = Offset(centerOffset.x - arcRadius, centerOffset.y - arcRadius)

                        var currentAngle = -90f
                        slices.forEach { slice ->
                            val sweepAngle = if (totalResources > 0) {
                                ((slice.count.toFloat() / totalResources) * 360f * animationProgress.value)
                            } else 0f

                            if (sweepAngle > 0f) {
                                drawArc(
                                    color = slice.color,
                                    startAngle = currentAngle,
                                    sweepAngle = sweepAngle,
                                    useCenter = false,
                                    topLeft = topLeft,
                                    size = arcSize,
                                    style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
                                )
                                currentAngle += sweepAngle
                            }
                        }
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "$totalResources",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Resources",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Severity summary bars
                Column(modifier = Modifier.padding(start = 8.dp)) {
                    Text(
                        text = "RISK SEVERITY",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    SeverityRow("High", highCount, FinOpsRubyCritical)
                    Spacer(modifier = Modifier.height(4.dp))
                    SeverityRow("Medium", medCount, FinOpsAmberWarning)
                    Spacer(modifier = Modifier.height(4.dp))
                    SeverityRow("Low", lowCount, FinOpsCyanSecondary)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Dynamic Legend
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                slices.forEach { slice ->
                    val pct = (slice.count.toFloat() / totalResources * 100).toInt()
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(slice.color)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "${slice.label} (${slice.count} · $pct%)",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SeverityRow(label: String, count: Int, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.width(140.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Text(
            text = "$count",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CableColor
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/**
 * Beautiful interactive cross-section visual preview of the selected electrical cable,
 * showing the outer colored insulation sheath and inner copper stranded conductor core.
 */
@Composable
fun CableVisualPreview(
    sectionMm2: Double,
    cableColor: CableColor,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp)),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Visual Cross-Section Cut Representation
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF0F172A))
                    .padding(4.dp),
                contentAlignment = Alignment.Center
            ) {
                CableCutCanvas(
                    sectionMm2 = sectionMm2,
                    cableColor = cableColor,
                    modifier = Modifier.size(80.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Cable Specifications Badge Column
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Cabo Flexível ${sectionMm2.toString().replace('.', ',')} mm²",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Condutor de Cobre Eletrolítico (PVC 70°C)",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )

                Text(
                    text = "Tensão 750V / 1kV • NBR NM 247-3 • Cobre Cl. 5",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }
        }
    }
}

@Composable
fun CableCutCanvas(
    sectionMm2: Double,
    cableColor: CableColor,
    modifier: Modifier = Modifier
) {
    // Relative scaling based on section
    val normalizedScale = when {
        sectionMm2 <= 1.0 -> 0.45f
        sectionMm2 <= 2.5 -> 0.55f
        sectionMm2 <= 6.0 -> 0.68f
        sectionMm2 <= 16.0 -> 0.80f
        sectionMm2 <= 50.0 -> 0.90f
        else -> 1.0f
    }

    val animatedScale by animateFloatAsState(
        targetValue = normalizedScale,
        animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing),
        label = "scale"
    )

    Canvas(modifier = modifier) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val maxRadius = min(size.width, size.height) / 2f - 2f
        val outerRadius = maxRadius * animatedScale
        val copperRadius = outerRadius * 0.58f

        // 1. Outer PVC Insulation Layer
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    cableColor.color,
                    cableColor.color.copy(alpha = 0.85f),
                    Color(0xFF0F172A).copy(alpha = 0.4f)
                ),
                center = center,
                radius = outerRadius
            ),
            radius = outerRadius,
            center = center
        )

        // Subtle outer border highlight
        drawCircle(
            color = Color.White.copy(alpha = 0.3f),
            radius = outerRadius,
            center = center,
            style = Stroke(width = 1.5.dp.toPx())
        )

        // 2. Inner core shadow
        drawCircle(
            color = Color.Black.copy(alpha = 0.35f),
            radius = copperRadius + 2.dp.toPx(),
            center = center
        )

        // 3. Copper Base
        val copperColor1 = Color(0xFFF59E0B) // Bright Copper
        val copperColor2 = Color(0xFFD97706) // Deep Copper
        val copperColor3 = Color(0xFFB45309) // Dark Copper

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(copperColor1, copperColor2, copperColor3),
                center = center - Offset(copperRadius * 0.2f, copperRadius * 0.2f),
                radius = copperRadius
            ),
            radius = copperRadius,
            center = center
        )

        // 4. Multiple copper strands representation (Class 5 flexible conductors)
        val strandCount = if (sectionMm2 >= 10.0) 12 else 7
        val strandRadius = copperRadius / 3.4f

        // Center strand
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFFFDE68A), copperColor2),
                center = center,
                radius = strandRadius
            ),
            radius = strandRadius,
            center = center
        )
        drawCircle(
            color = Color(0xFF78350F),
            radius = strandRadius,
            center = center,
            style = Stroke(width = 0.8.dp.toPx())
        )

        // Outer ring of copper strands
        val ringRadius = copperRadius - strandRadius - 1.dp.toPx()
        for (i in 0 until strandCount) {
            val angle = (2 * Math.PI * i / strandCount).toFloat()
            val strandCenter = Offset(
                center.x + ringRadius * cos(angle),
                center.y + ringRadius * sin(angle)
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFFFEF3C7), copperColor1, copperColor3),
                    center = strandCenter - Offset(strandRadius * 0.2f, strandRadius * 0.2f),
                    radius = strandRadius
                ),
                radius = strandRadius,
                center = strandCenter
            )
            drawCircle(
                color = Color(0xFF78350F).copy(alpha = 0.8f),
                radius = strandRadius,
                center = strandCenter,
                style = Stroke(width = 0.7.dp.toPx())
            )
        }
    }
}

package com.example.drivesafer.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Komponen yang menampilkan skor mengemudi dalam bentuk lingkaran
 * dengan animasi yang menunjukkan persentase skor.
 */
@Composable
fun ScoreCircle(
    score: Int,
    maxScore: Int = 100,
    size: Dp = 200.dp,
    thickness: Dp = 20.dp,
    animDuration: Int = 1000,
    scoreColor: Color,
    backgroundColor: Color = Color.LightGray.copy(alpha = 0.3f)
) {
    // Calculate percentage
    val percentage = score.toFloat() / maxScore.toFloat()

    // Animate the percentage
    var animationPlayed by remember { mutableStateOf(false) }
    val currentPercentage = animateFloatAsState(
        targetValue = if (animationPlayed) percentage else 0f,
        animationSpec = tween(durationMillis = animDuration),
        label = "percentage"
    ).value

    LaunchedEffect(key1 = score) {
        animationPlayed = true
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(size)
    ) {
        // Background and progress circles
        Canvas(modifier = Modifier.size(size)) {
            // Background circle
            drawArc(
                color = backgroundColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = thickness.toPx(), cap = StrokeCap.Round)
            )

            // Progress arc
            drawArc(
                color = scoreColor,
                startAngle = -90f,
                sweepAngle = 360f * currentPercentage,
                useCenter = false,
                style = Stroke(width = thickness.toPx(), cap = StrokeCap.Round)
            )
        }

        // Score text and label
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = (currentPercentage * maxScore).toInt().toString(),
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = "SCORE",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}

/**
 * Komponen yang menampilkan skor mengemudi dengan keterangan deskriptif
 */
@Composable
fun ScoreDisplay(
    score: Int,
    scoreColor: Color,
    scoreDescription: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ScoreCircle(
            score = score,
            scoreColor = scoreColor,
            size = 220.dp,
            thickness = 25.dp
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Driving Quality: $scoreDescription",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = scoreColor
        )
    }
}
package com.smit.web.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.Font
import smitweb.composeapp.generated.resources.*

@Composable
fun jetbrainsMonoFontFamily() = FontFamily(
    Font(Res.font.JetBrainsMono_Regular, FontWeight.Normal),
    Font(Res.font.JetBrainsMono_Medium, FontWeight.Medium),
    Font(Res.font.JetBrainsMono_Bold, FontWeight.Bold),
    Font(Res.font.JetBrainsMono_SemiBold, FontWeight.SemiBold),
    Font(Res.font.JetBrainsMono_ExtraBold, FontWeight.ExtraBold),
    Font(Res.font.JetBrainsMono_Light, FontWeight.Light)
)

@Composable
fun typography(): Typography {
    val jetbrainsMono = jetbrainsMonoFontFamily()
    return Typography(
        headlineLarge = TextStyle(
            fontFamily = jetbrainsMono,
            fontWeight = FontWeight.Bold,
            fontSize = 32.sp
        ),
        headlineSmall = TextStyle(
            fontFamily = jetbrainsMono,
            fontWeight = FontWeight.SemiBold,
            fontSize = 24.sp
        ),
        bodyLarge = TextStyle(
            fontFamily = jetbrainsMono,
            fontWeight = FontWeight.Normal,
            fontSize = 16.sp,
            lineHeight = 24.sp,
            letterSpacing = 0.5.sp
        ),
        bodySmall = TextStyle(
            fontFamily = jetbrainsMono,
            fontWeight = FontWeight.Light,
            fontSize = 12.sp,
        ),
        labelSmall = TextStyle(
            fontFamily = jetbrainsMono,
            fontWeight = FontWeight.Medium,
            fontSize = 10.sp
        )
    )
}
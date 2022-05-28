package gr.stavros_melidoniotis.eortologio.ui.theme

import androidx.wear.compose.material.MaterialTheme
import androidx.compose.runtime.Composable

@Composable
fun EortologioTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colors = wearColorPalette,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}
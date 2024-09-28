package com.eriktrummal.scalefinder.ui.views

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eriktrummal.scalefinder.R
import com.eriktrummal.scalefinder.ui.components.BackgroundImage

@Composable
fun SettingsView(modifier: Modifier = Modifier) {
    BackgroundImage(
        backgroundImageRes = R.drawable.bg6,
        backgroundTint = colorResource(id = R.color.background_image_tint),
        backgroundOpacity = 0.4f
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SettingItem(icon = R.drawable.baseline_info_outline_24, text = "About This App")
            SettingItem(icon = R.drawable.baseline_app_shortcut_24, text = "Support Me")
            SettingItem(icon = R.drawable.baseline_rate_review_24, text = "Give Feedback")
            SettingItem(icon = R.drawable.baseline_settings_backup_restore_24, text = "Restore Default Settings")
        }
    }
}

@Composable
private fun SettingItem(icon: Int, text: String) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = icon),
                contentDescription = null,
                modifier = Modifier.padding(end = 16.dp),
                tint = Color.White
            )
            Text(text = text, fontSize = 20.sp, color = Color.White)
        }
        Divider(color = Color.Gray.copy(alpha = 0.5f))
    }
}

@Composable
private fun Divider(color: Color) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(color)
    )
}
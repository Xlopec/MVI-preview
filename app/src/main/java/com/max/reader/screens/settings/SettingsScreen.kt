package com.max.reader.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brightness3
import androidx.compose.material.icons.filled.Brightness5
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.max.reader.app.AppState
import com.max.reader.app.message.Message
import com.max.reader.app.message.ToggleDarkMode

@Composable
fun SettingsScreen(
    innerPadding: PaddingValues,
    state: AppState,
    onMessage: (Message) -> Unit,
) {
    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .fillMaxSize()
            .padding(innerPadding)
    ) {

        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                Icon(
                    contentDescription = "Dark Mode",
                    imageVector = if (state.isDarkModeEnabled) Icons.Default.Brightness3 else Icons.Default.Brightness5
                )

                Column {
                    Text(
                        text = "Dark mode",
                        style = MaterialTheme.typography.subtitle1
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "${if (state.isDarkModeEnabled) "Dis" else "En"}ables dark mode in the app",
                        style = MaterialTheme.typography.body1
                    )
                }

                Spacer(modifier = Modifier.weight(weight = 1f, fill = false))

                Switch(
                    checked = state.isDarkModeEnabled,
                    onCheckedChange = { onMessage(ToggleDarkMode) }
                )
            }
            Divider()
        }
    }
}

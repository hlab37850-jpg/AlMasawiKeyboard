package com.almasawi.keyboard.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.almasawi.keyboard.layouts.DigitStyle

@Composable
fun SettingsScreen(vm: SettingsViewModel = viewModel()) {
    val s by vm.settings.collectAsState()
    var height by remember(s.keyHeightPercent) { mutableFloatStateOf(s.keyHeightPercent.toFloat()) }

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        Text("الإعدادات", style = MaterialTheme.typography.headlineSmall)

        SwitchRow("الاهتزاز عند الضغط", s.haptic) { v -> vm.update { it.copy(haptic = v) } }
        SwitchRow("صوت الضغط", s.sound) { v -> vm.update { it.copy(sound = v) } }
        SwitchRow("صف الأرقام", s.numberRow) { v -> vm.update { it.copy(numberRow = v) } }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("شكل الأرقام")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = s.digitStyle == DigitStyle.WESTERN,
                    onClick = { vm.update { it.copy(digitStyle = DigitStyle.WESTERN) } },
                    label = { Text("1 2 3") },
                )
                FilterChip(
                    selected = s.digitStyle == DigitStyle.ARABIC_INDIC,
                    onClick = { vm.update { it.copy(digitStyle = DigitStyle.ARABIC_INDIC) } },
                    label = { Text("١ ٢ ٣") },
                )
            }
        }

        Column {
            Text("ارتفاع المفاتيح: ${height.toInt()}%")
            Slider(
                value = height,
                onValueChange = { height = it },
                onValueChangeFinished = { vm.update { it.copy(keyHeightPercent = height.toInt()) } },
                valueRange = 80f..130f,
            )
        }
    }
}

@Composable
private fun SwitchRow(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(label, Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onChange)
    }
}

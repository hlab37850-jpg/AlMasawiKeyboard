package com.almasawi.keyboard.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.almasawi.keyboard.keyboard.KeyboardView
import com.almasawi.keyboard.layouts.LayoutFactory
import com.almasawi.keyboard.layouts.LayoutId
import com.almasawi.keyboard.themes.KeyboardTheme
import com.almasawi.keyboard.themes.ThemeCatalog

@Composable
fun ThemesScreen(vm: SettingsViewModel = viewModel()) {
    val s by vm.settings.collectAsState()
    var picked by remember(s.themeId) { mutableStateOf(s.themeId) }
    val theme = ThemeCatalog.byId(picked)
    val layout = remember(s.digitStyle, s.numberRow) {
        LayoutFactory.build(LayoutId.ARABIC, s.digitStyle, s.numberRow)
    }

    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("معاينة مباشرة", style = MaterialTheme.typography.titleMedium)
        AndroidView(
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)),
            factory = { KeyboardView(it) },
            update = {
                it.keyboardTheme = theme
                it.layout = layout
                it.heightScale = s.keyHeightPercent / 100f
            },
        )
        LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(ThemeCatalog.all, key = { it.id }) { t ->
                ThemeRow(t, selected = t.id == picked) { picked = t.id }
            }
        }
        Button(
            onClick = { vm.update { it.copy(themeId = picked) } },
            enabled = picked != s.themeId,
            modifier = Modifier.fillMaxWidth(),
        ) { Text(if (picked == s.themeId) "الثيم المطبّق" else "تطبيق الثيم") }
    }
}

@Composable
private fun ThemeRow(t: KeyboardTheme, selected: Boolean, onClick: () -> Unit) {
    val border = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent
    Row(
        Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(2.dp, border, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Swatch(t.background); Swatch(t.key); Swatch(t.accent)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(t.nameAr)
            Text(t.nameEn, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun Swatch(argb: Int) {
    Box(
        Modifier.padding(end = 4.dp).size(22.dp).clip(CircleShape)
            .background(Color(argb)).border(1.dp, Color(0x55FFFFFF), CircleShape),
    )
}

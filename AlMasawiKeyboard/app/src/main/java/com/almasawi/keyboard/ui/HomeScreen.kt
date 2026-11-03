package com.almasawi.keyboard.ui

import android.content.Intent
import android.provider.Settings
import android.view.inputmethod.InputMethodManager
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.almasawi.keyboard.ime.ImeStatus

@Composable
fun HomeScreen() {
    val ctx = LocalContext.current
    val owner = LocalLifecycleOwner.current
    var tick by remember { mutableIntStateOf(0) }
    DisposableEffect(owner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) tick++
        }
        owner.lifecycle.addObserver(observer)
        onDispose { owner.lifecycle.removeObserver(observer) }
    }
    val enabled = remember(tick) { ImeStatus.isEnabled(ctx) }
    val selected = remember(tick) { ImeStatus.isSelected(ctx) }
    var sample by rememberSaveable { mutableStateOf("") }

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text("لوحة مفاتيح المساوى", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text("اكتب بإبداع... وتحدث بلا حدود", color = MaterialTheme.colorScheme.primary)

        StepRow(1, "فعّل لوحة المساوى من إعدادات أندرويد", done = enabled, actionLabel = "تفعيل", actionEnabled = true) {
            ctx.startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS))
        }
        StepRow(2, "اختر لوحة المساوى كلوحة المفاتيح الحالية", done = selected, actionLabel = "اختيار", actionEnabled = enabled) {
            ctx.getSystemService(InputMethodManager::class.java)?.showInputMethodPicker()
        }

        OutlinedTextField(
            value = sample,
            onValueChange = { sample = it },
            label = { Text("جرّب الكتابة هنا") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
        )
    }
}

@Composable
private fun StepRow(
    number: Int,
    text: String,
    done: Boolean,
    actionLabel: String,
    actionEnabled: Boolean,
    onAction: () -> Unit,
) {
    Surface(shape = RoundedCornerShape(14.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
        Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("$number", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(12.dp))
            Text(text, Modifier.weight(1f))
            if (done) {
                Icon(Icons.Default.CheckCircle, contentDescription = "تم", tint = MaterialTheme.colorScheme.primary)
            } else {
                Button(onClick = onAction, enabled = actionEnabled) { Text(actionLabel) }
            }
        }
    }
}

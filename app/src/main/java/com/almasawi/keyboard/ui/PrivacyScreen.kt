package com.almasawi.keyboard.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun PrivacyScreen() {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text("الخصوصية", style = MaterialTheme.typography.headlineSmall)
        Text("هذا الإصدار يعمل بالكامل على جهازك. لا يطلب التطبيق صلاحية الإنترنت ولا الميكروفون ولا التخزين، ولذلك لا يستطيع إرسال أي شيء تكتبه إلى أي جهة.")
        Text("لا تُسجَّل ضغطات المفاتيح ولا تُحفظ النصوص التي تكتبها. ما يُحفظ على الجهاز هو إعدادات المظهر فقط: الثيم، الاهتزاز، الصوت، صف الأرقام، شكل الأرقام وارتفاع المفاتيح.")
        Text("حقول كلمات المرور تُعامل كحقول حساسة. تُفتح فيها اللوحة الإنجليزية افتراضياً، ولن يُخزَّن أو يُرسل أي شيء منها في أي ميزة حالية أو مستقبلية.")
        Text("عند إضافة ميزات الذكاء الاصطناعي السحابية لاحقاً ستكون اختيارية ومعطّلة افتراضياً، وسيُطلب منك الموافقة قبل إرسال أي نص، مع إبقاء الوضع المحلي الكامل متاحاً.")
        Text("لوحة مفاتيح المساوى — الإصدار 0.1.0", color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

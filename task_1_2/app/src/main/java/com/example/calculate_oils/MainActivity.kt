package com.example.calculate_oils

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                OilCombustionCalculatorScreen()
            }
        }
    }
}

@Composable
fun OilCombustionCalculatorScreen() {
    var carbon by remember { mutableStateOf("") }
    var hydrogen by remember { mutableStateOf("") }
    var sulfur by remember { mutableStateOf("") }
    var oxygen by remember { mutableStateOf("") }
    var moisture by remember { mutableStateOf("") }
    var ash by remember { mutableStateOf("") }
    var vanadium by remember { mutableStateOf("") }
    var lowerHeatingValue by remember { mutableStateOf("") }
    var result by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Калькулятор для мазуту",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        FuelInputField(label = "Вуглець (C, %)", value = carbon) { carbon = it }
        FuelInputField(label = "Водень (H, %)", value = hydrogen) { hydrogen = it }
        FuelInputField(label = "Сірка (S, %)", value = sulfur) { sulfur = it }
        FuelInputField(label = "Кисень (O, %)", value = oxygen) { oxygen = it }
        FuelInputField(label = "Волога (W, %)", value = moisture) { moisture = it }
        FuelInputField(label = "Зола (A, %)", value = ash) { ash = it }
        FuelInputField(label = "Ванадій (V, ppm)", value = vanadium) { vanadium = it }
        FuelInputField(label = "Нижча теплота згоряння (Q, кДж/кг)", value = lowerHeatingValue) { lowerHeatingValue = it }

        Button(
            onClick = {
                val c = carbon.toDoubleOrNull() ?: 0.0
                val h = hydrogen.toDoubleOrNull() ?: 0.0
                val s = sulfur.toDoubleOrNull() ?: 0.0
                val o = oxygen.toDoubleOrNull() ?: 0.0
                val w = moisture.toDoubleOrNull() ?: 0.0
                val a = ash.toDoubleOrNull() ?: 0.0
                val v = vanadium.toDoubleOrNull() ?: 0.0
                val lhValue = lowerHeatingValue.toDoubleOrNull() ?: 0.0

                result = OilCompositionCalculator(c, h, o, s, a, v, w, lhValue)
            },
            modifier = Modifier
                .padding(top = 16.dp)
                .width(200.dp)
                .height(48.dp)
        ) {
            Text(text = "Обчислити")
        }

        if (result.isNotEmpty()) {
            Text(text = result, modifier = Modifier.padding(top = 16.dp))
        }

        Spacer(modifier = Modifier.height(64.dp))
    }
}

fun OilCompositionCalculator(
    carbon: Double, hydrogen: Double, oxygen: Double, sulfur: Double,
    ash: Double, vanadium: Double, moisture: Double, lowerHeatingValue: Double
): String {
    val carbonWork = carbon * (100 - moisture - ash) / 100
    val hydrogenWork = hydrogen * (100 - moisture - ash) / 100
    val oxygenWork = oxygen * (100 - moisture - ash) / 100
    val sulfurWork = sulfur * (100 - moisture - ash) / 100
    val ashWork = ash * (100 - moisture) / 100
    val vanadiumWork = vanadium * (100 - moisture) / 100

    val lowerHeatingValueWork = lowerHeatingValue * (100 - moisture - ash) / 100 - 0.025 * moisture

    return """
        Склад робочої маси мазуту:
        Вуглець (C) = ${String.format("%.2f", carbonWork)}%
        Водень (H) = ${String.format("%.2f", hydrogenWork)}%
        Кисень (O) = ${String.format("%.2f", oxygenWork)}%
        Сірка (S) = ${String.format("%.2f", sulfurWork)}%
        Зола (A) = ${String.format("%.2f", ashWork)}%
        Ванадій (V) = ${String.format("%.2f", vanadiumWork)} ppm

        Нижча теплота згоряння (робоча маса):
        Qр = ${String.format("%.2f", lowerHeatingValueWork)} МДж/кг
    """.trimIndent()
}

@Composable
fun FuelInputField(label: String, value: String, onValueChange: (String) -> Unit) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(text = label, style = TextStyle(fontSize = 16.sp))
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .border(1.dp, Color.Gray)
                .padding(8.dp)
                .background(Color.White),
            textStyle = TextStyle(fontSize = 16.sp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun OilCombustionCalculatorPreview() {
    MaterialTheme {
        OilCombustionCalculatorScreen()
    }
}

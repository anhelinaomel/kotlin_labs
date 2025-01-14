package com.example.electricitylines

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlin.math.pow
import kotlin.math.sqrt

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                VoltageDropCalculatorScreen()
            }
        }
    }
}

@Composable
fun VoltageDropCalculatorScreen() {
    var cableLength by remember { mutableStateOf("") }
    var currentLoad by remember { mutableStateOf("") }
    var cableResistance by remember { mutableStateOf("") }
    var cableReactance by remember { mutableStateOf("") }
    var voltage by remember { mutableStateOf("") }
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
            text = "Розрахунок падіння напруги у кабелі",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        InputField(label = "Довжина кабелю (м)", value = cableLength) { cableLength = it }
        InputField(label = "Навантаження струму (А)", value = currentLoad) { currentLoad = it }
        InputField(label = "Активний опір кабелю (Ом/км)", value = cableResistance) { cableResistance = it }
        InputField(label = "Реактивний опір кабелю (Ом/км)", value = cableReactance) { cableReactance = it }
        InputField(label = "Напруга мережі (кВ)", value = voltage) { voltage = it }

        Button(onClick = {
            result = calculateVoltageDrop(
                cableLength = cableLength.toDoubleOrNull() ?: 0.0,
                currentLoad = currentLoad.toDoubleOrNull() ?: 0.0,
                cableResistance = cableResistance.toDoubleOrNull() ?: 0.0,
                cableReactance = cableReactance.toDoubleOrNull() ?: 0.0,
                voltage = voltage.toDoubleOrNull() ?: 0.0
            )
        }) {
            Text("Розрахувати")
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (result.isNotEmpty()) {
            Text(result, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(top = 16.dp))
        }
    }
}

@Composable
fun InputField(label: String, value: String, onValueChange: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.fillMaxWidth()) {
        Text(text = label, style = MaterialTheme.typography.bodyLarge)
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .background(Color(0xFFF1F1F1))
                .padding(horizontal = 8.dp, vertical = 16.dp),
            textStyle = MaterialTheme.typography.bodyLarge
        )
    }
}

fun calculateVoltageDrop(
    cableLength: Double,
    currentLoad: Double,
    cableResistance: Double,
    cableReactance: Double,
    voltage: Double
): String {
    if (cableLength <= 0 || currentLoad <= 0 || cableResistance <= 0 || cableReactance <= 0 || voltage <= 0) {
        return "Перевірте правильність введених даних!"
    }

    val totalResistance = cableResistance * cableLength / 1000
    val totalReactance = cableReactance * cableLength / 1000
    val impedance = sqrt(totalResistance.pow(2) + totalReactance.pow(2))
    val voltageDrop = 2 * currentLoad * impedance
    val voltageDropPercentage = (voltageDrop / (voltage * 1000)) * 100

    return """
        Загальний активний опір: ${String.format("%.2f", totalResistance)} Ом
        Загальний реактивний опір: ${String.format("%.2f", totalReactance)} Ом
        Повний опір кабелю: ${String.format("%.2f", impedance)} Ом
        Падіння напруги: ${String.format("%.2f", voltageDrop)} В
        Відсоток падіння напруги: ${String.format("%.2f", voltageDropPercentage)}%
    """.trimIndent()
}

package com.example.electricity_reliability

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.rememberScrollState
import kotlin.math.pow

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                ReliabilityCalculatorScreen()
            }
        }
    }
}

@Composable
fun ReliabilityCalculatorScreen() {
    // Поля для завдання 1
    var maxDowntimeCoefficient by remember { mutableStateOf("") }
    val frequencies = remember { mutableStateListOf<String>() }
    val restorationTimes = remember { mutableStateListOf<String>() }

    // Поля для завдання 2
    var accidentLosses by remember { mutableStateOf("") }
    var plannedLosses by remember { mutableStateOf("") }
    var failureFrequency by remember { mutableStateOf("") }
    var averageRestorationTime by remember { mutableStateOf("") }
    var plannedDowntimeCoefficient by remember { mutableStateOf("") }
    var maxSystemPower by remember { mutableStateOf("") }
    var calculationPeriod by remember { mutableStateOf("") }

    var resultTask1 by remember { mutableStateOf("") }
    var resultTask2 by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Калькулятор надійності систем",
            style = MaterialTheme.typography.titleLarge
        )

        // Поля для завдання 1
        Text("Завдання 1: Порівняння надійності систем")
        InputField(label = "Макс. коефіцієнт простою (год.)", value = maxDowntimeCoefficient) {
            maxDowntimeCoefficient = it
        }
        frequencies.forEachIndexed { index, frequency ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                InputField(
                    label = "Частота відмов ${index + 1} (рік^-1)",
                    value = frequency,
                    onValueChange = { frequencies[index] = it }
                )
                InputField(
                    label = "Час відновлення ${index + 1} (год.)",
                    value = restorationTimes.getOrElse(index) { "" },
                    onValueChange = {
                        if (index < restorationTimes.size) {
                            restorationTimes[index] = it
                        } else {
                            restorationTimes.add(it)
                        }
                    }
                )
            }
        }

        Button(onClick = {
            frequencies.add("")
            restorationTimes.add("")
        }) {
            Text(text = "Додати запис")
        }

        Button(onClick = {
            resultTask1 = calculateReliability(
                maxDowntimeCoefficient.toDoubleOrNull() ?: 0.0,
                frequencies.map { it.toDoubleOrNull() ?: 0.0 },
                restorationTimes.map { it.toDoubleOrNull() ?: 0.0 }
            )
        }) {
            Text(text = "Розрахувати Завдання 1")
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (resultTask1.isNotEmpty()) {
            Text(text = resultTask1, modifier = Modifier.padding(top = 16.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Поля для завдання 2
        Text("Завдання 2: Розрахунок збитків")
        InputField(label = "Збитки у разі аварійних вимкнень (грн./кВт·год)", value = accidentLosses) {
            accidentLosses = it
        }
        InputField(label = "Збитки у разі планових вимкнень (грн./кВт·год)", value = plannedLosses) {
            plannedLosses = it
        }
        InputField(label = "Частота відмов (рік^-1)", value = failureFrequency) {
            failureFrequency = it
        }
        InputField(label = "Середній час відновлення (год.)", value = averageRestorationTime) {
            averageRestorationTime = it
        }
        InputField(label = "Коефіцієнт планового простою", value = plannedDowntimeCoefficient) {
            plannedDowntimeCoefficient = it
        }
        InputField(label = "Середньомаксимальна потужність системи (кВт)", value = maxSystemPower) {
            maxSystemPower = it
        }
        InputField(label = "Тривалість розрахункового періоду (год.)", value = calculationPeriod) {
            calculationPeriod = it
        }

        Button(onClick = {
            resultTask2 = calculateLossesFromPowerOutages(
                accidentLosses.toDoubleOrNull() ?: 0.0,
                plannedLosses.toDoubleOrNull() ?: 0.0,
                failureFrequency.toDoubleOrNull() ?: 0.0,
                averageRestorationTime.toDoubleOrNull() ?: 0.0,
                plannedDowntimeCoefficient.toDoubleOrNull() ?: 0.0,
                maxSystemPower.toDoubleOrNull() ?: 0.0,
                calculationPeriod.toDoubleOrNull() ?: 0.0
            )
        }) {
            Text(text = "Розрахувати Завдання 2")
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (resultTask2.isNotEmpty()) {
            Text(text = resultTask2, modifier = Modifier.padding(top = 16.dp))
        }
    }
}

@Composable
fun InputField(label: String, value: String, onValueChange: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(text = label, style = MaterialTheme.typography.bodyLarge)
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .border(1.dp, Color.Gray)
                .padding(8.dp)
                .background(Color.White),
            textStyle = MaterialTheme.typography.bodyLarge
        )
    }
}

fun calculateReliability(
    maxDowntimeCoefficient: Double,
    failureFrequencies: List<Double>,
    restorationTimes: List<Double>
): String {
    if (failureFrequencies.isEmpty() || restorationTimes.isEmpty()) return "Перевірте правильність введених даних!"

    val totalFailureFrequency = failureFrequencies.sum()
    val averageRestorationTime = failureFrequencies.zip(restorationTimes).sumOf { it.first * it.second } / totalFailureFrequency

    val failureDowntimeCoefficient = (totalFailureFrequency * averageRestorationTime) / 8760
    val plannedDowntimeCoefficient = 1.2 * (maxDowntimeCoefficient / 8760)

    val doubleCircuitFailureFrequency = 2 * totalFailureFrequency * (failureDowntimeCoefficient + plannedDowntimeCoefficient) + 0.02

    return """
        Частота відмов одноколової системи: ${String.format("%.2f", totalFailureFrequency)} рік^-1
        Частота відмов двоколової системи: ${String.format("%.2f", doubleCircuitFailureFrequency)} рік^-1
        ${if (doubleCircuitFailureFrequency < totalFailureFrequency) "Двоколова система надійніша." else "Одноколова система надійніша."}
    """.trimIndent()
}

fun calculateLossesFromPowerOutages(
    accidentLosses: Double, plannedLosses: Double, failureFrequency: Double,
    averageRestorationTime: Double, plannedDowntimeCoefficient: Double,
    maxSystemPower: Double, calculationPeriod: Double
): String {
    val accidentalPowerOutages = failureFrequency * averageRestorationTime * maxSystemPower * calculationPeriod
    val plannedPowerOutages = plannedDowntimeCoefficient * maxSystemPower * calculationPeriod
    val expectedLosses = accidentLosses * accidentalPowerOutages + plannedLosses * plannedPowerOutages

    return "Математичне сподівання збитків: ${String.format("%.2f", expectedLosses)} грн"
}

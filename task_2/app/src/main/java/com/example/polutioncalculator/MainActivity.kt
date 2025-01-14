package com.example.polutioncalculator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import kotlin.math.pow

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                PollutionCalculatorScreen()
            }
        }
    }
}

@Composable
fun PollutionCalculatorScreen() {
    var selectedFuel by remember { mutableStateOf("") }
    var fuelMass by remember { mutableStateOf("") }
    var result by remember { mutableStateOf("") }

    val fuelOptions = listOf(
        "Вугілля" to "coal",
        "Мазут" to "oils",
        "Природний газ" to "gas"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Калькулятор забруднень",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Text("Оберіть паливо:")
        fuelOptions.forEach { (label, value) ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = selectedFuel == value,
                    onClick = { selectedFuel = value }
                )
                Text(text = label)
            }
        }

        FuelInputField(
            label = "Маса палива (тонн або тис. м³)",
            value = fuelMass
        ) { fuelMass = it }

        Button(
            onClick = {
                result = calculatePollution(selectedFuel, fuelMass.toDoubleOrNull() ?: 0.0)
            },
            enabled = selectedFuel.isNotEmpty() && fuelMass.isNotEmpty()
        ) {
            Text(text = "Обчислити")
        }

        if (result.isNotEmpty()) {
            Text(text = result, modifier = Modifier.padding(top = 16.dp))
        }

        Spacer(modifier = Modifier.height(64.dp))
    }
}

@Composable
fun FuelInputField(label: String, value: String, onValueChange: (String) -> Unit) {
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
            textStyle = MaterialTheme.typography.bodyLarge,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
    }
}

fun calculatePollution(fuel: String, mass: Double): String {
    val cleaningEfficiency = 0.985
    val lowerHeatValues = mapOf("coal" to 20.47, "oils" to 39.48, "gas" to 33.08)
    val ashFractions = mapOf("coal" to 25.20, "oils" to 0.15, "gas" to 0.0)
    val flyAshRatios = mapOf("coal" to 0.8, "oils" to 1.0, "gas" to 0.0)
    val unburnedCombustibles = mapOf("coal" to 1.5, "oils" to 0.0, "gas" to 0.0)

    val heatValue = lowerHeatValues[fuel] ?: return "Неправильно обрано паливо!"
    val ashFraction = ashFractions[fuel] ?: 0.0
    val flyAshRatio = flyAshRatios[fuel] ?: 0.0
    val unburned = unburnedCombustibles[fuel] ?: 0.0

    val emissionRate = (1_000_000 / heatValue) * flyAshRatio *
            (ashFraction / (100 - unburned)) *
            (1 - cleaningEfficiency)

    val grossEmissions = 1e-6 * emissionRate * heatValue * mass

    return """
        Показник емісії: ${String.format("%.2f", emissionRate)} г/ГДж
        Валовий викид: ${String.format("%.2f", grossEmissions)} т
    """.trimIndent()
}

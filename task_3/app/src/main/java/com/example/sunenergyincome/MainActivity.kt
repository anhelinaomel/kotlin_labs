package com.example.sunenergyincome

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import kotlin.math.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                SolarEnergyProfitCalculator()
            }
        }
    }
}

@Composable
fun SolarEnergyProfitCalculator() {
    var averageDailyPower by remember { mutableStateOf("") }
    var standardDeviation by remember { mutableStateOf("") }
    var forecastErrorPercentage by remember { mutableStateOf("") }
    var energyTariff by remember { mutableStateOf("") }
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
            text = "Калькулятор прибутку сонячної енергії",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        InputField(label = "Середньодобова потужність (МВт)", value = averageDailyPower) {
            averageDailyPower = it
        }
        InputField(label = "Стандартне відхилення (МВт)", value = standardDeviation) {
            standardDeviation = it
        }
        InputField(label = "Похибка прогнозу (%)", value = forecastErrorPercentage) {
            forecastErrorPercentage = it
        }
        InputField(label = "Тариф на електроенергію (грн/кВт·год)", value = energyTariff) {
            energyTariff = it
        }

        Button(
            onClick = {
                result = calculateSolarEnergyProfit(
                    forecastErrorPercentage = forecastErrorPercentage.toDoubleOrNull() ?: 0.0,
                    standardDeviation = standardDeviation.toDoubleOrNull() ?: 0.0,
                    averageDailyPower = averageDailyPower.toDoubleOrNull() ?: 0.0,
                    energyTariff = energyTariff.toDoubleOrNull() ?: 0.0
                )
            },
            enabled = averageDailyPower.isNotEmpty() && standardDeviation.isNotEmpty() &&
                    forecastErrorPercentage.isNotEmpty() && energyTariff.isNotEmpty()
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

fun calculateSolarEnergyProfit(
    forecastErrorPercentage: Double,
    standardDeviation: Double,
    averageDailyPower: Double,
    energyTariff: Double
): String {
    val energyShareWithoutImbalances = calculateEnergyShareWithoutImbalances(
        forecastErrorPercentage, standardDeviation, averageDailyPower
    )
    val energyWithoutImbalances = (averageDailyPower * 24.0 * energyShareWithoutImbalances) / 100.0
    val imbalanceShare = 100.0 - energyShareWithoutImbalances
    val imbalanceEnergy = (averageDailyPower * 24.0 * imbalanceShare) / 100.0
    val profit = energyWithoutImbalances * energyTariff * 1000
    val penalty = imbalanceEnergy * energyTariff * 1000 * 0.5

    return """
        Частка енергії без небалансів: ${String.format("%.2f", energyShareWithoutImbalances)}%
        Енергія без небалансів: ${String.format("%.2f", energyWithoutImbalances)} МВт·год
        Прибуток: ${String.format("%.2f", profit)} грн

        Частка небалансів: ${String.format("%.2f", imbalanceShare)}%
        Енергія небалансів: ${String.format("%.2f", imbalanceEnergy)} МВт·год
        Штраф: ${String.format("%.2f", penalty)} грн

        Чистий прибуток: ${String.format("%.2f", profit - penalty)} грн
    """.trimIndent()
}

fun calculateEnergyShareWithoutImbalances(
    forecastErrorPercentage: Double,
    standardDeviation: Double,
    averagePower: Double
): Double {
    val upperBound = averagePower + (averagePower * forecastErrorPercentage / 100.0)
    val lowerBound = averagePower - (averagePower * forecastErrorPercentage / 100.0)
    val steps = 1000
    val stepSize = (upperBound - lowerBound) / steps
    var integralSum = 0.0

    for (i in 0 until steps) {
        val x1 = lowerBound + i * stepSize
        val x2 = lowerBound + (i + 1) * stepSize
        integralSum += (calculateDensity(standardDeviation, x1, averagePower) +
                calculateDensity(standardDeviation, x2, averagePower)) * stepSize / 2.0
    }

    return integralSum * 100
}

fun calculateDensity(standardDeviation: Double, x: Double, mean: Double): Double {
    return (1 / (standardDeviation * sqrt(2 * PI))) *
            exp(-((x - mean).pow(2)) / (2 * standardDeviation.pow(2)))
}

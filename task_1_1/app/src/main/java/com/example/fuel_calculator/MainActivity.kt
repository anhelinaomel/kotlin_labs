package com.example.fuel_calculator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.fuel_calculator.ui.theme.Fuel_calculatorTheme
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Fuel_calculatorTheme {
                FuelCalculatorScreen()
            }
        }
    }
}

@Composable
fun FuelCalculatorScreen() {
    var hydrogen by remember { mutableStateOf("") }
    var carbon by remember { mutableStateOf("") }
    var sulfur by remember { mutableStateOf("") }
    var nitrogen by remember { mutableStateOf("") }
    var oxygen by remember { mutableStateOf("") }
    var moisture by remember { mutableStateOf("") }
    var ash by remember { mutableStateOf("") }
    var result by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Калькулятор палива",
            style = androidx.compose.material3.MaterialTheme.typography.titleLarge,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        if (errorMessage.isNotEmpty()) {
            Text(
                text = errorMessage,
                color = Color.Red,
                style = TextStyle(fontSize = 16.sp),
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        FuelInputField(label = "Водень (Hₚ, %)", value = hydrogen) { hydrogen = it }
        FuelInputField(label = "Вуглець (Cₚ, %)", value = carbon) { carbon = it }
        FuelInputField(label = "Сірка (Sₚ, %)", value = sulfur) { sulfur = it }
        FuelInputField(label = "Азот (Nₚ, %)", value = nitrogen) { nitrogen = it }
        FuelInputField(label = "Кисень (Oₚ, %)", value = oxygen) { oxygen = it }
        FuelInputField(label = "Волога (Wₚ, %)", value = moisture) { moisture = it }
        FuelInputField(label = "Зола (Aₚ, %)", value = ash) { ash = it }

        Button(
            onClick = {
                errorMessage = ""
                result = ""

                val hydrogenValue = hydrogen.toDoubleOrNull() ?: 0.0
                val carbonValue = carbon.toDoubleOrNull() ?: 0.0
                val sulfurValue = sulfur.toDoubleOrNull() ?: 0.0
                val nitrogenValue = nitrogen.toDoubleOrNull() ?: 0.0
                val oxygenValue = oxygen.toDoubleOrNull() ?: 0.0
                val moistureValue = moisture.toDoubleOrNull() ?: 0.0
                val ashValue = ash.toDoubleOrNull() ?: 0.0

                if (hydrogen.isBlank() || carbon.isBlank() || sulfur.isBlank() || nitrogen.isBlank() || oxygen.isBlank() || moisture.isBlank() || ash.isBlank()) {
                    errorMessage = "Заповніть усі поля!"
                    return@Button
                }

                val totalPercentage = hydrogenValue + carbonValue + sulfurValue + nitrogenValue + oxygenValue + moistureValue + ashValue
                if (totalPercentage > 100.0 + 1e-6) {
                    errorMessage = "Сума компонентів не має перевищувати 100%!"
                    return@Button
                }

                val krs = 100 / (100 - moistureValue)
                val krg = 100 / (100 - moistureValue - ashValue)

                val hydrogenDry = hydrogenValue * krs
                val carbonDry = carbonValue * krs
                val sulfurDry = sulfurValue * krs
                val nitrogenDry = nitrogenValue * krs
                val oxygenDry = oxygenValue * krs
                val ashDry = ashValue * krs

                val hydrogenCombustible = hydrogenValue * krg
                val carbonCombustible = carbonValue * krg
                val sulfurCombustible = sulfurValue * krg
                val nitrogenCombustible = nitrogenValue * krg
                val oxygenCombustible = oxygenValue * krg

                val lowerHeatingValueWorking = (339 * carbonValue + 1030 * hydrogenValue - 108.8 * (oxygenValue - sulfurValue) - 25 * moistureValue) / 1000
                val lowerHeatingValueDry = (339 * carbonDry + 1030 * hydrogenDry - 108.8 * (oxygenDry - sulfurDry)) / 1000
                val lowerHeatingValueCombustible = (339 * carbonCombustible + 1030 * hydrogenCombustible - 108.8 * (oxygenCombustible - sulfurCombustible)) / 1000

                result = """
                    Суха маса:
                    Водень = ${String.format("%.2f", hydrogenDry)}%
                    Вуглець = ${String.format("%.2f", carbonDry)}%
                    Сірка = ${String.format("%.2f", sulfurDry)}%
                    Азот = ${String.format("%.2f", nitrogenDry)}%
                    Кисень = ${String.format("%.2f", oxygenDry)}%
                    Зола = ${String.format("%.2f", ashDry)}%

                    Горюча маса:
                    Водень = ${String.format("%.2f", hydrogenCombustible)}%
                    Вуглець = ${String.format("%.2f", carbonCombustible)}%
                    Сірка = ${String.format("%.2f", sulfurCombustible)}%
                    Азот = ${String.format("%.2f", nitrogenCombustible)}%
                    Кисень = ${String.format("%.2f", oxygenCombustible)}%

                    Нижча теплота згоряння робочої маси:
                    Q = ${String.format("%.2f", lowerHeatingValueWorking)} МДж/кг

                    Нижча теплота згоряння сухої маси:
                    Q = ${String.format("%.2f", lowerHeatingValueDry)} МДж/кг

                    Нижча теплота згоряння горючої маси:
                    Q = ${String.format("%.2f", lowerHeatingValueCombustible)} МДж/кг
                """.trimIndent()
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

        // порожні рядки в кінці
        Spacer(modifier = Modifier.height(64.dp))
    }
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
fun FuelCalculatorPreview() {
    Fuel_calculatorTheme {
        FuelCalculatorScreen()
    }
}

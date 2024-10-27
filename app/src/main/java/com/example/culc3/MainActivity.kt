package com.example.culc3

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight

import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import kotlin.math.exp
import kotlin.math.sqrt

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CalculationScreen()
        }
    }
}


@Composable
fun CalculationScreen() {
    var pcValue by remember { mutableStateOf("") }
    var deltaValue by remember { mutableStateOf("") }
    var energyPercentageValue by remember { mutableStateOf("") }
    var result by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {


        Text(text = "Розрахунок прибутку від сонячних електростанцій з встановленою системою прогнозування сонячної потужності.",
            color = Color.Black,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Default,

            modifier = Modifier
                .background(Color(0xFFEDEDED)) // Світло-сірий фон
                .padding(8.dp))

        // Поле введення середньодобової потужності Pc
        OutlinedTextField(
            value = pcValue,
            onValueChange = { pcValue = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            shape = RoundedCornerShape(50.dp),
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = Color(0xFFF88837),
                unfocusedIndicatorColor = Color(0xFFF88837),
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            ),
            label = { Text("Середньодобова потужність (Pc) у МВт") },
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Decimal)
        )

        // Поле введення похибки прогнозу δ
        OutlinedTextField(
            value = deltaValue,
            onValueChange = { deltaValue = it },
            label = { Text("Похибка прогнозу (%)") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            shape = RoundedCornerShape(50.dp), // Округлення кутів
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = Color(0xFFF88837), // Колір обводки при фокусі
                unfocusedIndicatorColor = Color(0xFFF88837), // Колір обводки без фокусу
                focusedContainerColor = Color.White, // Колір фону при фокусі
                unfocusedContainerColor = Color.White // Колір фону без фокусу
            ),
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Decimal)
        )

        // Кнопка для розрахунку
        Button(onClick = {
            result = calculateProfitOrPenalty(
                pcValue.toDoubleOrNull() ?: 0.0,
                deltaValue.toDoubleOrNull() ?: 0.0,
            )
        },shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFF88837), // Колір фону кнопки
                contentColor = Color.White         // Колір тексту
            ),
            modifier = Modifier.fillMaxWidth()) {
            Text(text = "Розрахувати",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold)


        }

        // Відображення результату
        Text(
            text = result,
            modifier = Modifier.padding(top = 16.dp),
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

fun calculateProfitOrPenalty(pc: Double, delta: Double): String {
    val B = 7.0
    val σ = 1.0
    val new_σ = 0.25
    val lowerBound = pc - 0.25
    val upperBound = pc + 0.25

    val energyPercentage = (normalDistributionCDF(upperBound, pc, σ) - normalDistributionCDF(lowerBound, pc, σ)) * 100
    val profit_1 = (pc * 24) * B * (energyPercentage / 100)
    val penalty_1 = (pc * 24) * B * ((100 - energyPercentage) / 100)

    val new_energyPercentage = (normalDistributionCDF(upperBound, pc, new_σ) - normalDistributionCDF(lowerBound, pc, new_σ)) * 100
    val profit_2 = (pc * 24) * B * (new_energyPercentage / 100)
    val penalty_2 = (pc * 24) * B * ((100 - new_energyPercentage) / 100)

    val res = profit_2 - penalty_2

    return """
        Середньодобова потужність: $pc МВт
        Похибка прогнозу: $delta %
        
        Відсоток енергії: ${String.format("%.2f", energyPercentage)} %
        Прибуток: ${String.format("%.2f", profit_1)} тис. грн
        Штраф: ${String.format("%.2f", penalty_1)} тис. грн
        
        
        Відсоток енергії (новий σ): ${String.format("%.2f", new_energyPercentage)} %
        Прибуток: ${String.format("%.2f", profit_2)} тис. грн
        Штраф: ${String.format("%.2f", penalty_2)} тис. грн
        
        Можна отримати ${String.format("%.2f", res)} тис. грн прибутку!
    """.trimIndent()
}

fun normalDistributionCDF(x: Double, mean: Double, stdDev: Double): Double {
    return 0.5 * (1 + erf((x - mean) / (stdDev * sqrt(2.0))))
}

fun erf(x: Double): Double {
    val sign = if (x >= 0) 1 else -1
    val a1 = 0.254829592
    val a2 = -0.284496736
    val a3 = 1.421413741
    val a4 = -1.453152027
    val a5 = 1.061405429
    val p = 0.3275911
    val absX = kotlin.math.abs(x)

    val t = 1.0 / (1.0 + p * absX)
    val y = 1.0 - (((((a5 * t + a4) * t) + a3) * t + a2) * t + a1) * t * exp(-x * x)

    return sign * y
}


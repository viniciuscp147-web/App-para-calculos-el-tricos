package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CableSectionInfo
import com.example.model.PowerCalcMode
import com.example.model.PowerCalcResult
import com.example.model.PowerCalculator
import com.example.model.StandardCableSections
import com.example.viewmodel.AppTab
import com.example.viewmodel.CableViewModel
import java.util.Locale

@Composable
fun PowerOhmScreen(
    viewModel: CableViewModel,
    modifier: Modifier = Modifier
) {
    val powerMode by viewModel.powerMode.collectAsState()
    val powerWatts by viewModel.powerWatts.collectAsState()
    val powerVolts by viewModel.powerVolts.collectAsState()
    val powerCurrent by viewModel.powerCurrent.collectAsState()
    val powerFactor by viewModel.powerFactor.collectAsState()
    val isThreePhase by viewModel.powerIsThreePhase.collectAsState()

    val result = viewModel.getPowerCalcResult()
    val scrollState = rememberScrollState()

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .testTag("power_ohm_screen")
    ) {
        val isWide = maxWidth >= 720.dp

        if (isWide) {
            // DESKTOP TWO-COLUMN LAYOUT
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // LEFT PANE: Mode & Inputs
                Column(
                    modifier = Modifier
                        .weight(1.1f)
                        .verticalScroll(scrollState)
                ) {
                    Text(
                        text = "Cálculos de Proporção",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Lei de Ohm, Potência Elétrica e Dimensionamento de Cabo por Corrente",
                        style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    ModeTabs(powerMode = powerMode, onSelect = { viewModel.setPowerMode(it) })

                    Spacer(modifier = Modifier.height(14.dp))

                    if (powerMode != PowerCalcMode.SIZING_BY_AMPERES) {
                        ThreePhaseToggleCard(isThreePhase = isThreePhase, onToggle = { viewModel.setPowerIsThreePhase(it) })
                        Spacer(modifier = Modifier.height(14.dp))
                    }

                    PowerModeInputs(
                        powerMode = powerMode,
                        powerWatts = powerWatts,
                        onWattsChange = { viewModel.setPowerWatts(it) },
                        powerVolts = powerVolts,
                        onVoltsChange = { viewModel.setPowerVolts(it) },
                        powerCurrent = powerCurrent,
                        onCurrentChange = { viewModel.setPowerCurrent(it) },
                        powerFactor = powerFactor,
                        onFactorChange = { viewModel.setPowerFactor(it) }
                    )
                }

                // RIGHT PANE: Results Card & Action
                Column(
                    modifier = Modifier
                        .weight(0.9f)
                        .verticalScroll(rememberScrollState())
                ) {
                    PowerResultCard(
                        powerMode = powerMode,
                        result = result,
                        powerCurrent = powerCurrent,
                        powerVolts = powerVolts,
                        onNavigateToVd = { currentVal, suitableSection ->
                            viewModel.setVdCurrent("%.1f".format(Locale.US, currentVal).replace('.', ','))
                            viewModel.setVdVoltage(powerVolts.ifBlank { "220" })
                            viewModel.setVdSection(suitableSection.sectionMm2)
                            viewModel.setTab(AppTab.VOLTAGE_DROP)
                        },
                        onNavigateToPrices = { section ->
                            viewModel.selectSection(section.sectionMm2)
                            viewModel.setTab(AppTab.CABLE_PRICES)
                        }
                    )
                }
            }
        } else {
            // MOBILE SINGLE COLUMN VERTICAL LAYOUT
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "Cálculos de Proporção",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "Lei de Ohm, Potência Elétrica e Dimensionamento por Corrente",
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )

                Spacer(modifier = Modifier.height(14.dp))

                ModeTabs(powerMode = powerMode, onSelect = { viewModel.setPowerMode(it) })

                Spacer(modifier = Modifier.height(14.dp))

                if (powerMode != PowerCalcMode.SIZING_BY_AMPERES) {
                    ThreePhaseToggleCard(isThreePhase = isThreePhase, onToggle = { viewModel.setPowerIsThreePhase(it) })
                    Spacer(modifier = Modifier.height(14.dp))
                }

                PowerModeInputs(
                    powerMode = powerMode,
                    powerWatts = powerWatts,
                    onWattsChange = { viewModel.setPowerWatts(it) },
                    powerVolts = powerVolts,
                    onVoltsChange = { viewModel.setPowerVolts(it) },
                    powerCurrent = powerCurrent,
                    onCurrentChange = { viewModel.setPowerCurrent(it) },
                    powerFactor = powerFactor,
                    onFactorChange = { viewModel.setPowerFactor(it) }
                )

                Spacer(modifier = Modifier.height(16.dp))

                PowerResultCard(
                    powerMode = powerMode,
                    result = result,
                    powerCurrent = powerCurrent,
                    powerVolts = powerVolts,
                    onNavigateToVd = { currentVal, suitableSection ->
                        viewModel.setVdCurrent("%.1f".format(Locale.US, currentVal).replace('.', ','))
                        viewModel.setVdVoltage(powerVolts.ifBlank { "220" })
                        viewModel.setVdSection(suitableSection.sectionMm2)
                        viewModel.setTab(AppTab.VOLTAGE_DROP)
                    },
                    onNavigateToPrices = { section ->
                        viewModel.selectSection(section.sectionMm2)
                        viewModel.setTab(AppTab.CABLE_PRICES)
                    }
                )

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun ModeTabs(
    powerMode: PowerCalcMode,
    onSelect: (PowerCalcMode) -> Unit
) {
    val modes = listOf(
        PowerCalcMode.SIZING_BY_AMPERES to "Ampères ➔ Cabo",
        PowerCalcMode.CALCULATE_CURRENT to "Ampères (I)",
        PowerCalcMode.CALCULATE_POWER to "Potência (W)",
        PowerCalcMode.CALCULATE_VOLTAGE to "Tensão (V)"
    )
    val selectedIndex = modes.indexOfFirst { it.first == powerMode }.coerceAtLeast(0)

    ScrollableTabRow(
        selectedTabIndex = selectedIndex,
        edgePadding = 8.dp,
        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = Modifier.clip(RoundedCornerShape(12.dp))
    ) {
        modes.forEachIndexed { index, (mode, label) ->
            Tab(
                selected = selectedIndex == index,
                onClick = { onSelect(mode) },
                text = { Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold) }
            )
        }
    }
}

@Composable
private fun ThreePhaseToggleCard(
    isThreePhase: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = if (isThreePhase) "Sistema Trifásico (√3 ≈ 1,732)" else "Sistema Monofásico / Bifásico",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = if (isThreePhase) "Fórmula com fator trifásico raiz de 3" else "Fórmula direta para 1 ou 2 fases",
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )
            }
            Switch(checked = isThreePhase, onCheckedChange = onToggle)
        }
    }
}

@Composable
private fun PowerModeInputs(
    powerMode: PowerCalcMode,
    powerWatts: String,
    onWattsChange: (String) -> Unit,
    powerVolts: String,
    onVoltsChange: (String) -> Unit,
    powerCurrent: String,
    onCurrentChange: (String) -> Unit,
    powerFactor: String,
    onFactorChange: (String) -> Unit
) {
    Column {
        when (powerMode) {
            PowerCalcMode.SIZING_BY_AMPERES -> {
                Text(
                    text = "Digite a corrente em Ampères (A) do circuito ou disjuntor para descobrir a bitola mínima exigida pela NBR 5410 (Método B1 - condutores embutidos em alvenaria):",
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = powerCurrent,
                    onValueChange = onCurrentChange,
                    label = { Text("Corrente em Ampères (A)") },
                    placeholder = { Text("Ex: 25, 32 ou 40") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_current_ampere_sizing"),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Atalhos rápidos de Correntes / Disjuntores Comuns:",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(6.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(listOf("10", "16", "20", "25", "32", "40", "50", "63", "70", "100")) { a ->
                        FilledTonalButton(
                            onClick = { onCurrentChange(a) },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("btn_quick_amp_${a}")
                        ) {
                            Text("${a}A", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            PowerCalcMode.CALCULATE_CURRENT -> {
                OutlinedTextField(
                    value = powerWatts,
                    onValueChange = onWattsChange,
                    label = { Text("Potência em Watts (W)") },
                    placeholder = { Text("Ex: 5500 ou 7500") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("1200", "3500", "5500", "7500").forEach { w ->
                        FilledTonalButton(
                            onClick = { onWattsChange(w) },
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(2.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("${w}W", fontSize = 11.sp)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = powerVolts,
                    onValueChange = onVoltsChange,
                    label = { Text("Tensão da Rede (V)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("110", "127", "220", "380").forEach { v ->
                        FilledTonalButton(
                            onClick = { onVoltsChange(v) },
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(2.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("${v}V", fontSize = 11.sp)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = powerFactor,
                    onValueChange = onFactorChange,
                    label = { Text("Fator de Potência (cos φ / FP)") },
                    placeholder = { Text("1.0 para resistivo, 0.92 para motores") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )
            }
            PowerCalcMode.CALCULATE_POWER -> {
                OutlinedTextField(
                    value = powerVolts,
                    onValueChange = onVoltsChange,
                    label = { Text("Tensão da Rede (V)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = powerCurrent,
                    onValueChange = onCurrentChange,
                    label = { Text("Corrente em Ampères (A)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("10", "16", "20", "32", "40").forEach { a ->
                        FilledTonalButton(
                            onClick = { onCurrentChange(a) },
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(2.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("${a}A", fontSize = 11.sp)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = powerFactor,
                    onValueChange = onFactorChange,
                    label = { Text("Fator de Potência (cos φ / FP)") },
                    placeholder = { Text("1.0 para resistivo, 0.92 para motores") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )
            }
            PowerCalcMode.CALCULATE_VOLTAGE -> {
                OutlinedTextField(
                    value = powerWatts,
                    onValueChange = onWattsChange,
                    label = { Text("Potência em Watts (W)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = powerCurrent,
                    onValueChange = onCurrentChange,
                    label = { Text("Corrente em Ampères (A)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = powerFactor,
                    onValueChange = onFactorChange,
                    label = { Text("Fator de Potência (cos φ / FP)") },
                    placeholder = { Text("1.0 para resistivo, 0.92 para motores") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )
            }
        }
    }
}

@Composable
private fun PowerResultCard(
    powerMode: PowerCalcMode,
    result: PowerCalcResult,
    powerCurrent: String,
    powerVolts: String,
    onNavigateToVd: (Double, CableSectionInfo) -> Unit,
    onNavigateToPrices: (CableSectionInfo) -> Unit
) {
    if (powerMode == PowerCalcMode.SIZING_BY_AMPERES) {
        val currentVal = powerCurrent.toDoubleOrNull() ?: 0.0
        val minCable = PowerCalculator.findMinCableForCurrent(currentVal)

        ElevatedCard(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("power_result_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Surface(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "DIMENSIONAMENTO NBR 5410",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Bitola Mínima Obrigatória:",
                    style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )

                Text(
                    text = minCable.displayLabel,
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary
                    )
                )

                Text(
                    text = if (currentVal > 0) "Para corrente informada de ${"%.1f A".format(Locale.US, currentVal).replace('.', ',')}" else "Informe a corrente em Ampères acima",
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )

                Spacer(modifier = Modifier.height(14.dp))
                Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                Spacer(modifier = Modifier.height(12.dp))

                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Capacidade Máx. de Condução:", style = MaterialTheme.typography.bodySmall)
                            Text(
                                "${minCable.nominalCurrentA} A (Método B1)",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Disjuntor Recomendado:", style = MaterialTheme.typography.bodySmall)
                            Text(
                                "${minCable.recommendedBreakerA} A (Curva C)",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Peso estimado cobre:", style = MaterialTheme.typography.bodySmall)
                            Text(
                                "~${minCable.copperWeightKgKm} kg/km",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }

                        Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                        Text(
                            text = "• Aplicação típica: ${minCable.typicalApplication}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { onNavigateToVd(currentVal, minCable) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("btn_test_voltage_drop_from_sizing"),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Testar Queda de Tensão com este Cabo")
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                }

                Spacer(modifier = Modifier.height(8.dp))

                FilledTonalButton(
                    onClick = { onNavigateToPrices(minCable) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Ver Preço e Tabela desta Bitola (${minCable.displayLabel})")
                }
            }
        }
    } else {
        ElevatedCard(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("power_result_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = when (powerMode) {
                        PowerCalcMode.CALCULATE_CURRENT -> "Corrente Resultante (I)"
                        PowerCalcMode.CALCULATE_POWER -> "Potência Resultante (P)"
                        PowerCalcMode.CALCULATE_VOLTAGE -> "Tensão Resultante (V)"
                        PowerCalcMode.SIZING_BY_AMPERES -> "Bitola Mínima"
                    },
                    style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = result.formatted,
                    style = MaterialTheme.typography.displaySmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))
                Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Fórmula Utilizada:", style = MaterialTheme.typography.bodySmall)
                    Text(
                        text = result.formulaUsed,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                }

                if (powerMode == PowerCalcMode.CALCULATE_CURRENT && result.calculatedValue > 0) {
                    val currentVal = result.calculatedValue
                    val suitableSection = StandardCableSections.ALL.firstOrNull { it.nominalCurrentA >= currentVal }
                        ?: StandardCableSections.ALL.last()

                    Spacer(modifier = Modifier.height(8.dp))

                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "Dimensionamento Recomendado (NBR 5410):",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "• Cabo sugerido: ${suitableSection.displayLabel} (suporta até ${suitableSection.nominalCurrentA}A)",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = "• Disjuntor sugerido: ${suitableSection.recommendedBreakerA}A (Curva C)",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = { onNavigateToVd(currentVal, suitableSection) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Calcular Queda de Tensão para esta Corrente")
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

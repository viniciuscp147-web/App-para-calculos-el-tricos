package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CircuitType
import com.example.model.StandardCableSections
import com.example.model.VoltageDropCalculator
import com.example.model.VoltageDropResult
import com.example.model.VoltageDropStatus
import com.example.viewmodel.AppTab
import com.example.viewmodel.CableViewModel
import java.util.Locale

@Composable
fun VoltageDropScreen(
    viewModel: CableViewModel,
    modifier: Modifier = Modifier
) {
    val distance by viewModel.vdDistance.collectAsState()
    val section by viewModel.vdSection.collectAsState()
    val current by viewModel.vdCurrent.collectAsState()
    val voltage by viewModel.vdVoltage.collectAsState()
    val circuitType by viewModel.vdCircuitType.collectAsState()

    val parsedDistance = distance.replace(',', '.').toDoubleOrNull() ?: 0.0
    val parsedCurrent = current.replace(',', '.').toDoubleOrNull() ?: 0.0
    val parsedVoltage = voltage.replace(',', '.').toDoubleOrNull() ?: 220.0

    // Deterministic calculation whenever any input or section changes
    val result = remember(parsedDistance, section, parsedCurrent, parsedVoltage, circuitType) {
        VoltageDropCalculator.calculate(
            distanceMeters = parsedDistance,
            sectionMm2 = section,
            currentAmperes = parsedCurrent,
            nominalVoltageVolts = parsedVoltage,
            circuitType = circuitType
        )
    }

    val scrollState = rememberScrollState()

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .testTag("voltage_drop_screen")
    ) {
        val isWide = maxWidth >= 720.dp

        if (isWide) {
            // DESKTOP / TABLET TWO-COLUMN SIDE-BY-SIDE LAYOUT
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // LEFT PANE: Inputs Form
                Column(
                    modifier = Modifier
                        .weight(1.1f)
                        .verticalScroll(scrollState)
                ) {
                    Text(
                        text = "Cálculo de Queda de Tensão",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Dimensionamento por perda máxima admissível e verificação de corrente (NBR 5410)",
                        style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    CircuitTypeSelector(circuitType = circuitType, onSelect = { viewModel.setVdCircuitType(it) })

                    Spacer(modifier = Modifier.height(16.dp))

                    DistanceAndCurrentInputs(
                        distance = distance,
                        onDistanceChange = { viewModel.setVdDistance(it) },
                        current = current,
                        onCurrentChange = { viewModel.setVdCurrent(it) }
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    VoltageInput(voltage = voltage, onVoltageChange = { viewModel.setVdVoltage(it) })

                    Spacer(modifier = Modifier.height(16.dp))

                    SectionSelector(
                        selectedSection = section,
                        currentAmperes = parsedCurrent,
                        onSectionSelect = { viewModel.setVdSection(it) }
                    )
                }

                // RIGHT PANE: Results Card & Action
                Column(
                    modifier = Modifier
                        .weight(0.9f)
                        .verticalScroll(rememberScrollState())
                ) {
                    VoltageDropResultCard(
                        result = result,
                        selectedSectionMm2 = section,
                        currentAmperes = parsedCurrent,
                        onFixSection = { viewModel.setVdSection(it) }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            viewModel.selectSection(section)
                            viewModel.setMetersInput(distance)
                            viewModel.setTab(AppTab.CABLE_PRICES)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Ver Preço e Tabela desta Bitola (${section.toString().replace('.', ',')} mm²)")
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
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
                    text = "Cálculo de Queda de Tensão",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "Dimensionamento por perda máxima admissível e verificação de corrente (NBR 5410)",
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )

                Spacer(modifier = Modifier.height(14.dp))

                CircuitTypeSelector(circuitType = circuitType, onSelect = { viewModel.setVdCircuitType(it) })

                Spacer(modifier = Modifier.height(14.dp))

                DistanceAndCurrentInputs(
                    distance = distance,
                    onDistanceChange = { viewModel.setVdDistance(it) },
                    current = current,
                    onCurrentChange = { viewModel.setVdCurrent(it) }
                )

                Spacer(modifier = Modifier.height(10.dp))

                VoltageInput(voltage = voltage, onVoltageChange = { viewModel.setVdVoltage(it) })

                Spacer(modifier = Modifier.height(14.dp))

                SectionSelector(
                    selectedSection = section,
                    currentAmperes = parsedCurrent,
                    onSectionSelect = { viewModel.setVdSection(it) }
                )

                Spacer(modifier = Modifier.height(16.dp))

                VoltageDropResultCard(
                    result = result,
                    selectedSectionMm2 = section,
                    currentAmperes = parsedCurrent,
                    onFixSection = { viewModel.setVdSection(it) }
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        viewModel.selectSection(section)
                        viewModel.setMetersInput(distance)
                        viewModel.setTab(AppTab.CABLE_PRICES)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Ver Preço e Tabela desta Bitola (${section.toString().replace('.', ',')} mm²)")
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun CircuitTypeSelector(
    circuitType: CircuitType,
    onSelect: (CircuitType) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Surface(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(10.dp))
                .clickable { onSelect(CircuitType.SINGLE_OR_TWO_PHASE) },
            color = if (circuitType == CircuitType.SINGLE_OR_TWO_PHASE) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(10.dp)
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Monofásico / Bifásico",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = if (circuitType == CircuitType.SINGLE_OR_TWO_PHASE) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "2 condutores (k = 2)",
                    fontSize = 10.sp,
                    color = if (circuitType == CircuitType.SINGLE_OR_TWO_PHASE) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }

        Surface(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(10.dp))
                .clickable { onSelect(CircuitType.THREE_PHASE) },
            color = if (circuitType == CircuitType.THREE_PHASE) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(10.dp)
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Trifásico Equilibrado",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = if (circuitType == CircuitType.THREE_PHASE) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "3/4 condutores (k = √3)",
                    fontSize = 10.sp,
                    color = if (circuitType == CircuitType.THREE_PHASE) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
private fun DistanceAndCurrentInputs(
    distance: String,
    onDistanceChange: (String) -> Unit,
    current: String,
    onCurrentChange: (String) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedTextField(
                value = distance,
                onValueChange = onDistanceChange,
                label = { Text("Distância (m)") },
                modifier = Modifier
                    .weight(1f)
                    .testTag("distance_input"),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                shape = RoundedCornerShape(10.dp)
            )

            OutlinedTextField(
                value = current,
                onValueChange = onCurrentChange,
                label = { Text("Corrente (A)") },
                modifier = Modifier
                    .weight(1f)
                    .testTag("current_input"),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                shape = RoundedCornerShape(10.dp)
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Quick Current Shortcut Chips for instantaneous selection
        Text(
            text = "Atalhos rápidos de Corrente (A):",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
        Spacer(modifier = Modifier.height(4.dp))
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(listOf("10", "16", "20", "25", "32", "40", "50", "63", "70", "100")) { a ->
                FilledTonalButton(
                    onClick = { onCurrentChange(a) },
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("vd_quick_amp_$a")
                ) {
                    Text("${a}A", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun VoltageInput(
    voltage: String,
    onVoltageChange: (String) -> Unit
) {
    Column {
        OutlinedTextField(
            value = voltage,
            onValueChange = onVoltageChange,
            label = { Text("Tensão Nominal da Rede (V)") },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("voltage_input"),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            shape = RoundedCornerShape(10.dp)
        )

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf("127", "220", "380", "440").forEach { v ->
                FilledTonalButton(
                    onClick = { onVoltageChange(v) },
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(vertical = 2.dp, horizontal = 2.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("${v}V", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun SectionSelector(
    selectedSection: Double,
    currentAmperes: Double,
    onSectionSelect: (Double) -> Unit
) {
    val selectedInfo = StandardCableSections.getBySection(selectedSection)
    val isCableIncompatible = currentAmperes > selectedInfo.nominalCurrentA && currentAmperes > 0

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Seção do Cabo (mm²):",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
            )
            Text(
                text = "${selectedSection.toString().replace('.', ',')} mm² (Máx: ${selectedInfo.nominalCurrentA.toInt()}A)",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = if (isCableIncompatible) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                )
            )
        }

        if (isCableIncompatible) {
            Spacer(modifier = Modifier.height(4.dp))
            Surface(
                color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.6f),
                shape = RoundedCornerShape(6.dp)
            ) {
                Text(
                    text = "❌ Cabo de ${selectedSection.toString().replace('.', ',')} mm² NÃO está de acordo com ${"%.1f".format(Locale.US, currentAmperes).replace('.', ',')}A (suporta máx. ${selectedInfo.nominalCurrentA.toInt()}A)",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    ),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            contentPadding = PaddingValues(vertical = 4.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(StandardCableSections.ALL) { sec ->
                val isSelected = sec.sectionMm2 == selectedSection
                val isUnderSized = currentAmperes > sec.nominalCurrentA && currentAmperes > 0

                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { onSectionSelect(sec.sectionMm2) }
                        .testTag("vd_section_${sec.sectionMm2}"),
                    color = when {
                        isSelected && isUnderSized -> MaterialTheme.colorScheme.error
                        isSelected -> MaterialTheme.colorScheme.primary
                        isUnderSized -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.35f)
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = if (isUnderSized) "${sec.displayLabel} (!)" else sec.displayLabel,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                            color = when {
                                isSelected && isUnderSized -> MaterialTheme.colorScheme.onError
                                isSelected -> MaterialTheme.colorScheme.onPrimary
                                isUnderSized -> MaterialTheme.colorScheme.error
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        ),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun VoltageDropResultCard(
    result: VoltageDropResult,
    selectedSectionMm2: Double,
    currentAmperes: Double,
    onFixSection: ((Double) -> Unit)? = null
) {
    val statusColor = when (result.status) {
        VoltageDropStatus.COMPLIANT -> Color(0xFF16A34A)
        VoltageDropStatus.WARNING -> Color(0xFFD97706)
        VoltageDropStatus.NON_COMPLIANT -> Color(0xFFDC2626)
        VoltageDropStatus.OVERCURRENT_HAZARD -> Color(0xFFDC2626)
    }

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("voltage_drop_result_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Status Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = statusColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (result.status.isCompliant) Icons.Default.CheckCircle else Icons.Default.Warning,
                            contentDescription = null,
                            tint = statusColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = result.status.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = statusColor
                        )
                    }
                }

                Text(
                    text = "Limite NBR: 4,0%",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // PROMINENT CRITICAL HAZARD ALERT: When cable ampacity is exceeded!
            if (result.isAmpacityExceeded) {
                Spacer(modifier = Modifier.height(14.dp))
                Surface(
                    color = Color(0xFFFEF2F2),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFFDC2626)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("ampacity_hazard_card")
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = Color(0xFFDC2626),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "❌ CABO NÃO ESTÁ DE ACORDO COM A CORRENTE!",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF991B1B)
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = result.ampacityWarningMessage
                                ?: "O cabo selecionado não suporta a corrente aplicada conforme a norma NBR 5410!",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF7F1D1D),
                                lineHeight = 18.sp
                            )
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Capacidade máxima do cabo (${selectedSectionMm2.toString().replace('.', ',')} mm²):",
                                style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF991B1B))
                            )
                            Text(
                                text = "${result.cableMaxAmpacityA.toInt()} A (NBR 5410)",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = Color(0xFF991B1B))
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Bitola Mínima Obrigatória:",
                                style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF991B1B))
                            )
                            Text(
                                text = "${result.minSectionForCurrentMm2.toString().replace('.', ',')} mm²",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, color = Color(0xFFDC2626))
                            )
                        }

                        if (onFixSection != null) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = { onFixSection(result.minSectionForCurrentMm2) },
                                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFDC2626),
                                    contentColor = Color.White
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(42.dp)
                                    .testTag("btn_fix_cable_section"),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    "Mudar para Bitola Segura (${result.minSectionForCurrentMm2.toString().replace('.', ',')} mm²)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = "Perda de Tensão (%)",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                    Text(
                        text = "%.2f%%".format(Locale.US, result.voltageDropPercent).replace('.', ','),
                        style = MaterialTheme.typography.displaySmall.copy(
                            fontWeight = FontWeight.Black,
                            color = statusColor
                        )
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Queda em Volts (ΔV)",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                    Text(
                        text = "%.2f V".format(Locale.US, result.voltageDropVolts).replace('.', ','),
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            val progress = (result.voltageDropPercent / 10.0).toFloat().coerceIn(0f, 1f)
            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = statusColor,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Spacer(modifier = Modifier.height(14.dp))
            Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Tensão restante na Carga:", style = MaterialTheme.typography.bodySmall)
                Text(
                    text = "%.2f V".format(Locale.US, result.finalVoltageAtLoad).replace('.', ','),
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Potência dissipada no cabo:", style = MaterialTheme.typography.bodySmall)
                Text(
                    text = "%.1f Watts".format(Locale.US, result.totalPowerLossWatts).replace('.', ','),
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                )
            }

            // High voltage drop recommendation (only if not already in thermal overload)
            if (result.status == VoltageDropStatus.NON_COMPLIANT && !result.isAmpacityExceeded) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Recomendação NBR: Aumente para no mínimo ${result.suggestedSectionMm2.toString().replace('.', ',')} mm² para manter a queda abaixo de 4%.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        )
                    }
                }
            }
        }
    }
}

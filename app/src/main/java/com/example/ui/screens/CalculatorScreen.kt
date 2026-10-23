package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Update
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CableColor
import com.example.model.StandardCableSections
import com.example.ui.components.CableVisualPreview
import com.example.viewmodel.AppTab
import com.example.viewmodel.CableViewModel
import java.util.Locale

@Composable
fun CalculatorScreen(
    viewModel: CableViewModel,
    modifier: Modifier = Modifier
) {
    val activeTable by viewModel.activeTable.collectAsState()
    val selectedSection by viewModel.selectedSection.collectAsState()
    val selectedColor by viewModel.selectedColor.collectAsState()
    val metersInput by viewModel.metersInput.collectAsState()
    val budgetItems by viewModel.budgetItems.collectAsState()

    val currentMeters = viewModel.getCurrentMeters()
    val unitPrice = viewModel.getCurrentUnitPrice()
    val rollDiscount = viewModel.getCurrentRollDiscount()
    val calculatedTotal = viewModel.getCalculatedTotal()
    val copperWeight = viewModel.getCalculatedCopperWeight()

    val sectionInfo = StandardCableSections.getBySection(selectedSection)
    val rollsCount = (currentMeters / 100).toInt()
    val leftoverMeters = currentMeters % 100

    val scrollState = rememberScrollState()

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .testTag("calculator_screen")
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
                // LEFT COLUMN: Controls & Configurations
                Column(
                    modifier = Modifier
                        .weight(1.1f)
                        .verticalScroll(scrollState)
                ) {
                    SupplierBanner(activeTable = activeTable, onOpenTables = { viewModel.setTab(AppTab.TABLES) })

                    Spacer(modifier = Modifier.height(16.dp))

                    SectionGaugeSelector(
                        selectedSection = selectedSection,
                        activeTable = activeTable,
                        formatCurrency = { viewModel.formatCurrency(it) },
                        onSelectSection = { viewModel.selectSection(it) }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    LengthMetersInput(
                        metersInput = metersInput,
                        onMetersChange = { viewModel.setMetersInput(it) },
                        onAddMeters = { viewModel.addMeters(it) }
                    )
                }

                // RIGHT COLUMN: Visual Preview & Live Price Card
                Column(
                    modifier = Modifier
                        .weight(0.9f)
                        .verticalScroll(rememberScrollState())
                ) {
                    CableVisualPreview(sectionMm2 = selectedSection, cableColor = selectedColor)

                    Spacer(modifier = Modifier.height(16.dp))

                    PriceCalculationCard(
                        calculatedTotal = calculatedTotal,
                        unitPrice = unitPrice,
                        rollsCount = rollsCount,
                        leftoverMeters = leftoverMeters,
                        rollDiscount = rollDiscount,
                        currentMeters = currentMeters,
                        sectionInfo = sectionInfo,
                        copperWeight = copperWeight,
                        formatCurrency = { viewModel.formatCurrency(it) }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { viewModel.addItemToBudget() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("add_to_budget_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.ShoppingCart, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Adicionar ao Orçamento",
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    AnimatedVisibility(
                        visible = budgetItems.isNotEmpty(),
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        Column {
                            Spacer(modifier = Modifier.height(10.dp))
                            OutlinedButton(
                                onClick = { viewModel.setTab(AppTab.BUDGET) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("view_budget_button"),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Ver Orçamento Completo (${budgetItems.size} ${if (budgetItems.size == 1) "item" else "itens"})")
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                            }
                        }
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
                SupplierBanner(activeTable = activeTable, onOpenTables = { viewModel.setTab(AppTab.TABLES) })

                Spacer(modifier = Modifier.height(14.dp))

                CableVisualPreview(sectionMm2 = selectedSection, cableColor = selectedColor)

                Spacer(modifier = Modifier.height(16.dp))

                SectionGaugeSelector(
                    selectedSection = selectedSection,
                    activeTable = activeTable,
                    formatCurrency = { viewModel.formatCurrency(it) },
                    onSelectSection = { viewModel.selectSection(it) }
                )

                Spacer(modifier = Modifier.height(16.dp))

                LengthMetersInput(
                    metersInput = metersInput,
                    onMetersChange = { viewModel.setMetersInput(it) },
                    onAddMeters = { viewModel.addMeters(it) }
                )

                Spacer(modifier = Modifier.height(18.dp))

                PriceCalculationCard(
                    calculatedTotal = calculatedTotal,
                    unitPrice = unitPrice,
                    rollsCount = rollsCount,
                    leftoverMeters = leftoverMeters,
                    rollDiscount = rollDiscount,
                    currentMeters = currentMeters,
                    sectionInfo = sectionInfo,
                    copperWeight = copperWeight,
                    formatCurrency = { viewModel.formatCurrency(it) }
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = { viewModel.addItemToBudget() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("add_to_budget_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.ShoppingCart, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Adicionar ao Orçamento",
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }

                AnimatedVisibility(
                    visible = budgetItems.isNotEmpty(),
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Column {
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedButton(
                            onClick = { viewModel.setTab(AppTab.BUDGET) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("view_budget_button"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Ver Orçamento Completo (${budgetItems.size} ${if (budgetItems.size == 1) "item" else "itens"})")
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun SupplierBanner(
    activeTable: com.example.model.SupplierWeeklyTable?,
    onOpenTables: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpenTables() },
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.85f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Update,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = activeTable?.supplierName ?: "Sil Fios e Cabos",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = activeTable?.weekTitle ?: "Tabela Semanal Vigente",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                )
            }

            Surface(
                color = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(20.dp)
            ) {
                Text(
                    text = "Alterar Tabela",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }
        }
    }
}

@Composable
private fun SectionGaugeSelector(
    selectedSection: Double,
    activeTable: com.example.model.SupplierWeeklyTable?,
    formatCurrency: (Double) -> String,
    onSelectSection: (Double) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "1. Seção do Cabo (Bitola)",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Text(
                text = "Padrão NBR NM 247-3",
                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.primary)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 4.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(StandardCableSections.ALL) { sec ->
                val isSelected = sec.sectionMm2 == selectedSection
                val priceForSec = activeTable?.getPrice(sec.sectionMm2) ?: 0.0

                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onSelectSection(sec.sectionMm2) }
                        .testTag("section_chip_${sec.sectionMm2}"),
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                    tonalElevation = if (isSelected) 4.dp else 1.dp,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = sec.displayLabel,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = formatCurrency(priceForSec) + "/m",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f) else MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LengthMetersInput(
    metersInput: String,
    onMetersChange: (String) -> Unit,
    onAddMeters: (Double) -> Unit
) {
    Column {
        Text(
            text = "2. Quantidade em Metros",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = metersInput,
                onValueChange = onMetersChange,
                label = { Text("Metros (m)") },
                modifier = Modifier
                    .weight(1f)
                    .testTag("meters_input_field"),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                trailingIcon = {
                    Text(
                        text = "metros",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(end = 12.dp)
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = { onAddMeters(-10.0) },
                modifier = Modifier
                    .size(48.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
            ) {
                Icon(Icons.Default.Remove, contentDescription = "Diminuir 10m")
            }

            Spacer(modifier = Modifier.width(6.dp))

            IconButton(
                onClick = { onAddMeters(10.0) },
                modifier = Modifier
                    .size(48.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
            ) {
                Icon(Icons.Default.Add, contentDescription = "Aumentar 10m")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf(50.0, 100.0, 200.0, 500.0).forEach { quickMeters ->
                FilledTonalButton(
                    onClick = { onMetersChange(quickMeters.toInt().toString()) },
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(vertical = 4.dp, horizontal = 2.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = if (quickMeters == 100.0) "1 Rolo (100m)" else "${quickMeters.toInt()}m",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun PriceCalculationCard(
    calculatedTotal: Double,
    unitPrice: Double,
    rollsCount: Int,
    leftoverMeters: Double,
    rollDiscount: Double,
    currentMeters: Double,
    sectionInfo: com.example.model.CableSectionInfo,
    copperWeight: Double,
    formatCurrency: (Double) -> String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = "Valor Total Estimado",
                        style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                    Text(
                        text = formatCurrency(calculatedTotal),
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.testTag("calculated_total_price")
                    )
                }

                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "${formatCurrency(unitPrice)}/m",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Composição:", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium))
                Text(
                    text = if (rollsCount > 0 && leftoverMeters > 0) {
                        "$rollsCount rolo(s) de 100m + ${leftoverMeters.toInt()}m fracionados"
                    } else if (rollsCount > 0) {
                        "$rollsCount rolo(s) de 100m fechados (-${rollDiscount.toInt()}% desc.)"
                    } else {
                        "${currentMeters.toInt()}m corte avulso"
                    },
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Capacidade de Corrente (NBR 5410):", style = MaterialTheme.typography.bodySmall)
                Text(
                    text = "${sectionInfo.nominalCurrentA} A (${sectionInfo.typicalApplication})",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Disjuntor Recomendado:", style = MaterialTheme.typography.bodySmall)
                Text(
                    text = "Disjuntor ${sectionInfo.recommendedBreakerA}A (Curva C)",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Peso Estimado de Cobre:", style = MaterialTheme.typography.bodySmall)
                Text(
                    text = "${"%.2f".format(Locale.US, copperWeight)} kg",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}

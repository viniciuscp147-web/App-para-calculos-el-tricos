package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.StandardCableSections
import com.example.model.SupplierWeeklyTable
import com.example.viewmodel.CableViewModel

@Composable
fun SupplierTablesScreen(
    viewModel: CableViewModel,
    modifier: Modifier = Modifier
) {
    val activeTable by viewModel.activeTable.collectAsState()
    val allTables by viewModel.allTables.collectAsState()

    // Dialog States
    var showNewWeekDialog by remember { mutableStateOf(false) }
    var showCopperAdjustmentDialog by remember { mutableStateOf(false) }
    var editingSection by remember { mutableStateOf<Double?>(null) }
    var editingPriceText by remember { mutableStateOf("") }
    var showResetConfirmDialog by remember { mutableStateOf(false) }

    // Dialog for Editing Single Gauge Price
    if (editingSection != null) {
        val section = editingSection!!
        val currentPrice = activeTable?.getPrice(section) ?: 0.0

        AlertDialog(
            onDismissRequest = { editingSection = null },
            title = {
                Text("Editar Preço - Cabo ${section.toString().replace('.', ',')} mm²")
            },
            text = {
                Column {
                    Text(
                        text = "Fornecedor: ${activeTable?.supplierName ?: ""}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = editingPriceText,
                        onValueChange = { editingPriceText = it.filter { c -> c.isDigit() || c == '.' || c == ',' }.replace(',', '.') },
                        label = { Text("Preço por Metro (R$/m)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    val newPrice = editingPriceText.toDoubleOrNull() ?: currentPrice
                    activeTable?.let { table ->
                        viewModel.updateSectionPrice(table.id, section, newPrice)
                    }
                    editingSection = null
                }) {
                    Text("Salvar Preço")
                }
            },
            dismissButton = {
                TextButton(onClick = { editingSection = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Dialog for Quick Weekly Copper Adjustment (+/- %)
    if (showCopperAdjustmentDialog) {
        var percentInput by remember { mutableStateOf("2.5") }

        AlertDialog(
            onDismissRequest = { showCopperAdjustmentDialog = false },
            title = { Text("Reajuste Semanal do Cobre") },
            text = {
                Column {
                    Text(
                        text = "Aplique uma variação percentual sobre todos os preços da tabela semanal ativa com base na oscilação da cotação do cobre:",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    OutlinedTextField(
                        value = percentInput,
                        onValueChange = { percentInput = it.filter { c -> c.isDigit() || c == '.' || c == ',' || c == '-' }.replace(',', '.') },
                        label = { Text("Variação Percentual (%)") },
                        placeholder = { Text("Ex: 2.5 ou -1.8") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(-2.0, -1.0, 1.5, 2.5, 3.5).forEach { p ->
                            FilledTonalButton(
                                onClick = { percentInput = p.toString() },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(if (p > 0) "+$p%" else "$p%", fontSize = 11.sp)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    val p = percentInput.toDoubleOrNull() ?: 0.0
                    activeTable?.let { table ->
                        viewModel.applyWeeklyCopperAdjustment(table.id, p)
                    }
                    showCopperAdjustmentDialog = false
                }) {
                    Text("Aplicar Reajuste")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCopperAdjustmentDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Dialog for Creating a New Weekly Table
    if (showNewWeekDialog) {
        var supplierName by remember { mutableStateOf(activeTable?.supplierName ?: "Sil Fios e Cabos") }
        var weekNum by remember { mutableStateOf(((activeTable?.weekNumber ?: 43) + 1).toString()) }
        var yearInput by remember { mutableStateOf((activeTable?.year ?: 2026).toString()) }
        var startDateInput by remember { mutableStateOf("26/10/2026") }
        var endDateInput by remember { mutableStateOf("01/11/2026") }
        var copperAdjustment by remember { mutableStateOf("0.0") }
        var discountInput by remember { mutableStateOf((activeTable?.rollDiscountPercent ?: 5.0).toString()) }

        AlertDialog(
            onDismissRequest = { showNewWeekDialog = false },
            title = { Text("Nova Tabela Semanal") },
            text = {
                Column {
                    Text(
                        text = "Crie uma nova tabela para a próxima semana mantendo o histórico da anterior:",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = supplierName,
                        onValueChange = { supplierName = it },
                        label = { Text("Nome do Fornecedor") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = weekNum,
                            onValueChange = { weekNum = it.filter { c -> c.isDigit() } },
                            label = { Text("Semana nº") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = yearInput,
                            onValueChange = { yearInput = it.filter { c -> c.isDigit() } },
                            label = { Text("Ano") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = startDateInput,
                            onValueChange = { startDateInput = it },
                            label = { Text("Início") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = endDateInput,
                            onValueChange = { endDateInput = it },
                            label = { Text("Término") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = copperAdjustment,
                        onValueChange = { copperAdjustment = it.replace(',', '.') },
                        label = { Text("Reajuste Cobre s/ Anterior (%)") },
                        placeholder = { Text("Ex: 2.0") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    val w = weekNum.toIntOrNull() ?: 44
                    val y = yearInput.toIntOrNull() ?: 2026
                    val adj = copperAdjustment.toDoubleOrNull() ?: 0.0
                    val disc = discountInput.toDoubleOrNull() ?: 5.0

                    viewModel.createNewWeeklyTable(
                        baseTableId = activeTable?.id,
                        supplierName = supplierName,
                        weekNumber = w,
                        year = y,
                        startDate = startDateInput,
                        endDate = endDateInput,
                        copperAdjustmentPercent = adj,
                        rollDiscountPercent = disc,
                        notes = "Tabela da Semana $w/$y atualizada pelo usuário"
                    )
                    showNewWeekDialog = false
                }) {
                    Text("Criar Tabela")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewWeekDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (showResetConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showResetConfirmDialog = false },
            title = { Text("Restaurar Tabelas Padrão?") },
            text = { Text("Isso restaurará as tabelas originais de Sil, Cobrecom e Prysmian.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.resetToDefaults()
                    showResetConfirmDialog = false
                }) {
                    Text("Restaurar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirmDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag("supplier_tables_screen")
    ) {
        // Header
        item {
            Text(
                text = "Tabelas de Preços Semanais",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
            Text(
                text = "Atualize os preços por semana ou aplique variação da cotação do cobre",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
            Spacer(modifier = Modifier.height(14.dp))
        }

        // Active Table Hero Card
        item {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "TABELA ATIVA",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        val adj = activeTable?.copperAdjustmentPercent ?: 0.0
                        Surface(
                            color = if (adj >= 0) Color(0xFFDCFCE7) else Color(0xFFFEE2E2),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (adj >= 0) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                                    contentDescription = null,
                                    tint = if (adj >= 0) Color(0xFF16A34A) else Color(0xFFDC2626),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (adj >= 0) "+$adj% Cobre" else "$adj% Cobre",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (adj >= 0) Color(0xFF16A34A) else Color(0xFFDC2626)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = activeTable?.supplierName ?: "Fornecedor Padrão",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )

                    Text(
                        text = activeTable?.weekTitle ?: "Semana Vigente",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    )

                    if (!activeTable?.notes.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = activeTable!!.notes,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(12.dp))

                    // Buttons for weekly update
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { showNewWeekDialog = true },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Nova Semana", fontSize = 12.sp)
                        }

                        FilledTonalButton(
                            onClick = { showCopperAdjustmentDialog = true },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Reajustar %", fontSize = 12.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Available Supplier Tables Carousel
        item {
            Text(
                text = "Alternar Fornecedor / Tabela",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(allTables) { table ->
                    val isActive = table.id == activeTable?.id
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { viewModel.setActiveTable(table.id) },
                        color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (isActive) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                            }
                            Column {
                                Text(
                                    text = table.supplierName,
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (isActive) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                                Text(
                                    text = "Sem. ${table.weekNumber}/${table.year}",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = if (isActive) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                    )
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))
        }

        // Section Prices Table Title
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Preços por Bitola (R$/m e R$/Rolo 100m)",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                IconButton(onClick = { showResetConfirmDialog = true }) {
                    Icon(
                        Icons.Default.RestartAlt,
                        contentDescription = "Restaurar padrões",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        // List of all Gauges in active table
        items(StandardCableSections.ALL) { sec ->
            val pricePerMeter = activeTable?.getPrice(sec.sectionMm2) ?: 0.0
            val pricePerRoll = activeTable?.getRollPrice(sec.sectionMm2) ?: 0.0

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Cabo Flexível ${sec.displayLabel}",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = "Rolo 100m: ${viewModel.formatCurrency(pricePerRoll)} (com desc.)",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${viewModel.formatCurrency(pricePerMeter)}/m",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        IconButton(
                            onClick = {
                                editingSection = sec.sectionMm2
                                editingPriceText = "%.2f".format(pricePerMeter).replace(',', '.')
                            },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = "Editar Preço",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

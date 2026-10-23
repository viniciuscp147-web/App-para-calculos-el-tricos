package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.AuthManager
import com.example.data.CableRepository
import com.example.data.DefaultSupplierData
import com.example.data.UserProfile
import com.example.model.BudgetItem
import com.example.model.CableColor
import com.example.model.CircuitType
import com.example.model.PowerCalcMode
import com.example.model.PowerCalcResult
import com.example.model.PowerCalculator
import com.example.model.SavedQuote
import com.example.model.StandardCableSections
import com.example.model.SupplierWeeklyTable
import com.example.model.VoltageDropCalculator
import com.example.model.VoltageDropResult
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

enum class AppTab(val title: String) {
    VOLTAGE_DROP("Queda Tensão"),
    POWER_OHM("Potência & A"),
    CABLE_PRICES("Preço Cabos"),
    TABLES("Tabelas Semanal"),
    BUDGET("Orçamento"),
    SETTINGS("Perfil/Conta")
}

class CableViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: CableRepository
    private val authManager: AuthManager = AuthManager(application)

    val allTables: StateFlow<List<SupplierWeeklyTable>>
    val activeTable: StateFlow<SupplierWeeklyTable?>
    val savedQuotes: StateFlow<List<SavedQuote>>

    // Active Navigation Tab
    private val _currentTab = MutableStateFlow(AppTab.VOLTAGE_DROP)
    val currentTab: StateFlow<AppTab> = _currentTab.asStateFlow()

    // Auth State
    private val _currentUser = MutableStateFlow<UserProfile?>(authManager.getCurrentUser())
    val currentUser: StateFlow<UserProfile?> = _currentUser.asStateFlow()

    // --- Voltage Drop State ---
    private val _vdDistance = MutableStateFlow("30") // 30 meters default
    val vdDistance: StateFlow<String> = _vdDistance.asStateFlow()

    private val _vdSection = MutableStateFlow(2.5) // 2.5 mm² default
    val vdSection: StateFlow<Double> = _vdSection.asStateFlow()

    private val _vdCurrent = MutableStateFlow("16") // 16 A default
    val vdCurrent: StateFlow<String> = _vdCurrent.asStateFlow()

    private val _vdVoltage = MutableStateFlow("220") // 220 V default
    val vdVoltage: StateFlow<String> = _vdVoltage.asStateFlow()

    private val _vdCircuitType = MutableStateFlow(CircuitType.SINGLE_OR_TWO_PHASE)
    val vdCircuitType: StateFlow<CircuitType> = _vdCircuitType.asStateFlow()

    // --- Power & Ohm's Law State ---
    private val _powerMode = MutableStateFlow(PowerCalcMode.SIZING_BY_AMPERES)
    val powerMode: StateFlow<PowerCalcMode> = _powerMode.asStateFlow()

    private val _powerWatts = MutableStateFlow("3500") // 3500W default
    val powerWatts: StateFlow<String> = _powerWatts.asStateFlow()

    private val _powerVolts = MutableStateFlow("220") // 220V default
    val powerVolts: StateFlow<String> = _powerVolts.asStateFlow()

    private val _powerCurrent = MutableStateFlow("16") // 16A default
    val powerCurrent: StateFlow<String> = _powerCurrent.asStateFlow()

    private val _powerFactor = MutableStateFlow("1.0") // 1.0 default
    val powerFactor: StateFlow<String> = _powerFactor.asStateFlow()

    private val _powerIsThreePhase = MutableStateFlow(false)
    val powerIsThreePhase: StateFlow<Boolean> = _powerIsThreePhase.asStateFlow()

    // --- Cable Pricing State ---
    private val _selectedSection = MutableStateFlow(2.5)
    val selectedSection: StateFlow<Double> = _selectedSection.asStateFlow()

    private val _selectedColor = MutableStateFlow(CableColor.BLUE)
    val selectedColor: StateFlow<CableColor> = _selectedColor.asStateFlow()

    private val _metersInput = MutableStateFlow("100")
    val metersInput: StateFlow<String> = _metersInput.asStateFlow()

    // --- Budget State ---
    private val _budgetItems = MutableStateFlow<List<BudgetItem>>(emptyList())
    val budgetItems: StateFlow<List<BudgetItem>> = _budgetItems.asStateFlow()

    private val _clientName = MutableStateFlow("")
    val clientName: StateFlow<String> = _clientName.asStateFlow()

    private val _projectName = MutableStateFlow("")
    val projectName: StateFlow<String> = _projectName.asStateFlow()

    private val _budgetMarginPercent = MutableStateFlow(0.0)
    val budgetMarginPercent: StateFlow<Double> = _budgetMarginPercent.asStateFlow()

    private val _budgetDiscountPercent = MutableStateFlow(0.0)
    val budgetDiscountPercent: StateFlow<Double> = _budgetDiscountPercent.asStateFlow()

    // Notifications
    private val _snackbarEvent = MutableSharedFlow<String>()
    val snackbarEvent: SharedFlow<String> = _snackbarEvent.asSharedFlow()

    init {
        val database = AppDatabase.getDatabase(application, viewModelScope)
        repository = CableRepository(database.priceTableDao(), database.savedQuoteDao())

        allTables = repository.allTables.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        activeTable = repository.activeTable.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            null
        )

        savedQuotes = repository.savedQuotes.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        viewModelScope.launch {
            repository.checkAndSeedDefaults()
        }
    }

    fun setTab(tab: AppTab) {
        _currentTab.value = tab
    }

    // --- Auth & Tutorial Actions ---
    fun loginWithGoogle() {
        val user = authManager.loginWithGoogle()
        _currentUser.value = user
        viewModelScope.launch {
            _snackbarEvent.emit("Conectado com o Google (${user.email})")
        }
    }

    fun loginWithEmail(email: String, pass: String): Boolean {
        val result = authManager.loginWithEmail(email, pass)
        return if (result.isSuccess) {
            val user = result.getOrNull()
            _currentUser.value = user
            viewModelScope.launch {
                _snackbarEvent.emit("Sessão iniciada com sucesso!")
            }
            true
        } else {
            val msg = result.exceptionOrNull()?.message ?: "Falha ao autenticar"
            viewModelScope.launch {
                _snackbarEvent.emit(msg)
            }
            false
        }
    }

    fun logout() {
        authManager.logout()
        _currentUser.value = null
        viewModelScope.launch {
            _snackbarEvent.emit("Desconectado com sucesso.")
        }
    }

    // --- Voltage Drop Methods ---
    fun setVdDistance(distance: String) {
        _vdDistance.value = distance.filter { it.isDigit() || it == '.' || it == ',' }
    }

    fun setVdSection(section: Double) {
        _vdSection.value = section
    }

    fun setVdCurrent(current: String) {
        _vdCurrent.value = current.filter { it.isDigit() || it == '.' || it == ',' }
    }

    fun setVdVoltage(voltage: String) {
        _vdVoltage.value = voltage.filter { it.isDigit() || it == '.' || it == ',' }
    }

    fun setVdCircuitType(type: CircuitType) {
        _vdCircuitType.value = type
    }

    fun getVoltageDropResult(): VoltageDropResult {
        val dist = _vdDistance.value.replace(',', '.').toDoubleOrNull() ?: 0.0
        val curr = _vdCurrent.value.replace(',', '.').toDoubleOrNull() ?: 0.0
        val volt = _vdVoltage.value.replace(',', '.').toDoubleOrNull() ?: 220.0
        val sec = _vdSection.value
        val type = _vdCircuitType.value

        return VoltageDropCalculator.calculate(
            distanceMeters = dist,
            sectionMm2 = sec,
            currentAmperes = curr,
            nominalVoltageVolts = volt,
            circuitType = type
        )
    }

    // --- Power / Ohm's Law Methods ---
    fun setPowerMode(mode: PowerCalcMode) {
        _powerMode.value = mode
    }

    fun setPowerWatts(watts: String) {
        _powerWatts.value = watts.filter { it.isDigit() || it == '.' || it == ',' }.replace(',', '.')
    }

    fun setPowerVolts(volts: String) {
        _powerVolts.value = volts.filter { it.isDigit() || it == '.' || it == ',' }.replace(',', '.')
    }

    fun setPowerCurrent(current: String) {
        _powerCurrent.value = current.filter { it.isDigit() || it == '.' || it == ',' }.replace(',', '.')
    }

    fun setPowerFactor(pf: String) {
        _powerFactor.value = pf.filter { it.isDigit() || it == '.' || it == ',' }.replace(',', '.')
    }

    fun setPowerIsThreePhase(threePhase: Boolean) {
        _powerIsThreePhase.value = threePhase
    }

    fun getPowerCalcResult(): PowerCalcResult {
        val w = _powerWatts.value.toDoubleOrNull() ?: 0.0
        val v = _powerVolts.value.toDoubleOrNull() ?: 0.0
        val a = _powerCurrent.value.toDoubleOrNull() ?: 0.0
        val pf = _powerFactor.value.toDoubleOrNull() ?: 1.0
        val threePhase = _powerIsThreePhase.value

        return when (_powerMode.value) {
            PowerCalcMode.CALCULATE_CURRENT -> PowerCalculator.calculateCurrent(w, v, pf, threePhase)
            PowerCalcMode.CALCULATE_POWER -> PowerCalculator.calculatePower(v, a, pf, threePhase)
            PowerCalcMode.CALCULATE_VOLTAGE -> PowerCalculator.calculateVoltage(w, a, pf, threePhase)
            PowerCalcMode.SIZING_BY_AMPERES -> {
                val minCable = PowerCalculator.findMinCableForCurrent(a)
                PowerCalcResult(
                    calculatedValue = minCable.sectionMm2,
                    unit = "mm²",
                    formatted = minCable.displayLabel,
                    formulaUsed = "Capacidade NBR 5410 (Método B1): até ${minCable.nominalCurrentA}A"
                )
            }
        }
    }

    fun getMinCableForCurrent(): com.example.model.CableSectionInfo {
        val a = _powerCurrent.value.toDoubleOrNull() ?: 0.0
        return PowerCalculator.findMinCableForCurrent(a)
    }

    // --- Cable Pricing Methods ---
    fun selectSection(section: Double) {
        _selectedSection.value = section
    }

    fun selectColor(color: CableColor) {
        _selectedColor.value = color
    }

    fun setMetersInput(meters: String) {
        _metersInput.value = meters.filter { it.isDigit() || it == '.' || it == ',' }.replace(',', '.')
    }

    fun addMeters(delta: Double) {
        val current = _metersInput.value.toDoubleOrNull() ?: 0.0
        val updated = (current + delta).coerceAtLeast(1.0)
        _metersInput.value = if (updated % 1.0 == 0.0) updated.toInt().toString() else "%.1f".format(Locale.US, updated)
    }

    fun getCurrentUnitPrice(): Double {
        val table = activeTable.value ?: return DefaultSupplierData.silPrices[_selectedSection.value] ?: 4.55
        return table.getPrice(_selectedSection.value)
    }

    fun getCurrentRollDiscount(): Double {
        return activeTable.value?.rollDiscountPercent ?: 5.0
    }

    fun getCurrentMeters(): Double {
        return _metersInput.value.toDoubleOrNull() ?: 0.0
    }

    fun getCalculatedTotal(): Double {
        val meters = getCurrentMeters()
        val unitPrice = getCurrentUnitPrice()
        val discount = getCurrentRollDiscount()
        val rolls = (meters / 100).toInt()
        val leftover = meters % 100
        val rollUnitPrice = unitPrice * 100 * (1.0 - discount / 100.0)
        return (rolls * rollUnitPrice) + (leftover * unitPrice)
    }

    fun getCalculatedCopperWeight(): Double {
        val meters = getCurrentMeters()
        val info = StandardCableSections.getBySection(_selectedSection.value)
        return (meters / 1000.0) * info.copperWeightKgKm
    }

    // --- Budget Methods ---
    fun addItemToBudget() {
        val meters = getCurrentMeters()
        if (meters <= 0) {
            viewModelScope.launch {
                _snackbarEvent.emit("Informe a quantidade de metros do cabo.")
            }
            return
        }

        val item = BudgetItem(
            sectionMm2 = _selectedSection.value,
            color = _selectedColor.value,
            meters = meters,
            unitPricePerMeter = getCurrentUnitPrice(),
            rollDiscountPercent = getCurrentRollDiscount()
        )

        _budgetItems.value = _budgetItems.value + item
        viewModelScope.launch {
            _snackbarEvent.emit("Adicionado: ${item.meters.toInt()}m de ${item.sectionMm2.toString().replace('.', ',')} mm²")
        }
    }

    fun removeItemFromBudget(itemId: String) {
        _budgetItems.value = _budgetItems.value.filterNot { it.id == itemId }
    }

    fun clearBudget() {
        _budgetItems.value = emptyList()
    }

    fun setClientName(name: String) {
        _clientName.value = name
    }

    fun setProjectName(name: String) {
        _projectName.value = name
    }

    fun setMarginPercent(margin: Double) {
        _budgetMarginPercent.value = margin
    }

    fun setDiscountPercent(discount: Double) {
        _budgetDiscountPercent.value = discount
    }

    fun saveBudget() {
        if (_budgetItems.value.isEmpty()) {
            viewModelScope.launch {
                _snackbarEvent.emit("O orçamento está vazio. Adicione itens primeiro.")
            }
            return
        }

        val table = activeTable.value
        val supplier = table?.supplierName ?: "Fornecedor Padrão"
        val week = table?.weekTitle ?: "Semana Vigente"

        viewModelScope.launch {
            repository.saveQuote(
                clientName = _clientName.value.ifBlank { "Cliente Geral" },
                projectName = _projectName.value.ifBlank { "Obra Residencial/Comercial" },
                supplierName = supplier,
                weekRef = week,
                items = _budgetItems.value,
                discountPercent = _budgetDiscountPercent.value,
                marginPercent = _budgetMarginPercent.value,
                notes = "Gerado por Eletric Calculator com tabela $supplier"
            )
            _snackbarEvent.emit("Orçamento salvo com sucesso!")
        }
    }

    fun loadQuoteIntoBudget(quote: SavedQuote) {
        _budgetItems.value = quote.items
        _clientName.value = quote.clientName
        _projectName.value = quote.projectName
        _budgetDiscountPercent.value = quote.discountPercent
        _budgetMarginPercent.value = quote.marginPercent
        _currentTab.value = AppTab.BUDGET
        viewModelScope.launch {
            _snackbarEvent.emit("Orçamento de '${quote.clientName}' carregado!")
        }
    }

    fun deleteQuote(quoteId: String) {
        viewModelScope.launch {
            repository.deleteQuote(quoteId)
            _snackbarEvent.emit("Orçamento removido.")
        }
    }

    // Weekly Supplier Table actions
    fun setActiveTable(tableId: String) {
        viewModelScope.launch {
            repository.setActiveTable(tableId)
            _snackbarEvent.emit("Tabela ativa atualizada!")
        }
    }

    fun updateSectionPrice(tableId: String, sectionMm2: Double, newPrice: Double) {
        viewModelScope.launch {
            repository.updateSectionPrice(tableId, sectionMm2, newPrice)
            _snackbarEvent.emit("Preço da seção ${sectionMm2.toString().replace('.', ',')} mm² atualizado para R$ ${"%.2f".format(newPrice)}/m")
        }
    }

    fun applyWeeklyCopperAdjustment(tableId: String, percentage: Double) {
        viewModelScope.launch {
            repository.applyCopperAdjustment(tableId, percentage)
            val sign = if (percentage >= 0) "+$percentage%" else "$percentage%"
            _snackbarEvent.emit("Reajuste semanal de $sign aplicado com sucesso em todas as bitolas!")
        }
    }

    fun createNewWeeklyTable(
        baseTableId: String?,
        supplierName: String,
        weekNumber: Int,
        year: Int,
        startDate: String,
        endDate: String,
        copperAdjustmentPercent: Double,
        rollDiscountPercent: Double,
        notes: String
    ) {
        viewModelScope.launch {
            val newId = repository.createNewWeeklyTable(
                baseTableId = baseTableId,
                supplierName = supplierName,
                weekNumber = weekNumber,
                year = year,
                startDate = startDate,
                endDate = endDate,
                copperAdjustmentPercent = copperAdjustmentPercent,
                rollDiscountPercent = rollDiscountPercent,
                notes = notes
            )
            _snackbarEvent.emit("Nova tabela da Semana $weekNumber criada e definida como ativa!")
        }
    }

    fun resetToDefaults() {
        viewModelScope.launch {
            repository.resetToDefaultTables()
            _snackbarEvent.emit("Tabelas padrão restauradas.")
        }
    }

    fun formatCurrency(value: Double): String {
        val ptBr = Locale("pt", "BR")
        val format = NumberFormat.getCurrencyInstance(ptBr)
        return format.format(value)
    }

    fun buildShareableQuoteText(): String {
        val table = activeTable.value
        val items = _budgetItems.value
        val subtotal = items.sumOf { it.totalPrice }
        val discount = _budgetDiscountPercent.value
        val margin = _budgetMarginPercent.value
        val total = (subtotal * (1.0 - discount / 100.0)) * (1.0 + margin / 100.0)
        val totalMeters = items.sumOf { it.meters }
        val totalCopperKg = items.sumOf { it.totalCopperWeightKg }

        val sb = StringBuilder()
        sb.append("📋 *ORÇAMENTO DE CABOS ELÉTRICOS*\n")
        sb.append("════════════════════════════\n")
        if (_clientName.value.isNotBlank()) sb.append("👤 *Cliente:* ${_clientName.value}\n")
        if (_projectName.value.isNotBlank()) sb.append("📍 *Obra:* ${_projectName.value}\n")
        sb.append("🏢 *Fornecedor:* ${table?.supplierName ?: "Sil / Cobrecom"}\n")
        sb.append("📅 *Vigência:* ${table?.weekTitle ?: "Semana Vigente"}\n")
        sb.append("════════════════════════════\n\n")

        sb.append("📦 *ITENS:*\n")
        items.forEachIndexed { index, item ->
            val sectionStr = item.sectionMm2.toString().replace('.', ',')
            val rollsStr = if (item.rolls100mCount > 0) {
                if (item.leftoverMeters > 0) " (${item.rolls100mCount} rolos + ${item.leftoverMeters.toInt()}m)"
                else " (${item.rolls100mCount} rolos de 100m)"
            } else ""

            sb.append("${index + 1}. *Cabo Flexível $sectionStr mm² - ${item.color.ptName}*\n")
            sb.append("   • Metragem: ${item.meters.toInt()}m$rollsStr\n")
            sb.append("   • Unitário: ${formatCurrency(item.unitPricePerMeter)}/m\n")
            sb.append("   • Subtotal: *${formatCurrency(item.totalPrice)}*\n\n")
        }

        sb.append("════════════════════════════\n")
        sb.append("📏 *Total de Cabos:* ${totalMeters.toInt()} metros\n")
        sb.append("⚖️ *Peso Estimado Cobre:* ${"%.2f".format(Locale.US, totalCopperKg)} kg\n")
        sb.append("💵 *Subtotal:* ${formatCurrency(subtotal)}\n")
        if (discount > 0) sb.append("🏷️ *Desconto (${discount}%):* -${formatCurrency(subtotal * discount / 100.0)}\n")
        if (margin > 0) sb.append("📈 *Margem (${margin}%):* +${formatCurrency(subtotal * (1.0 - discount / 100.0) * margin / 100.0)}\n")
        sb.append("💰 *TOTAL: ${formatCurrency(total)}*\n")
        sb.append("════════════════════════════\n")
        sb.append("ℹ️ *Normas:* NBR NM 247-3 / NBR 5410.\n")
        sb.append("Gerado por Eletric Calculator")

        return sb.toString()
    }
}

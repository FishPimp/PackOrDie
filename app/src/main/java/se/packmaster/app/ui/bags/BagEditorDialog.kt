package se.packmaster.app.ui.bags

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import se.packmaster.app.R
import se.packmaster.app.ui.components.EmojiButton
import se.packmaster.app.ui.components.daysLabel
import se.packmaster.app.ui.components.epochDayToUtcMillis
import se.packmaster.app.ui.components.formatAmount
import se.packmaster.app.ui.components.formatEpochDay
import se.packmaster.app.ui.components.parseDecimal
import se.packmaster.app.ui.components.utcMillisToEpochDay
import se.packmaster.domain.model.Bag
import se.packmaster.domain.model.BagType
import se.packmaster.domain.usecase.QuantityCalculator

/** Dialog för att skapa eller redigera en väska (vardag eller resa). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BagEditorDialog(
    initial: Bag?,
    onSave: (Bag) -> Unit,
    onDismiss: () -> Unit,
) {
    val base = initial ?: Bag(name = "", emoji = "🧳", type = BagType.EVERYDAY, manualDays = 3)
    var name by remember { mutableStateOf(base.name) }
    var emoji by remember { mutableStateOf(base.emoji) }
    var type by remember { mutableStateOf(base.type) }
    var useDates by remember { mutableStateOf(base.startEpochDay != null) }
    var start by remember { mutableStateOf(base.startEpochDay) }
    var end by remember { mutableStateOf(base.endEpochDay) }
    var days by remember { mutableStateOf(base.manualDays.coerceAtLeast(1)) }
    var laundry by remember { mutableStateOf(base.hasLaundry) }
    var weightEnabled by remember { mutableStateOf(base.maxWeightGrams != null) }
    var weightText by remember {
        mutableStateOf(base.maxWeightGrams?.let { formatAmount(it / 1000.0) } ?: "23")
    }
    var showDatePicker by remember { mutableStateOf(false) }

    val maxKg = parseDecimal(weightText)
    val datesValid = !useDates || (start != null && end != null)
    val isValid = name.isNotBlank() &&
        (type == BagType.EVERYDAY || datesValid) &&
        (!weightEnabled || (maxKg != null && maxKg > 0))

    fun buildBag(): Bag {
        val trip = type == BagType.TRIP
        return base.copy(
            name = name.trim(),
            emoji = emoji,
            type = type,
            startEpochDay = if (trip && useDates) start else null,
            endEpochDay = if (trip && useDates) end else null,
            manualDays = if (trip) days else base.manualDays,
            hasLaundry = trip && laundry,
            maxWeightGrams = if (weightEnabled && maxKg != null) (maxKg * 1000).toInt() else null,
        )
    }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(stringResource(if (initial == null) R.string.bag_new_title else R.string.bag_edit_title))
                    },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.action_close))
                        }
                    },
                    actions = {
                        TextButton(enabled = isValid, onClick = { onSave(buildBag()) }) {
                            Text(stringResource(R.string.action_save))
                        }
                    },
                )
            },
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .imePadding()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    EmojiButton(emoji = emoji, onPick = { emoji = it })
                    Spacer(Modifier.width(12.dp))
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text(stringResource(R.string.bag_name)) },
                        placeholder = {
                            Text(
                                stringResource(
                                    if (type == BagType.TRIP) R.string.bag_name_hint_trip else R.string.bag_name_hint_everyday
                                )
                            )
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                        modifier = Modifier.weight(1f),
                    )
                }

                Text(stringResource(R.string.bag_type), style = MaterialTheme.typography.titleSmall)
                SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                    BagType.entries.forEachIndexed { index, option ->
                        SegmentedButton(
                            selected = type == option,
                            onClick = { type = option },
                            shape = SegmentedButtonDefaults.itemShape(index, BagType.entries.size),
                        ) {
                            Text(
                                stringResource(
                                    if (option == BagType.EVERYDAY) R.string.bag_type_everyday else R.string.bag_type_trip
                                )
                            )
                        }
                    }
                }
                Text(
                    stringResource(
                        if (type == BagType.EVERYDAY) R.string.bag_type_everyday_hint else R.string.bag_type_trip_hint
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                if (type == BagType.TRIP) {
                    Text(stringResource(R.string.bag_duration), style = MaterialTheme.typography.titleSmall)
                    SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                        SegmentedButton(
                            selected = !useDates,
                            onClick = { useDates = false },
                            shape = SegmentedButtonDefaults.itemShape(0, 2),
                        ) { Text(stringResource(R.string.bag_duration_days)) }
                        SegmentedButton(
                            selected = useDates,
                            onClick = { useDates = true },
                            shape = SegmentedButtonDefaults.itemShape(1, 2),
                        ) { Text(stringResource(R.string.bag_duration_dates)) }
                    }

                    if (useDates) {
                        OutlinedButton(onClick = { showDatePicker = true }, modifier = Modifier.fillMaxWidth()) {
                            Icon(Icons.Filled.CalendarMonth, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            val s = start
                            val e = end
                            Text(
                                if (s != null && e != null) {
                                    stringResource(
                                        R.string.bag_date_range,
                                        formatEpochDay(s),
                                        formatEpochDay(e),
                                        daysLabel((e - s + 1).toInt()),
                                    )
                                } else {
                                    stringResource(R.string.bag_pick_dates)
                                }
                            )
                        }
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            FilledTonalIconButton(onClick = { if (days > 1) days-- }) {
                                Icon(Icons.Filled.Remove, contentDescription = stringResource(R.string.action_decrease))
                            }
                            Text(
                                daysLabel(days),
                                style = MaterialTheme.typography.titleLarge,
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 8.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            )
                            FilledTonalIconButton(onClick = { if (days < 365) days++ }) {
                                Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.action_increase))
                            }
                        }
                    }

                    SwitchRow(
                        title = stringResource(R.string.bag_laundry),
                        subtitle = stringResource(R.string.bag_laundry_hint, QuantityCalculator.LAUNDRY_MAX_DAYS),
                        checked = laundry,
                        onCheckedChange = { laundry = it },
                    )
                }

                SwitchRow(
                    title = stringResource(R.string.bag_weight_budget),
                    subtitle = stringResource(R.string.bag_weight_budget_hint),
                    checked = weightEnabled,
                    onCheckedChange = { weightEnabled = it },
                )
                if (weightEnabled) {
                    OutlinedTextField(
                        value = weightText,
                        onValueChange = { weightText = it },
                        label = { Text(stringResource(R.string.bag_max_weight)) },
                        suffix = { Text(stringResource(R.string.unit_kg)) },
                        isError = maxKg == null || maxKg <= 0,
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }

    if (showDatePicker) {
        val state = rememberDateRangePickerState(
            initialSelectedStartDateMillis = start?.let(::epochDayToUtcMillis),
            initialSelectedEndDateMillis = end?.let(::epochDayToUtcMillis),
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    enabled = state.selectedStartDateMillis != null && state.selectedEndDateMillis != null,
                    onClick = {
                        start = state.selectedStartDateMillis?.let(::utcMillisToEpochDay)
                        end = state.selectedEndDateMillis?.let(::utcMillisToEpochDay)
                        showDatePicker = false
                    },
                ) { Text(stringResource(R.string.action_ok)) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text(stringResource(R.string.action_cancel)) }
            },
        ) {
            DateRangePicker(
                state = state,
                title = {
                    Text(
                        stringResource(R.string.bag_pick_dates),
                        modifier = Modifier.padding(start = 24.dp, end = 12.dp, top = 16.dp),
                    )
                },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
fun SwitchRow(title: String, subtitle: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(Modifier.width(12.dp))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

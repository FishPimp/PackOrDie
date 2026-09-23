package se.packmaster.app.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.launch
import se.packmaster.app.R
import se.packmaster.domain.model.BagItem
import se.packmaster.domain.model.CatalogItem
import se.packmaster.domain.model.Category
import se.packmaster.domain.model.Profile
import se.packmaster.domain.model.QuantityRule

/** De redigerbara fälten som katalogobjekt och packlisteobjekt har gemensamt. */
data class ItemFields(
    val name: String = "",
    val emoji: String = "📦",
    val imagePath: String? = null,
    val categoryId: Long? = null,
    val profileId: Long? = null,
    val rule: QuantityRule = QuantityRule.FIXED,
    val amount: Double = 1.0,
    val weightGrams: Int = 0,
    val laundryAffected: Boolean = false,
)

fun CatalogItem.toFields() =
    ItemFields(name, emoji, imagePath, categoryId, profileId, rule, amount, weightGrams, laundryAffected)

fun BagItem.toFields() =
    ItemFields(name, emoji, imagePath, categoryId, profileId, rule, amount, weightGrams, laundryAffected)

fun CatalogItem.withFields(f: ItemFields) = copy(
    name = f.name, emoji = f.emoji, imagePath = f.imagePath, categoryId = f.categoryId,
    profileId = f.profileId, rule = f.rule, amount = f.amount, weightGrams = f.weightGrams,
    laundryAffected = f.laundryAffected,
)

fun BagItem.withFields(f: ItemFields) = copy(
    name = f.name, emoji = f.emoji, imagePath = f.imagePath, categoryId = f.categoryId,
    profileId = f.profileId, rule = f.rule, amount = f.amount, weightGrams = f.weightGrams,
    laundryAffected = f.laundryAffected,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemEditorDialog(
    title: String,
    initial: ItemFields,
    categories: List<Category>,
    profiles: List<Profile>,
    onImportImage: suspend (Uri) -> String?,
    onCreateCategory: suspend (name: String, emoji: String) -> Long,
    onSave: (ItemFields) -> Unit,
    onDismiss: () -> Unit,
    onDelete: (() -> Unit)? = null,
    extraContent: @Composable ColumnScope.() -> Unit = {},
) {
    val scope = rememberCoroutineScope()
    var fields by remember { mutableStateOf(initial) }
    var amountText by remember { mutableStateOf(formatAmount(initial.amount)) }
    var weightText by remember { mutableStateOf(if (initial.weightGrams > 0) initial.weightGrams.toString() else "") }
    var showNewCategory by remember { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf(false) }

    val amount = parseDecimal(amountText)
    val weight = if (weightText.isBlank()) 0 else weightText.trim().toIntOrNull()
    val isValid = fields.name.isNotBlank() && amount != null && amount > 0 && weight != null && weight >= 0

    val pickImage = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            scope.launch {
                onImportImage(uri)?.let { path -> fields = fields.copy(imagePath = path) }
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(title) },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.action_close))
                        }
                    },
                    actions = {
                        if (onDelete != null) {
                            IconButton(onClick = { confirmDelete = true }) {
                                Icon(Icons.Filled.Delete, contentDescription = stringResource(R.string.action_delete))
                            }
                        }
                        TextButton(
                            enabled = isValid,
                            onClick = {
                                onSave(
                                    fields.copy(
                                        name = fields.name.trim(),
                                        amount = amount ?: 1.0,
                                        weightGrams = weight ?: 0,
                                    )
                                )
                            },
                        ) { Text(stringResource(R.string.action_save)) }
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
                // Ikon/bild och namn
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ItemAvatar(emoji = fields.emoji, imagePath = fields.imagePath, size = 72.dp)
                    Spacer(Modifier.width(16.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        EmojiButtonRow(
                            emoji = fields.emoji,
                            onPick = { fields = fields.copy(emoji = it, imagePath = null) },
                        )
                        Row {
                            OutlinedButton(onClick = {
                                pickImage.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                            }) {
                                Icon(Icons.Filled.AddPhotoAlternate, contentDescription = null)
                                Spacer(Modifier.width(6.dp))
                                Text(stringResource(R.string.editor_pick_image))
                            }
                            if (fields.imagePath != null) {
                                IconButton(onClick = { fields = fields.copy(imagePath = null) }) {
                                    Icon(
                                        Icons.Filled.Clear,
                                        contentDescription = stringResource(R.string.editor_remove_image),
                                    )
                                }
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = fields.name,
                    onValueChange = { fields = fields.copy(name = it) },
                    label = { Text(stringResource(R.string.editor_name)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                    modifier = Modifier.fillMaxWidth(),
                )

                // Kategori
                Row(verticalAlignment = Alignment.CenterVertically) {
                    DropdownSelector(
                        label = stringResource(R.string.editor_category),
                        options = listOf<Category?>(null) + categories,
                        selected = categories.firstOrNull { it.id == fields.categoryId },
                        optionLabel = { it?.let { c -> "${c.emoji}  ${c.name}" } ?: stringResource(R.string.none_category) },
                        onSelect = { fields = fields.copy(categoryId = it?.id) },
                        modifier = Modifier.weight(1f),
                    )
                    TextButton(onClick = { showNewCategory = true }) {
                        Text(stringResource(R.string.editor_new_category))
                    }
                }

                // Person / profil
                DropdownSelector(
                    label = stringResource(R.string.editor_profile),
                    options = listOf<Profile?>(null) + profiles,
                    selected = profiles.firstOrNull { it.id == fields.profileId },
                    optionLabel = { it?.let { p -> "${p.emoji}  ${p.name}" } ?: stringResource(R.string.none_profile) },
                    onSelect = { fields = fields.copy(profileId = it?.id) },
                    modifier = Modifier.fillMaxWidth(),
                )

                extraContent()

                // Antal
                Text(stringResource(R.string.editor_quantity_rule), style = MaterialTheme.typography.titleSmall)
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    QuantityRule.entries.forEachIndexed { index, rule ->
                        SegmentedButton(
                            selected = fields.rule == rule,
                            onClick = { fields = fields.copy(rule = rule) },
                            shape = SegmentedButtonDefaults.itemShape(index, QuantityRule.entries.size),
                        ) { Text(ruleLabel(rule), maxLines = 1) }
                    }
                }
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = {
                        Text(
                            when (fields.rule) {
                                QuantityRule.FIXED -> stringResource(R.string.editor_amount_fixed)
                                QuantityRule.PER_DAY -> stringResource(R.string.editor_amount_per_day)
                                QuantityRule.PER_NIGHT -> stringResource(R.string.editor_amount_per_night)
                            }
                        )
                    },
                    supportingText = { Text(stringResource(R.string.editor_amount_hint)) },
                    isError = amount == null || amount <= 0,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                )

                if (fields.rule != QuantityRule.FIXED) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(stringResource(R.string.editor_laundry), style = MaterialTheme.typography.bodyLarge)
                            Text(
                                stringResource(R.string.editor_laundry_hint),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Switch(
                            checked = fields.laundryAffected,
                            onCheckedChange = { fields = fields.copy(laundryAffected = it) },
                        )
                    }
                }

                OutlinedTextField(
                    value = weightText,
                    onValueChange = { weightText = it.filter(Char::isDigit).take(6) },
                    label = { Text(stringResource(R.string.editor_weight)) },
                    supportingText = { Text(stringResource(R.string.editor_weight_hint)) },
                    suffix = { Text(stringResource(R.string.unit_grams)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                )

                if (onDelete != null) {
                    Button(
                        onClick = { confirmDelete = true },
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer,
                        ),
                        modifier = Modifier.fillMaxWidth(),
                    ) { Text(stringResource(R.string.action_delete)) }
                }
            }
        }
    }

    if (showNewCategory) {
        NameEmojiDialog(
            title = stringResource(R.string.category_new),
            initialName = "",
            initialEmoji = "🏷️",
            onConfirm = { name, emoji ->
                scope.launch {
                    val id = onCreateCategory(name, emoji)
                    fields = fields.copy(categoryId = id)
                }
            },
            onDismiss = { showNewCategory = false },
        )
    }

    if (confirmDelete && onDelete != null) {
        ConfirmDialog(
            title = stringResource(R.string.confirm_delete_item_title),
            message = stringResource(R.string.confirm_delete_item_message, fields.name),
            confirmLabel = stringResource(R.string.action_delete),
            onConfirm = onDelete,
            onDismiss = { confirmDelete = false },
        )
    }
}

@Composable
private fun EmojiButtonRow(emoji: String, onPick: (String) -> Unit) {
    var open by remember { mutableStateOf(false) }
    OutlinedButton(onClick = { open = true }) {
        Text(emoji)
        Spacer(Modifier.width(6.dp))
        Text(stringResource(R.string.editor_pick_emoji))
    }
    if (open) EmojiPickerDialog(current = emoji, onPick = onPick, onDismiss = { open = false })
}

/** Enkel dialog för namn + emoji, används för kategorier, profiler och underväskor. */
@Composable
fun NameEmojiDialog(
    title: String,
    initialName: String,
    initialEmoji: String,
    onConfirm: (name: String, emoji: String) -> Unit,
    onDismiss: () -> Unit,
    extraContent: @Composable ColumnScope.() -> Unit = {},
    confirmEnabled: Boolean = true,
) {
    var name by remember { mutableStateOf(initialName) }
    var emoji by remember { mutableStateOf(initialEmoji) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    EmojiButton(emoji = emoji, onPick = { emoji = it })
                    Spacer(Modifier.width(12.dp))
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text(stringResource(R.string.editor_name)) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                        modifier = Modifier.weight(1f),
                    )
                }
                extraContent()
            }
        },
        confirmButton = {
            TextButton(
                enabled = name.isNotBlank() && confirmEnabled,
                onClick = {
                    onConfirm(name.trim(), emoji)
                    onDismiss()
                },
            ) { Text(stringResource(R.string.action_save)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) }
        },
    )
}

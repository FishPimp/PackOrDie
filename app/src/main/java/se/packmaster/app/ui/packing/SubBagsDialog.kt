package se.packmaster.app.ui.packing

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import se.packmaster.app.R
import se.packmaster.app.ui.components.ConfirmDialog
import se.packmaster.app.ui.components.ItemAvatar
import se.packmaster.app.ui.components.NameEmojiDialog
import se.packmaster.app.ui.components.formatAmount
import se.packmaster.app.ui.components.formatWeight
import se.packmaster.app.ui.components.parseDecimal
import se.packmaster.domain.model.SubBag

/** Hantera underväskor/förvaring, t.ex. Kabinväska, Hygienväska, Barnens ryggsäck. */
@Composable
fun SubBagsDialog(
    bagId: Long,
    subBags: List<SubBag>,
    onSave: (SubBag) -> Unit,
    onDelete: (SubBag) -> Unit,
    onDismiss: () -> Unit,
) {
    var editing by remember { mutableStateOf<SubBag?>(null) }
    var toDelete by remember { mutableStateOf<SubBag?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.sub_bags_title)) },
        text = {
            Column(
                Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    stringResource(R.string.sub_bags_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (subBags.isEmpty()) {
                    Text(stringResource(R.string.sub_bags_empty), style = MaterialTheme.typography.bodyMedium)
                }
                subBags.forEach { subBag ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        ItemAvatar(emoji = subBag.emoji, size = 36.dp)
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(subBag.name, style = MaterialTheme.typography.bodyLarge)
                            subBag.maxWeightGrams?.let {
                                Text(
                                    stringResource(R.string.chip_max_weight, formatWeight(it)),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                        IconButton(onClick = { editing = subBag }) {
                            Icon(Icons.Filled.Edit, contentDescription = stringResource(R.string.action_edit))
                        }
                        IconButton(onClick = { toDelete = subBag }) {
                            Icon(Icons.Filled.Delete, contentDescription = stringResource(R.string.action_delete))
                        }
                    }
                }
                TextButton(onClick = { editing = SubBag(bagId = bagId, name = "", emoji = "🎒") }) {
                    Icon(Icons.Filled.Add, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text(stringResource(R.string.sub_bags_add))
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_done)) }
        },
    )

    editing?.let { subBag ->
        var weightText by remember(subBag) {
            mutableStateOf(subBag.maxWeightGrams?.let { formatAmount(it / 1000.0) } ?: "")
        }
        val kg = parseDecimal(weightText)
        NameEmojiDialog(
            title = stringResource(if (subBag.id == 0L) R.string.sub_bags_add else R.string.sub_bags_edit),
            initialName = subBag.name,
            initialEmoji = subBag.emoji,
            confirmEnabled = weightText.isBlank() || (kg != null && kg > 0),
            onConfirm = { name, emoji ->
                onSave(
                    subBag.copy(
                        name = name,
                        emoji = emoji,
                        maxWeightGrams = kg?.takeIf { it > 0 }?.let { (it * 1000).toInt() },
                    )
                )
            },
            onDismiss = { editing = null },
            extraContent = {
                OutlinedTextField(
                    value = weightText,
                    onValueChange = { weightText = it },
                    label = { Text(stringResource(R.string.sub_bags_max_weight)) },
                    suffix = { Text(stringResource(R.string.unit_kg)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                )
            },
        )
    }

    toDelete?.let { subBag ->
        ConfirmDialog(
            title = stringResource(R.string.confirm_delete_sub_bag_title),
            message = stringResource(R.string.confirm_delete_sub_bag_message, subBag.name),
            confirmLabel = stringResource(R.string.action_delete),
            onConfirm = { onDelete(subBag) },
            onDismiss = { toDelete = null },
        )
    }
}

package se.packmaster.app.ui.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.dp
import se.packmaster.app.R
import se.packmaster.app.ui.components.ConfirmDialog
import se.packmaster.app.ui.components.ItemAvatar
import se.packmaster.app.ui.components.NameEmojiDialog
import se.packmaster.domain.model.Category

/** Hantera kategorier: lägg till egna, byt namn/ikon och ta bort. */
@Composable
fun CategoriesDialog(
    categories: List<Category>,
    onSave: (Category) -> Unit,
    onDelete: (Category) -> Unit,
    onDismiss: () -> Unit,
) {
    var editing by remember { mutableStateOf<Category?>(null) }
    var toDelete by remember { mutableStateOf<Category?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.categories_title)) },
        text = {
            Column(
                Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                categories.forEach { category ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        ItemAvatar(emoji = category.emoji, size = 36.dp)
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(category.name, style = MaterialTheme.typography.bodyLarge)
                            if (category.isCustom) {
                                Text(
                                    stringResource(R.string.catalog_custom_badge),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                        IconButton(onClick = { editing = category }) {
                            Icon(Icons.Filled.Edit, contentDescription = stringResource(R.string.action_edit))
                        }
                        IconButton(onClick = { toDelete = category }) {
                            Icon(Icons.Filled.Delete, contentDescription = stringResource(R.string.action_delete))
                        }
                    }
                }
                TextButton(onClick = { editing = Category(name = "", emoji = "🏷️", isCustom = true) }) {
                    Icon(Icons.Filled.Add, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text(stringResource(R.string.category_new))
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_done)) }
        },
    )

    editing?.let { category ->
        NameEmojiDialog(
            title = stringResource(if (category.id == 0L) R.string.category_new else R.string.category_edit),
            initialName = category.name,
            initialEmoji = category.emoji,
            onConfirm = { name, emoji -> onSave(category.copy(name = name, emoji = emoji)) },
            onDismiss = { editing = null },
        )
    }

    toDelete?.let { category ->
        ConfirmDialog(
            title = stringResource(R.string.confirm_delete_category_title),
            message = stringResource(R.string.confirm_delete_category_message, category.name),
            confirmLabel = stringResource(R.string.action_delete),
            onConfirm = { onDelete(category) },
            onDismiss = { toDelete = null },
        )
    }
}

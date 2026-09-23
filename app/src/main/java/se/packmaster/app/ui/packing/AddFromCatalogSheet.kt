package se.packmaster.app.ui.packing

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import se.packmaster.app.R
import se.packmaster.app.ui.components.DropdownSelector
import se.packmaster.app.ui.components.ItemAvatar
import se.packmaster.app.ui.components.ruleDescription
import se.packmaster.domain.model.CatalogItem
import se.packmaster.domain.model.Category
import se.packmaster.domain.model.SubBag

/** Bottenark där man väljer objekt ur masterkatalogen att lägga i väskan. */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun AddFromCatalogSheet(
    catalog: List<CatalogItem>,
    categories: List<Category>,
    subBags: List<SubBag>,
    alreadyInBag: Set<Long>,
    onAdd: (ids: List<Long>, subBagId: Long?) -> Unit,
    onCreateNew: (subBagId: Long?) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var query by remember { mutableStateOf("") }
    val selected = remember { mutableStateListOf<Long>() }
    var targetSubBag by remember { mutableStateOf<SubBag?>(null) }

    val filtered = catalog.filter { query.isBlank() || it.name.contains(query.trim(), ignoreCase = true) }
    val categoryOrder = categories.withIndex().associate { (i, c) -> c.id to i }
    val grouped = filtered
        .groupBy { item -> categories.firstOrNull { it.id == item.categoryId } }
        .toList()
        .sortedBy { (category, _) -> category?.let { categoryOrder[it.id] } ?: Int.MAX_VALUE }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            Modifier
                .fillMaxHeight(0.92f)
                .padding(horizontal = 16.dp),
        ) {
            Text(stringResource(R.string.add_sheet_title), style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.padding(4.dp))
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                placeholder = { Text(stringResource(R.string.search_catalog)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            if (subBags.isNotEmpty()) {
                Spacer(Modifier.padding(4.dp))
                DropdownSelector(
                    label = stringResource(R.string.add_sheet_target),
                    options = listOf<SubBag?>(null) + subBags,
                    selected = targetSubBag,
                    optionLabel = { it?.let { s -> "${s.emoji}  ${s.name}" } ?: stringResource(R.string.main_bag) },
                    onSelect = { targetSubBag = it },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                grouped.forEach { (category, items) ->
                    stickyHeader(key = "cat_${category?.id}") {
                        Surface(color = MaterialTheme.colorScheme.surfaceContainerLow, modifier = Modifier.fillMaxWidth()) {
                            Text(
                                category?.let { "${it.emoji}  ${it.name}" } ?: stringResource(R.string.none_category),
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(vertical = 8.dp),
                            )
                        }
                    }
                    items(items, key = { it.id }) { item ->
                        val checked = item.id in selected
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { if (checked) selected.remove(item.id) else selected.add(item.id) }
                                .padding(vertical = 4.dp),
                        ) {
                            Checkbox(
                                checked = checked,
                                onCheckedChange = { if (it) selected.add(item.id) else selected.remove(item.id) },
                            )
                            ItemAvatar(emoji = item.emoji, imagePath = item.imagePath, size = 36.dp)
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(item.name, style = MaterialTheme.typography.bodyLarge)
                                Text(
                                    buildString {
                                        append(ruleDescription(item.rule, item.amount))
                                        if (item.id in alreadyInBag) {
                                            append(" · ")
                                            append(stringResource(R.string.add_sheet_already_added))
                                        }
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(vertical = 12.dp),
            ) {
                OutlinedButton(onClick = { onCreateNew(targetSubBag?.id) }, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Filled.Add, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text(stringResource(R.string.add_sheet_create_new), maxLines = 1)
                }
                Button(
                    enabled = selected.isNotEmpty(),
                    onClick = { onAdd(selected.toList(), targetSubBag?.id) },
                    modifier = Modifier.weight(1f),
                ) {
                    Text(pluralStringResource(R.plurals.add_selected, selected.size, selected.size), maxLines = 1)
                }
            }
        }
    }
}

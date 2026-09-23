package se.packmaster.app.ui.packing

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Backpack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import se.packmaster.app.R
import se.packmaster.app.ui.bags.BagEditorDialog
import se.packmaster.app.ui.bags.bagSubtitle
import se.packmaster.app.ui.components.ConfirmDialog
import se.packmaster.app.ui.components.DropdownSelector
import se.packmaster.app.ui.components.EmptyState
import se.packmaster.app.ui.components.ItemEditorDialog
import se.packmaster.app.ui.components.ItemFields
import se.packmaster.app.ui.components.rememberHaptics
import se.packmaster.app.ui.components.toFields
import se.packmaster.app.ui.components.withFields
import se.packmaster.domain.model.BagItem
import se.packmaster.domain.model.CatalogItem
import se.packmaster.domain.model.GroupKey
import se.packmaster.domain.model.Grouping
import se.packmaster.domain.model.SubBag

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun PackingScreen(
    onBack: () -> Unit,
    viewModel: PackingViewModel = viewModel(factory = PackingViewModel.Factory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val haptics = rememberHaptics()
    val context = LocalContext.current
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val currentOnBack by rememberUpdatedState(onBack)

    var menuOpen by remember { mutableStateOf(false) }
    var showAddSheet by remember { mutableStateOf(false) }
    var showSubBags by remember { mutableStateOf(false) }
    var showEditBag by remember { mutableStateOf(false) }
    var confirmDeleteBag by remember { mutableStateOf(false) }
    var editingItem by remember { mutableStateOf<BagItem?>(null) }
    // Underväska som ett nytt katalogobjekt ska hamna i; null-värdet "ingen dialog" hanteras separat.
    var creatingNew by remember { mutableStateOf(false) }
    var createTargetSubBag by remember { mutableStateOf<Long?>(null) }

    LaunchedEffect(viewModel) {
        viewModel.eventFlow.collect { event ->
            when (event) {
                is PackingEvent.ListReset -> {
                    haptics.softTick()
                    val result = snackbar.showSnackbar(
                        message = context.getString(R.string.snackbar_list_reset),
                        actionLabel = context.getString(R.string.action_undo),
                        duration = SnackbarDuration.Long,
                    )
                    if (result == SnackbarResult.ActionPerformed) viewModel.undoReset(event.previouslyPacked)
                }

                PackingEvent.AllPacked -> {
                    haptics.celebrate()
                    snackbar.showSnackbar(context.getString(R.string.snackbar_all_packed))
                }

                PackingEvent.BagDeleted -> currentOnBack()
            }
        }
    }

    val bag = state.bag
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            TopAppBar(
                scrollBehavior = scrollBehavior,
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.action_back))
                    }
                },
                title = {
                    if (bag != null) {
                        Column {
                            Text(
                                "${bag.emoji} ${bag.name}",
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Text(
                                bagSubtitle(bag),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { showEditBag = true }, enabled = bag != null) {
                        Icon(Icons.Filled.Edit, contentDescription = stringResource(R.string.bag_edit_title))
                    }
                    IconButton(onClick = { menuOpen = true }) {
                        Icon(Icons.Filled.MoreVert, contentDescription = stringResource(R.string.action_more))
                    }
                    DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.action_reset_list)) },
                            leadingIcon = { Icon(Icons.Filled.RestartAlt, contentDescription = null) },
                            onClick = { menuOpen = false; viewModel.resetList() },
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.sub_bags_title)) },
                            leadingIcon = { Icon(Icons.Filled.Backpack, contentDescription = null) },
                            onClick = { menuOpen = false; showSubBags = true },
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.bag_delete)) },
                            leadingIcon = { Icon(Icons.Filled.Delete, contentDescription = null) },
                            onClick = { menuOpen = false; confirmDeleteBag = true },
                        )
                    }
                },
            )
        },
        floatingActionButton = {
            if (bag != null) {
                ExtendedFloatingActionButton(
                    onClick = { showAddSheet = true },
                    icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                    text = { Text(stringResource(R.string.packing_add_items)) },
                )
            }
        },
    ) { padding ->
        val list = state.packingList
        if (state.isLoading || bag == null || list == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                if (state.isLoading) CircularProgressIndicator()
            }
            return@Scaffold
        }

        val profilesById = state.profiles.associateBy { it.id }
        LazyColumn(
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = padding.calculateTopPadding() + 8.dp,
                bottom = padding.calculateBottomPadding() + 96.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            item(key = "header") {
                ProgressHeader(bag = bag, list = list, onReset = viewModel::resetList)
            }
            item(key = "controls") {
                GroupingControls(
                    grouping = state.grouping,
                    onlyUnpacked = state.onlyUnpacked,
                    onGroupingChange = viewModel::setGrouping,
                    onOnlyUnpackedChange = viewModel::setOnlyUnpacked,
                )
            }

            if (list.progress.total == 0) {
                item(key = "empty") {
                    EmptyState(
                        emoji = "🧺",
                        title = stringResource(R.string.packing_empty_title),
                        message = stringResource(R.string.packing_empty_message),
                    )
                }
            } else if (list.groups.isEmpty()) {
                item(key = "all_packed") {
                    EmptyState(
                        emoji = "🎉",
                        title = stringResource(R.string.packing_all_packed_title),
                        message = stringResource(R.string.packing_all_packed_message),
                    )
                }
            }

            list.groups.forEach { group ->
                stickyHeader(key = "group_${group.key.grouping}_${group.key.id}") {
                    GroupHeader(
                        title = group.title ?: fallbackGroupTitle(group.key),
                        emoji = group.emoji ?: fallbackGroupEmoji(group.key),
                        packed = group.progress.packed,
                        total = group.progress.total,
                    )
                }
                items(group.entries, key = { it.item.id }) { entry ->
                    PackingItemRow(
                        entry = entry,
                        profile = entry.item.profileId?.let(profilesById::get),
                        showProfile = state.grouping != Grouping.PROFILE,
                        onToggle = {
                            if (entry.item.isPacked) haptics.softTick() else haptics.tick()
                            viewModel.togglePacked(entry.item)
                        },
                        onEdit = { editingItem = entry.item },
                        modifier = Modifier.animateItem(),
                    )
                }
            }
        }
    }

    if (showAddSheet && bag != null) {
        AddFromCatalogSheet(
            catalog = state.catalog,
            categories = state.categories,
            subBags = state.subBags,
            alreadyInBag = state.bagItems.mapNotNull { it.catalogItemId }.toSet(),
            onAdd = { ids, subBagId ->
                viewModel.addFromCatalog(ids, subBagId)
                showAddSheet = false
                scope.launch {
                    snackbar.showSnackbar(context.resources.getQuantityString(R.plurals.snackbar_added, ids.size, ids.size))
                }
            },
            onCreateNew = { subBagId ->
                createTargetSubBag = subBagId
                showAddSheet = false
                creatingNew = true
            },
            onDismiss = { showAddSheet = false },
        )
    }

    if (creatingNew) {
        ItemEditorDialog(
            title = stringResource(R.string.editor_new_item),
            initial = ItemFields(),
            categories = state.categories,
            profiles = state.profiles,
            onImportImage = viewModel::importImage,
            onCreateCategory = viewModel::createCategory,
            onSave = { fields ->
                val item = CatalogItem(name = fields.name, categoryId = null, profileId = null, emoji = fields.emoji)
                    .withFields(fields)
                viewModel.createCatalogItemAndAdd(item, createTargetSubBag)
                creatingNew = false
            },
            onDismiss = { creatingNew = false },
        )
    }

    editingItem?.let { item ->
        var subBagId by remember(item.id) { mutableStateOf(item.subBagId) }
        ItemEditorDialog(
            title = stringResource(R.string.editor_edit_item),
            initial = item.toFields(),
            categories = state.categories,
            profiles = state.profiles,
            onImportImage = viewModel::importImage,
            onCreateCategory = viewModel::createCategory,
            onSave = { fields ->
                viewModel.updateItem(item.withFields(fields).copy(subBagId = subBagId))
                editingItem = null
            },
            onDelete = {
                viewModel.deleteItem(item)
                editingItem = null
            },
            onDismiss = { editingItem = null },
            extraContent = {
                if (state.subBags.isNotEmpty()) {
                    DropdownSelector(
                        label = stringResource(R.string.editor_sub_bag),
                        options = listOf<SubBag?>(null) + state.subBags,
                        selected = state.subBags.firstOrNull { it.id == subBagId },
                        optionLabel = { it?.let { s -> "${s.emoji}  ${s.name}" } ?: stringResource(R.string.main_bag) },
                        onSelect = { subBagId = it?.id },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            },
        )
    }

    if (showSubBags && bag != null) {
        SubBagsDialog(
            bagId = bag.id,
            subBags = state.subBags,
            onSave = viewModel::saveSubBag,
            onDelete = viewModel::deleteSubBag,
            onDismiss = { showSubBags = false },
        )
    }

    if (showEditBag && bag != null) {
        BagEditorDialog(
            initial = bag,
            onSave = {
                viewModel.updateBag(it)
                showEditBag = false
            },
            onDismiss = { showEditBag = false },
        )
    }

    if (confirmDeleteBag && bag != null) {
        ConfirmDialog(
            title = stringResource(R.string.confirm_delete_bag_title),
            message = stringResource(R.string.confirm_delete_bag_message, bag.name),
            confirmLabel = stringResource(R.string.action_delete),
            onConfirm = viewModel::deleteBag,
            onDismiss = { confirmDeleteBag = false },
        )
    }
}

@Composable
private fun GroupingControls(
    grouping: Grouping,
    onlyUnpacked: Boolean,
    onGroupingChange: (Grouping) -> Unit,
    onOnlyUnpackedChange: (Boolean) -> Unit,
) {
    Column(Modifier.padding(vertical = 8.dp)) {
        Text(
            stringResource(R.string.group_by),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.padding(2.dp))
        SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
            Grouping.entries.forEachIndexed { index, option ->
                SegmentedButton(
                    selected = grouping == option,
                    onClick = { onGroupingChange(option) },
                    shape = SegmentedButtonDefaults.itemShape(index, Grouping.entries.size),
                ) {
                    Text(
                        when (option) {
                            Grouping.CATEGORY -> stringResource(R.string.group_by_category)
                            Grouping.SUB_BAG -> stringResource(R.string.group_by_sub_bag)
                            Grouping.PROFILE -> stringResource(R.string.group_by_profile)
                        },
                        maxLines = 1,
                    )
                }
            }
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .padding(top = 4.dp),
        ) {
            FilterChip(
                selected = !onlyUnpacked,
                onClick = { onOnlyUnpackedChange(false) },
                label = { Text(stringResource(R.string.filter_show_all)) },
            )
            FilterChip(
                selected = onlyUnpacked,
                onClick = { onOnlyUnpackedChange(true) },
                leadingIcon = { Icon(Icons.Filled.FilterList, contentDescription = null) },
                label = { Text(stringResource(R.string.filter_only_unpacked)) },
            )
        }
    }
}

@Composable
private fun GroupHeader(title: String, emoji: String, packed: Int, total: Int) {
    Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(top = 12.dp, bottom = 6.dp, start = 4.dp),
        ) {
            Text(emoji, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.width(8.dp))
            Text(
                title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f),
            )
            Text(
                stringResource(R.string.group_progress, packed, total),
                style = MaterialTheme.typography.labelLarge,
                color = if (packed == total) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun fallbackGroupTitle(key: GroupKey): String = when (key.grouping) {
    Grouping.CATEGORY -> stringResource(R.string.none_category)
    Grouping.SUB_BAG -> stringResource(R.string.main_bag)
    Grouping.PROFILE -> stringResource(R.string.none_profile)
}

private fun fallbackGroupEmoji(key: GroupKey): String = when (key.grouping) {
    Grouping.CATEGORY -> "📦"
    Grouping.SUB_BAG -> "🧳"
    Grouping.PROFILE -> "👥"
}

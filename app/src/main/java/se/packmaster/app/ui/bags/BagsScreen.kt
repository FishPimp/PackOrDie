package se.packmaster.app.ui.bags

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocalLaundryService
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import se.packmaster.app.R
import se.packmaster.app.ui.components.ConfirmDialog
import se.packmaster.app.ui.components.EmptyState
import se.packmaster.app.ui.components.ItemAvatar
import se.packmaster.app.ui.components.daysLabel
import se.packmaster.app.ui.components.formatEpochDay
import se.packmaster.app.ui.components.formatWeight
import se.packmaster.domain.model.Bag
import se.packmaster.domain.model.BagType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BagsScreen(
    onOpenBag: (Long) -> Unit,
    viewModel: BagsViewModel = viewModel(factory = BagsViewModel.Factory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showCreate by remember { mutableStateOf(false) }
    var bagToDelete by remember { mutableStateOf<Bag?>(null) }
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            LargeTopAppBar(
                title = {
                    Column {
                        Text(stringResource(R.string.app_name))
                        if (scrollBehavior.state.collapsedFraction < 0.5f) {
                            Text(
                                stringResource(R.string.bags_subtitle),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                },
                scrollBehavior = scrollBehavior,
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showCreate = true },
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text(stringResource(R.string.bags_new)) },
            )
        },
    ) { padding ->
        when {
            state.isLoading -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }

            state.everyday.isEmpty() && state.trips.isEmpty() -> EmptyState(
                emoji = "🧳",
                title = stringResource(R.string.bags_empty_title),
                message = stringResource(R.string.bags_empty_message),
                modifier = Modifier.padding(padding),
            )

            else -> LazyColumn(
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = padding.calculateTopPadding() + 8.dp,
                    bottom = 96.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                if (state.everyday.isNotEmpty()) {
                    item(key = "header_everyday") {
                        SectionHeader(
                            title = stringResource(R.string.bags_section_everyday),
                            subtitle = stringResource(R.string.bags_section_everyday_hint),
                        )
                    }
                    items(state.everyday, key = { it.bag.id }) { card ->
                        BagCardView(card, onClick = { onOpenBag(card.bag.id) }, onDelete = { bagToDelete = card.bag })
                    }
                }
                if (state.trips.isNotEmpty()) {
                    item(key = "header_trips") {
                        SectionHeader(
                            title = stringResource(R.string.bags_section_trips),
                            subtitle = stringResource(R.string.bags_section_trips_hint),
                        )
                    }
                    items(state.trips, key = { it.bag.id }) { card ->
                        BagCardView(card, onClick = { onOpenBag(card.bag.id) }, onDelete = { bagToDelete = card.bag })
                    }
                }
            }
        }
    }

    if (showCreate) {
        BagEditorDialog(
            initial = null,
            onSave = { bag ->
                showCreate = false
                viewModel.createBag(bag, onCreated = onOpenBag)
            },
            onDismiss = { showCreate = false },
        )
    }

    bagToDelete?.let { bag ->
        ConfirmDialog(
            title = stringResource(R.string.confirm_delete_bag_title),
            message = stringResource(R.string.confirm_delete_bag_message, bag.name),
            confirmLabel = stringResource(R.string.action_delete),
            onConfirm = { viewModel.deleteBag(bag.id) },
            onDismiss = { bagToDelete = null },
        )
    }
}

@Composable
private fun SectionHeader(title: String, subtitle: String) {
    Column(Modifier.padding(top = 8.dp, start = 4.dp)) {
        Text(title, style = MaterialTheme.typography.titleLarge)
        Text(
            subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun BagCardView(card: BagCard, onClick: () -> Unit, onDelete: () -> Unit) {
    val bag = card.bag
    val progress = card.progress
    var menuOpen by remember { mutableStateOf(false) }
    val complete = progress.isComplete

    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (complete) {
                MaterialTheme.colorScheme.secondaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceContainerLow
            },
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .combinedClickable(onClick = onClick, onLongClick = { menuOpen = true }),
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                ItemAvatar(
                    emoji = bag.emoji,
                    size = 52.dp,
                    containerColor = if (bag.type == BagType.TRIP) {
                        MaterialTheme.colorScheme.tertiaryContainer
                    } else {
                        MaterialTheme.colorScheme.primaryContainer
                    },
                )
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        bag.name,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        bagSubtitle(bag),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Text(
                    if (complete) "🎉" else stringResource(R.string.percent, progress.percent),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
                DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.action_delete)) },
                        leadingIcon = { Icon(Icons.Filled.Delete, contentDescription = null) },
                        onClick = {
                            menuOpen = false
                            onDelete()
                        },
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = { progress.fraction },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(CircleShape),
                trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                drawStopIndicator = {},
            )
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    stringResource(R.string.progress_items_packed, progress.packed, progress.total),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                )
                if (bag.hasLaundry) {
                    SmallChip(
                        label = stringResource(R.string.chip_laundry),
                        icon = { Icon(Icons.Filled.LocalLaundryService, contentDescription = null) },
                    )
                }
                bag.maxWeightGrams?.let {
                    Spacer(Modifier.width(6.dp))
                    SmallChip(
                        label = stringResource(R.string.chip_max_weight, formatWeight(it)),
                        icon = { Icon(Icons.Filled.Scale, contentDescription = null) },
                    )
                }
            }
        }
    }
}

@Composable
private fun SmallChip(label: String, icon: @Composable () -> Unit) {
    AssistChip(
        onClick = {},
        label = { Text(label, style = MaterialTheme.typography.labelSmall) },
        leadingIcon = icon,
        modifier = Modifier.height(28.dp),
        colors = AssistChipDefaults.assistChipColors(leadingIconContentColor = MaterialTheme.colorScheme.primary),
    )
}

@Composable
fun bagSubtitle(bag: Bag): String {
    if (bag.type == BagType.EVERYDAY) return stringResource(R.string.bag_type_everyday)
    val start = bag.startEpochDay
    val end = bag.endEpochDay
    return if (start != null && end != null) {
        stringResource(R.string.bag_date_range, formatEpochDay(start), formatEpochDay(end), daysLabel(bag.tripDays))
    } else {
        stringResource(R.string.bag_trip_days, daysLabel(bag.tripDays))
    }
}

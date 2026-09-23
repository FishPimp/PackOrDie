package se.packmaster.app.ui.packing

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocalLaundryService
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import se.packmaster.app.R
import se.packmaster.app.ui.components.ItemAvatar
import se.packmaster.app.ui.components.daysLabel
import se.packmaster.app.ui.components.formatWeight
import se.packmaster.app.ui.components.ruleDescription
import se.packmaster.domain.model.Bag
import se.packmaster.domain.model.BagType
import se.packmaster.domain.model.PackingEntry
import se.packmaster.domain.model.PackingList
import se.packmaster.domain.model.Profile
import se.packmaster.domain.usecase.QuantityCalculator

/** Sammanfattningskort överst: framsteg, resinfo och viktbudget. */
@Composable
fun ProgressHeader(
    bag: Bag,
    list: PackingList,
    onReset: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val progress = list.progress
    val animated by animateFloatAsState(
        targetValue = progress.fraction,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 200f),
        label = "progress",
    )
    val container by animateColorAsState(
        if (progress.isComplete) MaterialTheme.colorScheme.secondaryContainer
        else MaterialTheme.colorScheme.primaryContainer,
        label = "container",
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = container),
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    stringResource(R.string.percent, progress.percent),
                    style = MaterialTheme.typography.displaySmall,
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    stringResource(R.string.progress_items_packed, progress.packed, progress.total),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 6.dp),
                )
            }
            LinearProgressIndicator(
                progress = { animated },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(14.dp)
                    .clip(CircleShape),
                color = if (progress.isComplete) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceContainerLowest.copy(alpha = 0.6f),
                drawStopIndicator = {},
            )
            Text(
                progressMessage(progress.percent, progress.total),
                style = MaterialTheme.typography.bodyMedium,
            )

            if (bag.type == BagType.TRIP) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        stringResource(R.string.packing_trip_info, daysLabel(list.tripDays)),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.weight(1f),
                    )
                    if (bag.hasLaundry) {
                        Icon(
                            Icons.Filled.LocalLaundryService,
                            contentDescription = null,
                            modifier = Modifier.padding(end = 4.dp),
                        )
                        Text(stringResource(R.string.chip_laundry), style = MaterialTheme.typography.bodySmall)
                    }
                }
                if (list.laundryCapApplied) {
                    Text(
                        stringResource(R.string.packing_laundry_cap, QuantityCalculator.LAUNDRY_MAX_DAYS),
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }

            WeightSection(list)

            if (bag.type == BagType.EVERYDAY) {
                FilledTonalButton(onClick = onReset, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Filled.RestartAlt, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.action_reset_list))
                }
            }
        }
    }
}

@Composable
private fun progressMessage(percent: Int, total: Int): String = when {
    total == 0 -> stringResource(R.string.progress_msg_empty)
    percent >= 100 -> stringResource(R.string.progress_msg_done)
    percent >= 75 -> stringResource(R.string.progress_msg_almost)
    percent >= 40 -> stringResource(R.string.progress_msg_half)
    percent > 0 -> stringResource(R.string.progress_msg_started)
    else -> stringResource(R.string.progress_msg_start)
}

@Composable
private fun WeightSection(list: PackingList) {
    val weight = list.weight
    val maxGrams = weight.maxGrams
    if (maxGrams == null && weight.totalGrams == 0) return
    val over = weight.isOverBudget
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Scale, contentDescription = null, modifier = Modifier.padding(end = 6.dp))
            Text(
                if (maxGrams != null) {
                    stringResource(R.string.weight_of_max, formatWeight(weight.totalGrams), formatWeight(maxGrams))
                } else {
                    stringResource(R.string.weight_total, formatWeight(weight.totalGrams))
                },
                style = MaterialTheme.typography.titleSmall,
                color = if (over) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
            )
            if (over) {
                Text(
                    stringResource(
                        R.string.weight_over_by,
                        formatWeight(weight.totalGrams - (maxGrams ?: 0)),
                    ),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }
        if (maxGrams != null) {
            LinearProgressIndicator(
                progress = { weight.fraction.coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape),
                color = if (over) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.tertiary,
                trackColor = MaterialTheme.colorScheme.surfaceContainerLowest.copy(alpha = 0.6f),
                drawStopIndicator = {},
            )
        }
        // Vikt per underväska
        weight.perSubBag.filter { it.subBag != null && (it.weightGrams > 0 || it.subBag?.maxWeightGrams != null) }
            .forEach { sub ->
                val subBag = sub.subBag ?: return@forEach
                val subMax = subBag.maxWeightGrams
                Text(
                    if (subMax != null) {
                        stringResource(
                            R.string.weight_sub_bag_of_max,
                            subBag.emoji,
                            subBag.name,
                            formatWeight(sub.weightGrams),
                            formatWeight(subMax),
                        )
                    } else {
                        stringResource(R.string.weight_sub_bag, subBag.emoji, subBag.name, formatWeight(sub.weightGrams))
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = if (sub.isOverBudget) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
                )
            }
    }
}

/** En rad i checklistan. */
@Composable
fun PackingItemRow(
    entry: PackingEntry,
    profile: Profile?,
    showProfile: Boolean,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val item = entry.item
    val packed = item.isPacked
    val background by animateColorAsState(
        if (packed) MaterialTheme.colorScheme.surfaceContainer else MaterialTheme.colorScheme.surfaceContainerLowest,
        label = "rowBackground",
    )
    Surface(
        color = background,
        shape = MaterialTheme.shapes.small,
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.small)
            .clickable(onClick = onToggle),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 4.dp, end = 4.dp, top = 6.dp, bottom = 6.dp),
        ) {
            Checkbox(checked = packed, onCheckedChange = { onToggle() })
            ItemAvatar(
                emoji = item.emoji,
                imagePath = item.imagePath,
                size = 40.dp,
                modifier = Modifier.alpha(if (packed) 0.5f else 1f),
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    item.name,
                    style = MaterialTheme.typography.bodyLarge,
                    textDecoration = if (packed) TextDecoration.LineThrough else null,
                    color = if (packed) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                val details = buildList {
                    add(ruleDescription(item.rule, item.amount))
                    if (showProfile && profile != null) add("${profile.emoji} ${profile.name}")
                    if (entry.totalWeightGrams > 0) add(formatWeight(entry.totalWeightGrams))
                }
                Text(
                    details.joinToString(" · "),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Surface(
                shape = CircleShape,
                color = if (packed) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.tertiaryContainer,
            ) {
                Text(
                    stringResource(R.string.quantity_badge, entry.quantity),
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                )
            }
            IconButton(onClick = onEdit) {
                Icon(Icons.Filled.Edit, contentDescription = stringResource(R.string.action_edit))
            }
        }
    }
}

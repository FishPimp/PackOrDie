package se.packmaster.domain.usecase

import se.packmaster.domain.model.Bag
import se.packmaster.domain.model.BagItem
import se.packmaster.domain.model.Category
import se.packmaster.domain.model.GroupKey
import se.packmaster.domain.model.Grouping
import se.packmaster.domain.model.PackingEntry
import se.packmaster.domain.model.PackingGroup
import se.packmaster.domain.model.PackingList
import se.packmaster.domain.model.PackingProgress
import se.packmaster.domain.model.Profile
import se.packmaster.domain.model.QuantityRule
import se.packmaster.domain.model.SubBag
import se.packmaster.domain.model.SubBagWeight
import se.packmaster.domain.model.WeightSummary

/**
 * Bygger den grupperade checklistan för en väska, inklusive framsteg och vikt.
 * Framsteg och vikt räknas alltid på hela listan, oberoende av filtret.
 */
class BuildPackingListUseCase {

    operator fun invoke(
        bag: Bag,
        items: List<BagItem>,
        subBags: List<SubBag>,
        categories: List<Category>,
        profiles: List<Profile>,
        grouping: Grouping,
        onlyUnpacked: Boolean,
    ): PackingList {
        val tripDays = bag.tripDays
        val entries = items.map { item ->
            val quantity = QuantityCalculator.quantity(
                rule = item.rule,
                amount = item.amount,
                tripDays = tripDays,
                hasLaundry = bag.hasLaundry,
                laundryAffected = item.laundryAffected,
            )
            PackingEntry(item, quantity, quantity * item.weightGrams)
        }

        val categoryById = categories.associateBy { it.id }
        val profileById = profiles.associateBy { it.id }
        val subBagById = subBags.associateBy { it.id }

        val groups = entries
            .groupBy { entry ->
                val id = when (grouping) {
                    Grouping.CATEGORY -> entry.item.categoryId?.takeIf { it in categoryById }
                    Grouping.SUB_BAG -> entry.item.subBagId?.takeIf { it in subBagById }
                    Grouping.PROFILE -> entry.item.profileId?.takeIf { it in profileById }
                }
                GroupKey(grouping, id)
            }
            .map { (key, groupEntries) ->
                val (title, emoji) = when (grouping) {
                    Grouping.CATEGORY -> categoryById[key.id]?.let { it.name to it.emoji }
                    Grouping.SUB_BAG -> subBagById[key.id]?.let { it.name to it.emoji }
                    Grouping.PROFILE -> profileById[key.id]?.let { it.name to it.emoji }
                } ?: (null to null)
                val visible = groupEntries
                    .filter { !onlyUnpacked || !it.item.isPacked }
                    .sortedWith(compareBy<PackingEntry> { it.item.isPacked }.thenBy { it.item.name.lowercase() })
                PackingGroup(
                    key = key,
                    title = title,
                    emoji = emoji,
                    entries = visible,
                    progress = PackingProgress(
                        packed = groupEntries.count { it.item.isPacked },
                        total = groupEntries.size,
                    ),
                )
            }
            .filter { it.entries.isNotEmpty() }
            .sortedWith(groupOrder(grouping, categories, subBags, profiles))

        val weight = WeightSummary(
            totalGrams = entries.sumOf { it.totalWeightGrams },
            packedGrams = entries.filter { it.item.isPacked }.sumOf { it.totalWeightGrams },
            maxGrams = bag.maxWeightGrams,
            perSubBag = buildList {
                val bySubBag = entries.groupBy { it.item.subBagId?.takeIf { id -> id in subBagById } }
                bySubBag[null]?.let { add(SubBagWeight(null, it.sumOf { e -> e.totalWeightGrams })) }
                subBags.forEach { subBag ->
                    add(SubBagWeight(subBag, bySubBag[subBag.id].orEmpty().sumOf { it.totalWeightGrams }))
                }
            },
        )

        return PackingList(
            groups = groups,
            progress = PackingProgress(entries.count { it.item.isPacked }, entries.size),
            weight = weight,
            tripDays = tripDays,
            laundryCapApplied = QuantityCalculator.isLaundryCapActive(tripDays, bag.hasLaundry) &&
                items.any { it.laundryAffected && it.rule != QuantityRule.FIXED },
        )
    }

    /** Grupper visas i samma ordning som källistan; grupper utan tillhörighet hamnar sist. */
    private fun groupOrder(
        grouping: Grouping,
        categories: List<Category>,
        subBags: List<SubBag>,
        profiles: List<Profile>,
    ): Comparator<PackingGroup> {
        val order: Map<Long, Int> = when (grouping) {
            Grouping.CATEGORY -> categories.map { it.id }
            Grouping.SUB_BAG -> subBags.map { it.id }
            Grouping.PROFILE -> profiles.map { it.id }
        }.withIndex().associate { (index, id) -> id to index }
        return compareBy { group ->
            // Utan underväska = huvudväskan, som visas först. Övriga "saknas"-grupper visas sist.
            when (val id = group.key.id) {
                null -> if (grouping == Grouping.SUB_BAG) -1 else Int.MAX_VALUE
                else -> order[id] ?: Int.MAX_VALUE - 1
            }
        }
    }
}

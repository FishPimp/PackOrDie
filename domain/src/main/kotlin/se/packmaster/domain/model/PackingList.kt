package se.packmaster.domain.model

/** Hur checklistan grupperas. */
enum class Grouping { CATEGORY, SUB_BAG, PROFILE }

/**
 * Nyckel för en grupp i checklistan. [id] är null för objekt som saknar
 * kategori, underväska eller person – UI:t väljer då en passande rubrik.
 */
data class GroupKey(val grouping: Grouping, val id: Long?)

data class PackingEntry(
    val item: BagItem,
    val quantity: Int,
    val totalWeightGrams: Int,
)

data class PackingGroup(
    val key: GroupKey,
    val title: String?,
    val emoji: String?,
    val entries: List<PackingEntry>,
    val progress: PackingProgress,
)

data class SubBagWeight(
    val subBag: SubBag?,
    val weightGrams: Int,
) {
    val isOverBudget: Boolean
        get() = subBag?.maxWeightGrams?.let { weightGrams > it } ?: false
}

data class WeightSummary(
    val totalGrams: Int,
    val packedGrams: Int,
    val maxGrams: Int?,
    val perSubBag: List<SubBagWeight>,
) {
    val isOverBudget: Boolean get() = maxGrams != null && totalGrams > maxGrams
    val fraction: Float
        get() = if (maxGrams == null || maxGrams == 0) 0f else totalGrams.toFloat() / maxGrams
}

data class PackingList(
    val groups: List<PackingGroup>,
    val progress: PackingProgress,
    val weight: WeightSummary,
    val tripDays: Int,
    val laundryCapApplied: Boolean,
)

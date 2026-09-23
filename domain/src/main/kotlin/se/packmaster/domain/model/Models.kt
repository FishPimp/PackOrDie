package se.packmaster.domain.model

/** Typ av väska: återkommande vardagsväska eller en resa med tidsberäkning. */
enum class BagType { EVERYDAY, TRIP }

/** Hur antalet av ett objekt räknas fram. */
enum class QuantityRule {
    /** Ett fast antal oavsett resans längd. */
    FIXED,

    /** Antal per dag, t.ex. 4 blöjor per dag. */
    PER_DAY,

    /** Antal per natt, t.ex. 1 pyjamas per natt. */
    PER_NIGHT,
}

data class Category(
    val id: Long = 0,
    val name: String,
    val emoji: String,
    val isCustom: Boolean = true,
)

data class Profile(
    val id: Long = 0,
    val name: String,
    val emoji: String,
)

data class CatalogItem(
    val id: Long = 0,
    val name: String,
    val categoryId: Long?,
    val profileId: Long?,
    val emoji: String,
    val imagePath: String? = null,
    val rule: QuantityRule = QuantityRule.FIXED,
    val amount: Double = 1.0,
    val weightGrams: Int = 0,
    /** Om antalet ska begränsas när tvättmöjlighet finns (gäller främst dagliga kläder). */
    val laundryAffected: Boolean = false,
    val isCustom: Boolean = true,
)

data class Bag(
    val id: Long = 0,
    val name: String,
    val emoji: String,
    val type: BagType,
    val startEpochDay: Long? = null,
    val endEpochDay: Long? = null,
    /** Antal dagar som anges manuellt när inga datum är valda. */
    val manualDays: Int = 1,
    val hasLaundry: Boolean = false,
    val maxWeightGrams: Int? = null,
    val createdAt: Long = 0,
) {
    /** Resans längd i dagar. Vardagsväskor räknas alltid som en dag. */
    val tripDays: Int
        get() = when {
            type == BagType.EVERYDAY -> 1
            startEpochDay != null && endEpochDay != null ->
                (endEpochDay - startEpochDay + 1).toInt().coerceAtLeast(1)
            else -> manualDays.coerceAtLeast(1)
        }
}

/** Underväska / förvaring inom en väska, t.ex. Hygienväska eller Barnens ryggsäck. */
data class SubBag(
    val id: Long = 0,
    val bagId: Long,
    val name: String,
    val emoji: String,
    val maxWeightGrams: Int? = null,
)

/** Ett objekt i en specifik väskas packlista. */
data class BagItem(
    val id: Long = 0,
    val bagId: Long,
    val catalogItemId: Long?,
    val name: String,
    val categoryId: Long?,
    val profileId: Long?,
    val subBagId: Long?,
    val emoji: String,
    val imagePath: String?,
    val rule: QuantityRule,
    val amount: Double,
    val weightGrams: Int,
    val laundryAffected: Boolean,
    val isPacked: Boolean = false,
)

data class PackingProgress(val packed: Int, val total: Int) {
    val fraction: Float get() = if (total == 0) 0f else packed.toFloat() / total
    val percent: Int get() = (fraction * 100).toInt()
    val isComplete: Boolean get() = total > 0 && packed == total

    companion object {
        val EMPTY = PackingProgress(0, 0)
    }
}

fun CatalogItem.toBagItem(bagId: Long, subBagId: Long? = null): BagItem = BagItem(
    bagId = bagId,
    catalogItemId = id,
    name = name,
    categoryId = categoryId,
    profileId = profileId,
    subBagId = subBagId,
    emoji = emoji,
    imagePath = imagePath,
    rule = rule,
    amount = amount,
    weightGrams = weightGrams,
    laundryAffected = laundryAffected,
)

package se.packmaster.domain.usecase

import se.packmaster.domain.model.QuantityRule
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min

/**
 * Räknar fram hur många av ett objekt som ska packas utifrån objektets regel,
 * resans längd och om det finns tvättmöjlighet.
 */
object QuantityCalculator {

    /** Med tvättmaskin behöver man som mest packa dagliga kläder för en vecka. */
    const val LAUNDRY_MAX_DAYS = 7

    fun effectiveDays(tripDays: Int, hasLaundry: Boolean, laundryAffected: Boolean): Int {
        val days = max(tripDays, 1)
        return if (hasLaundry && laundryAffected) min(days, LAUNDRY_MAX_DAYS) else days
    }

    fun quantity(
        rule: QuantityRule,
        amount: Double,
        tripDays: Int,
        hasLaundry: Boolean = false,
        laundryAffected: Boolean = false,
    ): Int {
        val safeAmount = max(amount, 0.0)
        val days = effectiveDays(tripDays, hasLaundry, laundryAffected)
        val raw = when (rule) {
            QuantityRule.FIXED -> safeAmount
            QuantityRule.PER_DAY -> safeAmount * days
            // En resa på en dag har ingen natt, men minst en natt räknas för flerdagsresor.
            QuantityRule.PER_NIGHT -> safeAmount * max(days - 1, if (tripDays > 1) 1 else 0)
        }
        // Liten tolerans så att t.ex. 0.1 * 30 inte blir 4 p.g.a. flyttalsfel.
        return max(ceil(raw - 1e-9).toInt(), 0)
    }

    /** True om tvättbegränsningen faktiskt påverkar resan. */
    fun isLaundryCapActive(tripDays: Int, hasLaundry: Boolean): Boolean =
        hasLaundry && tripDays > LAUNDRY_MAX_DAYS
}

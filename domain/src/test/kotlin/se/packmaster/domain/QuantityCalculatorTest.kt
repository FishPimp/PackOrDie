package se.packmaster.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import se.packmaster.domain.model.QuantityRule
import se.packmaster.domain.usecase.QuantityCalculator

class QuantityCalculatorTest {

    @Test
    fun `fast antal påverkas inte av resans längd`() {
        assertEquals(2, QuantityCalculator.quantity(QuantityRule.FIXED, 2.0, tripDays = 14))
    }

    @Test
    fun `per dag multipliceras med antal dagar`() {
        assertEquals(7, QuantityCalculator.quantity(QuantityRule.PER_DAY, 1.0, tripDays = 7))
        assertEquals(28, QuantityCalculator.quantity(QuantityRule.PER_DAY, 4.0, tripDays = 7))
    }

    @Test
    fun `per dag avrundas uppåt`() {
        assertEquals(2, QuantityCalculator.quantity(QuantityRule.PER_DAY, 0.5, tripDays = 3))
        assertEquals(3, QuantityCalculator.quantity(QuantityRule.PER_DAY, 0.1, tripDays = 30))
    }

    @Test
    fun `tvättmöjlighet begränsar dagliga kläder till sju dagar`() {
        assertEquals(
            7,
            QuantityCalculator.quantity(QuantityRule.PER_DAY, 1.0, 14, hasLaundry = true, laundryAffected = true),
        )
    }

    @Test
    fun `tvättmöjlighet påverkar inte objekt som inte är tvättbara`() {
        assertEquals(
            56,
            QuantityCalculator.quantity(QuantityRule.PER_DAY, 4.0, 14, hasLaundry = true, laundryAffected = false),
        )
    }

    @Test
    fun `per natt räknar nätter`() {
        assertEquals(0, QuantityCalculator.quantity(QuantityRule.PER_NIGHT, 1.0, tripDays = 1))
        assertEquals(1, QuantityCalculator.quantity(QuantityRule.PER_NIGHT, 1.0, tripDays = 2))
        assertEquals(6, QuantityCalculator.quantity(QuantityRule.PER_NIGHT, 1.0, tripDays = 7))
    }

    @Test
    fun `tvättbegränsning aktiv endast för långa resor`() {
        assertFalse(QuantityCalculator.isLaundryCapActive(7, hasLaundry = true))
        assertTrue(QuantityCalculator.isLaundryCapActive(8, hasLaundry = true))
        assertFalse(QuantityCalculator.isLaundryCapActive(20, hasLaundry = false))
    }
}

package se.packmaster.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import se.packmaster.domain.model.Bag
import se.packmaster.domain.model.BagItem
import se.packmaster.domain.model.BagType
import se.packmaster.domain.model.Category
import se.packmaster.domain.model.Grouping
import se.packmaster.domain.model.Profile
import se.packmaster.domain.model.QuantityRule
import se.packmaster.domain.model.SubBag
import se.packmaster.domain.usecase.BuildPackingListUseCase

class BuildPackingListUseCaseTest {

    private val useCase = BuildPackingListUseCase()
    private val clothes = Category(1, "Kläder", "👕")
    private val hygiene = Category(2, "Hygienartiklar", "🧴")
    private val adult = Profile(1, "Vuxen 1", "🧑")
    private val kid = Profile(2, "Barn 1", "🧒")
    private val toiletry = SubBag(1, bagId = 1, name = "Hygienväska", emoji = "🧼", maxWeightGrams = 100)

    private fun item(
        id: Long,
        name: String,
        category: Long?,
        profile: Long? = null,
        subBag: Long? = null,
        rule: QuantityRule = QuantityRule.FIXED,
        amount: Double = 1.0,
        weight: Int = 100,
        laundry: Boolean = false,
        packed: Boolean = false,
    ) = BagItem(id, 1, null, name, category, profile, subBag, "•", null, rule, amount, weight, laundry, packed)

    private val trip = Bag(1, "Semester", "✈️", BagType.TRIP, startEpochDay = 100, endEpochDay = 109, maxWeightGrams = 2000)

    private val items = listOf(
        item(1, "T-shirt", 1, profile = 1, rule = QuantityRule.PER_DAY, weight = 150, laundry = true, packed = true),
        item(2, "Tandborste", 2, profile = 2, subBag = 1, weight = 20),
        item(3, "Schampo", 2, subBag = 1, weight = 300, packed = true),
        item(4, "Okänd pryl", null),
    )

    private fun build(grouping: Grouping, onlyUnpacked: Boolean = false, bag: Bag = trip) =
        useCase(bag, items, listOf(toiletry), listOf(clothes, hygiene), listOf(adult, kid), grouping, onlyUnpacked)

    @Test
    fun `resans längd räknas från datum`() {
        assertEquals(10, trip.tripDays)
        val list = build(Grouping.CATEGORY)
        assertEquals(10, list.groups.first().entries.first { it.item.name == "T-shirt" }.quantity)
    }

    @Test
    fun `framsteg räknas på alla objekt även när filtret är på`() {
        val list = build(Grouping.CATEGORY, onlyUnpacked = true)
        assertEquals(2, list.progress.packed)
        assertEquals(4, list.progress.total)
        assertEquals(50, list.progress.percent)
        assertTrue(list.groups.flatMap { it.entries }.none { it.item.isPacked })
    }

    @Test
    fun `gruppering per kategori följer kategoriordning och okänt hamnar sist`() {
        val list = build(Grouping.CATEGORY)
        assertEquals(listOf(1L, 2L, null), list.groups.map { it.key.id })
        assertNull(list.groups.last().title)
    }

    @Test
    fun `gruppering per underväska visar huvudväskan först`() {
        val list = build(Grouping.SUB_BAG)
        assertEquals(listOf(null, 1L), list.groups.map { it.key.id })
    }

    @Test
    fun `gruppering per person`() {
        val list = build(Grouping.PROFILE)
        assertEquals(listOf(1L, 2L, null), list.groups.map { it.key.id })
    }

    @Test
    fun `vikt summeras och budget kontrolleras`() {
        val list = build(Grouping.CATEGORY)
        // 10 t-shirts * 150 + 20 + 300 + 100
        assertEquals(1920, list.weight.totalGrams)
        assertTrue(!list.weight.isOverBudget)
        val hygieneBag = list.weight.perSubBag.first { it.subBag?.id == 1L }
        assertEquals(320, hygieneBag.weightGrams)
        assertTrue(hygieneBag.isOverBudget)
    }

    @Test
    fun `tvättmaskin begränsar kläder på långa resor`() {
        val list = build(Grouping.CATEGORY, bag = trip.copy(hasLaundry = true))
        assertTrue(list.laundryCapApplied)
        assertEquals(7, list.groups.first().entries.first { it.item.name == "T-shirt" }.quantity)
    }

    @Test
    fun `vardagsväska räknas som en dag`() {
        val everyday = trip.copy(type = BagType.EVERYDAY)
        assertEquals(1, everyday.tripDays)
        val list = build(Grouping.CATEGORY, bag = everyday)
        assertEquals(1, list.groups.first().entries.first { it.item.name == "T-shirt" }.quantity)
    }
}

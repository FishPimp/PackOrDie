package se.packmaster.app.data.seed

import android.content.Context
import androidx.annotation.StringRes
import androidx.room.withTransaction
import se.packmaster.app.R
import se.packmaster.app.data.local.BagEntity
import se.packmaster.app.data.local.BagItemEntity
import se.packmaster.app.data.local.CatalogItemEntity
import se.packmaster.app.data.local.CategoryEntity
import se.packmaster.app.data.local.PackMasterDatabase
import se.packmaster.app.data.local.ProfileEntity
import se.packmaster.app.data.local.SubBagEntity
import se.packmaster.app.data.prefs.SettingsStore
import se.packmaster.domain.model.BagType
import se.packmaster.domain.model.QuantityRule
import se.packmaster.domain.model.QuantityRule.FIXED
import se.packmaster.domain.model.QuantityRule.PER_DAY
import se.packmaster.domain.model.QuantityRule.PER_NIGHT

/**
 * Fyller databasen med förvalda svenska kategorier, profiler, katalogobjekt
 * och några exempelväskor första gången appen startas. Alla namn hämtas från
 * strings.xml.
 */
class DatabaseSeeder(
    private val context: Context,
    private val db: PackMasterDatabase,
    private val settings: SettingsStore,
) {

    private enum class Cat(@StringRes val nameRes: Int, val emoji: String) {
        CLOTHES(R.string.seed_category_clothes, "👕"),
        HYGIENE(R.string.seed_category_hygiene, "🧴"),
        ELECTRONICS(R.string.seed_category_electronics, "🔌"),
        BABY(R.string.seed_category_baby, "🍼"),
        DOCUMENTS(R.string.seed_category_documents, "📄"),
        TRAINING(R.string.seed_category_training, "🏋️"),
        SWIM(R.string.seed_category_swim, "🏖️"),
        FOOD(R.string.seed_category_food, "🍎"),
        MEDICINE(R.string.seed_category_medicine, "💊"),
        OTHER(R.string.seed_category_other, "🎒"),
    }

    private enum class Who(@StringRes val nameRes: Int, val emoji: String) {
        ADULT(R.string.seed_profile_adult, "🧑"),
        CHILD(R.string.seed_profile_child, "🧒"),
        SHARED(R.string.seed_profile_shared, "👨‍👩‍👧"),
    }

    private enum class Item(
        @StringRes val nameRes: Int,
        val emoji: String,
        val cat: Cat,
        val who: Who?,
        val rule: QuantityRule = FIXED,
        val amount: Double = 1.0,
        val grams: Int = 0,
        val laundry: Boolean = false,
    ) {
        TSHIRT(R.string.seed_item_tshirt, "👕", Cat.CLOTHES, Who.ADULT, PER_DAY, 1.0, 150, true),
        UNDERWEAR(R.string.seed_item_underwear, "🩲", Cat.CLOTHES, Who.ADULT, PER_DAY, 1.0, 60, true),
        SOCKS(R.string.seed_item_socks, "🧦", Cat.CLOTHES, Who.ADULT, PER_DAY, 1.0, 50, true),
        TROUSERS(R.string.seed_item_trousers, "👖", Cat.CLOTHES, Who.ADULT, FIXED, 2.0, 500),
        SWEATER(R.string.seed_item_sweater, "🧥", Cat.CLOTHES, Who.ADULT, FIXED, 1.0, 400),
        JACKET(R.string.seed_item_jacket, "🧥", Cat.CLOTHES, Who.ADULT, FIXED, 1.0, 900),
        PYJAMAS(R.string.seed_item_pyjamas, "🛌", Cat.CLOTHES, Who.ADULT, FIXED, 1.0, 300),
        SHOES(R.string.seed_item_shoes, "👟", Cat.CLOTHES, Who.ADULT, FIXED, 1.0, 800),
        KID_CLOTHES(R.string.seed_item_kid_clothes, "👚", Cat.CLOTHES, Who.CHILD, PER_DAY, 1.0, 200, true),
        KID_PYJAMAS(R.string.seed_item_kid_pyjamas, "🌙", Cat.CLOTHES, Who.CHILD, PER_NIGHT, 0.5, 150, true),

        TOOTHBRUSH(R.string.seed_item_toothbrush, "🪥", Cat.HYGIENE, Who.ADULT, FIXED, 1.0, 20),
        TOOTHPASTE(R.string.seed_item_toothpaste, "🦷", Cat.HYGIENE, Who.SHARED, FIXED, 1.0, 100),
        SHAMPOO(R.string.seed_item_shampoo, "🧴", Cat.HYGIENE, Who.SHARED, FIXED, 1.0, 300),
        DEODORANT(R.string.seed_item_deodorant, "🌸", Cat.HYGIENE, Who.ADULT, FIXED, 1.0, 100),
        HAIRBRUSH(R.string.seed_item_hairbrush, "💇", Cat.HYGIENE, Who.SHARED, FIXED, 1.0, 80),
        RAZOR(R.string.seed_item_razor, "🪒", Cat.HYGIENE, Who.ADULT, FIXED, 1.0, 50),
        SUNSCREEN(R.string.seed_item_sunscreen, "☀️", Cat.HYGIENE, Who.SHARED, FIXED, 1.0, 200),

        CHARGER(R.string.seed_item_charger, "🔌", Cat.ELECTRONICS, Who.ADULT, FIXED, 1.0, 100),
        HEADPHONES(R.string.seed_item_headphones, "🎧", Cat.ELECTRONICS, Who.ADULT, FIXED, 1.0, 150),
        POWERBANK(R.string.seed_item_powerbank, "🔋", Cat.ELECTRONICS, Who.SHARED, FIXED, 1.0, 250),
        TABLET(R.string.seed_item_tablet, "📱", Cat.ELECTRONICS, Who.CHILD, FIXED, 1.0, 500),
        CAMERA(R.string.seed_item_camera, "📷", Cat.ELECTRONICS, Who.SHARED, FIXED, 1.0, 600),

        DIAPERS(R.string.seed_item_diapers, "🧷", Cat.BABY, Who.CHILD, PER_DAY, 4.0, 40),
        WIPES(R.string.seed_item_wipes, "🧻", Cat.BABY, Who.CHILD, FIXED, 1.0, 400),
        BOTTLE(R.string.seed_item_bottle, "🍼", Cat.BABY, Who.CHILD, FIXED, 2.0, 100),
        PACIFIER(R.string.seed_item_pacifier, "😶", Cat.BABY, Who.CHILD, FIXED, 2.0, 15),
        BODYSUITS(R.string.seed_item_bodysuits, "👶", Cat.BABY, Who.CHILD, PER_DAY, 2.0, 80, true),
        SPARE_CLOTHES(R.string.seed_item_spare_clothes, "🎽", Cat.BABY, Who.CHILD, FIXED, 1.0, 300),
        CUDDLY_TOY(R.string.seed_item_cuddly_toy, "🧸", Cat.BABY, Who.CHILD, FIXED, 1.0, 150),
        RAIN_CLOTHES(R.string.seed_item_rain_clothes, "☔", Cat.BABY, Who.CHILD, FIXED, 1.0, 400),
        BLANKET(R.string.seed_item_blanket, "🧣", Cat.BABY, Who.CHILD, FIXED, 1.0, 400),

        PASSPORT(R.string.seed_item_passport, "🛂", Cat.DOCUMENTS, Who.ADULT, FIXED, 1.0, 50),
        ID_CARD(R.string.seed_item_id_card, "🪪", Cat.DOCUMENTS, Who.ADULT, FIXED, 1.0, 10),
        TICKETS(R.string.seed_item_tickets, "🎫", Cat.DOCUMENTS, Who.SHARED, FIXED, 1.0, 10),
        INSURANCE(R.string.seed_item_insurance, "📋", Cat.DOCUMENTS, Who.SHARED, FIXED, 1.0, 10),
        WALLET(R.string.seed_item_wallet, "👛", Cat.DOCUMENTS, Who.ADULT, FIXED, 1.0, 150),

        GYM_CLOTHES(R.string.seed_item_gym_clothes, "🩳", Cat.TRAINING, Who.ADULT, FIXED, 1.0, 300),
        GYM_SHOES(R.string.seed_item_gym_shoes, "👟", Cat.TRAINING, Who.ADULT, FIXED, 1.0, 700),
        TOWEL(R.string.seed_item_towel, "🧖", Cat.TRAINING, Who.ADULT, FIXED, 1.0, 400),
        WATER_BOTTLE(R.string.seed_item_water_bottle, "🚰", Cat.TRAINING, Who.ADULT, FIXED, 1.0, 200),
        PADLOCK(R.string.seed_item_padlock, "🔒", Cat.TRAINING, Who.ADULT, FIXED, 1.0, 100),
        GYM_CARD(R.string.seed_item_gym_card, "💳", Cat.TRAINING, Who.ADULT, FIXED, 1.0, 10),

        SWIMWEAR(R.string.seed_item_swimwear, "🩱", Cat.SWIM, Who.SHARED, FIXED, 1.0, 150),
        BEACH_TOWEL(R.string.seed_item_beach_towel, "🏖️", Cat.SWIM, Who.SHARED, FIXED, 1.0, 500),
        GOGGLES(R.string.seed_item_goggles, "🥽", Cat.SWIM, Who.CHILD, FIXED, 1.0, 50),
        SWIM_TOYS(R.string.seed_item_swim_toys, "🦆", Cat.SWIM, Who.CHILD, FIXED, 1.0, 300),
        SUNGLASSES(R.string.seed_item_sunglasses, "🕶️", Cat.SWIM, Who.ADULT, FIXED, 1.0, 50),

        SNACKS(R.string.seed_item_snacks, "🍪", Cat.FOOD, Who.SHARED, PER_DAY, 2.0, 100),
        LUNCHBOX(R.string.seed_item_lunchbox, "🍱", Cat.FOOD, Who.SHARED, FIXED, 1.0, 400),

        PLASTERS(R.string.seed_item_plasters, "🩹", Cat.MEDICINE, Who.SHARED, FIXED, 1.0, 50),
        PAINKILLERS(R.string.seed_item_painkillers, "💊", Cat.MEDICINE, Who.SHARED, FIXED, 1.0, 100),
        PRESCRIPTION(R.string.seed_item_prescription, "💉", Cat.MEDICINE, Who.ADULT, PER_DAY, 1.0, 5),

        KEYS(R.string.seed_item_keys, "🔑", Cat.OTHER, Who.ADULT, FIXED, 1.0, 50),
        UMBRELLA(R.string.seed_item_umbrella, "☂️", Cat.OTHER, Who.SHARED, FIXED, 1.0, 350),
    }

    private data class SubBagSpec(@StringRes val nameRes: Int, val emoji: String, val maxGrams: Int? = null)

    private data class BagSpec(
        @StringRes val nameRes: Int,
        val emoji: String,
        val type: BagType,
        val days: Int = 1,
        val laundry: Boolean = false,
        val maxGrams: Int? = null,
        val subBags: List<SubBagSpec> = emptyList(),
        /** Objekt och index i [subBags] (null = huvudväskan). */
        val items: List<Pair<Item, Int?>>,
    )

    private val bags = listOf(
        BagSpec(
            R.string.seed_bag_gym, "🏋️", BagType.EVERYDAY,
            items = listOf(
                Item.GYM_CLOTHES, Item.GYM_SHOES, Item.TOWEL, Item.WATER_BOTTLE,
                Item.PADLOCK, Item.GYM_CARD, Item.DEODORANT, Item.HEADPHONES, Item.SHAMPOO,
            ).map { it to null },
        ),
        BagSpec(
            R.string.seed_bag_preschool, "👶", BagType.EVERYDAY,
            items = listOf(
                Item.DIAPERS, Item.WIPES, Item.SPARE_CLOTHES, Item.RAIN_CLOTHES,
                Item.BOTTLE, Item.SNACKS, Item.CUDDLY_TOY, Item.PACIFIER,
            ).map { it to null },
        ),
        BagSpec(
            R.string.seed_bag_swim, "🏊", BagType.EVERYDAY,
            items = listOf(
                Item.SWIMWEAR, Item.BEACH_TOWEL, Item.GOGGLES, Item.SHAMPOO,
                Item.SWIM_TOYS, Item.WATER_BOTTLE, Item.SNACKS,
            ).map { it to null },
        ),
        BagSpec(
            R.string.seed_bag_country_house, "🏡", BagType.TRIP,
            days = 3,
            subBags = listOf(
                SubBagSpec(R.string.seed_subbag_toiletry, "🧼"),
                SubBagSpec(R.string.seed_subbag_kids_backpack, "🎒", 5000),
            ),
            items = listOf(
                Item.TSHIRT to null, Item.UNDERWEAR to null, Item.SOCKS to null,
                Item.PYJAMAS to null, Item.SWEATER to null, Item.CHARGER to null,
                Item.TOOTHBRUSH to 0, Item.TOOTHPASTE to 0, Item.SHAMPOO to 0,
                Item.KID_CLOTHES to 1, Item.KID_PYJAMAS to 1, Item.DIAPERS to 1,
                Item.CUDDLY_TOY to 1, Item.SNACKS to 1,
            ),
        ),
        BagSpec(
            R.string.seed_bag_vacation, "✈️", BagType.TRIP,
            days = 7,
            laundry = true,
            maxGrams = 23_000,
            subBags = listOf(
                SubBagSpec(R.string.seed_subbag_cabin, "💼", 8000),
                SubBagSpec(R.string.seed_subbag_toiletry, "🧼"),
                SubBagSpec(R.string.seed_subbag_kids_backpack, "🎒", 5000),
            ),
            items = listOf(
                Item.TSHIRT to null, Item.UNDERWEAR to null, Item.SOCKS to null,
                Item.TROUSERS to null, Item.SWEATER to null, Item.SHOES to null,
                Item.SWIMWEAR to null, Item.BEACH_TOWEL to null, Item.KID_CLOTHES to null,
                Item.BODYSUITS to null,
                Item.PASSPORT to 0, Item.TICKETS to 0, Item.INSURANCE to 0, Item.WALLET to 0,
                Item.CHARGER to 0, Item.HEADPHONES to 0, Item.POWERBANK to 0, Item.PRESCRIPTION to 0,
                Item.TOOTHBRUSH to 1, Item.TOOTHPASTE to 1, Item.SHAMPOO to 1, Item.SUNSCREEN to 1,
                Item.DEODORANT to 1, Item.PLASTERS to 1,
                Item.DIAPERS to 2, Item.WIPES to 2, Item.TABLET to 2, Item.CUDDLY_TOY to 2,
                Item.SNACKS to 2, Item.SUNGLASSES to null,
            ),
        ),
    )

    suspend fun seedIfNeeded() {
        if (settings.isSeeded) return
        db.withTransaction { seed() }
        settings.isSeeded = true
    }

    private suspend fun seed() {
        val categoryIds = Cat.entries.associateWith { cat ->
            db.categoryDao().upsert(
                CategoryEntity(
                    name = context.getString(cat.nameRes),
                    emoji = cat.emoji,
                    isCustom = false,
                    sortOrder = cat.ordinal,
                )
            )
        }
        val profileIds = Who.entries.associateWith { who ->
            db.profileDao().upsert(ProfileEntity(name = context.getString(who.nameRes), emoji = who.emoji))
        }
        val itemIds = Item.entries.associateWith { item ->
            db.catalogItemDao().upsert(item.toEntity(categoryIds, profileIds))
        }

        val now = System.currentTimeMillis()
        bags.forEachIndexed { index, spec ->
            val bagId = db.bagDao().upsert(
                BagEntity(
                    name = context.getString(spec.nameRes),
                    emoji = spec.emoji,
                    type = spec.type,
                    startEpochDay = null,
                    endEpochDay = null,
                    manualDays = spec.days,
                    hasLaundry = spec.laundry,
                    maxWeightGrams = spec.maxGrams,
                    createdAt = now + index,
                )
            )
            val subBagIds = spec.subBags.map { sub ->
                db.subBagDao().upsert(
                    SubBagEntity(
                        bagId = bagId,
                        name = context.getString(sub.nameRes),
                        emoji = sub.emoji,
                        maxWeightGrams = sub.maxGrams,
                    )
                )
            }
            db.bagItemDao().insertAll(
                spec.items.map { (item, subIndex) ->
                    val catalog = item.toEntity(categoryIds, profileIds)
                    BagItemEntity(
                        bagId = bagId,
                        catalogItemId = itemIds.getValue(item),
                        name = catalog.name,
                        categoryId = catalog.categoryId,
                        profileId = catalog.profileId,
                        subBagId = subIndex?.let { subBagIds[it] },
                        emoji = catalog.emoji,
                        imagePath = null,
                        rule = catalog.rule,
                        amount = catalog.amount,
                        weightGrams = catalog.weightGrams,
                        laundryAffected = catalog.laundryAffected,
                        isPacked = false,
                    )
                }
            )
        }
    }

    private fun Item.toEntity(categoryIds: Map<Cat, Long>, profileIds: Map<Who, Long>) = CatalogItemEntity(
        name = context.getString(nameRes),
        categoryId = categoryIds.getValue(cat),
        profileId = who?.let { profileIds.getValue(it) },
        emoji = emoji,
        imagePath = null,
        rule = rule,
        amount = amount,
        weightGrams = grams,
        laundryAffected = laundry,
        isCustom = false,
    )
}

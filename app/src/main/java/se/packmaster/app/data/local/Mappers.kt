package se.packmaster.app.data.local

import se.packmaster.domain.model.Bag
import se.packmaster.domain.model.BagItem
import se.packmaster.domain.model.CatalogItem
import se.packmaster.domain.model.Category
import se.packmaster.domain.model.Profile
import se.packmaster.domain.model.SubBag

fun CategoryEntity.toDomain() = Category(id, name, emoji, isCustom)

fun ProfileEntity.toDomain() = Profile(id, name, emoji)
fun Profile.toEntity() = ProfileEntity(id, name, emoji)

fun CatalogItemEntity.toDomain() = CatalogItem(
    id = id,
    name = name,
    categoryId = categoryId,
    profileId = profileId,
    emoji = emoji,
    imagePath = imagePath,
    rule = rule,
    amount = amount,
    weightGrams = weightGrams,
    laundryAffected = laundryAffected,
    isCustom = isCustom,
)

fun CatalogItem.toEntity() = CatalogItemEntity(
    id = id,
    name = name,
    categoryId = categoryId,
    profileId = profileId,
    emoji = emoji,
    imagePath = imagePath,
    rule = rule,
    amount = amount,
    weightGrams = weightGrams,
    laundryAffected = laundryAffected,
    isCustom = isCustom,
)

fun BagEntity.toDomain() = Bag(
    id = id,
    name = name,
    emoji = emoji,
    type = type,
    startEpochDay = startEpochDay,
    endEpochDay = endEpochDay,
    manualDays = manualDays,
    hasLaundry = hasLaundry,
    maxWeightGrams = maxWeightGrams,
    createdAt = createdAt,
)

fun Bag.toEntity() = BagEntity(
    id = id,
    name = name,
    emoji = emoji,
    type = type,
    startEpochDay = startEpochDay,
    endEpochDay = endEpochDay,
    manualDays = manualDays,
    hasLaundry = hasLaundry,
    maxWeightGrams = maxWeightGrams,
    createdAt = createdAt,
)

fun SubBagEntity.toDomain() = SubBag(id, bagId, name, emoji, maxWeightGrams)
fun SubBag.toEntity() = SubBagEntity(id, bagId, name, emoji, maxWeightGrams)

fun BagItemEntity.toDomain() = BagItem(
    id = id,
    bagId = bagId,
    catalogItemId = catalogItemId,
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
    isPacked = isPacked,
)

fun BagItem.toEntity() = BagItemEntity(
    id = id,
    bagId = bagId,
    catalogItemId = catalogItemId,
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
    isPacked = isPacked,
)

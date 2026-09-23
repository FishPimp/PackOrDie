package se.packmaster.app.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import se.packmaster.domain.model.BagType
import se.packmaster.domain.model.QuantityRule

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val emoji: String,
    @ColumnInfo(name = "is_custom") val isCustom: Boolean,
    @ColumnInfo(name = "sort_order") val sortOrder: Int = 0,
)

@Entity(tableName = "profiles")
data class ProfileEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val emoji: String,
)

@Entity(
    tableName = "catalog_items",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["category_id"],
            onDelete = ForeignKey.SET_NULL,
        ),
        ForeignKey(
            entity = ProfileEntity::class,
            parentColumns = ["id"],
            childColumns = ["profile_id"],
            onDelete = ForeignKey.SET_NULL,
        ),
    ],
    indices = [Index("category_id"), Index("profile_id")],
)
data class CatalogItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    @ColumnInfo(name = "category_id") val categoryId: Long?,
    @ColumnInfo(name = "profile_id") val profileId: Long?,
    val emoji: String,
    @ColumnInfo(name = "image_path") val imagePath: String?,
    val rule: QuantityRule,
    val amount: Double,
    @ColumnInfo(name = "weight_grams") val weightGrams: Int,
    @ColumnInfo(name = "laundry_affected") val laundryAffected: Boolean,
    @ColumnInfo(name = "is_custom") val isCustom: Boolean,
)

@Entity(tableName = "bags")
data class BagEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val emoji: String,
    val type: BagType,
    @ColumnInfo(name = "start_epoch_day") val startEpochDay: Long?,
    @ColumnInfo(name = "end_epoch_day") val endEpochDay: Long?,
    @ColumnInfo(name = "manual_days") val manualDays: Int,
    @ColumnInfo(name = "has_laundry") val hasLaundry: Boolean,
    @ColumnInfo(name = "max_weight_grams") val maxWeightGrams: Int?,
    @ColumnInfo(name = "created_at") val createdAt: Long,
)

@Entity(
    tableName = "sub_bags",
    foreignKeys = [
        ForeignKey(
            entity = BagEntity::class,
            parentColumns = ["id"],
            childColumns = ["bag_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("bag_id")],
)
data class SubBagEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "bag_id") val bagId: Long,
    val name: String,
    val emoji: String,
    @ColumnInfo(name = "max_weight_grams") val maxWeightGrams: Int?,
)

@Entity(
    tableName = "bag_items",
    foreignKeys = [
        ForeignKey(
            entity = BagEntity::class,
            parentColumns = ["id"],
            childColumns = ["bag_id"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = SubBagEntity::class,
            parentColumns = ["id"],
            childColumns = ["sub_bag_id"],
            onDelete = ForeignKey.SET_NULL,
        ),
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["category_id"],
            onDelete = ForeignKey.SET_NULL,
        ),
        ForeignKey(
            entity = ProfileEntity::class,
            parentColumns = ["id"],
            childColumns = ["profile_id"],
            onDelete = ForeignKey.SET_NULL,
        ),
        ForeignKey(
            entity = CatalogItemEntity::class,
            parentColumns = ["id"],
            childColumns = ["catalog_item_id"],
            onDelete = ForeignKey.SET_NULL,
        ),
    ],
    indices = [
        Index("bag_id"),
        Index("sub_bag_id"),
        Index("category_id"),
        Index("profile_id"),
        Index("catalog_item_id"),
    ],
)
data class BagItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "bag_id") val bagId: Long,
    @ColumnInfo(name = "catalog_item_id") val catalogItemId: Long?,
    val name: String,
    @ColumnInfo(name = "category_id") val categoryId: Long?,
    @ColumnInfo(name = "profile_id") val profileId: Long?,
    @ColumnInfo(name = "sub_bag_id") val subBagId: Long?,
    val emoji: String,
    @ColumnInfo(name = "image_path") val imagePath: String?,
    val rule: QuantityRule,
    val amount: Double,
    @ColumnInfo(name = "weight_grams") val weightGrams: Int,
    @ColumnInfo(name = "laundry_affected") val laundryAffected: Boolean,
    @ColumnInfo(name = "is_packed") val isPacked: Boolean,
)

/** Resultatrad för framstegsöversikten på startsidan. */
data class BagProgressRow(
    @ColumnInfo(name = "bag_id") val bagId: Long,
    val packed: Int,
    val total: Int,
)

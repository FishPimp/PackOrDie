package se.packmaster.app.data.local

import androidx.room.TypeConverter
import se.packmaster.domain.model.BagType
import se.packmaster.domain.model.QuantityRule

class Converters {
    @TypeConverter
    fun fromBagType(value: BagType): String = value.name

    @TypeConverter
    fun toBagType(value: String): BagType =
        BagType.entries.firstOrNull { it.name == value } ?: BagType.EVERYDAY

    @TypeConverter
    fun fromQuantityRule(value: QuantityRule): String = value.name

    @TypeConverter
    fun toQuantityRule(value: String): QuantityRule =
        QuantityRule.entries.firstOrNull { it.name == value } ?: QuantityRule.FIXED
}

package se.packmaster.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        CategoryEntity::class,
        ProfileEntity::class,
        CatalogItemEntity::class,
        BagEntity::class,
        SubBagEntity::class,
        BagItemEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class PackMasterDatabase : RoomDatabase() {
    abstract fun categoryDao(): CategoryDao
    abstract fun profileDao(): ProfileDao
    abstract fun catalogItemDao(): CatalogItemDao
    abstract fun bagDao(): BagDao
    abstract fun subBagDao(): SubBagDao
    abstract fun bagItemDao(): BagItemDao

    companion object {
        fun build(context: Context): PackMasterDatabase =
            Room.databaseBuilder(context, PackMasterDatabase::class.java, "packmaster.db")
                .build()
    }
}

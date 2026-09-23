package se.packmaster.app.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories ORDER BY sort_order, name COLLATE NOCASE")
    fun observeAll(): Flow<List<CategoryEntity>>

    @Query("SELECT COALESCE(MAX(sort_order), 0) FROM categories")
    suspend fun maxSortOrder(): Int

    @Query("SELECT sort_order FROM categories WHERE id = :id")
    suspend fun sortOrderOf(id: Long): Int?

    @Upsert
    suspend fun upsert(category: CategoryEntity): Long

    @Delete
    suspend fun delete(category: CategoryEntity)
}

@Dao
interface ProfileDao {
    @Query("SELECT * FROM profiles ORDER BY id")
    fun observeAll(): Flow<List<ProfileEntity>>

    @Upsert
    suspend fun upsert(profile: ProfileEntity): Long

    @Delete
    suspend fun delete(profile: ProfileEntity)
}

@Dao
interface CatalogItemDao {
    @Query("SELECT * FROM catalog_items ORDER BY name COLLATE NOCASE")
    fun observeAll(): Flow<List<CatalogItemEntity>>

    @Query("SELECT * FROM catalog_items WHERE id IN (:ids)")
    suspend fun getByIds(ids: List<Long>): List<CatalogItemEntity>

    @Upsert
    suspend fun upsert(item: CatalogItemEntity): Long

    @Delete
    suspend fun delete(item: CatalogItemEntity)
}

@Dao
interface BagDao {
    @Query("SELECT * FROM bags ORDER BY created_at")
    fun observeAll(): Flow<List<BagEntity>>

    @Query("SELECT * FROM bags WHERE id = :bagId")
    fun observe(bagId: Long): Flow<BagEntity?>

    @Query(
        "SELECT bag_id, SUM(CASE WHEN is_packed THEN 1 ELSE 0 END) AS packed, COUNT(*) AS total " +
            "FROM bag_items GROUP BY bag_id"
    )
    fun observeProgress(): Flow<List<BagProgressRow>>

    @Upsert
    suspend fun upsert(bag: BagEntity): Long

    @Query("DELETE FROM bags WHERE id = :bagId")
    suspend fun delete(bagId: Long)
}

@Dao
interface SubBagDao {
    @Query("SELECT * FROM sub_bags WHERE bag_id = :bagId ORDER BY id")
    fun observeForBag(bagId: Long): Flow<List<SubBagEntity>>

    @Upsert
    suspend fun upsert(subBag: SubBagEntity): Long

    @Delete
    suspend fun delete(subBag: SubBagEntity)

    @Query("DELETE FROM sub_bags WHERE bag_id = :bagId")
    suspend fun deleteForBag(bagId: Long)
}

@Dao
interface BagItemDao {
    @Query("SELECT * FROM bag_items WHERE bag_id = :bagId")
    fun observeForBag(bagId: Long): Flow<List<BagItemEntity>>

    @Insert
    suspend fun insertAll(items: List<BagItemEntity>)

    @Update
    suspend fun update(item: BagItemEntity)

    @Delete
    suspend fun delete(item: BagItemEntity)

    @Query("UPDATE bag_items SET is_packed = :packed WHERE id IN (:ids)")
    suspend fun setPacked(ids: List<Long>, packed: Boolean)

    @Query("SELECT id FROM bag_items WHERE bag_id = :bagId AND is_packed = 1")
    suspend fun packedIds(bagId: Long): List<Long>

    @Query("UPDATE bag_items SET is_packed = 0 WHERE bag_id = :bagId")
    suspend fun unpackAll(bagId: Long)

    @Query("UPDATE bag_items SET sub_bag_id = NULL WHERE sub_bag_id = :subBagId")
    suspend fun clearSubBag(subBagId: Long)

    @Query("DELETE FROM bag_items WHERE bag_id = :bagId")
    suspend fun deleteForBag(bagId: Long)
}

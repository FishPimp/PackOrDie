package se.packmaster.app.data.repository

import androidx.room.withTransaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import se.packmaster.app.data.local.PackMasterDatabase
import se.packmaster.app.data.local.toDomain
import se.packmaster.app.data.local.toEntity
import se.packmaster.domain.model.Bag
import se.packmaster.domain.model.BagItem
import se.packmaster.domain.model.PackingProgress
import se.packmaster.domain.model.SubBag
import se.packmaster.domain.repository.BagRepository

class RoomBagRepository(private val db: PackMasterDatabase) : BagRepository {

    private val bagDao = db.bagDao()
    private val subBagDao = db.subBagDao()
    private val itemDao = db.bagItemDao()

    override fun observeBags(): Flow<List<Bag>> =
        bagDao.observeAll().map { list -> list.map { it.toDomain() } }

    override fun observeBag(bagId: Long): Flow<Bag?> =
        bagDao.observe(bagId).map { it?.toDomain() }

    override fun observeProgress(): Flow<Map<Long, PackingProgress>> =
        bagDao.observeProgress().map { rows ->
            rows.associate { it.bagId to PackingProgress(it.packed, it.total) }
        }

    override fun observeItems(bagId: Long): Flow<List<BagItem>> =
        itemDao.observeForBag(bagId).map { list -> list.map { it.toDomain() } }

    override fun observeSubBags(bagId: Long): Flow<List<SubBag>> =
        subBagDao.observeForBag(bagId).map { list -> list.map { it.toDomain() } }

    override suspend fun saveBag(bag: Bag): Long {
        val entity = bag.toEntity().let {
            if (bag.id == 0L && bag.createdAt == 0L) it.copy(createdAt = System.currentTimeMillis()) else it
        }
        val id = bagDao.upsert(entity)
        return if (bag.id == 0L) id else bag.id
    }

    override suspend fun deleteBag(bagId: Long) = db.withTransaction {
        itemDao.deleteForBag(bagId)
        subBagDao.deleteForBag(bagId)
        bagDao.delete(bagId)
    }

    override suspend fun saveSubBag(subBag: SubBag): Long {
        val id = subBagDao.upsert(subBag.toEntity())
        return if (subBag.id == 0L) id else subBag.id
    }

    override suspend fun deleteSubBag(subBag: SubBag) = db.withTransaction {
        // Objekten flyttas tillbaka till huvudväskan.
        itemDao.clearSubBag(subBag.id)
        subBagDao.delete(subBag.toEntity())
    }

    override suspend fun addItems(items: List<BagItem>) =
        itemDao.insertAll(items.map { it.toEntity().copy(id = 0) })

    override suspend fun updateItem(item: BagItem) = itemDao.update(item.toEntity())

    override suspend fun deleteItem(item: BagItem) = itemDao.delete(item.toEntity())

    override suspend fun setPacked(itemIds: List<Long>, packed: Boolean) =
        itemDao.setPacked(itemIds, packed)

    override suspend fun resetPacked(bagId: Long): List<Long> = db.withTransaction {
        val packed = itemDao.packedIds(bagId)
        itemDao.unpackAll(bagId)
        packed
    }
}

package se.packmaster.domain.repository

import kotlinx.coroutines.flow.Flow
import se.packmaster.domain.model.Bag
import se.packmaster.domain.model.BagItem
import se.packmaster.domain.model.CatalogItem
import se.packmaster.domain.model.Category
import se.packmaster.domain.model.PackingProgress
import se.packmaster.domain.model.Profile
import se.packmaster.domain.model.SubBag

interface CatalogRepository {
    fun observeCategories(): Flow<List<Category>>
    fun observeProfiles(): Flow<List<Profile>>
    fun observeCatalogItems(): Flow<List<CatalogItem>>

    suspend fun getCatalogItems(ids: List<Long>): List<CatalogItem>

    suspend fun saveCategory(category: Category): Long
    suspend fun deleteCategory(category: Category)

    suspend fun saveProfile(profile: Profile): Long
    suspend fun deleteProfile(profile: Profile)

    suspend fun saveCatalogItem(item: CatalogItem): Long
    suspend fun deleteCatalogItem(item: CatalogItem)
}

interface BagRepository {
    fun observeBags(): Flow<List<Bag>>
    fun observeBag(bagId: Long): Flow<Bag?>
    fun observeProgress(): Flow<Map<Long, PackingProgress>>
    fun observeItems(bagId: Long): Flow<List<BagItem>>
    fun observeSubBags(bagId: Long): Flow<List<SubBag>>

    suspend fun saveBag(bag: Bag): Long
    suspend fun deleteBag(bagId: Long)

    suspend fun saveSubBag(subBag: SubBag): Long
    suspend fun deleteSubBag(subBag: SubBag)

    suspend fun addItems(items: List<BagItem>)
    suspend fun updateItem(item: BagItem)
    suspend fun deleteItem(item: BagItem)

    suspend fun setPacked(itemIds: List<Long>, packed: Boolean)

    /** Bockar av alla objekt i väskan och returnerar id:n som var packade innan. */
    suspend fun resetPacked(bagId: Long): List<Long>
}

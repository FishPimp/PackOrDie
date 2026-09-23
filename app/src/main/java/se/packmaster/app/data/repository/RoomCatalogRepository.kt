package se.packmaster.app.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import se.packmaster.app.data.local.CategoryEntity
import se.packmaster.app.data.local.PackMasterDatabase
import se.packmaster.app.data.local.toDomain
import se.packmaster.app.data.local.toEntity
import se.packmaster.domain.model.CatalogItem
import se.packmaster.domain.model.Category
import se.packmaster.domain.model.Profile
import se.packmaster.domain.repository.CatalogRepository

class RoomCatalogRepository(db: PackMasterDatabase) : CatalogRepository {

    private val categoryDao = db.categoryDao()
    private val profileDao = db.profileDao()
    private val itemDao = db.catalogItemDao()

    override fun observeCategories(): Flow<List<Category>> =
        categoryDao.observeAll().map { list -> list.map { it.toDomain() } }

    override fun observeProfiles(): Flow<List<Profile>> =
        profileDao.observeAll().map { list -> list.map { it.toDomain() } }

    override fun observeCatalogItems(): Flow<List<CatalogItem>> =
        itemDao.observeAll().map { list -> list.map { it.toDomain() } }

    override suspend fun getCatalogItems(ids: List<Long>): List<CatalogItem> {
        val byId = itemDao.getByIds(ids).associateBy { it.id }
        // Behåll användarens valordning.
        return ids.mapNotNull { byId[it]?.toDomain() }
    }

    override suspend fun saveCategory(category: Category): Long {
        val sortOrder = if (category.id == 0L) {
            categoryDao.maxSortOrder() + 1
        } else {
            // Behåll befintlig sorteringsordning vid redigering.
            categoryDao.sortOrderOf(category.id) ?: 0
        }
        val id = categoryDao.upsert(
            CategoryEntity(category.id, category.name, category.emoji, category.isCustom, sortOrder)
        )
        return if (category.id == 0L) id else category.id
    }

    override suspend fun deleteCategory(category: Category) {
        categoryDao.delete(CategoryEntity(category.id, category.name, category.emoji, category.isCustom))
    }

    override suspend fun saveProfile(profile: Profile): Long {
        val id = profileDao.upsert(profile.toEntity())
        return if (profile.id == 0L) id else profile.id
    }

    override suspend fun deleteProfile(profile: Profile) = profileDao.delete(profile.toEntity())

    override suspend fun saveCatalogItem(item: CatalogItem): Long {
        val id = itemDao.upsert(item.toEntity())
        return if (item.id == 0L) id else item.id
    }

    override suspend fun deleteCatalogItem(item: CatalogItem) = itemDao.delete(item.toEntity())
}

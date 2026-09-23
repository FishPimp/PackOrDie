package se.packmaster.app.ui.catalog

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import se.packmaster.app.data.image.ImageStorage
import se.packmaster.app.ui.appContainer
import se.packmaster.domain.model.CatalogItem
import se.packmaster.domain.model.Category
import se.packmaster.domain.model.Profile
import se.packmaster.domain.repository.CatalogRepository

data class CatalogSection(val category: Category?, val items: List<CatalogItem>)

data class CatalogUiState(
    val isLoading: Boolean = true,
    val query: String = "",
    val selectedCategoryId: Long? = null,
    val categories: List<Category> = emptyList(),
    val profiles: List<Profile> = emptyList(),
    val sections: List<CatalogSection> = emptyList(),
    val totalItems: Int = 0,
)

class CatalogViewModel(
    private val repository: CatalogRepository,
    private val imageStorage: ImageStorage,
) : ViewModel() {

    private val query = MutableStateFlow("")
    private val selectedCategory = MutableStateFlow<Long?>(null)

    val uiState: StateFlow<CatalogUiState> = combine(
        repository.observeCatalogItems(),
        repository.observeCategories(),
        repository.observeProfiles(),
        query,
        selectedCategory,
    ) { items, categories, profiles, query, selectedCategory ->
        val filtered = items.filter { item ->
            (query.isBlank() || item.name.contains(query.trim(), ignoreCase = true)) &&
                (selectedCategory == null || item.categoryId == selectedCategory)
        }
        val byCategory = filtered.groupBy { it.categoryId }
        val sections = categories.mapNotNull { category ->
            byCategory[category.id]?.let { CatalogSection(category, it) }
        } + listOfNotNull(
            filtered.filter { item -> categories.none { it.id == item.categoryId } }
                .takeIf { it.isNotEmpty() }
                ?.let { CatalogSection(null, it) }
        )
        CatalogUiState(
            isLoading = false,
            query = query,
            selectedCategoryId = selectedCategory,
            categories = categories,
            profiles = profiles,
            sections = sections,
            totalItems = items.size,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CatalogUiState())

    fun setQuery(value: String) {
        query.value = value
    }

    fun selectCategory(id: Long?) {
        selectedCategory.value = id
    }

    fun saveItem(item: CatalogItem) {
        viewModelScope.launch { repository.saveCatalogItem(item) }
    }

    fun deleteItem(item: CatalogItem) {
        viewModelScope.launch { repository.deleteCatalogItem(item) }
    }

    fun saveCategory(category: Category) {
        viewModelScope.launch { repository.saveCategory(category) }
    }

    fun deleteCategory(category: Category) {
        viewModelScope.launch {
            if (selectedCategory.value == category.id) selectedCategory.value = null
            repository.deleteCategory(category)
        }
    }

    suspend fun createCategory(name: String, emoji: String): Long =
        repository.saveCategory(Category(name = name, emoji = emoji, isCustom = true))

    suspend fun importImage(uri: Uri): String? = imageStorage.import(uri)

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val container = appContainer()
                CatalogViewModel(container.catalogRepository, container.imageStorage)
            }
        }
    }
}

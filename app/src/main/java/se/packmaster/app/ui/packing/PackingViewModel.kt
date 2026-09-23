package se.packmaster.app.ui.packing

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import se.packmaster.app.data.image.ImageStorage
import se.packmaster.app.ui.appContainer
import se.packmaster.app.ui.navigation.BAG_ID_ARG
import se.packmaster.domain.model.Bag
import se.packmaster.domain.model.BagItem
import se.packmaster.domain.model.CatalogItem
import se.packmaster.domain.model.Category
import se.packmaster.domain.model.Grouping
import se.packmaster.domain.model.PackingList
import se.packmaster.domain.model.Profile
import se.packmaster.domain.model.SubBag
import se.packmaster.domain.model.toBagItem
import se.packmaster.domain.repository.BagRepository
import se.packmaster.domain.repository.CatalogRepository
import se.packmaster.domain.usecase.AddCatalogItemsToBagUseCase
import se.packmaster.domain.usecase.BuildPackingListUseCase
import se.packmaster.domain.usecase.ResetPackingListUseCase

data class PackingUiState(
    val isLoading: Boolean = true,
    val bag: Bag? = null,
    val bagItems: List<BagItem> = emptyList(),
    val packingList: PackingList? = null,
    val subBags: List<SubBag> = emptyList(),
    val categories: List<Category> = emptyList(),
    val profiles: List<Profile> = emptyList(),
    val catalog: List<CatalogItem> = emptyList(),
    val grouping: Grouping = Grouping.CATEGORY,
    val onlyUnpacked: Boolean = false,
)

sealed interface PackingEvent {
    data class ListReset(val previouslyPacked: List<Long>) : PackingEvent
    data object AllPacked : PackingEvent
    data object BagDeleted : PackingEvent
}

class PackingViewModel(
    savedStateHandle: SavedStateHandle,
    private val bagRepository: BagRepository,
    private val catalogRepository: CatalogRepository,
    private val buildPackingList: BuildPackingListUseCase,
    private val addCatalogItems: AddCatalogItemsToBagUseCase,
    private val resetPackingList: ResetPackingListUseCase,
    private val imageStorage: ImageStorage,
) : ViewModel() {

    private val bagId: Long = checkNotNull(savedStateHandle.get<Long>(BAG_ID_ARG))

    private val grouping = MutableStateFlow(Grouping.CATEGORY)
    private val onlyUnpacked = MutableStateFlow(false)

    private val events = Channel<PackingEvent>(Channel.BUFFERED)
    val eventFlow: Flow<PackingEvent> = events.receiveAsFlow()

    private data class BagData(val bag: Bag?, val items: List<BagItem>, val subBags: List<SubBag>)
    private data class CatalogData(val categories: List<Category>, val profiles: List<Profile>, val catalog: List<CatalogItem>)

    private val bagData = combine(
        bagRepository.observeBag(bagId),
        bagRepository.observeItems(bagId),
        bagRepository.observeSubBags(bagId),
    ) { bag, items, subBags -> BagData(bag, items, subBags) }

    private val catalogData = combine(
        catalogRepository.observeCategories(),
        catalogRepository.observeProfiles(),
        catalogRepository.observeCatalogItems(),
    ) { categories, profiles, catalog -> CatalogData(categories, profiles, catalog) }

    val uiState: StateFlow<PackingUiState> =
        combine(bagData, catalogData, grouping, onlyUnpacked) { bagData, catalogData, grouping, onlyUnpacked ->
            val bag = bagData.bag
            PackingUiState(
                isLoading = false,
                bag = bag,
                bagItems = bagData.items,
                packingList = bag?.let {
                    buildPackingList(
                        bag = it,
                        items = bagData.items,
                        subBags = bagData.subBags,
                        categories = catalogData.categories,
                        profiles = catalogData.profiles,
                        grouping = grouping,
                        onlyUnpacked = onlyUnpacked,
                    )
                },
                subBags = bagData.subBags,
                categories = catalogData.categories,
                profiles = catalogData.profiles,
                catalog = catalogData.catalog,
                grouping = grouping,
                onlyUnpacked = onlyUnpacked,
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PackingUiState())

    fun setGrouping(value: Grouping) {
        grouping.value = value
    }

    fun setOnlyUnpacked(value: Boolean) {
        onlyUnpacked.value = value
    }

    fun togglePacked(item: BagItem) {
        viewModelScope.launch {
            val nowPacked = !item.isPacked
            bagRepository.setPacked(listOf(item.id), nowPacked)
            val progress = uiState.value.packingList?.progress
            if (nowPacked && progress != null && progress.packed + 1 == progress.total) {
                events.send(PackingEvent.AllPacked)
            }
        }
    }

    fun resetList() {
        viewModelScope.launch {
            val previouslyPacked = resetPackingList(bagId)
            events.send(PackingEvent.ListReset(previouslyPacked))
        }
    }

    fun undoReset(previouslyPacked: List<Long>) {
        viewModelScope.launch { resetPackingList.undo(previouslyPacked) }
    }

    fun addFromCatalog(catalogIds: List<Long>, subBagId: Long?) {
        viewModelScope.launch { addCatalogItems(bagId, catalogIds, subBagId) }
    }

    /** Skapar ett nytt objekt i masterkatalogen och lägger direkt till det i väskan. */
    fun createCatalogItemAndAdd(item: CatalogItem, subBagId: Long?) {
        viewModelScope.launch {
            val id = catalogRepository.saveCatalogItem(item)
            bagRepository.addItems(listOf(item.copy(id = id).toBagItem(bagId, subBagId)))
        }
    }

    fun updateItem(item: BagItem) {
        viewModelScope.launch { bagRepository.updateItem(item) }
    }

    fun deleteItem(item: BagItem) {
        viewModelScope.launch { bagRepository.deleteItem(item) }
    }

    fun saveSubBag(subBag: SubBag) {
        viewModelScope.launch { bagRepository.saveSubBag(subBag.copy(bagId = bagId)) }
    }

    fun deleteSubBag(subBag: SubBag) {
        viewModelScope.launch { bagRepository.deleteSubBag(subBag) }
    }

    fun updateBag(bag: Bag) {
        viewModelScope.launch { bagRepository.saveBag(bag) }
    }

    fun deleteBag() {
        viewModelScope.launch {
            bagRepository.deleteBag(bagId)
            events.send(PackingEvent.BagDeleted)
        }
    }

    suspend fun importImage(uri: Uri): String? = imageStorage.import(uri)

    suspend fun createCategory(name: String, emoji: String): Long =
        catalogRepository.saveCategory(Category(name = name, emoji = emoji, isCustom = true))

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val container = appContainer()
                PackingViewModel(
                    savedStateHandle = createSavedStateHandle(),
                    bagRepository = container.bagRepository,
                    catalogRepository = container.catalogRepository,
                    buildPackingList = container.buildPackingList,
                    addCatalogItems = container.addCatalogItemsToBag,
                    resetPackingList = container.resetPackingList,
                    imageStorage = container.imageStorage,
                )
            }
        }
    }
}

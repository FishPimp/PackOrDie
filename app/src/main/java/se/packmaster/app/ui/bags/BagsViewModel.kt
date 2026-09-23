package se.packmaster.app.ui.bags

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import se.packmaster.app.ui.appContainer
import se.packmaster.domain.model.Bag
import se.packmaster.domain.model.BagType
import se.packmaster.domain.model.PackingProgress
import se.packmaster.domain.repository.BagRepository

data class BagCard(val bag: Bag, val progress: PackingProgress)

data class BagsUiState(
    val isLoading: Boolean = true,
    val everyday: List<BagCard> = emptyList(),
    val trips: List<BagCard> = emptyList(),
)

class BagsViewModel(private val bagRepository: BagRepository) : ViewModel() {

    val uiState: StateFlow<BagsUiState> =
        combine(bagRepository.observeBags(), bagRepository.observeProgress()) { bags, progress ->
            val cards = bags.map { BagCard(it, progress[it.id] ?: PackingProgress.EMPTY) }
            BagsUiState(
                isLoading = false,
                everyday = cards.filter { it.bag.type == BagType.EVERYDAY },
                trips = cards.filter { it.bag.type == BagType.TRIP },
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), BagsUiState())

    fun createBag(bag: Bag, onCreated: (Long) -> Unit) {
        viewModelScope.launch { onCreated(bagRepository.saveBag(bag)) }
    }

    fun deleteBag(bagId: Long) {
        viewModelScope.launch { bagRepository.deleteBag(bagId) }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer { BagsViewModel(appContainer().bagRepository) }
        }
    }
}

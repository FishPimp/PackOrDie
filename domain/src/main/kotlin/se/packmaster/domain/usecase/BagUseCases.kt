package se.packmaster.domain.usecase

import se.packmaster.domain.model.toBagItem
import se.packmaster.domain.repository.BagRepository
import se.packmaster.domain.repository.CatalogRepository

/** Kopierar valda objekt från masterkatalogen till en väskas packlista. */
class AddCatalogItemsToBagUseCase(
    private val catalogRepository: CatalogRepository,
    private val bagRepository: BagRepository,
) {
    suspend operator fun invoke(bagId: Long, catalogItemIds: List<Long>, subBagId: Long?) {
        if (catalogItemIds.isEmpty()) return
        val items = catalogRepository.getCatalogItems(catalogItemIds)
            .map { it.toBagItem(bagId = bagId, subBagId = subBagId) }
        bagRepository.addItems(items)
    }
}

/** "Återställ packlista" – bockar av alla objekt inför nästa användning. */
class ResetPackingListUseCase(private val bagRepository: BagRepository) {
    /** Returnerar id:n som var packade, så att återställningen kan ångras. */
    suspend operator fun invoke(bagId: Long): List<Long> = bagRepository.resetPacked(bagId)

    suspend fun undo(previouslyPacked: List<Long>) {
        if (previouslyPacked.isNotEmpty()) bagRepository.setPacked(previouslyPacked, true)
    }
}

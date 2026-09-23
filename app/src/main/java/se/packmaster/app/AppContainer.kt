package se.packmaster.app

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import se.packmaster.app.data.image.ImageStorage
import se.packmaster.app.data.local.PackMasterDatabase
import se.packmaster.app.data.prefs.SettingsStore
import se.packmaster.app.data.repository.RoomBagRepository
import se.packmaster.app.data.repository.RoomCatalogRepository
import se.packmaster.app.data.seed.DatabaseSeeder
import se.packmaster.domain.repository.BagRepository
import se.packmaster.domain.repository.CatalogRepository
import se.packmaster.domain.usecase.AddCatalogItemsToBagUseCase
import se.packmaster.domain.usecase.BuildPackingListUseCase
import se.packmaster.domain.usecase.ResetPackingListUseCase

/** Enkel manuell beroendeinjektion för hela appen. */
class AppContainer(context: Context) {

    private val appContext = context.applicationContext
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val database: PackMasterDatabase by lazy { PackMasterDatabase.build(appContext) }

    val settings: SettingsStore by lazy { SettingsStore(appContext) }
    val imageStorage: ImageStorage by lazy { ImageStorage(appContext) }

    val catalogRepository: CatalogRepository by lazy { RoomCatalogRepository(database) }
    val bagRepository: BagRepository by lazy { RoomBagRepository(database) }

    val buildPackingList: BuildPackingListUseCase by lazy { BuildPackingListUseCase() }
    val addCatalogItemsToBag: AddCatalogItemsToBagUseCase by lazy {
        AddCatalogItemsToBagUseCase(catalogRepository, bagRepository)
    }
    val resetPackingList: ResetPackingListUseCase by lazy { ResetPackingListUseCase(bagRepository) }

    fun seedDatabase() {
        appScope.launch { DatabaseSeeder(appContext, database, settings).seedIfNeeded() }
    }
}

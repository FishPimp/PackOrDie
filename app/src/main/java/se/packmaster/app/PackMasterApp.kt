package se.packmaster.app

import android.app.Application

class PackMasterApp : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        container.seedDatabase()
    }
}

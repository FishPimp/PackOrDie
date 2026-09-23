package se.packmaster.app.ui

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import se.packmaster.app.AppContainer
import se.packmaster.app.PackMasterApp

/** Hämtar appens beroenden inifrån en ViewModel-factory. */
fun CreationExtras.appContainer(): AppContainer =
    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as PackMasterApp).container

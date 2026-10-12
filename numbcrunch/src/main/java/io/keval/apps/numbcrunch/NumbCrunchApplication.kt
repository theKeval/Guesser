package io.keval.apps.numbcrunch

class NumbCrunchApplication : android.app.Application() {
	val appContainer: AppContainer by lazy { AppContainer(this) }
}

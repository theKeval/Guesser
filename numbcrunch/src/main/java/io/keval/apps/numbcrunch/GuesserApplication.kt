package io.keval.apps.numbcrunch

class GuesserApplication : android.app.Application() {
	val appContainer: AppContainer by lazy { AppContainer(this) }
}

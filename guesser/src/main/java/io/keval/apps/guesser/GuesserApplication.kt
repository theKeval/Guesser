package io.keval.apps.guesser

class GuesserApplication : android.app.Application() {
	val appContainer: AppContainer by lazy { AppContainer() }
}

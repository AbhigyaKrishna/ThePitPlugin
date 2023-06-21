package me.abhigya.pit.features.pitevents

interface PitEventFactory<T : PitEvent> {

    fun create(): T

}

interface PitEvent {

    val state: State

    fun key(): String

    fun start()

    fun end()

    enum class State {
        INITIALIZED,
        STARTING,
        RUNNING,
        ENDING,
        ENDED
    }

}
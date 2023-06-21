package me.abhigya.pit.features.pitevents

import me.abhigya.pit.model.RunningArena
import java.util.concurrent.atomic.AtomicReference

abstract class AbstractPitEvent(
    private val key: String,
    protected val arena: RunningArena
) : PitEvent {

    protected val atomicState = AtomicReference(PitEvent.State.INITIALIZED)

    override val state: PitEvent.State
        get() = atomicState.get()

    override fun key(): String = key

    override fun start() {
        atomicState.set(PitEvent.State.STARTING)
        startEvent()
    }

    protected abstract fun startEvent()

}
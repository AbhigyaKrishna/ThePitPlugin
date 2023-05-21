package me.abhigya.pit.features

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class EventLoop(scope: CoroutineScope) : Runnable, CoroutineScope by scope {

    fun start() {
        this.launch {
            while (true) {
                val start = System.currentTimeMillis()
                this@EventLoop.run()
                val end = System.currentTimeMillis()
                val time = end - start
                if (time < 1000) {
                    delay(1000 - time)
                }
            }
        }
    }

    override fun run() {

    }

}
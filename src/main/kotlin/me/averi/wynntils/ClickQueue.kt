package me.averi.wynntils

import kotlinx.coroutines.Runnable
import me.averi.wynntils.utils.attack
import me.averi.wynntils.utils.localPlayer
import me.averi.wynntils.utils.use
import java.util.concurrent.LinkedBlockingQueue
import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.ExperimentalAtomicApi

object ClickQueue {
  @OptIn(ExperimentalAtomicApi::class)
  private val running = AtomicBoolean(true)
  private val queue = LinkedBlockingQueue<Boolean>()

  fun start() = Thread(ClickExecutor).start()

  @OptIn(ExperimentalAtomicApi::class)
  fun stop() = running.store(false)

  fun add(clicks: BooleanArray) {
    if (!queue.isEmpty()) return
    queue.addAll(clicks.toTypedArray())
  }

  val isEmpty
    get() = queue.isEmpty()

  private object ClickExecutor : Runnable {
    private fun click(rightClick: Boolean) {
      if (!rightClick) {
        localPlayer?.attack()
      } else {
        localPlayer?.use()
      }
    }

    @OptIn(ExperimentalAtomicApi::class)
    override fun run() {
      while (running.load()) {
        try {
          click(queue.take()/* xor (localPlayer?.getWynnClass() == WynnClass.ARCHER)*/)
          Thread.sleep(80)
        } catch (e: InterruptedException) {
          e.printStackTrace()
        }
      }
    }
  }
}

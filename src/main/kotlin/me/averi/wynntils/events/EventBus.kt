package me.averi.wynntils.events

import net.neoforged.bus.api.Event
import net.neoforged.bus.api.ICancellableEvent
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.reflect.KClass

object EventBus {
  private val listeners = ConcurrentHashMap<KClass<out Event>, CopyOnWriteArrayList<Listener>>()

  fun <T : Event> subscribe(type: KClass<T>, priority: Int, callback: (T) -> Unit) {
    @Suppress("unchecked_cast")
    val listener = Listener(priority, callback as ((Event) -> Unit))

    val typedListeners = listeners.computeIfAbsent(type) { CopyOnWriteArrayList() }
    typedListeners.add(listener)

    typedListeners.sortByDescending { it.priority }
  }

  inline fun <reified T : Event> subscribe(priority: Int = 0, noinline callback: (T) -> Unit) =
    subscribe(T::class, priority, callback)

  inline fun <reified T : Event> subscribe(noinline callback: (T) -> Unit, priority: Int = 0) =
    subscribe(T::class, priority, callback)

  /**
   * @return did the event get canceled
   */
  @JvmStatic
  fun <T : Event> publish(event: T): Boolean {
    listeners[event::class]?.forEach { listener ->
      listener.callback(event)
      if (event is ICancellableEvent && event.isCanceled) return true
    }

    return false
  }

  private data class Listener(val priority: Int, val callback: (Event) -> Unit)
}

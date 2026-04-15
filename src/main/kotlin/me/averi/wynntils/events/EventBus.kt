package me.averi.wynntils.events

import net.neoforged.bus.api.Event
import net.neoforged.bus.api.ICancellableEvent
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.reflect.KClass

object EventBus {
  private val listeners = ConcurrentHashMap<KClass<out Event>, CopyOnWriteArrayList<(Event) -> Unit>>()

  fun <T : Event> subscribe(type: KClass<T>, listener: (T) -> Unit) {
    val typedListeners = listeners.computeIfAbsent(type) { CopyOnWriteArrayList() }

    @Suppress("UNCHECKED_CAST") typedListeners.add(listener as ((Event) -> Unit))
  }

  inline fun <reified T : Event> subscribe(noinline listener: (T) -> Unit) = subscribe(T::class, listener)

  fun <T : Event> publish(event: T): Boolean {
    listeners[event::class]?.forEach { listener ->
      listener(event)
      if (event is ICancellableEvent && event.isCanceled) return true
    }

    return false
  }
}

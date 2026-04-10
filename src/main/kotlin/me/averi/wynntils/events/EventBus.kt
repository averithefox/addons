package me.averi.wynntils.events

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.reflect.KClass

interface Event

interface CancellableEvent : Event {
  var isCancelled: Boolean

  fun cancel() {
    isCancelled = true
  }
}

fun interface EventSubscription {
  fun unsubscribe()
}

object EventBus {
  private val listeners =
    ConcurrentHashMap<KClass<out Event>, CopyOnWriteArrayList<(Event) -> Unit>>()

  fun <T : Event> subscribe(type: KClass<T>, listener: T.() -> Unit): EventSubscription {
    val typedListeners = listeners.computeIfAbsent(type) { CopyOnWriteArrayList() }
    @Suppress("UNCHECKED_CAST")
    val wrappedListener: (Event) -> Unit = { event -> (event as T).listener() }

    typedListeners.add(wrappedListener)

    return EventSubscription {
      typedListeners.remove(wrappedListener)
      if (typedListeners.isEmpty()) {
        listeners.remove(type, typedListeners)
      }
    }
  }

  inline fun <reified T : Event> subscribe(noinline listener: T.() -> Unit): EventSubscription =
    subscribe(T::class, listener)

  fun <T : Event> publish(event: T): T {
    listeners[event::class]?.forEach { listener ->
      listener(event)
      if (event is CancellableEvent && event.isCancelled) {
        return event
      }
    }

    return event
  }
}

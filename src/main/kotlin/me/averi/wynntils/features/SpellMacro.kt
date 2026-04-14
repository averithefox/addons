package me.averi.wynntils.features

import me.averi.wynntils.FoxAddons.KEY_CATEGORY
import me.averi.wynntils.Spell
import me.averi.wynntils.constants.WynnClass
import me.averi.wynntils.events.EventBus.subscribe
import me.averi.wynntils.events.StartAttackEvent
import me.averi.wynntils.events.UseItemEvent
import me.averi.wynntils.utils.attack
import me.averi.wynntils.utils.getWynnClass
import me.averi.wynntils.utils.localPlayer
import me.averi.wynntils.utils.use
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.minecraft.client.KeyMapping
import java.util.concurrent.LinkedBlockingQueue
import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.ExperimentalAtomicApi

@OptIn(ExperimentalAtomicApi::class)
object SpellMacro {
  const val DELAY = 80L

  private val spell1Key = KeyMapping("key.foxaddons.spell1", -1, KEY_CATEGORY)
  private val spell2Key = KeyMapping("key.foxaddons.spell2", -1, KEY_CATEGORY)
  private val spell3Key = KeyMapping("key.foxaddons.spell3", -1, KEY_CATEGORY)
  private val spell4Key = KeyMapping("key.foxaddons.spell4", -1, KEY_CATEGORY)

  private val running = AtomicBoolean(true)
  private val queue = LinkedBlockingQueue<Boolean>()

  fun register() {
    ClientLifecycleEvents.CLIENT_STARTED.register { start() }
    ClientLifecycleEvents.CLIENT_STOPPING.register { stop() }

    KeyBindingHelper.registerKeyBinding(spell1Key)
    KeyBindingHelper.registerKeyBinding(spell2Key)
    KeyBindingHelper.registerKeyBinding(spell3Key)
    KeyBindingHelper.registerKeyBinding(spell4Key)

    ClientTickEvents.END_CLIENT_TICK.register {
      checkSpellKey(spell1Key, Spell.FIRST)
      checkSpellKey(spell2Key, Spell.SECOND)
      checkSpellKey(spell3Key, Spell.THIRD)
      checkSpellKey(spell4Key, Spell.FOURTH)
    }

    subscribe<StartAttackEvent> {
      if (!queue.isEmpty()) cancel()
    }

    subscribe<UseItemEvent> {
      if (!player.isLocalPlayer) return@subscribe
      if (!queue.isEmpty()) cancel()
    }
  }

  private fun checkSpellKey(key: KeyMapping, spell: Spell) {
    if (!key.consumeClick()) return
    if (!queue.isEmpty()) return
    queue.addAll(spell.clicks.toTypedArray())
  }

  private fun click(rightClick: Boolean) {
    if (!rightClick) {
      localPlayer?.attack()
    } else {
      localPlayer?.use()
    }
  }

  fun start() = Thread {
    while (running.load()) {
      try {
        click(queue.take() xor (localPlayer?.getWynnClass() == WynnClass.ARCHER))
        Thread.sleep(DELAY)
      } catch (e: InterruptedException) {
        e.printStackTrace()
      }
    }
  }.start()

  fun stop() = running.store(false)
}

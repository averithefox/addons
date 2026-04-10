package me.averi.wynntils

import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.minecraft.client.KeyMapping
import net.minecraft.resources.Identifier

object FoxAddons : ClientModInitializer {
  val isDebug by lazy { java.lang.Boolean.getBoolean("foxaddons.debug") }

  val KEY_CATEGORY = KeyMapping.Category.register(Identifier.fromNamespaceAndPath("foxaddons", "main"))

  val spell1Key = KeyMapping("key.foxaddons.spell1", -1, KEY_CATEGORY)
  val spell2Key = KeyMapping("key.foxaddons.spell2", -1, KEY_CATEGORY)
  val spell3Key = KeyMapping("key.foxaddons.spell3", -1, KEY_CATEGORY)
  val spell4Key = KeyMapping("key.foxaddons.spell4", -1, KEY_CATEGORY)

  override fun onInitializeClient() {
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

    ClientLifecycleEvents.CLIENT_STARTED.register { ClickQueue.start() }
    ClientLifecycleEvents.CLIENT_STOPPING.register { ClickQueue.stop() }
  }

  private fun checkSpellKey(key: KeyMapping, spell: Spell) {
    if (key.consumeClick()) ClickQueue.add(spell.clicks)
  }
}

package me.averi.wynntils

import me.averi.wynntils.features.SpellMacro
import me.averi.wynntils.features.TotemTimer
import net.fabricmc.api.ClientModInitializer
import net.minecraft.client.KeyMapping
import net.minecraft.resources.Identifier

object FoxAddons : ClientModInitializer {
  val isDebug by lazy { java.lang.Boolean.getBoolean("foxaddons.debug") }

  val KEY_CATEGORY = KeyMapping.Category.register(Identifier.fromNamespaceAndPath("foxaddons", "main"))

  override fun onInitializeClient() {
    SpellMacro.register()
    TotemTimer.register()
  }
}

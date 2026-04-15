package me.averi.wynntils

import me.averi.wynntils.features.Debug
import me.averi.wynntils.features.TotemMinifier
import me.averi.wynntils.features.TotemTimer
import net.fabricmc.api.ClientModInitializer

object FoxAddons : ClientModInitializer {
  val isDebug by lazy { java.lang.Boolean.getBoolean("foxaddons.debug") }

  override fun onInitializeClient() {
    TotemTimer.register()
    Debug.register()
    TotemMinifier.register()
  }
}

package me.averi.wynntils.dx

import com.wynntils.core.consumers.features.Configurable

interface Configurable : Configurable {
  val settings: MutableList<Setting<*>>

  fun registerSetting(setting: Setting<*>) {
    settings.add(setting)
  }
}

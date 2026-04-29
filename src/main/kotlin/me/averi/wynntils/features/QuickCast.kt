package me.averi.wynntils.features

import com.wynntils.features.combat.QuickCastFeature
import me.averi.wynntils.dx.Configurable
import me.averi.wynntils.dx.Setting

object QuickCast : QuickCastFeature(), Configurable {
  override val settings: MutableList<Setting<*>> = ArrayList()

  val swing by Setting(false)
}

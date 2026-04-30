package me.averi.wynntils.features

import com.wynntils.core.persisted.config.Category
import com.wynntils.core.persisted.config.ConfigCategory
import com.wynntils.features.combat.QuickCastFeature
import me.averi.wynntils.dx.Configurable
import me.averi.wynntils.dx.Setting

@ConfigCategory(Category.COMBAT)
object QuickCast : QuickCastFeature(), Configurable {
  override val settings: MutableList<Setting<*>> = ArrayList()

  val swing by Setting(false)
}

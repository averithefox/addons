package me.averi.wynntils.dx

import com.wynntils.core.consumers.features.Feature
import com.wynntils.core.consumers.features.ProfileDefault
import com.wynntils.core.persisted.config.Category
import net.minecraft.client.resources.language.I18n

abstract class Feature(profileDefault: ProfileDefault) : Feature(profileDefault), Configurable {
  override val settings: MutableList<Setting<*>> = ArrayList()

  val isEnabled: Boolean
    @JvmName($$"fox$isEnabled") get() = userEnabled.get()

  override fun getCategory(): Category {
    return Category.valueOf("FOX_ADDONS")
  }

  override fun getTranslation(keySuffix: String, vararg parameters: Any): String {
    return I18n.get("feature.foxaddons.$translationKeyName.$keySuffix", parameters)
  }
}

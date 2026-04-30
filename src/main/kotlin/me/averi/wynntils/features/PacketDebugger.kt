package me.averi.wynntils.features

import com.wynntils.core.persisted.config.Category
import com.wynntils.core.persisted.config.ConfigCategory
import com.wynntils.features.debug.PacketDebuggerFeature
import me.averi.wynntils.dx.Configurable
import me.averi.wynntils.dx.Setting

@ConfigCategory(Category.DEBUG)
object PacketDebugger : PacketDebuggerFeature(), Configurable {
  override val settings: MutableList<Setting<*>> = ArrayList()

  val packetDirection by Setting(PacketDirection.BOTH)

  enum class PacketDirection {
    BOTH, CLIENTBOUND, SERVERBOUND
  }
}

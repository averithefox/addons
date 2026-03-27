package me.averi.skyblock.dungeons

import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.util.datafix.fixes.BlockStateData
import net.minecraft.world.level.block.state.BlockState

object LegacyBlockIds {
  private val modernToLegacyId: Map<String, Int> by lazy(::buildModernToLegacyIdMap)

  fun getLegacyBlockId(state: BlockState): Int {
    val blockName = BuiltInRegistries.BLOCK.getKey(state.block).toString()
    return modernToLegacyId[blockName] ?: 0
  }

  private fun buildModernToLegacyIdMap(): Map<String, Int> {
    val modernToLegacy = HashMap<String, Int>(256)
    for (legacyStateId in 0..4095) {
      val modernName = BlockStateData.upgradeBlock(legacyStateId) ?: continue
      val legacyBlockId = legacyStateId ushr 4
      modernToLegacy.putIfAbsent(modernName, legacyBlockId)
    }
    return modernToLegacy
  }
}

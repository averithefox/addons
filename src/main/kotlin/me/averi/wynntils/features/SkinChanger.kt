package me.averi.wynntils.features

import com.wynntils.core.consumers.features.ProfileDefault
import com.wynntils.models.gear.type.GearType
import me.averi.wynntils.dx.Feature
import me.averi.wynntils.dx.ItemModelSetting
import me.averi.wynntils.events.EventBus.subscribe
import me.averi.wynntils.events.ItemModelResolveEvent
import me.averi.wynntils.utils.customModel
import me.averi.wynntils.utils.mc
import me.averi.wynntils.utils.modelRange
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items

object SkinChanger : Feature(ProfileDefault.DISABLED) {
  private val spearModel by ItemModelSetting(GearType.SPEAR.modelRange, itemSlot = EquipmentSlot.MAINHAND)
  private val wandModel by ItemModelSetting(GearType.WAND.modelRange, itemSlot = EquipmentSlot.MAINHAND)
  private val daggerModel by ItemModelSetting(GearType.DAGGER.modelRange, itemSlot = EquipmentSlot.MAINHAND)
  private val bowModel by ItemModelSetting(GearType.BOW.modelRange, itemSlot = EquipmentSlot.MAINHAND)
  private val relikModel by ItemModelSetting(GearType.RELIK.modelRange, itemSlot = EquipmentSlot.MAINHAND)
  private val helmetModel by ItemModelSetting(GearType.HELMET.modelRange, itemSlot = EquipmentSlot.HEAD)

  private val mainHandItem = ItemStack(Items.POTION)
  private val headItem = ItemStack(Items.POTION)

  init {
    subscribe(::onItemModelResolve)
  }

  fun onItemModelResolve(e: ItemModelResolveEvent) {
    if (!isEnabled) return
    val localPlayer = mc.player ?: return
    val isHelmet = ItemStack.matches(e.itemStack, localPlayer.getItemBySlot(EquipmentSlot.HEAD))
    if (!ItemStack.matches(e.itemStack, localPlayer.mainHandItem) && !isHelmet) return

    val (model, item) = when (e.itemStack.customModel ?: return) {
      in GearType.SPEAR.modelRange -> spearModel to mainHandItem
      in GearType.WAND.modelRange -> wandModel to mainHandItem
      in GearType.DAGGER.modelRange -> daggerModel to mainHandItem
      in GearType.BOW.modelRange -> bowModel to mainHandItem
      in GearType.RELIK.modelRange -> relikModel to mainHandItem
      in GearType.HELMET.modelRange -> helmetModel to headItem
      else -> return
    }
    item.customModel = model ?: return
    e.returnValue = item
  }
}

package me.averi.skyblock.dungeons

import com.mojang.blaze3d.pipeline.RenderPipeline
import com.mojang.blaze3d.platform.DepthTestFunction
import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.blaze3d.vertex.VertexConsumer
import me.averi.skyblock.FoxAddons.isDebug
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents
import net.minecraft.ChatFormatting
import net.minecraft.client.Minecraft
import net.minecraft.client.multiplayer.ClientLevel
import net.minecraft.client.player.LocalPlayer
import net.minecraft.client.renderer.RenderPipelines
import net.minecraft.client.renderer.RenderStateShard
import net.minecraft.client.renderer.RenderType
import net.minecraft.core.BlockPos
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import net.minecraft.network.protocol.game.ClientboundSoundPacket
import net.minecraft.network.protocol.game.ClientboundTakeItemEntityPacket
import net.minecraft.resources.ResourceLocation
import net.minecraft.sounds.SoundEvents
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.Vec3
import net.minecraft.world.scores.DisplaySlot
import net.minecraft.world.scores.PlayerScoreEntry
import org.slf4j.LoggerFactory
import kotlin.math.floor

object DungeonSecretWaypoints {
  private const val DEBUG_LOG_PREFIX = "[FA]"

  private val logger = LoggerFactory.getLogger(DungeonSecretWaypoints::class.java)

  private const val DUNGEON_MIN = 0
  private const val DUNGEON_MAX = 5
  private const val RESCAN_INTERVAL_TICKS = 10
  private const val MAX_ITEM_MATCH_DISTANCE_SQ = 25.0
  private const val MAX_BAT_MATCH_DISTANCE_SQ = 144.0
  private const val MAX_SKULL_MATCH_DISTANCE_SQ = 16.0

  private const val BOX_FILL_ALPHA_SCALE = 0.52f

  private var lastWorldId: Int = 0
  private var wasInDungeon = false
  private var useKeyWasDown = false
  private var tickCounter = 0
  private var lastScannedComponent: RoomComponent? = null

  private val collectedSecrets = linkedSetOf<BlockPos>()
  private var currentRoom: DungeonRoomInstance? = null
  private var lastAnnouncedRoomKey: String? = null

  private var lastScanFailureSignature: String? = null
  private var lastOutOfGridDebugTick: Int = -10_000

  private val secretFilledThroughWallsType: RenderType by lazy {
    val pipeline = RenderPipelines.register(
      RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET)
        .withLocation(ResourceLocation.fromNamespaceAndPath("fox-addons", "pipeline/secret_filled_box"))
        .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST).build(),
    )
    RenderType.create(
      "fox_secret_filled_box",
      1536,
      false,
      true,
      pipeline,
      RenderType.CompositeState.builder().setLayeringState(RenderStateShard.VIEW_OFFSET_Z_LAYERING)
        .createCompositeState(false),
    )
  }

  fun init() {
    ClientTickEvents.END_CLIENT_TICK.register(::onClientTick)
    WorldRenderEvents.BEFORE_DEBUG_RENDER.register(::renderSecrets)
    registerDebugHud()
  }

  private fun registerDebugHud() {
    HudElementRegistry.attachElementBefore(
      VanillaHudElements.CHAT,
      ResourceLocation.fromNamespaceAndPath("fox-addons", "hud/debug_room_name"),
    ) { graphics, _ ->
      debugHudRoomLine()?.let { line ->
        val client = Minecraft.getInstance()
        graphics.drawString(client.font, line, 4, 4, -1, true)
      }
    }
  }

  fun computeStandingStartCoreHash(): Int? {
    val client = Minecraft.getInstance()
    val level = client.level ?: return null
    val player = client.player ?: return null
    val component = RoomComponent.fromWorld(player.x, player.z) ?: return null
    return buildRoomCore(level, component.centerX, component.centerZ).hash
  }

  private fun debugHudRoomLine(): Component? {
    if (!isDebug) return null
    val client = Minecraft.getInstance()
    val level = client.level ?: return null
    if (!isInDungeon(level)) return null
    val room = currentRoom ?: return Component.literal("Room: ")
      .append(Component.literal("—").withStyle(ChatFormatting.DARK_GRAY))
    val complete = DungeonRoomRepository.hasCompleteCores(room.data)
    val nameColor = if (complete) ChatFormatting.GREEN else ChatFormatting.RED
    return Component.literal("Room: ").append(Component.literal(room.data.name).withStyle(nameColor))
  }

  fun handleItemPickup(packet: ClientboundTakeItemEntityPacket) {
    val client = Minecraft.getInstance()
    val player = client.player ?: return
    if (packet.playerId != player.id) return
    val level = client.level ?: return
    val entity = level.getEntity(packet.itemId) as? ItemEntity ?: return
    markNearestSecret(
      types = setOf(SecretType.ITEM), origin = entity.position(), maxDistanceSq = MAX_ITEM_MATCH_DISTANCE_SQ
    )
  }

  fun handleSound(packet: ClientboundSoundPacket) {
    if (packet.sound.value() != SoundEvents.BAT_DEATH) return

    markNearestSecret(
      types = setOf(SecretType.BAT),
      origin = Vec3(packet.x, packet.y, packet.z),
      maxDistanceSq = MAX_BAT_MATCH_DISTANCE_SQ,
    )
  }

  private fun onClientTick(client: Minecraft) {
    val level = client.level
    val player = client.player
    if (level == null || player == null) {
      clearState()
      return
    }

    val worldId = System.identityHashCode(level)
    if (worldId != lastWorldId) {
      clearState()
      lastWorldId = worldId
    }

    val inDungeon = isInDungeon(level)
    if (!inDungeon) {
      wasInDungeon = false
      currentRoom = null
      lastScannedComponent = null
      lastAnnouncedRoomKey = null
      handleUseKey(client, player, level)
      return
    }

    if (!wasInDungeon) {
      collectedSecrets.clear()
    }
    wasInDungeon = true

    tickCounter++
    val component = RoomComponent.fromWorld(player.x, player.z)
    if (component == null) {
      currentRoom = null
      lastScannedComponent = null
      if (tickCounter - lastOutOfGridDebugTick >= 80) {
        lastOutOfGridDebugTick = tickCounter
        announceDebugOutOfGrid(client, player)
      }
      handleUseKey(client, player, level)
      return
    }

    val shouldRescan = component != lastScannedComponent || tickCounter % RESCAN_INTERVAL_TICKS == 0
    if (shouldRescan) {
      when (val outcome = scanCurrentRoom(level, component)) {
        is RoomScanOutcome.Ok -> {
          lastScanFailureSignature = null
          val roomKey = roomDebugKey(outcome.room)
          if (roomKey != lastAnnouncedRoomKey) {
            lastAnnouncedRoomKey = roomKey
            announceSecretCounts(client, outcome.room)
          }
          currentRoom = outcome.room
        }

        is RoomScanOutcome.Failed -> {
          currentRoom = null
          maybeAnnounceScanFailure(client, component, outcome)
        }
      }
      lastScannedComponent = component
    }

    handleUseKey(client, player, level)
  }

  private fun roomDebugKey(room: DungeonRoomInstance): String =
    "${room.data.name}|${room.cornerX}|${room.cornerZ}|${room.rotation}"

  private fun emitDungeonDebug(client: Minecraft, plainBody: String, chatLine: MutableComponent) {
    logger.info("$DEBUG_LOG_PREFIX $plainBody")
    if (isDebug) client.gui.chat.addMessage(chatLine)
  }

  private fun announceSecretCounts(client: Minecraft, room: DungeonRoomInstance) {
    val counts = room.secrets.groupingBy { it.type }.eachCount()
    val compact = SecretType.entries.joinToString(" ") { type ->
      val abbrev = when (type) {
        SecretType.CHEST -> "c"
        SecretType.ITEM -> "i"
        SecretType.WITHER -> "w"
        SecretType.BAT -> "b"
        SecretType.REDSTONE_KEY -> "r"
      }
      "$abbrev=${counts[type] ?: 0}"
    }
    val plain = "${room.data.name} $compact Σ${room.secrets.size}"
    val chat: MutableComponent = Component.literal("$DEBUG_LOG_PREFIX ").withStyle(ChatFormatting.GOLD)
      .append(Component.literal(room.data.name).withStyle(ChatFormatting.AQUA))
      .append(Component.literal(" $compact ").withStyle(ChatFormatting.GRAY))
      .append(Component.literal("Σ${room.secrets.size}").withStyle(ChatFormatting.DARK_GRAY))
    emitDungeonDebug(client, plain, chat)
  }

  private fun announceDebugOutOfGrid(client: Minecraft, player: LocalPlayer) {
    val pos = player.blockPosition()
    val plain = "off dungeon grid ${pos.x} ${pos.y} ${pos.z}"
    val chat: MutableComponent = Component.literal("$DEBUG_LOG_PREFIX ").withStyle(ChatFormatting.GOLD)
      .append(Component.literal("off dungeon grid ").withStyle(ChatFormatting.RED))
      .append(Component.literal("${pos.x} ${pos.y} ${pos.z}").withStyle(ChatFormatting.WHITE))
    emitDungeonDebug(client, plain, chat)
  }

  private fun maybeAnnounceScanFailure(client: Minecraft, component: RoomComponent, failed: RoomScanOutcome.Failed) {
    val sig = "${component.arrayX},${component.arrayZ}|${failed.stage}|${failed.detail}"
    if (sig == lastScanFailureSignature) return
    lastScanFailureSignature = sig
    val plain = "${failed.stage}: ${failed.detail}"
    val chat: MutableComponent = Component.literal("$DEBUG_LOG_PREFIX ").withStyle(ChatFormatting.GOLD)
      .append(Component.literal("${failed.stage}: ").withStyle(ChatFormatting.RED))
      .append(Component.literal(failed.detail).withStyle(ChatFormatting.GRAY))
    emitDungeonDebug(client, plain, chat)
  }

  private fun handleUseKey(client: Minecraft, player: LocalPlayer, level: ClientLevel) {
    val down = client.options.keyUse.isDown
    val pressed = down && !useKeyWasDown
    useKeyWasDown = down
    if (!pressed) return

    val hit = client.hitResult as? BlockHitResult ?: return
    if (hit.type != net.minecraft.world.phys.HitResult.Type.BLOCK) return

    val blockPos = hit.blockPos
    val state = level.getBlockState(blockPos)
    when {
      state.`is`(Blocks.CHEST) || state.`is`(Blocks.TRAPPED_CHEST) -> {
        markNearestSecret(
          types = setOf(SecretType.CHEST),
          origin = blockPos.center,
          maxDistanceSq = 0.0,
        )
      }

      state.`is`(Blocks.PLAYER_HEAD) || state.`is`(Blocks.PLAYER_WALL_HEAD) -> {
        markNearestSecret(
          types = setOf(SecretType.WITHER, SecretType.REDSTONE_KEY),
          origin = blockPos.center,
          maxDistanceSq = MAX_SKULL_MATCH_DISTANCE_SQ,
        )
      }

      state.`is`(Blocks.LEVER) -> {
        // Lever interaction is useful room context, but the lever itself is not a secret marker.
        if (currentRoom?.contains(player.blockPosition()) == false) {
          currentRoom = null
        }
      }
    }
  }

  private fun renderSecrets(context: WorldRenderContext) {
    val room = currentRoom ?: return
    val matrices = context.matrices() ?: return
    val consumers = context.consumers()
    val cameraPos = Minecraft.getInstance().gameRenderer.mainCamera.position

    val renderType = secretFilledThroughWallsType
    for (secret in room.secrets) {
      if (collectedSecrets.contains(secret.worldPos)) continue
      val box = secret.box.move(-cameraPos.x, -cameraPos.y, -cameraPos.z)
      val buffer = consumers.getBuffer(renderType)
      addFilledBoxVertices(
        matrices,
        buffer,
        box.minX,
        box.minY,
        box.minZ,
        box.maxX,
        box.maxY,
        box.maxZ,
        secret.type.red,
        secret.type.green,
        secret.type.blue,
        secret.type.alpha * BOX_FILL_ALPHA_SCALE,
      )
    }
  }

  // Emits 24 QUADS vertices (6 faces × 4 vertices) for a filled axis-aligned box.
  // Matches the technique used by odtheking/Odin PrimitiveRenderer.addChainedFilledBoxVertices.
  private fun addFilledBoxVertices(
    poseStack: PoseStack,
    buffer: VertexConsumer,
    minX: Double, minY: Double, minZ: Double,
    maxX: Double, maxY: Double, maxZ: Double,
    r: Float, g: Float, b: Float, a: Float,
  ) {
    val pose = poseStack.last()
    val x0 = minX.toFloat()
    val y0 = minY.toFloat()
    val z0 = minZ.toFloat()
    val x1 = maxX.toFloat()
    val y1 = maxY.toFloat()
    val z1 = maxZ.toFloat()

    buffer.addVertex(pose, x0, y0, z0).setColor(r, g, b, a)
    buffer.addVertex(pose, x0, y0, z1).setColor(r, g, b, a)
    buffer.addVertex(pose, x0, y1, z1).setColor(r, g, b, a)
    buffer.addVertex(pose, x0, y1, z0).setColor(r, g, b, a)

    buffer.addVertex(pose, x1, y0, z1).setColor(r, g, b, a)
    buffer.addVertex(pose, x1, y0, z0).setColor(r, g, b, a)
    buffer.addVertex(pose, x1, y1, z0).setColor(r, g, b, a)
    buffer.addVertex(pose, x1, y1, z1).setColor(r, g, b, a)

    buffer.addVertex(pose, x0, y0, z0).setColor(r, g, b, a)
    buffer.addVertex(pose, x0, y1, z0).setColor(r, g, b, a)
    buffer.addVertex(pose, x1, y1, z0).setColor(r, g, b, a)
    buffer.addVertex(pose, x1, y0, z0).setColor(r, g, b, a)

    buffer.addVertex(pose, x1, y0, z1).setColor(r, g, b, a)
    buffer.addVertex(pose, x1, y1, z1).setColor(r, g, b, a)
    buffer.addVertex(pose, x0, y1, z1).setColor(r, g, b, a)
    buffer.addVertex(pose, x0, y0, z1).setColor(r, g, b, a)

    buffer.addVertex(pose, x0, y0, z0).setColor(r, g, b, a)
    buffer.addVertex(pose, x1, y0, z0).setColor(r, g, b, a)
    buffer.addVertex(pose, x1, y0, z1).setColor(r, g, b, a)
    buffer.addVertex(pose, x0, y0, z1).setColor(r, g, b, a)

    buffer.addVertex(pose, x0, y1, z1).setColor(r, g, b, a)
    buffer.addVertex(pose, x1, y1, z1).setColor(r, g, b, a)
    buffer.addVertex(pose, x1, y1, z0).setColor(r, g, b, a)
    buffer.addVertex(pose, x0, y1, z0).setColor(r, g, b, a)
  }

  private fun isInDungeon(level: ClientLevel): Boolean {
    val scoreboard = level.scoreboard
    val objective = scoreboard.getDisplayObjective(DisplaySlot.SIDEBAR) ?: return false
    val entries = scoreboard.listPlayerScores(objective)

    if (entries.isEmpty()) return false

    val lines = entries.map { entry -> getSidebarLine(scoreboard, entry) }.filter { it.isNotBlank() }.toList()

    return lines.any { line ->
      line.contains("The Catacombs", ignoreCase = true) || line.contains("Master Mode Catacombs", ignoreCase = true)
    }
  }

  private fun getSidebarLine(scoreboard: net.minecraft.world.scores.Scoreboard, entry: PlayerScoreEntry): String {
    val team = scoreboard.getPlayersTeam(entry.owner())
    val owner = entry.owner()
    val ownerLooksSynthetic = owner.startsWith("§") || owner.startsWith("#") || owner.all { it == ' ' }
    val ownerText = if (ownerLooksSynthetic) "" else owner
    val prefix = team?.playerPrefix?.string.orEmpty()
    val suffix = team?.playerSuffix?.string.orEmpty()
    val explicitDisplay = entry.display()?.string.orEmpty()
    val combined = explicitDisplay.ifBlank { prefix + ownerText + suffix }
    return stripFormatting(combined).trim()
  }

  private fun stripFormatting(text: String): String = text.replace(Regex("§."), "")

  private sealed class RoomScanOutcome {
    data class Ok(val room: DungeonRoomInstance) : RoomScanOutcome()
    data class Failed(val stage: String, val detail: String) : RoomScanOutcome()
  }

  private fun scanCurrentRoom(level: ClientLevel, start: RoomComponent): RoomScanOutcome {
    val startCore = buildRoomCore(level, start.centerX, start.centerZ)
    val visited = linkedSetOf<RoomComponent>()
    val roofHeights = mutableMapOf<RoomComponent, Int>()
    val queue = ArrayDeque<RoomComponent>()
    queue.add(start)

    val coreHashesTried = linkedSetOf<Int>()
    var matchedRoomData: DungeonRoomData? = null
    while (queue.isNotEmpty()) {
      val component = queue.removeFirst()
      if (!visited.add(component)) continue

      val roofHeight = getHighestBlock(level, component.centerX, component.centerZ) ?: continue
      roofHeights[component] = roofHeight

      if (matchedRoomData == null) {
        val built = if (component == start) startCore else buildRoomCore(level, component.centerX, component.centerZ)
        coreHashesTried.add(built.hash)
        matchedRoomData = DungeonRoomRepository.getRoomByCore(built.hash)
      }

      for (direction in directions) {
        val neighbor = component.offset(direction.roomDx, direction.roomDz) ?: continue
        if (isRoomExtension(level, component, roofHeight, direction)) {
          queue.add(neighbor)
        }
      }
    }

    if (roofHeights.isEmpty()) {
      return RoomScanOutcome.Failed(
        "void_columns",
        "no solid column (n=${visited.size} ~${start.centerX},${start.centerZ})",
      )
    }

    val roomData = matchedRoomData ?: return RoomScanOutcome.Failed(
      "no_room_data", "no core match @(${start.centerX},${start.centerZ}) hash=${startCore.hash}"
    )

    val roofHeight =
      roofHeights.values.maxOrNull() ?: return RoomScanOutcome.Failed("no_roof", "roof height aggregate failed")

    val rotationAndCorner = detectRotationAndCorner(level, visited, roofHeight) ?: return RoomScanOutcome.Failed(
      "no_blue_corner",
      blueCornerFailureDetail(visited, roofHeight),
    )

    return RoomScanOutcome.Ok(
      DungeonRoomInstance(
        data = roomData,
        components = visited,
        roofHeight = roofHeight,
        rotation = rotationAndCorner.rotation,
        cornerX = rotationAndCorner.cornerX,
        cornerZ = rotationAndCorner.cornerZ,
      ),
    )
  }

  private fun blueCornerFailureDetail(visited: Set<RoomComponent>, roofHeight: Int): String {
    val minX = visited.minOf(RoomComponent::centerX)
    val maxX = visited.maxOf(RoomComponent::centerX)
    val minZ = visited.minOf(RoomComponent::centerZ)
    val maxZ = visited.maxOf(RoomComponent::centerZ)
    val checks = listOf(
      Triple(0, minX - 15, minZ - 15),
      Triple(1, maxX + 15, minZ - 15),
      Triple(2, maxX + 15, maxZ + 15),
      Triple(3, minX - 15, maxZ + 15),
    ).joinToString(" ") { (_, cx, cz) -> "($cx,$roofHeight,$cz)" }
    return "no blue clay y=$roofHeight $checks (${visited.size} cells)"
  }

  private fun isRoomExtension(
    level: ClientLevel,
    component: RoomComponent,
    roofHeight: Int,
    direction: ScanDirection,
  ): Boolean {
    val midpointX = component.centerX + direction.midDx
    val midpointZ = component.centerZ + direction.midDz
    if (getHighestBlock(level, midpointX, midpointZ) == null) return false

    val block = level.getBlockState(BlockPos(midpointX, roofHeight, midpointZ))
    val blockAbove = level.getBlockState(BlockPos(midpointX, roofHeight + 1, midpointZ))
    val spawnDoorBlock = level.getBlockState(BlockPos(midpointX, 69, midpointZ))

    return !block.isAir && blockAbove.isAir && !spawnDoorBlock.`is`(Blocks.INFESTED_STONE_BRICKS)
  }

  private fun detectRotationAndCorner(
    level: ClientLevel,
    components: Set<RoomComponent>,
    roofHeight: Int,
  ): RotationAndCorner? {
    val minX = components.minOf(RoomComponent::centerX)
    val maxX = components.maxOf(RoomComponent::centerX)
    val minZ = components.minOf(RoomComponent::centerZ)
    val maxZ = components.maxOf(RoomComponent::centerZ)

    val corners = listOf(
      RotationAndCorner(0, minX - 15, minZ - 15),
      RotationAndCorner(1, maxX + 15, minZ - 15),
      RotationAndCorner(2, maxX + 15, maxZ + 15),
      RotationAndCorner(3, minX - 15, maxZ + 15),
    )

    return corners.firstOrNull { corner ->
      level.getBlockState(BlockPos(corner.cornerX, roofHeight, corner.cornerZ)).`is`(Blocks.BLUE_TERRACOTTA)
    }
  }

  private fun getHighestBlock(level: ClientLevel, x: Int, z: Int): Int? {
    for (y in level.maxY downTo level.minY) {
      val state = level.getBlockState(BlockPos(x, y, z))
      if (state.isAir || state.`is`(Blocks.GOLD_BLOCK)) continue
      return y
    }
    return null
  }

  private data class RoomCoreBuild(val hash: Int, val fingerprint: String)

  private fun buildRoomCore(level: ClientLevel, x: Int, z: Int): RoomCoreBuild {
    val segments = ArrayList<String>(129)
    for (y in 140 downTo 12) {
      val state = level.getBlockState(BlockPos(x, y, z))
      val segment = when {
        state.isAir -> "0"
        state.`is`(Blocks.CHEST) || state.`is`(Blocks.TRAPPED_CHEST) || state.`is`(Blocks.IRON_BARS) -> "0"
        else -> BuiltInRegistries.BLOCK.getKey(state.block).toString()
      }
      segments.add(segment)
    }
    val fingerprint = segments.joinToString(",")
    return RoomCoreBuild(fingerprint.hashCode(), fingerprint)
  }

  private fun markNearestSecret(
    types: Set<SecretType>, origin: Vec3, maxDistanceSq: Double
  ) {
    val room = currentRoom ?: return
    val candidate = room.secrets.filter { it.type in types && !collectedSecrets.contains(it.worldPos) }
      .map { secret -> secret to secret.center.distanceToSqr(origin) }
      .filter { (_, distanceSq) -> distanceSq <= maxDistanceSq }.minByOrNull { (_, distanceSq) -> distanceSq }

    if (candidate != null) {
      val pos = candidate.first.worldPos
      collectedSecrets.add(pos)

      val plain = "collected secret $types at ${pos.x} ${pos.y} ${pos.z} from ${candidate.second}"
      val chat: MutableComponent = Component.literal("$DEBUG_LOG_PREFIX ").withStyle(ChatFormatting.GOLD)
        .append(Component.literal("collected secret ").withStyle(ChatFormatting.BLUE))
        .append(Component.literal("$types ").withStyle(ChatFormatting.GRAY))
        .append(Component.literal("at ").withStyle(ChatFormatting.BLUE))
        .append(Component.literal("${pos.x} ${pos.y} ${pos.z} ").withStyle(ChatFormatting.WHITE))
        .append(Component.literal("from ").withStyle(ChatFormatting.BLUE))
        .append(Component.literal("${candidate.second}").withStyle(ChatFormatting.WHITE))
      emitDungeonDebug(Minecraft.getInstance(), plain, chat)

      return
    }
  }

  private fun clearState() {
    collectedSecrets.clear()
    currentRoom = null
    lastScannedComponent = null
    lastAnnouncedRoomKey = null
    lastScanFailureSignature = null
    useKeyWasDown = false
    tickCounter = 0
    wasInDungeon = false
    lastWorldId = 0
  }

  private data class RoomComponent(val arrayX: Int, val arrayZ: Int) {
    val centerX: Int
      get() = -185 + arrayX * 32

    val centerZ: Int
      get() = -185 + arrayZ * 32

    fun offset(dx: Int, dz: Int): RoomComponent? {
      val x = arrayX + dx
      val z = arrayZ + dz
      if (x !in DUNGEON_MIN..DUNGEON_MAX || z !in DUNGEON_MIN..DUNGEON_MAX) return null
      return RoomComponent(x, z)
    }

    companion object {
      fun fromWorld(worldX: Double, worldZ: Double): RoomComponent? {
        val arrayX = floor((worldX + 200.5) / 32.0).toInt()
        val arrayZ = floor((worldZ + 200.5) / 32.0).toInt()
        if (arrayX !in DUNGEON_MIN..DUNGEON_MAX || arrayZ !in DUNGEON_MIN..DUNGEON_MAX) return null
        return RoomComponent(arrayX, arrayZ)
      }
    }
  }

  private data class RotationAndCorner(
    val rotation: Int,
    val cornerX: Int,
    val cornerZ: Int,
  )

  private data class ScanDirection(
    val midDx: Int,
    val midDz: Int,
    val roomDx: Int,
    val roomDz: Int,
  )

  private data class DungeonRoomInstance(
    val data: DungeonRoomData,
    val components: Set<RoomComponent>,
    val roofHeight: Int,
    val rotation: Int,
    val cornerX: Int,
    val cornerZ: Int,
  ) {
    val secrets: List<ResolvedSecret> by lazy {
      data.secretCoords.flatMap { (type, positions) ->
        positions.map { relative ->
          val worldPos = toWorldPos(relative)
          ResolvedSecret(type, worldPos, type.toBox(worldPos))
        }
      }
    }

    fun contains(pos: BlockPos): Boolean = RoomComponent.fromWorld(pos.x.toDouble(), pos.z.toDouble()) in components

    private fun toWorldPos(relative: BlockPos): BlockPos {
      val rotated = rotate(relative, 4 - rotation)
      return BlockPos(cornerX + rotated.x, rotated.y, cornerZ + rotated.z)
    }
  }

  private data class ResolvedSecret(
    val type: SecretType,
    val worldPos: BlockPos,
    val box: AABB,
  ) {
    val center: Vec3 = Vec3.atCenterOf(worldPos)
  }

  private fun rotate(pos: BlockPos, degrees: Int): BlockPos {
    val normalized = ((degrees % 4) + 4) % 4
    return when (normalized) {
      0 -> pos
      1 -> BlockPos(pos.z, pos.y, -pos.x)
      2 -> BlockPos(-pos.x, pos.y, -pos.z)
      3 -> BlockPos(-pos.z, pos.y, pos.x)
      else -> pos
    }
  }

  private fun SecretType.toBox(pos: BlockPos): AABB = when (this) {
    SecretType.CHEST -> AABB(
      pos.x + 0.0625,
      pos.y.toDouble(),
      pos.z + 0.0625,
      pos.x + 0.9375,
      pos.y + 0.875,
      pos.z + 0.9375,
    )

    SecretType.ITEM, SecretType.WITHER, SecretType.REDSTONE_KEY -> AABB(
      pos.x + 0.25,
      pos.y.toDouble(),
      pos.z + 0.25,
      pos.x + 0.75,
      pos.y + 0.5,
      pos.z + 0.75,
    )

    SecretType.BAT -> AABB(
      pos.x + 0.25,
      pos.y + 0.25,
      pos.z + 0.25,
      pos.x + 0.75,
      pos.y + 0.75,
      pos.z + 0.75,
    )
  }

  private val directions = listOf(
    ScanDirection(midDx = 0, midDz = -16, roomDx = 0, roomDz = -1),
    ScanDirection(midDx = 16, midDz = 0, roomDx = 1, roomDz = 0),
    ScanDirection(midDx = 0, midDz = 16, roomDx = 0, roomDz = 1),
    ScanDirection(midDx = -16, midDz = 0, roomDx = -1, roomDz = 0),
  )
}

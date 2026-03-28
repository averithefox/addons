package me.averi.skyblock.dungeons

import com.mojang.blaze3d.platform.DepthTestFunction
import com.mojang.blaze3d.pipeline.RenderPipeline
import com.mojang.blaze3d.vertex.DefaultVertexFormat
import com.mojang.blaze3d.vertex.VertexFormat
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents
import net.minecraft.client.Minecraft
import net.minecraft.client.multiplayer.ClientLevel
import net.minecraft.client.player.LocalPlayer
import net.minecraft.client.renderer.RenderPipelines
import net.minecraft.client.renderer.RenderStateShard
import net.minecraft.client.renderer.RenderType
import net.minecraft.client.renderer.ShapeRenderer
import net.minecraft.resources.ResourceLocation
import net.minecraft.core.BlockPos
import net.minecraft.network.protocol.game.ClientboundSoundPacket
import net.minecraft.network.protocol.game.ClientboundTakeItemEntityPacket
import net.minecraft.sounds.SoundEvents
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.Vec3
import net.minecraft.world.scores.DisplaySlot
import net.minecraft.world.scores.PlayerScoreEntry
import kotlin.math.floor

object DungeonSecretWaypoints {
  private const val DUNGEON_MIN = 0
  private const val DUNGEON_MAX = 5
  private const val RESCAN_INTERVAL_TICKS = 10
  private const val MAX_ITEM_MATCH_DISTANCE_SQ = 25.0
  private const val MAX_BAT_MATCH_DISTANCE_SQ = 144.0
  private const val MAX_SKULL_MATCH_DISTANCE_SQ = 16.0

  private val blacklistedLegacyIds = setOf(54, 101)

  private var lastWorldId: Int = 0
  private var wasInDungeon = false
  private var useKeyWasDown = false
  private var tickCounter = 0
  private var lastScannedComponent: RoomComponent? = null
  private var lastSidebarRoomId: String? = null

  private val collectedSecrets = linkedSetOf<BlockPos>()
  private var currentRoom: DungeonRoomInstance? = null

  private val secretFilledThroughWallsType: RenderType by lazy {
    val pipeline = RenderPipelines.register(
      RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET)
        .withLocation(ResourceLocation.fromNamespaceAndPath("fox-addons", "pipeline/secret_filled_box"))
        .withVertexFormat(DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.TRIANGLE_STRIP)
        .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
        .withDepthWrite(false)
        .build(),
    )
    RenderType.create(
      "fox_secret_filled_box",
      1536,
      false,
      true,
      pipeline,
      RenderType.CompositeState.builder()
        .setLayeringState(RenderStateShard.VIEW_OFFSET_Z_LAYERING)
        .createCompositeState(false),
    )
  }

  fun init() {
    ClientTickEvents.END_CLIENT_TICK.register(::onClientTick)
    WorldRenderEvents.BEFORE_DEBUG_RENDER.register(::renderSecrets)
  }

  fun handleItemPickup(packet: ClientboundTakeItemEntityPacket) {
    val client = Minecraft.getInstance()
    val player = client.player ?: return
    if (packet.playerId != player.id) return

    val level = client.level ?: return
    val entity = level.getEntity(packet.itemId) as? ItemEntity ?: return
    markNearestSecret(
      types = setOf(SecretType.ITEM),
      origin = entity.position(),
      maxDistanceSq = MAX_ITEM_MATCH_DISTANCE_SQ,
      fallback = entity.blockPosition(),
    )
  }

  fun handleSound(packet: ClientboundSoundPacket) {
    if (packet.sound.value() != SoundEvents.BAT_DEATH) return

    markNearestSecret(
      types = setOf(SecretType.BAT),
      origin = Vec3(packet.x, packet.y, packet.z),
      maxDistanceSq = MAX_BAT_MATCH_DISTANCE_SQ,
      fallback = null,
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
      handleUseKey(client, player, level)
      return
    }

    val shouldRescan = component != lastScannedComponent || tickCounter % RESCAN_INTERVAL_TICKS == 0
    if (shouldRescan) {
      currentRoom = scanCurrentRoom(level, component)
      lastScannedComponent = component
    }

    handleUseKey(client, player, level)
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
          origin = Vec3.atCenterOf(blockPos),
          maxDistanceSq = null,
          fallback = blockPos,
        )
      }

      state.`is`(Blocks.PLAYER_HEAD) || state.`is`(Blocks.PLAYER_WALL_HEAD) -> {
        markNearestSecret(
          types = setOf(SecretType.WITHER, SecretType.REDSTONE_KEY),
          origin = Vec3.atCenterOf(blockPos),
          maxDistanceSq = MAX_SKULL_MATCH_DISTANCE_SQ,
          fallback = blockPos,
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
      ShapeRenderer.addChainedFilledBoxVertices(
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
        secret.type.alpha * 0.28f,
      )
    }
  }

  private fun isInDungeon(level: ClientLevel): Boolean {
    val scoreboard = level.scoreboard
    val objective = scoreboard.getDisplayObjective(DisplaySlot.SIDEBAR) ?: return false
    val entries = scoreboard.listPlayerScores(objective)

    if (entries.isEmpty()) return false

    val lines =
      entries.asSequence().map { entry -> getSidebarLine(scoreboard, entry) }.filter { it.isNotBlank() }.toList()
    lastSidebarRoomId = extractSidebarRoomId(lines)

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

  private fun extractSidebarRoomId(lines: List<String>): String? {
    val primaryPattern = Regex("^\\d{2}/\\d{2}/\\d{2}\\s+\\S+\\s+(-?\\d+,-?\\d+)$")
    val secondaryPattern = Regex("^\\d{2}/\\d{2}/\\d{2}\\s+\\S+\\s+(-?\\d+,-?\\d+)\\b")
    return lines.asSequence().mapNotNull { line ->
      primaryPattern.matchEntire(line)?.groupValues?.get(1) ?: secondaryPattern.find(line)?.groupValues?.get(1)
    }.firstOrNull()
  }

  private fun scanCurrentRoom(level: ClientLevel, start: RoomComponent): DungeonRoomInstance? {
    val visited = linkedSetOf<RoomComponent>()
    val roofHeights = mutableMapOf<RoomComponent, Int>()
    val queue = ArrayDeque<RoomComponent>()
    queue.add(start)

    var matchedRoomData: DungeonRoomData? = null
    while (queue.isNotEmpty()) {
      val component = queue.removeFirst()
      if (!visited.add(component)) continue

      val roofHeight = getHighestBlock(level, component.centerX, component.centerZ) ?: continue
      roofHeights[component] = roofHeight

      if (matchedRoomData == null) {
        matchedRoomData = lastSidebarRoomId?.let(DungeonRoomRepository::getRoomById)
        if (matchedRoomData == null) {
          matchedRoomData = DungeonRoomRepository.getRoomByCore(
            computeRoomCore(level, component.centerX, component.centerZ),
          )
        }
      }

      for (direction in directions) {
        val neighbor = component.offset(direction.roomDx, direction.roomDz) ?: continue
        if (isRoomExtension(level, component, roofHeight, direction)) {
          queue.add(neighbor)
        }
      }
    }

    val roomData = matchedRoomData ?: return null
    val roofHeight = roofHeights.values.maxOrNull() ?: return null
    val rotationAndCorner = detectRotationAndCorner(level, visited, roofHeight) ?: return null

    return DungeonRoomInstance(
      data = roomData,
      components = visited,
      roofHeight = roofHeight,
      rotation = rotationAndCorner.rotation,
      cornerX = rotationAndCorner.cornerX,
      cornerZ = rotationAndCorner.cornerZ,
    )
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

  private fun computeRoomCore(level: ClientLevel, x: Int, z: Int): Int {
    val blockIds = StringBuilder()
    for (y in 140 downTo 12) {
      val legacyId = LegacyBlockIds.getLegacyBlockId(level.getBlockState(BlockPos(x, y, z)))
      if (legacyId in blacklistedLegacyIds) {
        blockIds.append('0')
      } else {
        blockIds.append(legacyId)
      }
    }
    return blockIds.toString().hashCode()
  }

  private fun markNearestSecret(
    types: Set<SecretType>,
    origin: Vec3,
    maxDistanceSq: Double?,
    fallback: BlockPos?,
  ) {
    val room = currentRoom ?: return
    val candidate = room.secrets.asSequence().filter { it.type in types && !collectedSecrets.contains(it.worldPos) }
      .map { secret -> secret to secret.center.distanceToSqr(origin) }
      .filter { (_, distanceSq) -> maxDistanceSq == null || distanceSq <= maxDistanceSq }
      .minByOrNull { (_, distanceSq) -> distanceSq }?.first

    if (candidate != null) {
      collectedSecrets.add(candidate.worldPos)
      return
    }

    if (fallback != null) {
      collectedSecrets.add(fallback.immutable())
    }
  }

  private fun clearState() {
    collectedSecrets.clear()
    currentRoom = null
    lastScannedComponent = null
    useKeyWasDown = false
    tickCounter = 0
    wasInDungeon = false
    lastWorldId = 0
    lastSidebarRoomId = null
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

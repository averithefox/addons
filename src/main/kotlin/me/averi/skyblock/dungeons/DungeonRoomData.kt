package me.averi.skyblock.dungeons

import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonParser
import net.minecraft.core.BlockPos
import java.io.InputStreamReader
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.inputStream
import kotlin.io.path.isRegularFile

enum class SecretType(
  val key: String,
  val red: Float,
  val green: Float,
  val blue: Float,
  val alpha: Float = 0.9f,
) {
  CHEST("chest", 0f, 1f, 0f), ITEM("item", 0f, 0.35f, 1f), WITHER("wither", 1f, 0f, 1f), BAT(
    "bat", 0f, 1f, 0f
  ),
  REDSTONE_KEY("redstone_key", 1f, 0f, 0f), ;

  companion object {
    private val byKey = entries.associateBy(SecretType::key)

    fun fromKey(key: String): SecretType? = byKey[key]
  }
}

data class DungeonRoomData(
  val id: List<String>,
  val name: String,
  val shape: String,
  val secrets: Int,
  val cores: List<Int>,
  val secretCoords: Map<SecretType, List<BlockPos>>,
)

object DungeonRoomRepository {
  private const val BUNDLED_ROOMDATA = "data/fox-addons/dungeons/roomdata.json"

  @Volatile
  private var fileOverride: Path? = null

  private var roomsById: Map<String, DungeonRoomData> = emptyMap()
  private var roomsByCore: Map<Int, DungeonRoomData> = emptyMap()
  private var roomsList: List<DungeonRoomData> = emptyList()

  init {
    reload()
  }

  fun getRoomDataPath(): Path? = fileOverride

  fun setRoomDataPath(path: Path?) {
    fileOverride = path
    reload()
  }

  fun reload() {
    val rooms = loadRoomsFromActiveSource()
    roomsList = rooms
    roomsById = rooms.flatMap { room -> room.id.map { id -> id to room } }.toMap()
    roomsByCore = rooms.flatMap { room -> room.cores.map { core -> core to room } }.toMap()
  }

  fun getRoomByCore(core: Int): DungeonRoomData? = roomsByCore[core]

  fun allRooms(): List<DungeonRoomData> = roomsList

  fun roomNamesSortedDistinct(): List<String> = roomsList.map { it.name }.distinct().sorted()

  fun hasCompleteCores(room: DungeonRoomData): Boolean = room.cores.isNotEmpty()

  fun appendCoreForRoomName(roomName: String, core: Int): AppendCoreResult {
    val path = fileOverride ?: return AppendCoreResult.NoFileConfigured
    try {
      path.toAbsolutePath().parent?.let { parent ->
        if (!parent.exists()) {
          Files.createDirectories(parent)
        }
      }
      if (!path.exists() || !path.isRegularFile()) {
        val bundled =
          javaClass.classLoader.getResourceAsStream(BUNDLED_ROOMDATA)
            ?: return AppendCoreResult.IoError("Missing bundled $BUNDLED_ROOMDATA")
        bundled.use { input ->
          Files.copy(input, path, java.nio.file.StandardCopyOption.REPLACE_EXISTING)
        }
      }

      val text = Files.readString(path, Charsets.UTF_8)
      val root = JsonParser.parseString(text).asJsonArray
      var matchedObjects = 0
      var addedToAny = false

      for (element in root) {
        val obj = element.asJsonObject
        if (obj.get("name")?.asString != roomName) {
          continue
        }
        matchedObjects++
        var cores = obj.getAsJsonArray("cores")
        if (cores == null) {
          cores = JsonArray()
          obj.add("cores", cores)
        }
        if (cores.any { it.isJsonPrimitive && it.asInt == core }) {
          continue
        }
        cores.add(core)
        addedToAny = true
      }

      when {
        matchedObjects == 0 -> return AppendCoreResult.RoomNameNotFound(roomName)
        !addedToAny -> return AppendCoreResult.CoreAlreadyPresent(roomName, core)
        else -> {
          val gson = GsonBuilder().disableHtmlEscaping().create()
          Files.writeString(path, gson.toJson(root), Charsets.UTF_8)
          reload()
          return AppendCoreResult.Ok(matchedObjects)
        }
      }
    } catch (e: Exception) {
      return AppendCoreResult.IoError(e.message ?: e.toString())
    }
  }

  sealed class AppendCoreResult {
    data class Ok(val roomsUpdated: Int) : AppendCoreResult()

    data class CoreAlreadyPresent(val roomName: String, val core: Int) : AppendCoreResult()

    data class RoomNameNotFound(val roomName: String) : AppendCoreResult()

    data object NoFileConfigured : AppendCoreResult()

    data class IoError(val message: String) : AppendCoreResult()
  }

  private fun loadRoomsFromActiveSource(): List<DungeonRoomData> {
    val path = fileOverride
    val stream =
      if (path != null && path.exists() && path.isRegularFile()) {
        path.inputStream()
      } else {
        javaClass.classLoader.getResourceAsStream(BUNDLED_ROOMDATA)
          ?: error("Missing dungeon room resource: $BUNDLED_ROOMDATA")
      }

    InputStreamReader(stream, Charsets.UTF_8).use { reader ->
      val root = JsonParser.parseReader(reader).asJsonArray
      return root.map { element ->
        val obj = element.asJsonObject
        val secretsByType = buildMap {
          val secretObj = obj.getAsJsonObject("secret_coords")
          for ((key, value) in secretObj.entrySet()) {
            val type = SecretType.fromKey(key) ?: continue
            put(
              type,
              value.asJsonArray.map { coord ->
                val arr = coord.asJsonArray
                BlockPos(arr[0].asInt, arr[1].asInt, arr[2].asInt)
              },
            )
          }
        }

        DungeonRoomData(
          id = obj.getAsJsonArray("id")?.map { it.asString } ?: emptyList(),
          name = obj.get("name").asString,
          shape = obj.get("shape").asString,
          secrets = obj.get("secrets").asInt,
          cores = obj.getAsJsonArray("cores")?.map { it.asInt } ?: emptyList(),
          secretCoords = secretsByType,
        )
      }
    }
  }
}

package me.averi.skyblock.dungeons

import com.mojang.brigadier.arguments.StringArgumentType
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.minecraft.ChatFormatting
import net.minecraft.client.Minecraft
import net.minecraft.network.chat.Component
import java.nio.file.Paths

object DebugCommands {
  fun register() {
    ClientCommandRegistrationCallback.EVENT.register { dispatcher, _ ->
      val roomdataBranch = literal("roomdata").then(
        literal("path").then(
          literal("set").then(
            argument("file", StringArgumentType.greedyString()).executes { ctx ->
              val raw = StringArgumentType.getString(ctx, "file").trim()
              val path = Paths.get(raw).toAbsolutePath().normalize()
              DungeonRoomRepository.setRoomDataPath(path)
              ctx.source.sendFeedback(
                Component.literal("Roomdata file: ").withStyle(ChatFormatting.GRAY)
                  .append(Component.literal(path.toString()).withStyle(ChatFormatting.AQUA)),
              )
              1
            },
          ),
        ).then(
          literal("clear").executes {
            DungeonRoomRepository.setRoomDataPath(null)
            it.source.sendFeedback(
              Component.literal("Roomdata override cleared; using bundled roomdata.json.")
                .withStyle(ChatFormatting.GRAY),
            )
            1
          },
        ).then(
          literal("get").executes {
            val path = DungeonRoomRepository.getRoomDataPath()
            if (path == null) {
              it.source.sendFeedback(
                Component.literal("Roomdata path: ").withStyle(ChatFormatting.GRAY).append(
                  Component.literal("(bundled resource, no file override)").withStyle(ChatFormatting.DARK_AQUA)
                ),
              )
            } else {
              it.source.sendFeedback(
                Component.literal("Roomdata path: ").withStyle(ChatFormatting.GRAY)
                  .append(Component.literal(path.toString()).withStyle(ChatFormatting.AQUA)),
              )
            }
            1
          },
        ),
      ).then(
        literal("addcore").then(
          argument("room_name", StringArgumentType.greedyString()).suggests { _, builder ->
            val remaining = builder.remaining.trim().lowercase()
            for (name in DungeonRoomRepository.roomNamesSortedDistinct()) {
              if (name.lowercase().startsWith(remaining)) {
                builder.suggest(name)
              }
            }
            builder.buildFuture()
          }.executes { ctx ->
            val roomName = StringArgumentType.getString(ctx, "room_name").trim()
            if (roomName.isEmpty()) {
              ctx.source.sendError(Component.literal("Specify a room name."))
              return@executes 0
            }
            val core = DungeonSecretWaypoints.computeStandingStartCoreHash() ?: run {
              ctx.source.sendError(
                Component.literal("Could not compute core (need loaded world, dungeon, on-grid position)."),
              )
              return@executes 0
            }
            when (val result = DungeonRoomRepository.appendCoreForRoomName(roomName, core)) {
              is DungeonRoomRepository.AppendCoreResult.Ok -> {
                ctx.source.sendFeedback(
                  Component.literal("Added core ").withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(core.toString()).withStyle(ChatFormatting.WHITE))
                    .append(Component.literal(" to ").withStyle(ChatFormatting.GRAY))
                    .append(Component.literal(roomName).withStyle(ChatFormatting.AQUA)).append(
                      Component.literal(" (${result.roomsUpdated} object(s))").withStyle(ChatFormatting.DARK_GRAY),
                    ),
                )
              }

              is DungeonRoomRepository.AppendCoreResult.CoreAlreadyPresent -> {
                ctx.source.sendFeedback(
                  Component.literal("Core ").withStyle(ChatFormatting.YELLOW)
                    .append(Component.literal(core.toString()).withStyle(ChatFormatting.WHITE))
                    .append(Component.literal(" already listed for ").withStyle(ChatFormatting.YELLOW))
                    .append(Component.literal(roomName).withStyle(ChatFormatting.AQUA)),
                )
              }

              is DungeonRoomRepository.AppendCoreResult.RoomNameNotFound -> {
                ctx.source.sendError(Component.literal("No room named \"$roomName\" in roomdata."))
              }

              DungeonRoomRepository.AppendCoreResult.NoFileConfigured -> {
                ctx.source.sendError(
                  Component.literal("Set a roomdata file first: ").append(
                    Component.literal("/foxaddons roomdata path set <file>").withStyle(ChatFormatting.AQUA)
                  ),
                )
              }

              is DungeonRoomRepository.AppendCoreResult.IoError -> {
                ctx.source.sendError(Component.literal(result.message))
              }
            }
            1
          },
        ),
      ).then(
        literal("status").executes {
          val client = Minecraft.getInstance()
          val lines = buildRoomCoreStatusMessages()
          val maxLines = 80
          val truncated = if (lines.size > maxLines) {
            lines.take(maxLines) + listOf(
              Component.literal("… (${lines.size - maxLines} more lines omitted)").withStyle(ChatFormatting.DARK_GRAY),
            )
          } else {
            lines
          }
          for (line in truncated) {
            client.gui.chat.addMessage(line)
          }
          1
        },
      )

      dispatcher.register(literal("foxaddons").then(roomdataBranch))
      dispatcher.register(literal("fa").then(roomdataBranch))
    }
  }

  private fun buildRoomCoreStatusMessages(): List<Component> {
    val rooms = DungeonRoomRepository.allRooms()
    val rows = rooms.map { room ->
      val ok = DungeonRoomRepository.hasCompleteCores(room)
      ok to room
    }.sortedWith(compareBy<Pair<Boolean, DungeonRoomData>> { it.first }.thenBy { it.second.name })
    val incomplete = rows.count { !it.first }
    val header = Component.literal("[FA] room cores: ").withStyle(ChatFormatting.GOLD)
      .append(Component.literal("$incomplete incomplete").withStyle(ChatFormatting.RED))
      .append(Component.literal(", ").withStyle(ChatFormatting.GRAY))
      .append(Component.literal("${rooms.size - incomplete} complete").withStyle(ChatFormatting.GREEN))
    val rowLines = rows.map { (ok, room) ->
      val mark = if (ok) "✓" else "✗"
      val accent = if (ok) ChatFormatting.GREEN else ChatFormatting.RED
      Component.literal(" $mark ").withStyle(accent).append(Component.literal(room.name).withStyle(accent))
        .append(Component.literal(" (").withStyle(ChatFormatting.DARK_GRAY))
        .append(Component.literal(room.shape).withStyle(ChatFormatting.GRAY))
        .append(Component.literal(")").withStyle(ChatFormatting.DARK_GRAY))
    }
    return listOf(header) + rowLines
  }
}

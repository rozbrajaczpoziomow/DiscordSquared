package futbol.rozbrajacz.discordsquared

import dev.kord.common.entity.Permission
import dev.kord.common.entity.Permissions
import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.behavior.interaction.respondPublic
import dev.kord.core.entity.Member
import dev.kord.core.entity.interaction.GuildChatInputCommandInteraction
import futbol.rozbrajacz.discordsquared.DiscordSquared.fmt

object CommandHandler {
	val commands: HashMap<String, Command> = hashMapOf()

	private val actions: Map<String, CommandAction> = mapOf(
		"message_reply" to CommandAction(1) { arguments ->
			respondPublic {
				content = arguments[0].fmt(
					"onlinePlayerCount" to DiscordSquared.server.currentPlayerCount,
					"onlinePlayers" to DiscordSquared.server.playerList.onlinePlayerNames.joinToString(", "),
					"maxPlayerCount" to DiscordSquared.server.maxPlayers
				)
			}
		},
		"run_command" to CommandAction(1) { arguments ->
			DiscordSquared.server.commandManager.executeCommand(DiscordSquared.server, arguments[0])
		}
	)

	private val permissions: Map<String, Permission> = mapOf(
		"create_instant_invite" to Permission.CreateInstantInvite,
		"kick_members" to Permission.KickMembers,
		"ban_members" to Permission.BanMembers,
		"administrator" to Permission.Administrator,
		"manage_channels" to Permission.ManageChannels,
		"manage_guild" to Permission.ManageGuild,
		"add_reactions" to Permission.AddReactions,
		"view_audit_log" to Permission.ViewAuditLog,
		"priority_speaker" to Permission.PrioritySpeaker,
		"stream" to Permission.Stream,
		"view_channel" to Permission.ViewChannel,
		"send_messages" to Permission.SendMessages,
		"send_tts_messages" to Permission.SendTTSMessages,
		"manage_messages" to Permission.ManageMessages,
		"embed_links" to Permission.EmbedLinks,
		"attach_files" to Permission.AttachFiles,
		"read_message_history" to Permission.ReadMessageHistory,
		"mention_everyone" to Permission.MentionEveryone,
		"use_external_emojis" to Permission.UseExternalEmojis,
		"view_guild_insights" to Permission.ViewGuildInsights,
		"connect" to Permission.Connect,
		"speak" to Permission.Speak,
		"mute_members" to Permission.MuteMembers,
		"deafen_members" to Permission.DeafenMembers,
		"move_members" to Permission.MoveMembers,
		"use_vad" to Permission.UseVAD,
		"change_nickname" to Permission.ChangeNickname,
		"manage_nicknames" to Permission.ManageNicknames,
		"manage_roles" to Permission.ManageRoles,
		"manage_webhooks" to Permission.ManageWebhooks,
		"manage_guild_expressions" to Permission.ManageGuildExpressions,
		"use_application_commands" to Permission.UseApplicationCommands,
		"request_to_speak" to Permission.RequestToSpeak,
		"manage_events" to Permission.ManageEvents,
		"manage_threads" to Permission.ManageThreads,
		"create_public_threads" to Permission.CreatePublicThreads,
		"create_private_threads" to Permission.CreatePrivateThreads,
		"use_external_stickers" to Permission.UseExternalStickers,
		"send_messages_in_threads" to Permission.SendMessagesInThreads,
		"use_embedded_activities" to Permission.UseEmbeddedActivities,
		"moderate_members" to Permission.ModerateMembers,
		"view_creator_monetization_analytics" to Permission.ViewCreatorMonetizationAnalytics,
		"use_soundboard" to Permission.UseSoundboard,
		"create_guild_expressions" to Permission.CreateGuildExpressions,
		"create_events" to Permission.CreateEvents,
		"use_external_sounds" to Permission.UseExternalSounds,
		"send_voice_messages" to Permission.SendVoiceMessages
	)

	fun init() {
		if(!ConfigHandler.discordBot.commands.enabled)
			return
		for(command in ConfigHandler.discordBot.commands.commands)
			parseCommand(command)
	}

	private fun parseCommand(command: String) {
		// since we allow \; I'd rather just write a custom splitter
		val split = mutableListOf<String>()
		null.let { // variable context separation
			val parse = command + if(command.last() != ';') ';' else ""
			var prev = 0
			for(i in parse.indices)
				if(parse[i] == ';' && (i == 0 || parse[i - 1] != '\\')) {
					val str = parse.substring(prev, i)
					if(str.isNotEmpty())
						split.add(str.replace("\\;", ";"))
					prev = i + 1
				}
		}

		// name;desc;perm is minimum
		if(split.size < 2) {
			DiscordSquared.logger.error("Invalid command, expected at least 2 sections but found ${split.size}: $command")
			return
		}

		val name = split.removeAt(0)
		val description = split.removeAt(0)
		val permissionStr = split.removeAt(0)
		val boundActionDispatchers = mutableListOf<suspend GuildChatInputCommandInteraction.() -> Unit>()

		// parse permissions
		val users = hashSetOf<ULong>()
		val roles = hashSetOf<ULong>()
		var perms = Permissions()
		var wildcard = false

		if(permissionStr.isEmpty()) {
			DiscordSquared.logger.error("Invalid command, no permissions provided, no one would be able to execute it: $command")
			return
		}

		permissionStr.split(',').forEach {
			if(it == "*")
				wildcard = true
			else if(it.startsWith("u:"))
				users.add(it.substringAfter(':').toULong())
			else if(it.startsWith("r:"))
				roles.add(it.substringAfter(':').toULong())
			else if(it.startsWith("p:"))
				perms += permissions[it.substringAfter(':')]!!
			else {
				DiscordSquared.logger.error("Invalid command, couldn't parse permission '$it': $command")
				return
			}
		}
		val commandPermission = CommandPermission(users, roles, perms, wildcard)

		// parse actions
		while(split.isNotEmpty()) {
			val actionStr = split.removeAt(0)
			val action = actions.getOrElse(actionStr) {
				DiscordSquared.logger.error("Invalid command, couldn't find action '$actionStr': $command")
				return
			}
			if(action.argumentCount > split.size) {
				DiscordSquared.logger.error("Invalid command, action $actionStr requires ${action.argumentCount} arguments but only ${split.size} were found: $command")
				return
			}
			val arguments = split.subList(0, action.argumentCount).toTypedArray()
			boundActionDispatchers.add { action.dispatcher(this, arguments) }
			repeat(action.argumentCount) { split.removeFirst() }
		}

		commands[name] = Command(description, commandPermission, boundActionDispatchers)
	}

	class Command(val description: String, val commandPermission: CommandPermission, val boundActionDispatchers: List<suspend GuildChatInputCommandInteraction.() -> Unit>) {
		fun execute(ctx: GuildChatInputCommandInteraction) {
			ExecutionThread.execute {
				if(!commandPermission.validate(ctx.user))
					ctx.respondEphemeral {
						content = "You don't have enough permissions to execute this command"
					}
				else
					boundActionDispatchers.forEach {
						it(ctx)
					}
			}
		}
	}

	class CommandAction(val argumentCount: Int, val dispatcher: suspend GuildChatInputCommandInteraction.(arguments: Array<String>) -> Unit)

	class CommandPermission(val users: HashSet<ULong>, val roles: HashSet<ULong>, val perms: Permissions, val wildcard: Boolean) {
		fun validate(on: Member) = wildcard || users.contains(on.id.value) || on.roleIds.any { roles.contains(it.value) } || (perms.values.isNotEmpty() && on.permissions!!.contains(perms))
	}
}

package futbol.rozbrajacz.discordsquared

import net.minecraftforge.common.config.Config
import net.minecraftforge.common.config.ConfigManager
import net.minecraftforge.fml.client.event.ConfigChangedEvent
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@Config(modid = Reference.MODID, name = Reference.MODID)
object ConfigHandler {
	@JvmField
	@Config.RequiresMcRestart
	@Config.Name("a Enable ${Reference.MOD_NAME}")
	var enabled = false

	@JvmField
	@Config.Name("b Discord Bot Settings")
	@Config.Comment("Options for a Discord Bot for 2-way communication")
	val discordBot = DiscordBot()

	class DiscordBot {
		@JvmField
		@Config.RequiresMcRestart
		@Config.Name("a Token")
		@Config.Comment(
			"Required for 2-way communication, can be obtained at https://discord.dev if you know what you're doing, the message content intent is required",
			"Can be left empty if all you need is Minecraft -> Discord communication"
		)
		var token = ""

		@JvmField
		@Config.RequiresMcRestart
		@Config.Name("b Channel ID")
		@Config.Comment("Required if you want to use a Discord bot")
		var channelID = ""

		@JvmField
		@Config.Name("c Presence")
		@Config.Comment("Bot presence configuration")
		val presence = Presence()

		class Presence {
			@JvmField
			@Config.Name("a Enabled")
			@Config.Comment("Enable presence")
			var enabled = true

			@JvmField
			@Config.Name("b Status")
			@Config.Comment("Valid values are: online, idle, dnd, invisible")
			var status = "idle"

			@JvmField
			@Config.Name("c Activity")
			@Config.Comment("Valid values are: playing, listening, watching, competing (empty/invalid to not set any activity)")
			var activity = "playing"

			@JvmField
			@Config.Name("d Activity Text")
			@Config.Comment("Available substitutions same as status text")
			var activityText = "Minecraft"

			@JvmField
			@Config.Name("e Status Text")
			@Config.Comment("Available substitutions: {onlinePlayers}, {maxPlayers}") // TODO: what else????
			var text = "{onlinePlayers}/{maxPlayers} players online"
		}
	}

	@JvmField
	@Config.Name("c Webhook Settings")
	@Config.Comment("Options for webhook Minecraft -> Discord communication")
	val webhook = Webhook()

	class Webhook() {
		@JvmField
		@Config.RequiresMcRestart
		@Config.Name("a Webhook URL")
		@Config.Comment("Can be left empty if you want to use a Discord bot, required otherwise.")
		var url = ""

		@JvmField
		@Config.Name("b Player Avatar URL")
		@Config.Comment(
			"For player-related messages, the avatar to use for the message posted to the Discord channel",
			"Available substitutions: {username}, {uuid}"
		)
		var playerAvatarURL = "https://api.mineatar.io/head/{uuid}"

		@JvmField
		@Config.Name("c Webhook System Username")
		@Config.Comment(
			"For system-related messages, used for server start/stop messages",
			"To set the avatar for system messages, edit the webhook settings in Discord"
		)
		var systemUsername = "Server"
	}

	@JvmField
	@Config.Name("d Chat Messages")
	@Config.Comment(
		"Minecraft -> Discord",
		"Player chat message configuration"
	)
	val chatMessages = ChatMessages()

	class ChatMessages {
		@JvmField
		@Config.Name("a Enabled")
		@Config.Comment("Send chat messages over to Discord")
		var enabled = true

		@JvmField
		@Config.Name("b Format")
		@Config.Comment("Message format, available substitutions: {username}, {uuid}, {message}")
		var format = "{message}"
	}

	@JvmField
	@Config.Name("e Message Format")
	@Config.Comment(
		"Discord -> Minecraft",
		"Message format, available substitutions: {username}, {displayName}, {message}, {userId}, {messageId}"
	)
	var messageFormat = "§5<{displayName}>§r {message}"

	@JvmField
	@Config.Name("f Allow Mentions")
	@Config.Comment(
		"Minecraft -> Discord",
		"Whether to allow pinging users/@everyone/…"
	)
	var allowMentions = false

	@JvmField
	@Config.Name("g Join/Leave Messages")
	@Config.Comment(
		"Minecraft -> Discord",
		"Player join/leave message configuration"
	)
	val joinLeaveMessages = JoinLeaveMessages()

	class JoinLeaveMessages {
		@JvmField
		@Config.Name("a Join - Enabled")
		@Config.Comment("Send player join messages over to Discord")
		var joinEnabled = true

		@JvmField
		@Config.Name("b Join - Message Format")
		@Config.Comment("Message format, available substitutions: {username}, {uuid}, {newOnlineCount}")
		var joinFormat = "{username} joined the game"

		@JvmField
		@Config.Name("c Leave - Enabled")
		@Config.Comment("Send player leave messages over to Discord")
		var leaveEnabled = true

		@JvmField
		@Config.Name("d Leave - Message Format")
		@Config.Comment("Message format, available substitutions: {username}, {uuid}, {newOnlineCount}")
		var leaveFormat = "{username} left the game"
	}

	@JvmField
	@Config.Name("h Death Messages")
	@Config.Comment(
		"Minecraft -> Discord",
		"Player death message configuration"
	)
	val deathMessages = DeathMessages()

	class DeathMessages {
		@JvmField
		@Config.Name("a Enabled")
		@Config.Comment("Send death messages over to Discord")
		var enabled = true

		@JvmField
		@Config.Name("b Format")
		@Config.Comment("Message format, available substitutions: {username}, {uuid}, {deathReason}")
		var format = "{username} {deathReason}"
	}

	@JvmField
	@Config.Name("i Server Start/Stop Messages")
	@Config.Comment(
		"Minecraft -> Discord",
		"Server start/stop message configuration"
	)
	val serverStartStopMessages = StartStopMessages()

	class StartStopMessages {
		@JvmField
		@Config.Name("a Server Start - Enabled")
		@Config.Comment("Send server start messages over to Discord")
		var startEnabled = true

		@JvmField
		@Config.Name("b Server Start - Message")
		@Config.Comment("Message to send when the server starts up and the bot logs in")
		var startMessage = "Server started"

		@JvmField
		@Config.Name("c Server Stop - Enabled")
		@Config.Comment("Send server stop messages over to Discord")
		var stopEnabled = true

		@JvmField
		@Config.Name("d Server Stop - Message")
		@Config.Comment("Message to send when the server starts stopping")
		var stopMessage = "Server stopped"
	}

	@Mod.EventBusSubscriber(modid = Reference.MODID)
	object ConfigEventHandler {
		@SubscribeEvent
		@JvmStatic
		fun onConfigChangedEvent(event: ConfigChangedEvent.OnConfigChangedEvent) {
			if(event.modID == Reference.MODID)
				ConfigManager.sync(Reference.MODID, Config.Type.INSTANCE)
		}
	}
}

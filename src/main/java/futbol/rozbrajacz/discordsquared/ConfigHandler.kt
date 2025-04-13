package futbol.rozbrajacz.discordsquared

import com.cleanroommc.configanytime.ConfigAnytime
import net.minecraftforge.common.config.Config
import net.minecraftforge.common.config.ConfigManager
import net.minecraftforge.fml.client.event.ConfigChangedEvent
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@Config(modid = Reference.MODID, name = Reference.MODID)
object ConfigHandler {
	@JvmField
	@Config.RequiresMcRestart
	@Config.Name("a. Enable ${Reference.MOD_NAME}")
	var enabled = false

	@JvmField
	@Config.Name("b. Discord Bot Settings")
	@Config.Comment("Options for a Discord Bot for 2-way communication")
	val discordBot = DiscordBot()

	class DiscordBot {
		@JvmField
		@Config.RequiresMcRestart
		@Config.Name("a. Token")
		@Config.Comment(
			"Required for 2-way communication, can be obtained at https://discord.dev if you know what you're doing, the message content intent is required",
			"Can be left empty if all you need is Minecraft -> Discord communication"
		)
		var token = ""

		@JvmField
		@Config.RequiresMcRestart
		@Config.Name("b. Channel ID")
		@Config.Comment("Required if you want to use a Discord bot")
		var channelID = ""
	}

	@JvmField
	@Config.Name("c. Webhook Settings")
	@Config.Comment("Options for a Webhook for Minecraft -> Discord communication only")
	val webhook = Webhook()

	class Webhook() {
		@JvmField
		@Config.RequiresMcRestart
		@Config.Name("a. Webhook URL")
		@Config.Comment(
			"Can be left empty if you want to use a Discord bot, required otherwise."
		)
		var url = ""
	}

	@JvmField
	@Config.RequiresMcRestart
	@Config.Name("d. Message Format")
	@Config.Comment(
		"Discord -> Minecraft",
		"Message format, available substitutions: {username}, {displayName}, {message}, {userId}, {messageId}"
	)
	var messageFormat = "<[D] {displayName}> {message}"

	@JvmField
	@Config.RequiresMcRestart
	@Config.Name("e. Allow Mentions")
	@Config.Comment(
		"Minecraft -> Discord",
		"Whether to allow pinging users/@everyone/…"
	)
	var allowMentions = true

	@JvmField
	@Config.RequiresMcRestart
	@Config.Name("f. Join/Leave Messages")
	@Config.Comment(
		"Minecraft -> Discord",
		"Whether send player join/leave messages"
	)
	var joinLeaveMessages = true

	@JvmField
	@Config.RequiresMcRestart
	@Config.Name("g. Death Messages")
	@Config.Comment(
		"Minecraft -> Discord",
		"Whether send player death messages"
	)
	var deathMessages = true

	@Mod.EventBusSubscriber(modid = Reference.MODID)
	object ConfigEventHandler {
		@SubscribeEvent
		@JvmStatic
		fun onConfigChangedEvent(event: ConfigChangedEvent.OnConfigChangedEvent) {
			if(event.modID == Reference.MODID)
				ConfigManager.sync(Reference.MODID, Config.Type.INSTANCE)
		}
	}

	init {
		ConfigAnytime.register(ConfigHandler::class.java)
	}
}

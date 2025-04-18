package futbol.rozbrajacz.discordsquared

import dev.kord.common.entity.PresenceStatus
import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.core.event.Event
import dev.kord.core.event.gateway.ReadyEvent
import dev.kord.core.event.interaction.GuildChatInputCommandInteractionCreateEvent
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.core.on
import dev.kord.gateway.Intent
import dev.kord.gateway.PrivilegedIntent
import dev.kord.rest.builder.message.allowedMentions
import dev.kord.rest.service.RestClient
import futbol.rozbrajacz.discordsquared.DiscordSquared.fmt
import kotlinx.coroutines.flow.first
import net.minecraft.entity.EntityLivingBase
import net.minecraft.util.text.TextComponentString

object BotHandler {
	private const val WEBHOOK_NAME = "dscsquared_webhook" // Username cannot contain "discord"

	val hasBot = ConfigHandler.discordBot.token.trim().isNotEmpty()
	lateinit var kord: Kord // only non-null when `hasBot` and post init()
		private set
	lateinit var rest: RestClient // always non-null post init()
		private set
	var webhook: WebhookData? = null // nullable
		private set
	var updatePresence = true

	suspend fun init() {
		// if we got a webhook specified, no need to create another one
		val webhookUrl = ConfigHandler.webhook.url.trim()
		if(webhookUrl.isNotEmpty())
			webhook = WebhookData(webhookUrl)

		if(!hasBot) {
			rest = RestClient("")
			postInit()
			return
		}

		val stringID = ConfigHandler.discordBot.channelID.trim()

		if(stringID.isEmpty()) {
			DiscordSquared.logger.error("channelID is not defined in the config, bailing out!")
			return
		}

		val channelID = stringID.toULong()
		var guildID: ULong?  // initialised at Ready

		kord = Kord(ConfigHandler.discordBot.token.trim())
		rest = kord.rest
		kord.on<MessageCreateEvent> {
			if(message.author?.isBot == true || message.webhookId != null || message.channelId.value != channelID || message.content.isEmpty())
				return@on

			val message = ConfigHandler.messageFormat.fmt(
				"username" to (message.author?.username ?: "?"),
				"displayName" to (message.getAuthorAsMemberOrNull()?.effectiveName ?: "?"),
				"message" to message.content,
				"userId" to (message.author?.id?.value ?: "?"),
				"messageId" to message.id.value
			)

			DiscordSquared.server.playerList.sendMessage(TextComponentString(message), false)
		}

		var alreadyLogged = false

		kord.on<ReadyEvent> {
			if(alreadyLogged)
				return@on

			alreadyLogged = true
			DiscordSquared.logger.info("Bot logged in - ${self.tag}")

			// no webhook specified by the user, try to find one before creating it
			if(webhook == null)
				kord.rest.webhook.getChannelWebhooks(Snowflake(channelID)).firstOrNull { it.name == WEBHOOK_NAME }?.let {
					webhook = WebhookData(it.id.value, it.token.value ?: "")
				}

			// no dice
			if(webhook == null)
				kord.rest.webhook.createWebhook(Snowflake(channelID), WEBHOOK_NAME) {
					reason = "${Reference.MOD_NAME} automatic webhook creation"
				}.let {
					webhook = WebhookData(it.id.value, it.token.value ?: "")
				}

			postInit()
			updatePresence()
			CommandHandler.init()

			guildID = kord.guilds.first { it.channelIds.any { id -> channelID == id.value } }.id.value

			if(CommandHandler.commands.isNotEmpty()) {
				CommandHandler.commands.entries.forEach { (name, command) ->
					kord.createGuildChatInputCommand(Snowflake(guildID), name, command.description)
				}

				kord.on<GuildChatInputCommandInteractionCreateEvent> {
					// require the command be executed in the correct server
					// we technically only register it in the correct server, but doesn't hurt to double-check
					if(interaction.invokedCommandGuildId?.value == guildID)
						CommandHandler.commands.get(interaction.invokedCommandName)?.execute(interaction)
				}
			}
		}

		// good enough
		kord.on<Event> {
			updatePresence()
		}

		kord.login {
			@OptIn(PrivilegedIntent::class)
			intents += Intent.MessageContent
		}
	}

	private fun postInit() {
		errorIfNoWebhook()
		if(ConfigHandler.serverStartStopMessages.startEnabled)
			postSystemMessage(ConfigHandler.serverStartStopMessages.startMessage)
	}

	private suspend fun updatePresence() {
		if(!ConfigHandler.discordBot.presence.enabled || !updatePresence)
			return

		kord.editPresence {
			status = PresenceStatus.from(ConfigHandler.discordBot.presence.status)
			val fmt = { str: String -> str.fmt("maxPlayers" to DiscordSquared.server.maxPlayers, "onlinePlayers" to DiscordSquared.server.playerList.currentPlayerCount) }
			val activityText = fmt(ConfigHandler.discordBot.presence.activityText)
			when(ConfigHandler.discordBot.presence.activity) {
				"playing" -> playing(activityText)
				"listening" -> listening(activityText)
				"watching" -> watching(activityText)
				"competing" -> competing(activityText)
			}
			state = fmt(ConfigHandler.discordBot.presence.text)
		}
		updatePresence = false
	}

	private fun errorIfNoWebhook() {
		if(webhook != null)
			return

		if(hasBot)
			DiscordSquared.logger.error("Bot failed to create (or find) a webhook, either specify a webhook url in the config, create a webhook with the name $WEBHOOK_NAME, or give the bot enough permissions to create a webhook by itself.")
		else
			DiscordSquared.logger.error("No webhook url specified in the config and no bot token provided to create one.")
	}

	fun postSystemMessage(message: String) =
		postWebhookMessage(message, ConfigHandler.webhook.systemUsername, null)

	fun postPlayerMessage(player: EntityLivingBase, message: String) =
		postWebhookMessage(message, player.name, ConfigHandler.webhook.playerAvatarURL.fmt(
			"username" to player.name,
			"uuid" to player.uniqueID
		))

	private fun postWebhookMessage(message: String, user: String, avatar: String?) {
		if(webhook == null)
			return

		ExecutionThread.execute {
			rest.webhook.executeWebhook(Snowflake(webhook!!.id), webhook!!.token, false, null) {
				content = message
				username = user
				suppressEmbeds = true
				avatarUrl = avatar
				if(!ConfigHandler.allowMentions)
					allowedMentions {}
			}
		}
	}

	class WebhookData {
		val id: ULong
		val token: String

		// https://discord.com/api/webhooks/{id}/{token}
		constructor(url: String) {
			if(!url.contains("discord.com/api/webhooks/"))
				throw Error("Provided webhook url is invalid: $url")
			val split = url.split('/').filter { it.isNotEmpty() }
			token = split.last()
			id = split[split.lastIndex - 1].toULong()
		}

		constructor(id: ULong, token: String) {
			this.id = id
			this.token = token
		}
	}
}

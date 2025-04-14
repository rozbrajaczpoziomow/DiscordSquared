package futbol.rozbrajacz.discordsquared

import dev.kord.common.entity.PresenceStatus
import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.core.event.Event
import dev.kord.core.event.gateway.ReadyEvent
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.core.on
import dev.kord.gateway.Intent
import dev.kord.gateway.PrivilegedIntent
import dev.kord.rest.builder.message.allowedMentions
import futbol.rozbrajacz.discordsquared.DiscordSquared.fmt
import kotlinx.coroutines.runBlocking
import net.minecraft.entity.EntityLivingBase
import net.minecraft.util.text.TextComponentString

object BotHandler {
	private const val WEBHOOK_NAME = "dscsquared_webhook" // Username cannot contain "discord"

	var hasBot = ConfigHandler.discordBot.token.trim().isNotEmpty()
		private set
	lateinit var kord: Kord
		private set
	var webhook: WebhookData? = null
		private set
	var updatePresence = true

	suspend fun init() {
		// if we got a webhook specified, no need to create another one
		val webhookUrl = ConfigHandler.webhook.url.trim()
		if(webhookUrl.isNotEmpty())
			webhook = WebhookData(webhookUrl)

		if(!hasBot) {
			postInit()
			return
		}

		val stringID = ConfigHandler.discordBot.channelID.trim()

		if(stringID.isEmpty()) {
			DiscordSquared.logger.error("channelID is not defined in the config, bailing out!")
			return
		}

		val channelID = stringID.toULong()

		kord = Kord(ConfigHandler.discordBot.token.trim())
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

		// creating a new thread for every message is maybe kinda overkill? but at the same time, there's noticable delay if we don't, so…
		// and it's not like it won't get killed instantly afterwards, so should be fine

		Thread {
			runBlocking {
				kord.rest.webhook.executeWebhook(Snowflake(webhook!!.id), webhook!!.token, false, null) {
					content = message
					username = user
					suppressEmbeds = true // TODO: does this need a config option?
					avatarUrl = avatar
					if(!ConfigHandler.allowMentions)
						allowedMentions {}
				}
			}
		}.apply {
			name = "${Reference.MODID}-webhook-message"
			start()
		}
	}

	class WebhookData {
		val id: ULong
		val token: String

		// https://discord.com/api/webhooks/{id}/{token}
		constructor(url: String) {
			if(!url.contains("discord.com/api/webhooks/"))
				throw Error("Provided webhook url is not valid: $url")
			token = url.takeLastWhile { it != '/' }
			val tmp = url.dropLastWhile { it != '/' }.dropLast(1)
			id = tmp.dropLastWhile { it != '/' }.toULong()
		}

		constructor(id: ULong, token: String) {
			this.id = id
			this.token = token
		}
	}
}

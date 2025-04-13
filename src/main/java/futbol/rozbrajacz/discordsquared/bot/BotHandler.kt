package futbol.rozbrajacz.discordsquared.bot

import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.core.entity.effectiveName
import dev.kord.core.event.gateway.ReadyEvent
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.core.on
import dev.kord.gateway.Intent
import dev.kord.gateway.PrivilegedIntent
import dev.kord.rest.builder.message.allowedMentions
import futbol.rozbrajacz.discordsquared.ConfigHandler
import futbol.rozbrajacz.discordsquared.DiscordSquared
import futbol.rozbrajacz.discordsquared.Reference
import kotlinx.coroutines.runBlocking
import net.minecraft.util.text.TextComponentString

object BotHandler {
	private const val WEBHOOK_NAME = "dscsquared_webhook" // Username cannot contain "discord"

	var hasBot = ConfigHandler.discordBot.token.trim().isNotEmpty()
		private set
	lateinit var kord: Kord
		private set
	var webhook: WebhookData? = null
		private set
	lateinit var postMessage: (user: String, message: String) -> Unit
		private set

	suspend fun init() {
		if(!hasBot)
			return

		val stringID = ConfigHandler.discordBot.channelID.trim()

		if(stringID.isEmpty()) {
			DiscordSquared.logger.error("channelID is not defined in the config, bailing out!")
			return
		}

		val channelID = stringID.toULong()

		// if we got a webhook specified, no need to create another one
		val webhookUrl = ConfigHandler.webhook.url.trim()
		if(webhookUrl.isNotEmpty())
			webhook = WebhookData(webhookUrl)

		kord = Kord(ConfigHandler.discordBot.token.trim())
		kord.on<MessageCreateEvent> {
			if(message.author?.isBot == true || message.webhookId != null || message.channelId.value != channelID || message.content.isEmpty())
				return@on

			// Available formats: {username} {displayName} {message} {userId} {messageId}
			val message = ConfigHandler.messageFormat
				.replace("{username}", message.author?.username ?: "?")
				.replace("{displayName}", message.author?.effectiveName ?: "?")
				.replace("{message}", message.content)
				.replace("{userId}", message.author?.id?.value?.toString() ?: "?")
				.replace("{messageId}", message.id.value.toString())

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
		}

		postMessage = { user: String, message: String ->
			println("postMessage $message")
			runBlocking {
				kord.rest.webhook.executeWebhook(Snowflake(webhook!!.id), webhook!!.token, false, null) {
					content = message
					username = user
					suppressEmbeds = true // TODO: does this need a config option?
					if(!ConfigHandler.allowMentions)
						allowedMentions {}
				}
			}
		}

		kord.login {
			@OptIn(PrivilegedIntent::class)
			intents += Intent.MessageContent
		}
	}

	class WebhookData {
		val id: ULong
		val token: String

		// https://discord.com/api/webhooks/{id}/{token}
		constructor(url: String) {
			if(!url.contains("discord.com/api/webhooks/"))
				throw Error("Provided Webhook URL is not valid: $url")
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

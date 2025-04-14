package futbol.rozbrajacz.discordsquared

import kotlinx.coroutines.runBlocking
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.server.MinecraftServer
import net.minecraftforge.event.ServerChatEvent
import net.minecraftforge.event.entity.living.LivingDeathEvent
import net.minecraftforge.fml.common.FMLCommonHandler
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent
import net.minecraftforge.fml.common.event.FMLServerStartedEvent
import net.minecraftforge.fml.common.event.FMLServerStartingEvent
import net.minecraftforge.fml.common.event.FMLServerStoppingEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.PlayerEvent
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.server.FMLServerHandler
import org.apache.logging.log4j.Logger
import kotlin.properties.Delegates

@Mod(
	modid = Reference.MODID,
	name = Reference.MOD_NAME,
	version = Reference.VERSION,
	dependencies = DiscordSquared.DEPENDENCIES,
	modLanguageAdapter = "io.github.chaosunity.forgelin.KotlinAdapter",
	serverSideOnly = true,
	acceptableRemoteVersions = "*"
)
@Mod.EventBusSubscriber(Side.SERVER, modid = Reference.MODID)
object DiscordSquared {
	const val DEPENDENCIES = "required-after:forgelin_continuous@[${Reference.KOTLIN_VERSION},);"

	lateinit var logger: Logger
		private set
	var enabled by Delegates.notNull<Boolean>()
		private set
	lateinit var server: MinecraftServer
		private set

	@Suppress("unused")
	@Mod.EventHandler
	fun preInit(e: FMLPreInitializationEvent) {
		logger = e.modLog
		enabled = ConfigHandler.enabled && FMLCommonHandler.instance().side.isServer
		if(enabled)
			server = FMLServerHandler.instance().server
	}

	@Suppress("unused")
	@Mod.EventHandler
	fun serverStart(e: FMLServerStartingEvent) {
		if(!enabled)
			return
		Thread {
			runBlocking {
				BotHandler.init()
			}
		}.apply {
			name = "${Reference.MOD_NAME} bot thread"
			start()
		}
	}

	@Suppress("unused")
	@Mod.EventHandler
	fun serverStop(e: FMLServerStoppingEvent) {
		if(!enabled || !ConfigHandler.serverStartStopMessages.stopEnabled)
			return

		BotHandler.postSystemMessage(ConfigHandler.serverStartStopMessages.stopMessage)
	}

	@Suppress("unused")
	@SubscribeEvent
	fun onMessage(e: ServerChatEvent) {
		if(!enabled || !ConfigHandler.chatMessages.enabled)
			return

		BotHandler.postPlayerMessage(e.player, ConfigHandler.chatMessages.format.fmt(
			"username" to e.username,
			"uuid" to e.player.uniqueID,
			"message" to e.message
		))
	}

	@Suppress("unused")
	@SubscribeEvent
	fun onJoin(e: PlayerEvent.PlayerLoggedInEvent) {
		if(!enabled || !ConfigHandler.joinLeaveMessages.joinEnabled)
			return

		BotHandler.updatePresence = true // onlinePlayers
		BotHandler.postPlayerMessage(e.player, ConfigHandler.joinLeaveMessages.joinFormat.fmt(
			"username" to e.player.name,
			"uuid" to e.player.uniqueID,
			"newOnlineCount" to server.playerList.currentPlayerCount
		))
	}

	@Suppress("unused")
	@SubscribeEvent
	fun onLeave(e: PlayerEvent.PlayerLoggedOutEvent) {
		if(!enabled || !ConfigHandler.joinLeaveMessages.leaveEnabled)
			return

		BotHandler.updatePresence = true // onlinePlayers
		BotHandler.postPlayerMessage(e.player, ConfigHandler.joinLeaveMessages.leaveFormat.fmt(
			"username" to e.player.name,
			"uuid" to e.player.uniqueID,
			"newOnlineCount" to server.playerList.currentPlayerCount
		))
	}

	@Suppress("unused")
	@SubscribeEvent
	fun onDeath(e: LivingDeathEvent) {
		if(!enabled || !ConfigHandler.deathMessages.enabled || e.entityLiving !is EntityPlayer)
			return

		BotHandler.postPlayerMessage(e.entityLiving, ConfigHandler.deathMessages.format.fmt(
			"username" to e.entityLiving.name,
			"uuid" to e.entityLiving.uniqueID,
			"deathReason" to e.entityLiving.combatTracker.deathMessage.unformattedText.removePrefix("${e.entityLiving.name} ")
		))
	}

	//because we're on server-side, we cannot translate any normal way
	//use translateToFallback to hardcode en-US locale
	//fun String.translate() = I18n.translateToFallback(this)

	fun String.fmt(vararg modifiers: Pair<String, Any>): String {
		var ret = this
		for((format, replacement) in modifiers)
			ret = ret.replace("{$format}", replacement.toString())
		return ret
	}
}

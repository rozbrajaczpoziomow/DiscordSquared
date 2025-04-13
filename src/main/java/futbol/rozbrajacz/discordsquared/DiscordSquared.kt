package futbol.rozbrajacz.discordsquared

import futbol.rozbrajacz.discordsquared.bot.BotHandler
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.server.MinecraftServer
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.ServerChatEvent
import net.minecraftforge.event.entity.living.LivingDeathEvent
import net.minecraftforge.fml.common.FMLCommonHandler
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.event.FMLInitializationEvent
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent
import net.minecraftforge.fml.common.event.FMLServerStartedEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.PlayerEvent
import net.minecraftforge.fml.common.network.FMLNetworkEvent
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.server.FMLServerHandler
import org.apache.logging.log4j.Logger
import kotlin.properties.Delegates

@Mod(
	modid = Reference.MODID,
	name = Reference.MOD_NAME,
	version = Reference.VERSION,
	dependencies = DiscordSquared.DEPENDENCIES,
	modLanguageAdapter = "io.github.chaosunity.forgelin.KotlinAdapter"
)
@Mod.EventBusSubscriber(Side.SERVER, modid = Reference.MODID)
object DiscordSquared {
	const val DEPENDENCIES = "required-after:configanytime;required-after:forgelin_continuous@[${Reference.KOTLIN_VERSION},);"

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
	fun serverStart(e: FMLServerStartedEvent) {
		if(!enabled)
			return
		Thread {
			runBlocking {
				BotHandler.init()
			}
		}.start()
	}

	@Suppress("unused")
	@SubscribeEvent
	fun onMessage(e: ServerChatEvent) {
		if(!enabled)
			return
		println("calling postMessage with ${e.message}")
		BotHandler.postMessage(e.username, e.message)
	}

	@Suppress("unused")
	@SubscribeEvent
	fun onJoin(e: PlayerEvent.PlayerLoggedInEvent) {
		if(!enabled || !ConfigHandler.joinLeaveMessages)
			return

		BotHandler.postMessage(e.player.name, "${e.player.name} joined the game")
	}

	@Suppress("unused")
	@SubscribeEvent
	fun onLeave(e: PlayerEvent.PlayerLoggedInEvent) {
		if(!enabled || !ConfigHandler.joinLeaveMessages)
			return

		BotHandler.postMessage(e.player.name, "${e.player.name} left the game")
	}

	@Suppress("unused")
	@SubscribeEvent
	fun onDeath(e: LivingDeathEvent) {
		if(!enabled || !ConfigHandler.deathMessages || e.entityLiving !is EntityPlayer)
			return

		BotHandler.postMessage(e.entityLiving.name, "${e.entityLiving.name} ${e.source.damageType}")
	}
}

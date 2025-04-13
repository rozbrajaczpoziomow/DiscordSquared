package futbol.rozbrajacz.discordsquared

import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.event.FMLInitializationEvent
import org.apache.logging.log4j.Logger

@Mod(
	modid = Reference.MODID,
	name = Reference.MOD_NAME,
	version = Reference.VERSION,
	dependencies = DiscordSquared.DEPENDENCIES,
	modLanguageAdapter = "io.github.chaosunity.forgelin.KotlinAdapter",
	serverSideOnly = true
)
object DiscordSquared {
	const val DEPENDENCIES = "required-after:configanytime;required-after:forgelin_continuous@[${Reference.KOTLIN_VERSION},);"

	lateinit var logger: Logger

	@Suppress("unused")
	@Mod.EventHandler
	fun init(e: FMLInitializationEvent) {
		MinecraftForge.EVENT_BUS.register(Events())
	}
}

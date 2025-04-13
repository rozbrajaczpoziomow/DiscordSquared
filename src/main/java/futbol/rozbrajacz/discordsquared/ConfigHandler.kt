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
	@Config.Name("Enable the mod")
	var enabled = false

	@JvmField
	@Config.RequiresMcRestart
	@Config.Name("Discord Bot Token")
	@Config.Comment(
		"Required for 2-way communication, can be obtained at https://discord.dev if you know what you're doing",
		"Can be left empty if all you need is Minecraft -> Discord communication"
	)
	var botToken = ""

	@JvmField
	@Config.RequiresMcRestart
	@Config.Name("Webhook URL")
	@Config.Comment(
		"Required for Minecraft -> Discord communication",
		"Can be left empty if you provided a discord token (in which case the bot will automatically create a webhook), required otherwise."
	)
	var webhookURL = ""
	//
	//@JvmField
	//@Config.Name("General")
	//@Config.LangKey("config.${Tags.MOD_ID}.general")
	//@Config.Comment("Options that affect the entire mod")
	//val GENERAL = General()
	//
	//class General {
	//	@JvmField
	//	@Config.RequiresMcRestart
	//	@Config.Name("Family Friendly Mode")
	//	@Config.Comment("Illegal drug compounds will have their names replaced with more family-friendly versions")
	//	var familyFriendlyMode = false
	//
	//	@JvmField
	//	@Config.RequiresMcRestart
	//	@Config.Name("Enable Automation")
	//	@Config.Comment("Enables item automation with hoppers, pipes, etc")
	//	var enableAutomation = true
	//}
	//
	//@JvmField
	//@Config.Name("Fission Reactor")
	//@Config.LangKey("config.${Tags.MOD_ID}.fission")
	//@Config.Comment("The Fisson Reactor is a multi-block structure that can splits one element into two new elements")
	//val FISSION = Fission()
	//
	//class Fission {
	//	@JvmField
	//	@Config.RequiresMcRestart
	//	@Config.Name("Energy Capacity")
	//	@Config.Comment("Max energy capacity of the Fission Reactor")
	//	@Config.RangeInt(min = 1, max = Integer.MAX_VALUE)
	//	var energyCapacity = 50000
	//
	//	@JvmField
	//	@Config.RequiresMcRestart
	//	@Config.Name("Energy per Tick")
	//	@Config.Comment("Energy consumption rate per tick for the Fission Reactor")
	//	@Config.RangeInt(min = 1, max = Integer.MAX_VALUE)
	//	var energyPerTick = 300
	//
	//	@JvmField
	//	@Config.RequiresMcRestart
	//	@Config.Name("Processing Ticks")
	//	@Config.Comment("Number of ticks per operation for the Fission Reactor")
	//	@Config.RangeInt(min = 0, max = Integer.MAX_VALUE)
	//	var processingTicks = 40
	//
	//	@JvmField
	//	@Config.RequiresMcRestart
	//	@Config.Name("Compact Fission Reactor")
	//	@Config.Comment(
	//		"If true, Fission Reactors can share casing blocks with adjacent reactors",
	//		"This allows up to 4 Fission Reactors to share a single set of casing blocks",
	//		"or for rows of reactors to share a wall of casing blocks."
	//	)
	//	var compactFissionReactor = false
	//
	//	@JvmField
	//	@Config.RequiresMcRestart
	//	@Config.Name("Fission Reactor Modifiers")
	//	@Config.Comment("List of fluid modifiers for the Fission Reactor. Syntax: fluidName;productivity;processing_time;energy")
	//	var fissionReactorFluidModifiers = arrayOf(
	//		"water;0;-0.02;-0.05",
	//		"lava;0.2;0.05;0.1"
	//	)
	//
	//	@JvmField
	//	@Config.RequiresMcRestart
	//	@Config.Name("Fission Reactor Moderator Blocks")
	//	@Config.Comment(
	//		"List of moderator block modifiers for the Fission Reactor.",
	//		"Syntax: mod:block:meta;productivity;processing_time;energy (meta is optional, mod is required)"
	//	)
	//	var fissionReactorBlockModifiers = arrayOf(
	//		"minecraft:coal_block;0;-0.05;-0.01",
	//		"minecraft:diamond_block;0.05;0.1;0.1"
	//	)
	//
	//	@JvmField
	//	@Config.RequiresMcRestart
	//	@Config.Name("Minimum Energy per Tick")
	//	@Config.Comment("Minimum energy consumption rate per tick for the Fission Reactor")
	//	@Config.RangeInt(min = 0, max = Integer.MAX_VALUE)
	//	var minEnergyPerTick = 0
	//}
	//
	//@JvmField
	//@Config.Name("Fusion Reactor")
	//@Config.LangKey("config.${Tags.MOD_ID}.fusion")
	//@Config.Comment("The Fusion Reactor is a multi-block structure that can fuses two elements into a new element")
	//val FUSION = Fusion()
	//
	//class Fusion {
	//	@JvmField
	//	@Config.RequiresMcRestart
	//	@Config.Name("Energy Capacity")
	//	@Config.Comment("Max energy capacity of the Fusion Reactor")
	//	@Config.RangeInt(min = 1, max = Integer.MAX_VALUE)
	//	var energyCapacity = 50000
	//
	//	@JvmField
	//	@Config.RequiresMcRestart
	//	@Config.Name("Energy per Tick")
	//	@Config.Comment("Energy consumption rate per tick for the Fusion Reactor")
	//	@Config.RangeInt(min = 1, max = Integer.MAX_VALUE)
	//	var energyPerTick = 300
	//
	//	@JvmField
	//	@Config.RequiresMcRestart
	//	@Config.Name("Processing Ticks")
	//	@Config.Comment("Number of ticks per operation for the Fusion Reactor")
	//	@Config.RangeInt(min = 0, max = Integer.MAX_VALUE)
	//	var processingTicks = 40
	//
	//	@JvmField
	//	@Config.RequiresMcRestart
	//	@Config.Name("Compact Fusion Reactor")
	//	@Config.Comment(
	//		"If true, Fusion Reactors can share casing blocks with adjacent reactors",
	//		"This allows up to 4 Fusion Reactors to share a single set of casing blocks",
	//		"or for rows of reactors to share a wall of casing blocks."
	//	)
	//	var compactFusionReactor = false
	//
	//	@JvmField
	//	@Config.RequiresMcRestart
	//	@Config.Name("Fusion Reactor Modifiers")
	//	@Config.Comment(
	//		"List of fluid modifiers for the Fusion Reactor.",
	//		"Syntax: fluidName;productivity;processing_time;energy"
	//	)
	//	var fusionReactorFluidModifiers = arrayOf(
	//		"water;0;-0.02;-0.03",
	//		"lava;0.2;0.05;0.1"
	//	)
	//
	//	@JvmField
	//	@Config.RequiresMcRestart
	//	@Config.Name("Fusion Reactor Moderator Blocks")
	//	@Config.Comment(
	//		"List of moderator block modifiers for the Fusion Reactor.",
	//		"Syntax: mod:block:meta;productivity;processing_time;energy (meta is optional, mod is required)"
	//	)
	//	var fusionReactorBlockModifiers = arrayOf(
	//		"minecraft:coal_block;0;-0.05;-0.01",
	//		"minecraft:diamond_block;0.05;0.1;0.1"
	//	)
	//
	//	@JvmField
	//	@Config.RequiresMcRestart
	//	@Config.Name("Minimum Energy per Tick")
	//	@Config.Comment("Minimum energy consumption rate per tick for the Fusion Reactor")
	//	@Config.RangeInt(min = 0, max = Integer.MAX_VALUE)
	//	var minEnergyPerTick = 0
	//}
	//
	//@JvmField
	//@Config.Name("Combiner")
	//@Config.LangKey("config.${Tags.MOD_ID}.combiner")
	//@Config.Comment("The Combiner creates molecules and items from elements by crafting them together")
	//val COMBINER = Combiner()
	//
	//class Combiner {
	//	@JvmField
	//	@Config.RequiresMcRestart
	//	@Config.Name("Energy Capacity")
	//	@Config.Comment("Max energy capacity of the Combiner")
	//	@Config.RangeInt(min = 1, max = Integer.MAX_VALUE)
	//	var energyCapacity = 10000
	//
	//	@JvmField
	//	@Config.RequiresMcRestart
	//	@Config.Name("Energy per Tick")
	//	@Config.Comment("Energy consumption rate per tick for the Combiner")
	//	@Config.RangeInt(min = 0, max = Integer.MAX_VALUE)
	//	var energyPerTick = 200
	//
	//	@JvmField
	//	@Config.RequiresMcRestart
	//	@Config.Name("Processing Ticks")
	//	@Config.Comment("Number of ticks per operation for the Combiner")
	//	@Config.RangeInt(min = 0, max = Integer.MAX_VALUE)
	//	var processingTicks = 5
	//}
	//
	//@JvmField
	//@Config.Name("Dissolver")
	//@Config.LangKey("config.${Tags.MOD_ID}.dissolver")
	//@Config.Comment("The Dissolver creates elements from molecules and items by separating them")
	//val DISSOLVER = Dissolver()
	//
	//class Dissolver {
	//	@JvmField
	//	@Config.RequiresMcRestart
	//	@Config.Name("Energy Capacity")
	//	@Config.Comment("Max energy capacity of the Dissolver")
	//	@Config.RangeInt(min = 1, max = Integer.MAX_VALUE)
	//	var energyCapacity = 10000
	//
	//	@JvmField
	//	@Config.RequiresMcRestart
	//	@Config.Name("Energy per Tick")
	//	@Config.Comment("Energy consumption rate per tick for the Dissolver")
	//	@Config.RangeInt(min = 0, max = Integer.MAX_VALUE)
	//	var energyPerTick = 100
	//
	//	@JvmField
	//	@Config.RequiresMcRestart
	//	@Config.Name("Processing Ticks")
	//	@Config.Comment("Number of ticks per operation for the Combiner")
	//	@Config.RangeInt(min = 0, max = Integer.MAX_VALUE)
	//	var processingTicks = 0
	//
	//	@JvmField
	//	@Config.RequiresMcRestart
	//	@Config.Name("Speed")
	//	@Config.Comment(
	//		"The max amount of items that the Dissolver will output each tick.",
	//		"Please note: only one element will be outputted per tick, and only the elements from one input are eligible at a time.",
	//		"For example: Cellulose (C6 H10 O5) with speed 4 would be outputted like so, with each comma-separated value representing 1 tick [4xC,2xC,4xH,4xH,2xH,4xO,1xO]"
	//	)
	//	@Config.RangeInt(min = 1, max = 64)
	//	var speed = 8
	//}
	//
	//@JvmField
	//@Config.Name("Electrolyzer")
	//@Config.LangKey("config.${Tags.MOD_ID}.electrolyzer")
	//@Config.Comment("The Electrolyzer creates elements from fluids by separating them utilizing a catalyst")
	//val ELECTROLYZER = Electrolyzer()
	//
	//class Electrolyzer {
	//	@JvmField
	//	@Config.RequiresMcRestart
	//	@Config.Name("Energy Capacity")
	//	@Config.Comment("Max energy capacity of the Electrolyzer")
	//	@Config.RangeInt(min = 1, max = Integer.MAX_VALUE)
	//	var energyCapacity = 10000
	//
	//	@JvmField
	//	@Config.RequiresMcRestart
	//	@Config.Name("Energy per Tick")
	//	@Config.Comment("Energy consumption rate per tick for the Electrolyzer")
	//	@Config.RangeInt(min = 0, max = Integer.MAX_VALUE)
	//	var energyPerTick = 100
	//
	//	@JvmField
	//	@Config.RequiresMcRestart
	//	@Config.Name("Processing Ticks")
	//	@Config.Comment("Number of ticks per Electrolyzer operation")
	//	@Config.RangeInt(min = 0, max = Integer.MAX_VALUE)
	//	var processingTicks = 10
	//}
	//
	//@JvmField
	//@Config.Name("Evaporator")
	//@Config.LangKey("config.${Tags.MOD_ID}.evaporator")
	//@Config.Comment("The Evaporator creates items from fluids by removing the fluid and leaving behind the solid")
	//val EVAPORATOR = Evaporator()
	//
	//class Evaporator {
	//	@JvmField
	//	@Config.RequiresMcRestart
	//	@Config.Name("Processing Ticks")
	//	@Config.Comment("The best possible processing time for the Evaporator. In practice it will be increased by biome, time of day, etc")
	//	@Config.RangeInt(min = 0, max = Integer.MAX_VALUE)
	//	var processingTicks = 160
	//}
	//
	//@JvmField
	//@Config.Name("Atomizer")
	//@Config.LangKey("config.${Tags.MOD_ID}.atomizer")
	//@Config.Comment("The Atomizer transforms liquids into their respective elements and molecules")
	//val ATOMIZER = Atomizer()
	//
	//class Atomizer {
	//	@JvmField
	//	@Config.RequiresMcRestart
	//	@Config.Name("Energy Capacity")
	//	@Config.Comment("Max energy capacity of the Atomizer")
	//	@Config.RangeInt(min = 1, max = Integer.MAX_VALUE)
	//	var energyCapacity = 10000
	//
	//	@JvmField
	//	@Config.RequiresMcRestart
	//	@Config.Name("Energy per Tick")
	//	@Config.Comment("Energy consumption rate per tick for the Atomizer")
	//	@Config.RangeInt(min = 0, max = Integer.MAX_VALUE)
	//	var energyPerTick = 50
	//
	//	@JvmField
	//	@Config.RequiresMcRestart
	//	@Config.Name("Processing Ticks")
	//	@Config.Comment("Number of ticks per Atomizer operation")
	//	@Config.RangeInt(min = 0, max = Integer.MAX_VALUE)
	//	var processingTicks = 100
	//}
	//
	//@JvmField
	//@Config.Name("Liquifier")
	//@Config.LangKey("config.${Tags.MOD_ID}.liquifier")
	//@Config.Comment("The Liquifier transforms elements and molecules into their respective liquids")
	//val LIQUIFIER = Liquifier()
	//
	//class Liquifier {
	//	@JvmField
	//	@Config.RequiresMcRestart
	//	@Config.Name("Energy Capacity")
	//	@Config.Comment("Max energy capacity of the Liquifier")
	//	@Config.RangeInt(min = 1, max = Integer.MAX_VALUE)
	//	var energyCapacity = 10000
	//
	//	@JvmField
	//	@Config.RequiresMcRestart
	//	@Config.Name("Energy per Tick")
	//	@Config.Comment("Energy consumption rate per tick for the Liquifier")
	//	@Config.RangeInt(min = 0, max = Integer.MAX_VALUE)
	//	var energyPerTick = 50
	//
	//	@JvmField
	//	@Config.RequiresMcRestart
	//	@Config.Name("Processing Ticks")
	//	@Config.Comment("Number of ticks per Liquifier operation")
	//	@Config.RangeInt(min = 0, max = Integer.MAX_VALUE)
	//	var processingTicks = 100
	//}

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

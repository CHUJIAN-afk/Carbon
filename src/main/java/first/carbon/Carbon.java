package first.carbon;

import com.mojang.brigadier.arguments.BoolArgumentType;
import first.carbon.client.ItemBatchRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.client.event.AddClientReloadListenersEvent;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.event.GameShuttingDownEvent;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

@Mod(value = "carbon", dist = Dist.CLIENT)
public final class Carbon {
    public static final first.carbon.compat.IrisCompatibility IRIS = new first.carbon.compat.IrisCompatibility();
    public static final first.carbon.compat.SodiumCompatibility SODIUM = new first.carbon.compat.SodiumCompatibility();
    public static final first.carbon.client.GenericItemRenderer GENERIC = new first.carbon.client.GenericItemRenderer();
    public static final first.carbon.client.GenericShadowRenderer SHADOWS = new first.carbon.client.GenericShadowRenderer();
    public static final ItemBatchRenderer RENDERER = new ItemBatchRenderer();
    public static final Settings CONFIG = new Settings();

    public Carbon(IEventBus modBus, ModContainer container) {
        var oldConfig = net.neoforged.fml.loading.FMLPaths.CONFIGDIR.get().resolve("item_model_optimization-client.toml");
        var config = net.neoforged.fml.loading.FMLPaths.CONFIGDIR.get().resolve("carbon-client.toml");
        if (java.nio.file.Files.isRegularFile(oldConfig) && !java.nio.file.Files.exists(config)) {
            try { java.nio.file.Files.copy(oldConfig, config); }
            catch (java.io.IOException exception) { org.slf4j.LoggerFactory.getLogger("Carbon").warn("Unable to migrate configuration", exception); }
        }
        container.registerConfig(ModConfig.Type.CLIENT, CONFIG.spec);
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
        modBus.addListener((AddClientReloadListenersEvent event) -> event.addListener(Identifier.fromNamespaceAndPath("carbon", "meshes"), (ResourceManagerReloadListener) manager -> { RENDERER.invalidate(); GENERIC.invalidate(); SHADOWS.invalidate(); }));
        NeoForge.EVENT_BUS.addListener((ClientPlayerNetworkEvent.LoggingOut event) -> { RENDERER.invalidate(); GENERIC.invalidate(); SHADOWS.invalidate(); });
        NeoForge.EVENT_BUS.addListener((GameShuttingDownEvent event) -> { RENDERER.invalidate(); GENERIC.invalidate(); SHADOWS.invalidate(); });
        NeoForge.EVENT_BUS.addListener((RenderLevelStageEvent.AfterOpaqueFeatures event) -> RENDERER.flush(event.getLevelRenderState().cameraRenderState));
        NeoForge.EVENT_BUS.addListener((RegisterClientCommandsEvent event) -> event.getDispatcher().register(
                literal("carbon")
                        .then(literal("enabled").then(argument("enabled", BoolArgumentType.bool()).executes(context -> {
                            CONFIG.enabled.set(BoolArgumentType.getBool(context, "enabled"));
                            CONFIG.spec.save();
                            return 1;
                        })))
                        .then(literal("stats").executes(context -> {
                            context.getSource().sendSuccess(() -> Component.literal(RENDERER.statistics()), false);
                            return 1;
                        }))));
    }

    public static final class Settings {
        public final ModConfigSpec spec;
        public final ModConfigSpec.BooleanValue enabled;
        public final ModConfigSpec.BooleanValue optimizeDroppedItems;
        public final ModConfigSpec.BooleanValue physicalDroppedItems;
        public final ModConfigSpec.BooleanValue skipVisibilityChecks;

        private Settings() {
            ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
            enabled = builder.comment("Cache static ground item models and draw matching stacks in batches.").translation("carbon.configuration.enabled").define("enabled", true);
            optimizeDroppedItems = builder.translation("carbon.configuration.optimizeDroppedItems").define("optimizeDroppedItems", true);
            physicalDroppedItems = builder.translation("carbon.configuration.physicalDroppedItems").define("physicalDroppedItems", false);
            skipVisibilityChecks = builder.comment("Submit batched items without vanilla frustum or Sodium visibility checks; distance limits still apply.").translation("carbon.configuration.skipVisibilityChecks").define("skipVisibilityChecks", true);
            spec = builder.build();
        }
    }
}

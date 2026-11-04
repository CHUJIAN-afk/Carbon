package first.carbon;

import com.mojang.brigadier.arguments.BoolArgumentType;
import first.carbon.client.ItemBatchRenderer;
import first.carbon.client.DroppedItemBatchRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.event.RenderFrameEvent;
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
    public static final ItemBatchRenderer RENDERER = new ItemBatchRenderer();
    public static final DroppedItemBatchRenderer DROPPED_RENDERER = new DroppedItemBatchRenderer();
    public static final Settings CONFIG = new Settings();

    public Carbon(IEventBus modBus, ModContainer container) {
        var oldConfig = net.neoforged.fml.loading.FMLPaths.CONFIGDIR.get().resolve("item_model_optimization-client.toml");
        var config = net.neoforged.fml.loading.FMLPaths.CONFIGDIR.get().resolve("carbon-client.toml");
        if (java.nio.file.Files.isRegularFile(oldConfig) && !java.nio.file.Files.exists(config)) {
            try {
                java.nio.file.Files.copy(oldConfig, config);
            } catch (java.io.IOException exception) {
                org.slf4j.LoggerFactory.getLogger("Carbon").warn("Unable to migrate the previous client configuration", exception);
            }
        }
        container.registerConfig(ModConfig.Type.CLIENT, CONFIG.spec);
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
        modBus.addListener((RegisterClientReloadListenersEvent event) -> event.registerReloadListener((ResourceManagerReloadListener) manager -> {
            IRIS.invalidate();
            RENDERER.invalidate();
            DROPPED_RENDERER.invalidate();
        }));
        NeoForge.EVENT_BUS.addListener((ClientPlayerNetworkEvent.LoggingOut event) -> {
            RENDERER.invalidate();
            DROPPED_RENDERER.invalidate();
        });
        NeoForge.EVENT_BUS.addListener((GameShuttingDownEvent event) -> {
            RENDERER.invalidate();
            DROPPED_RENDERER.invalidate();
        });
        NeoForge.EVENT_BUS.addListener((RenderFrameEvent.Pre event) -> {
            IRIS.beginFrame();
            RENDERER.beginFrame();
        });
        NeoForge.EVENT_BUS.addListener((RenderLevelStageEvent event) -> {
            if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_ENTITIES) {
                DROPPED_RENDERER.flush();
                RENDERER.flushShadows();
            }
            if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_LEVEL) {
                DROPPED_RENDERER.flush();
                DROPPED_RENDERER.endFrame();
                RENDERER.endWorld();
            }
        });
        NeoForge.EVENT_BUS.addListener((RegisterClientCommandsEvent event) -> event.getDispatcher().register(
                literal("carbon")
                        .then(literal("enabled").then(argument("enabled", BoolArgumentType.bool()).executes(context -> {
                            CONFIG.enabled.set(BoolArgumentType.getBool(context, "enabled"));
                            CONFIG.spec.save();
                            return 1;
                        })))
                        .then(literal("stats").executes(context -> {
                            context.getSource().sendSuccess(() -> Component.literal(RENDERER.statistics() + "; dropped: " + DROPPED_RENDERER.statistics()), false);
                            return 1;
                        }))));
    }

    public static final class Settings {
        public final ModConfigSpec spec;
        public final ModConfigSpec.BooleanValue enabled;
        public final ModConfigSpec.BooleanValue optimizeDroppedItems;
        public final ModConfigSpec.BooleanValue physicalDroppedItems;
        public final ModConfigSpec.BooleanValue skipVisibilityChecks;
        public final ModConfigSpec.DoubleValue cacheUpdateBudgetMs;

        private Settings() {
            ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
            enabled = builder.comment("Cache standard item model geometry and instance matching render submissions.").translation("carbon.configuration.enabled").define("enabled", true);
            optimizeDroppedItems = builder.comment("Take over dropped-item models, spin and scale on the GPU. Disable to preserve other mods' dropped-item rendering and transforms.").translation("carbon.configuration.optimizeDroppedItems").define("optimizeDroppedItems", true);
            physicalDroppedItems = builder.comment("Visual-only GPU tumbling and resting dropped items; requires optimizeDroppedItems. Does not change collisions, velocity or pickup.").translation("carbon.configuration.physicalDroppedItems").define("physicalDroppedItems", false);
            skipVisibilityChecks = builder.comment("Skip frustum and Sodium occlusion checks for the dedicated dropped-item pipeline; distance limits still apply.").translation("carbon.configuration.skipVisibilityChecks").define("skipVisibilityChecks", true);
            cacheUpdateBudgetMs = builder.comment("CPU cache update budget per frame in milliseconds. Keep drawing the previous scene until the queue completes. 0 updates immediately; a single item can exceed the budget.").translation("carbon.configuration.cacheUpdateBudgetMs").defineInRange("cacheUpdateBudgetMs", 2.0, 0.0, 20.0);
            spec = builder.build();
        }
    }
}

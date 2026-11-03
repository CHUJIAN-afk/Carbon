package first.carbon.client;

import com.mojang.brigadier.arguments.BoolArgumentType;
import first.carbon.Carbon;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.GameShuttingDownEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public final class ClientEvents {


    public static void register() {
        var oldConfig = net.minecraftforge.fml.loading.FMLPaths.CONFIGDIR.get().resolve("item_model_optimization-client.toml");
        var config = net.minecraftforge.fml.loading.FMLPaths.CONFIGDIR.get().resolve("carbon-client.toml");
        if (java.nio.file.Files.isRegularFile(oldConfig) && !java.nio.file.Files.exists(config)) {
            try { java.nio.file.Files.copy(oldConfig, config); }
            catch (java.io.IOException exception) { org.slf4j.LoggerFactory.getLogger("Carbon").warn("Unable to migrate configuration", exception); }
        }
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, Carbon.CONFIG.spec);
        ModLoadingContext.get().registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class,
                () -> new ConfigScreenHandler.ConfigScreenFactory((minecraft, parent) -> new ItemConfigScreen(parent)));
        FMLJavaModLoadingContext.get().getModEventBus().addListener((RegisterClientReloadListenersEvent event) ->
                event.registerReloadListener((ResourceManagerReloadListener) manager -> { Carbon.Client.IRIS.invalidate(); Carbon.Client.RENDERER.invalidate(); Carbon.Client.DROPPED_RENDERER.invalidate(); }));
        MinecraftForge.EVENT_BUS.addListener((ClientPlayerNetworkEvent.LoggingOut event) -> { Carbon.Client.RENDERER.invalidate(); Carbon.Client.DROPPED_RENDERER.invalidate(); });
        MinecraftForge.EVENT_BUS.addListener((GameShuttingDownEvent event) -> { Carbon.Client.RENDERER.invalidate(); Carbon.Client.DROPPED_RENDERER.invalidate(); });
        MinecraftForge.EVENT_BUS.addListener((net.minecraftforge.event.TickEvent.RenderTickEvent event) -> {
            if (event.phase == net.minecraftforge.event.TickEvent.Phase.START) {
                Carbon.Client.IRIS.beginFrame();
                Carbon.Client.RENDERER.beginFrame();
            }
        });
        MinecraftForge.EVENT_BUS.addListener((RenderLevelStageEvent event) -> {
            if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_ENTITIES) {
                Carbon.Client.DROPPED_RENDERER.flush();
                Carbon.Client.RENDERER.flushShadows();
            } else if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_LEVEL) {
                Carbon.Client.DROPPED_RENDERER.flush();
                Carbon.Client.DROPPED_RENDERER.endFrame();
                Carbon.Client.RENDERER.endWorld();
            }
        });
        MinecraftForge.EVENT_BUS.addListener((RegisterClientCommandsEvent event) -> event.getDispatcher().register(
                literal("carbon")
                        .then(literal("enabled").then(argument("enabled", BoolArgumentType.bool()).executes(context -> {
                            Carbon.CONFIG.enabled.set(BoolArgumentType.getBool(context, "enabled"));
                            Carbon.CONFIG.spec.save();
                            return 1;
                        })))
                        .then(literal("stats").executes(context -> {
                            context.getSource().sendSuccess(() -> Component.literal(Carbon.Client.RENDERER.statistics() + "; dropped: " + Carbon.Client.DROPPED_RENDERER.statistics()), false);
                            return 1;
                        }))));
    }
}

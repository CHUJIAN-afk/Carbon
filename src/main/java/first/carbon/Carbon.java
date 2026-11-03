package first.carbon;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;

@Mod("carbon")
public final class Carbon {
    public static final Settings CONFIG = new Settings();

    public Carbon() {
        DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> first.carbon.client.ClientEvents::register);
    }

    public static final class Client {
        public static final first.carbon.compat.IrisCompatibility IRIS = new first.carbon.compat.IrisCompatibility();
        public static final first.carbon.client.ItemBatchRenderer RENDERER = new first.carbon.client.ItemBatchRenderer();
        public static final first.carbon.client.DroppedItemBatchRenderer DROPPED_RENDERER = new first.carbon.client.DroppedItemBatchRenderer();
    }

    public static final class Settings {
        public final ForgeConfigSpec spec;
        public final ForgeConfigSpec.BooleanValue enabled, optimizeDroppedItems, physicalDroppedItems, skipVisibilityChecks;

        private Settings() {
            ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
            enabled = builder.translation("carbon.configuration.enabled").define("enabled", true);
            optimizeDroppedItems = builder.translation("carbon.configuration.optimizeDroppedItems").define("optimizeDroppedItems", true);
            physicalDroppedItems = builder.translation("carbon.configuration.physicalDroppedItems").define("physicalDroppedItems", false);
            skipVisibilityChecks = builder.translation("carbon.configuration.skipVisibilityChecks").define("skipVisibilityChecks", true);
            spec = builder.build();
        }
    }
}

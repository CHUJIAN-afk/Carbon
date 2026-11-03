package first.carbon.mixin;

import first.carbon.client.DroppedItemBatchRenderer;
import net.minecraft.world.entity.item.ItemEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ItemEntity.class)
public abstract class ItemEntityCacheMixin implements DroppedItemBatchRenderer.EntityCacheAccess {
    @Unique
    private final DroppedItemBatchRenderer.EntityCache imo$cache = new DroppedItemBatchRenderer.EntityCache();

    @Override
    public DroppedItemBatchRenderer.EntityCache imo$cache() {
        return imo$cache;
    }
}

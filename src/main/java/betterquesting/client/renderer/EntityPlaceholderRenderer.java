package betterquesting.client.renderer;

import betterquesting.api.placeholders.EntityPlaceholder;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.util.ResourceLocation;

public class EntityPlaceholderRenderer extends Render {
    @Override
    public void doRender(Entity entity, double x, double y, double z, float yaw, float partialTick) {
        EntityItem item = ((EntityPlaceholder) entity).GetItemEntity();
        RenderManager.instance.renderEntityWithPosYaw(item, x, y + 1D, z, yaw, partialTick);
    }

    @Override
    protected ResourceLocation getEntityTexture(Entity entity) {
        return null;
    }
}

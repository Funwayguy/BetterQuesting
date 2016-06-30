package betterquesting.client.renderer;

import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.util.ResourceLocation;
import betterquesting.EntityPlaceholder;

public class EntityPlaceholderRenderer extends Render<EntityPlaceholder>
{
	protected EntityPlaceholderRenderer(RenderManager renderManager)
	{
		super(renderManager);
	}

	@Override
	public void doRender(EntityPlaceholder entity, double x, double y, double z, float yaw, float partialTick)
	{
		EntityItem item = ((EntityPlaceholder)entity).GetItemEntity();
		this.renderManager.renderEntityWithPosYaw(item, x, y + 1D, z, yaw, partialTick);
	}

	@Override
	protected ResourceLocation getEntityTexture(EntityPlaceholder entity)
	{
		return null;
	}	
}

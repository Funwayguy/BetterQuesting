package adv_director.client.renderer;

import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import adv_director.api.placeholders.EntityPlaceholder;

public class PlaceholderRenderFactory implements IRenderFactory<EntityPlaceholder>
{

	@Override
	public Render<? super EntityPlaceholder> createRenderFor(RenderManager manager)
	{
		return new EntityPlaceholderRenderer(manager);
	}
	
}

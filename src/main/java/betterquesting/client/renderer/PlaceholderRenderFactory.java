package betterquesting.client.renderer;

import betterquesting.api.placeholders.EntityPlaceholder;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraftforge.fml.client.registry.IRenderFactory;

public class PlaceholderRenderFactory implements IRenderFactory<EntityPlaceholder> {

    @Override
    public Render<? super EntityPlaceholder> createRenderFor(RenderManager manager) {
        return new EntityPlaceholderRenderer(manager);
    }

}

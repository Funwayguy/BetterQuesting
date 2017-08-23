package adv_director.rw2.api.client.gui.themes;

import net.minecraft.util.ResourceLocation;
import adv_director.rw2.api.client.gui.resources.IGuiTexture;

public interface IGuiTheme
{
	public String getName();
	public ResourceLocation getID();
	public IGuiTexture getTexture(ResourceLocation key);
}

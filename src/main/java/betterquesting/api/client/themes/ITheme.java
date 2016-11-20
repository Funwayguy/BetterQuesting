package betterquesting.api.client.themes;

import net.minecraft.util.ResourceLocation;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public interface ITheme
{
	public ResourceLocation getThemeID();
	public String getDisplayName();
	
	public ResourceLocation getGuiTexture();
	
	public int getTextColor();
	
	public IThemeRenderer getRenderer();
}
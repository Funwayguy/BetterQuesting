package betterquesting.api.client.themes;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@Deprecated
@SideOnly(Side.CLIENT)
public interface ITheme
{
	ResourceLocation getThemeID();
	String getDisplayName();
	
	ResourceLocation getGuiTexture();
	
	int getTextColor();
	
	IThemeRenderer getRenderer();
}
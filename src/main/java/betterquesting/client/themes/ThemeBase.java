package betterquesting.client.themes;

import java.awt.Color;
import net.minecraft.util.ResourceLocation;

public abstract class ThemeBase
{
	public abstract String GetName();
	public abstract ResourceLocation guiTexture();
	public abstract Color textColor();
}

package bq_standard.core.proxies;

import java.awt.Color;
import net.minecraft.util.ResourceLocation;
import betterquesting.client.themes.ThemeRegistry;
import betterquesting.client.themes.ThemeStandard;

public class ClientProxy extends CommonProxy
{
	@Override
	public boolean isClient()
	{
		return true;
	}
	
	@Override
	public void registerHandlers()
	{
		super.registerHandlers();
	}
	
	@Override
	public void registerThemes()
	{
		ThemeRegistry.RegisterTheme(new ThemeStandard("Standard Light", new ResourceLocation("betterquesting", "textures/gui/editor_gui.png")), "light");
		ThemeRegistry.RegisterTheme(new ThemeStandard("Standard Dark", new ResourceLocation("betterquesting", "textures/gui/editor_gui_dark.png")).setTextColor(Color.WHITE), "dark");
		ThemeRegistry.RegisterTheme(new ThemeStandard("Stronghold", new ResourceLocation("betterquesting", "textures/gui/editor_gui_stronghold.png")).setTextColor(Color.WHITE), "stronghold");
	}
}

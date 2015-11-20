package betterquesting.core.proxies;

import java.awt.Color;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import betterquesting.client.BQ_Keybindings;
import betterquesting.client.QuestNotification;
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
		MinecraftForge.EVENT_BUS.register(new QuestNotification());
		BQ_Keybindings.RegisterKeys();
	}
	
	@Override
	public void registerThemes()
	{
		ThemeRegistry.RegisterTheme(new ThemeStandard("Standard Light", new ResourceLocation("betterquesting", "textures/gui/editor_gui.png"), Color.BLACK), "light");
		ThemeRegistry.RegisterTheme(new ThemeStandard("Standard Dark", new ResourceLocation("betterquesting", "textures/gui/editor_gui_dark.png"), Color.WHITE), "dark");
	}
}

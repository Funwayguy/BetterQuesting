package betterquesting.client.gui;

import java.util.ArrayList;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Configuration;
import betterquesting.core.BetterQuesting;
import betterquesting.handlers.ConfigHandler;
import cpw.mods.fml.client.config.GuiConfig;
import cpw.mods.fml.client.config.IConfigElement;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiBQConfig extends GuiConfig
{
	public GuiBQConfig(GuiScreen parent)
	{
		super(parent, getCategories(ConfigHandler.config), BetterQuesting.MODID, false, false, BetterQuesting.NAME);
	}
	
	@SuppressWarnings({"rawtypes"})
	public static ArrayList<IConfigElement> getCategories(Configuration config)
	{
		ArrayList<IConfigElement> cats = new ArrayList<IConfigElement>();
		
		for(String s : config.getCategoryNames())
		{
			cats.add(new ConfigElement(config.getCategory(s)));
		}
		
		return cats;
	}
}

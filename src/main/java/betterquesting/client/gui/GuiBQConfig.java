package betterquesting.client.gui;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.client.config.IConfigElement;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import betterquesting.core.BetterQuesting;
import betterquesting.handlers.ConfigHandler;

@SideOnly(Side.CLIENT)
public class GuiBQConfig extends GuiConfig
{
	public GuiBQConfig(GuiScreen parent)
	{
		super(parent, getCategories(ConfigHandler.config), BetterQuesting.MODID, false, false, BetterQuesting.NAME);
	}
	
	public static List<IConfigElement> getCategories(Configuration config)
	{
		List<IConfigElement> cats = new ArrayList<>();
		
		for(String s : config.getCategoryNames())
		{
			cats.add(new ConfigElement(config.getCategory(s)));
		}
		
		return cats;
	}
}

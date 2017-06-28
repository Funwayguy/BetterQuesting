package adv_director.client.gui;

import java.util.ArrayList;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.client.config.IConfigElement;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import adv_director.core.AdvDirector;
import adv_director.handlers.ConfigHandler;

@SideOnly(Side.CLIENT)
public class GuiBQConfig extends GuiConfig
{
	public GuiBQConfig(GuiScreen parent)
	{
		super(parent, getCategories(ConfigHandler.config), AdvDirector.MODID, false, false, AdvDirector.NAME);
	}
	
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

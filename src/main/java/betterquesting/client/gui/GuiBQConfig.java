package betterquesting.client.gui;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GuiBQConfig extends Screen// extends GuiConfig
{
	public GuiBQConfig(Screen parent)
	{
	    super(new StringTextComponent("BQ CONFIG"));
		//super(parent, getCategories(ConfigHandler.config), BetterQuesting.MODID, false, false, BetterQuesting.NAME);
	}
	
	/*private static List<IConfigElement> getCategories(Configuration config)
	{
		List<IConfigElement> cats = new ArrayList<>();
		config.getCategoryNames().forEach((s) -> cats.add(new ConfigElement(config.getCategory(s))));
		return cats;
	}*/
}

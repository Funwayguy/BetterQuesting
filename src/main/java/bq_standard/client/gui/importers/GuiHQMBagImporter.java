package bq_standard.client.gui.importers;

import net.minecraft.client.resources.I18n;
import betterquesting.client.gui.GuiQuesting;
import betterquesting.client.gui.misc.GuiButtonQuesting;
import betterquesting.client.gui.misc.GuiEmbedded;
import betterquesting.client.themes.ThemeRegistry;
import betterquesting.utils.RenderUtils;
import bq_standard.importers.hqm.HQMBagImporter;

public class GuiHQMBagImporter extends GuiEmbedded
{
	GuiButtonQuesting btn;
	
	public GuiHQMBagImporter(GuiQuesting screen, int posX, int posY, int sizeX, int sizeY)
	{
		super(screen, posX, posY, sizeX, sizeY);
		btn = new GuiButtonQuesting(0, posX + sizeX/2 - 50, posY + sizeY - 20, 100, 20, I18n.format("betterquesting.btn.import"));
	}
	
	@Override
	public void drawGui(int mx, int my, float partialTick)
	{
		RenderUtils.drawSplitString(this.screen.mc.fontRenderer, I18n.format("bq_standard.importer.hqm_bag.desc"), this.posX + 8, this.posY, this.sizeX - 16, ThemeRegistry.curTheme().textColor().getRGB(), false);
		btn.drawButton(screen.mc, mx, my);
	}
	
	@Override
	public void mouseClick(int mx, int my, int button)
	{
		if(button == 0 && btn.mousePressed(screen.mc, mx, my))
		{
			HQMBagImporter.StartImport();
		}
	}
}

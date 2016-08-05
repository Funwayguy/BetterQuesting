package betterquesting.client.gui.editors;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.EnumChatFormatting;
import betterquesting.api.client.gui.premade.screens.GuiScreenThemed;
import betterquesting.api.client.io.IQuestIO;
import betterquesting.api.utils.RenderUtils;
import betterquesting.client.gui.misc.GuiButtonQuesting;
import betterquesting.importers.ImporterRegistry;

public class GuiImporters extends GuiScreenThemed
{
	List<IQuestIO> cachedImporters = new ArrayList<IQuestIO>();
	IQuestIO leftImp = null;
	IQuestIO rightImp = null;
	int scroll = 0;
	
	public GuiImporters(GuiScreen parent)
	{
		super(parent, "betterquesting.title.importers");
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void initGui()
	{
		super.initGui();
		
		scroll = 0;
		cachedImporters = ImporterRegistry.INSTANCE.getImporters();
		
		GuiButtonQuesting btn = new GuiButtonQuesting(1, guiLeft - 4, height/2 - 10, 20, 20, "<");
		this.buttonList.add(btn);
		btn = new GuiButtonQuesting(2, guiLeft + sizeX - 16, height/2 - 10, 20, 20, ">");
		this.buttonList.add(btn);
		
		UpdateScroll();
	}
	
	@Override
	public void drawScreen(int mx, int my, float partialTick)
	{
		super.drawScreen(mx, my, partialTick);
		
		if(leftImp != null)
		{
			String txt = EnumChatFormatting.UNDERLINE + I18n.format(leftImp.getUnlocalisedName());
			mc.fontRenderer.drawString(txt, guiLeft + 16 + (sizeX/2 - 24)/2 - mc.fontRenderer.getStringWidth(txt)/2, guiTop + 32, getTextColor());
			RenderUtils.drawSplitString(fontRendererObj, I18n.format(leftImp.getUnlocalisedDescrition()), guiLeft + 16, guiTop + 48, sizeX/2 - 24, getTextColor(), false);
			//leftGui.drawGui(mx, my, partialTick);
		}
		
		if(rightImp != null)
		{
			String txt = EnumChatFormatting.UNDERLINE + I18n.format(rightImp.getUnlocalisedName());
			mc.fontRenderer.drawString(txt, guiLeft + sizeX/2 + 8 + (sizeX/2 - 24)/2 - mc.fontRenderer.getStringWidth(txt)/2, guiTop + 32, getTextColor());
			RenderUtils.drawSplitString(fontRendererObj, I18n.format(rightImp.getUnlocalisedDescrition()), guiLeft + sizeX/2 + 8, guiTop + 48, sizeX/2 - 24, getTextColor(), false);
			//rightGui.drawGui(mx, my, partialTick);
		}
		
		RenderUtils.DrawLine(width/2, this.guiTop + 32, width/2, this.guiTop + sizeY - 32, 2F, getTextColor());
	}
	
	@Override
	public void actionPerformed(GuiButton button)
	{
		super.actionPerformed(button);
		
		if(button.id >= 1 && button.id <= 2)
		{
			if(button.id == 1)
			{
				scroll--;
			} else if(button.id == 2)
			{
				scroll++;
			}
			
			int size = Math.max(1, cachedImporters.size());
			scroll = (scroll%size + size)%size; // Fixes negative scroll
			UpdateScroll();
		}
	}
	
	public void UpdateScroll()
	{
		scroll = scroll%Math.max(1, cachedImporters.size());
		int s2 = (scroll + 1)%Math.max(1, cachedImporters.size());
		
		leftImp = null;
		rightImp = null;
		
		if(scroll < cachedImporters.size())
		{
			leftImp = cachedImporters.get(scroll);
			/*IGuiEmbedded gui = leftImp == null? null : leftImp.getGui(this, guiLeft + 16, guiTop + 48, sizeX/2 - 24, sizeY - 80);
			
			if(gui != null)
			{
				embedded.add(gui);
			}*/
		}
		
		if(scroll != s2 && s2 < cachedImporters.size())
		{
			rightImp = cachedImporters.get(s2);
			/*IGuiEmbedded gui = rightImp == null? null : rightImp.getGui(this, guiLeft + sizeX/2 + 8, guiTop + 48, sizeX/2 - 24, sizeY - 80);
			
			if(gui != null)
			{
				embedded.add(gui);
			}*/
		}
	}
}

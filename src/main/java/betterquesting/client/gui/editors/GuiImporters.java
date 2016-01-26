package betterquesting.client.gui.editors;

import java.util.ArrayList;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.EnumChatFormatting;
import betterquesting.client.gui.GuiQuesting;
import betterquesting.client.gui.misc.GuiButtonQuesting;
import betterquesting.client.gui.misc.GuiEmbedded;
import betterquesting.client.themes.ThemeRegistry;
import betterquesting.importers.ImporterBase;
import betterquesting.importers.ImporterRegistry;
import betterquesting.utils.RenderUtils;

public class GuiImporters extends GuiQuesting
{
	ArrayList<ImporterBase> cachedImporters = new ArrayList<ImporterBase>();
	ImporterBase leftImp = null;
	GuiEmbedded leftGui = null;
	ImporterBase rightImp = null;
	GuiEmbedded rightGui = null;
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
		cachedImporters = ImporterRegistry.getImporters();
		
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
		
		if(leftGui != null && rightImp != null)
		{
			String txt = EnumChatFormatting.UNDERLINE + I18n.format(leftImp.getUnlocalisedName());
			mc.fontRenderer.drawString(txt, guiLeft + 16 + (sizeX/2 - 24)/2 - mc.fontRenderer.getStringWidth(txt)/2, guiTop + 32, ThemeRegistry.curTheme().textColor().getRGB());
			leftGui.drawGui(mx, my, partialTick);
		}
		
		if(rightGui != null && rightImp != null)
		{
			String txt = EnumChatFormatting.UNDERLINE + I18n.format(rightImp.getUnlocalisedName());
			mc.fontRenderer.drawString(txt, guiLeft + sizeX/2 + 8 + (sizeX/2 - 24)/2 - mc.fontRenderer.getStringWidth(txt)/2, guiTop + 32, ThemeRegistry.curTheme().textColor().getRGB());
			rightGui.drawGui(mx, my, partialTick);
		}
		
		RenderUtils.DrawLine(width/2, this.guiTop + 32, width/2, this.guiTop + sizeY - 32, 2F, ThemeRegistry.curTheme().textColor());
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
	
	@Override
    protected void keyTyped(char character, int keyCode)
    {
        super.keyTyped(character, keyCode);
		
		if(leftGui != null)
		{
			leftGui.keyTyped(character, keyCode);;
		}
		
		if(rightGui != null)
		{
			rightGui.keyTyped(character, keyCode);;
		}
    }
	
	@Override
	public void handleMouseInput()
	{
		super.handleMouseInput();
		
		if(leftGui != null)
		{
			leftGui.handleMouse();
		}
		
		if(rightGui != null)
		{
			rightGui.handleMouse();
		}
	}
	
	public void UpdateScroll()
	{
		scroll = scroll%Math.max(1, cachedImporters.size());
		int s2 = (scroll + 1)%Math.max(1, cachedImporters.size());
		
		leftImp = null;
		leftGui = null;
		rightImp = null;
		rightGui = null;
		
		if(scroll < cachedImporters.size())
		{
			leftImp = cachedImporters.get(scroll);
			leftGui = leftImp.getGui(this, guiLeft + 16, guiTop + 48, sizeX/2 - 24, sizeY - 80);
		}
		
		if(scroll != s2 && s2 < cachedImporters.size())
		{
			rightImp = cachedImporters.get(s2);
			rightGui = rightImp.getGui(this, guiLeft + sizeX/2 + 8, guiTop + 48, sizeX/2 - 24, sizeY - 80);
		}
	}
}

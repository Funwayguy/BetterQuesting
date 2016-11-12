package betterquesting.client.gui.editors;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiLabel;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MathHelper;
import org.lwjgl.opengl.GL11;
import betterquesting.api.client.gui.GuiScreenThemed;
import betterquesting.api.client.gui.IGuiEmbedded;
import betterquesting.api.client.gui.controls.GuiButtonThemed;
import betterquesting.api.client.gui.misc.INeedsRefresh;
import betterquesting.api.client.gui.misc.IVolatileScreen;
import betterquesting.api.client.gui.quest.QuestLineButtonTree;
import betterquesting.api.client.toolbox.IToolboxTab;
import betterquesting.api.quests.IQuestLine;
import betterquesting.api.utils.RenderUtils;
import betterquesting.client.gui.GuiQuestLinesEmbedded;
import betterquesting.database.QuestLineDatabase;
import betterquesting.registry.ToolboxRegistry;

public class GuiQuestLineDesigner extends GuiScreenThemed implements IVolatileScreen, INeedsRefresh
{
	private List<IToolboxTab> tabList = new ArrayList<IToolboxTab>();
	private int lineID = -1;
	private IQuestLine qLine;
	private GuiQuestLinesEmbedded qlGui;
	private int tabIndex = 0;
	private IToolboxTab toolTab = null;
	private IGuiEmbedded tabGui = null;
	
	public GuiQuestLineDesigner(GuiScreen parent, IQuestLine qLine)
	{
		super(parent, "betterquesting.title.designer"); // This title won't be shown but for the sake of labels...
		this.qLine = qLine;
		this.lineID = QuestLineDatabase.INSTANCE.getKey(qLine);
	}
	
	public GuiQuestLinesEmbedded getEmbeddedGui()
	{
		return qlGui;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void initGui()
	{
		super.initGui();
		
		this.sizeX -= 96;
		((GuiButton)this.buttonList.get(0)).xPosition = guiLeft + sizeX/2 - 100;
		
		GuiQuestLinesEmbedded oldGui = qlGui;
		qlGui = new GuiQuestLinesEmbedded(guiLeft + 16, guiTop + 16, sizeX - 32, sizeY - 32);
		qlGui.setQuestLine(new QuestLineButtonTree(qLine), true);
		
		if(oldGui != null) // Preserve old settings
		{
			embedded.remove(oldGui);
			qlGui.copySettings(oldGui);
		}
		
		qlGui.clampScroll();
		embedded.add(qlGui);
		
		this.tabList.clear();
		this.tabList.addAll(ToolboxRegistry.INSTANCE.getAllTools());
		
		this.tabIndex = MathHelper.clamp_int(tabIndex, 0, Math.max(0, tabList.size() - 1));
		
		for(IToolboxTab tab : tabList)
		{
			tab.initTools(this.qlGui);
		}
		
		if(tabList.size() > 0)
		{
			toolTab = tabList.get(tabIndex);
			
			tabGui = toolTab.getTabGui(guiLeft + sizeX + 16, guiTop + 32, 64, sizeY - 48);
			
			if(tabGui != null)
			{
				embedded.add(tabGui);
			}
		}
		
		GuiButtonThemed btnLeft = new GuiButtonThemed(1, guiLeft + sizeX, guiTop + 16, 16, 16, "<", true);
		GuiButtonThemed btnRight = new GuiButtonThemed(2, guiLeft + sizeX + 80, guiTop + 16, 16, 16, ">", true);
		
		if(tabList.size() <= 1)
		{
			btnLeft.enabled = false;
			btnRight.enabled = false;
		}
		
		buttonList.add(btnLeft);
		buttonList.add(btnRight);
	}
	
	/**
	 * Modified version of super method to support extra toolbar
	 */
	public void drawScreen_(int mx, int my, float partialTick)
	{
		this.drawDefaultBackground();
		
		this.mc.renderEngine.bindTexture(currentTheme().getGuiTexture());
		
		for(int i = 0; i < 96; i += 16)
		{
			for(int j = 0; j < sizeY; j += 16)
			{
				int tx = 16;
				int ty = 16;
				
				if(i == 0)
				{
					tx -= 16;
				} else if(i == 80)
				{
					tx += 16;
				}
				
				if(j == 0)
				{
					ty -= 16;
				} else if(j == sizeY - 16)
				{
					ty += 16;
				}
				
				this.drawTexturedModalRect(guiLeft + sizeX + i, guiTop + j, tx, ty, 16, 16);
			}
		}
		
		for(int i = 0; i < this.sizeX; i += 16)
		{
			for(int j = 0; j < this.sizeY; j += 16)
			{
				int tx = 16;
				int ty = 16;
				
				if(i == 0)
				{
					tx -= 16;
				} else if(i == this.sizeX - 16)
				{
					tx += 16;
				}
				
				if(j == 0)
				{
					ty -= 16;
				} else if(j == this.sizeY - 16)
				{
					ty += 16;
				}
				
				this.drawTexturedModalRect(i + this.guiLeft, j + this.guiTop, tx, ty, 16, 16);
			}
		}
		
		String tmp = I18n.format("betterquesting.title.designer");
		this.fontRendererObj.drawString(EnumChatFormatting.BOLD + tmp, this.guiLeft + (sizeX/2) - this.fontRendererObj.getStringWidth(tmp)/2, this.guiTop + 18, getTextColor(), false);
		
		GL11.glColor4f(1F, 1F, 1F, 1F); // Lots of these because color commonly leaks everywhere -_-
		
		for(IGuiEmbedded e : embedded)
		{
			e.drawBackground(mx, my, partialTick);
		}
		
		// === START: GuiScreen ===
		
		GL11.glColor4f(1F, 1F, 1F, 1F);
        int k;
        
        for (k = 0; k < this.buttonList.size(); ++k)
        {
            ((GuiButton)this.buttonList.get(k)).drawButton(this.mc, mx, my);
        }
        
        for (k = 0; k < this.labelList.size(); ++k)
        {
            ((GuiLabel)this.labelList.get(k)).func_146159_a(this.mc, mx, my);
        }
        
        // === END: GuiScreen ===

		GL11.glColor4f(1F, 1F, 1F, 1F);
		
		for(IGuiEmbedded e : embedded)
		{
			e.drawForeground(mx, my, partialTick);
		}
		
		this.mc.renderEngine.bindTexture(currentTheme().getGuiTexture());
		GL11.glColor4f(1F, 1F, 1F, 1F);
	}
	
	@Override
	public void drawScreen(int mx, int my, float partialTick)
	{
		drawScreen_(mx, my, partialTick);
		
		RenderUtils.DrawLine(guiLeft + sizeX + 16, guiTop + 32, guiLeft + sizeX + 80, guiTop + 32, partialTick, getTextColor());
		
		if(toolTab != null)
		{
			String tabTitle = EnumChatFormatting.UNDERLINE + I18n.format(toolTab.getUnlocalisedName());
			this.fontRendererObj.drawString(tabTitle, guiLeft + sizeX + 48 - fontRendererObj.getStringWidth(tabTitle)/2, guiTop + 16 + 2, getTextColor(), false);
		}
	}
	
	@Override
	public void refreshGui()
	{
		qLine = QuestLineDatabase.INSTANCE.getValue(lineID);
		
		if(qLine == null)
		{
			mc.displayGuiScreen(parent);
			return;
		}
		
		qlGui.setQuestLine(new QuestLineButtonTree(qLine), false);
		qlGui.setActiveTool(qlGui.getActiveTool());
	}
	
	@Override
	public void actionPerformed(GuiButton button)
	{
		super.actionPerformed(button);
		
		int ts = tabList.size();
		
		if(ts > 1)
		{
			boolean flag = false;
			
			if(button.id == 1)
			{
				tabIndex = ((tabIndex - 1)%ts + ts)%ts;
				flag = true;
			} else if(button.id == 2)
			{
				tabIndex = (tabIndex + 1)%ts;
				flag = true;
			}
			
			if(flag)
			{
				toolTab = tabList.get(tabIndex);
				
				if(tabGui != null)
				{
					embedded.remove(tabGui);
				}
				
				tabGui = toolTab.getTabGui(guiLeft + sizeX + 16, guiTop + 32, 64, sizeY - 48);
				
				if(tabGui != null)
				{
					embedded.add(tabGui);
				}
			}
		}
	}
}

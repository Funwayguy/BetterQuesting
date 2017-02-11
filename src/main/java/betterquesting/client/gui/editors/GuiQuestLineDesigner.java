package betterquesting.client.gui.editors;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextFormatting;
import betterquesting.api.client.gui.GuiScreenThemed;
import betterquesting.api.client.gui.QuestLineButtonTree;
import betterquesting.api.client.gui.controls.GuiButtonThemed;
import betterquesting.api.client.gui.misc.IGuiEmbedded;
import betterquesting.api.client.gui.misc.INeedsRefresh;
import betterquesting.api.client.gui.misc.IVolatileScreen;
import betterquesting.api.client.toolbox.IToolboxTab;
import betterquesting.api.questing.IQuestLine;
import betterquesting.api.utils.RenderUtils;
import betterquesting.client.gui.GuiQuestLinesEmbedded;
import betterquesting.client.toolbox.ToolboxRegistry;
import betterquesting.questing.QuestLineDatabase;

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
		
		this.tabIndex = MathHelper.clamp(tabIndex, 0, Math.max(0, tabList.size() - 1));
		
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
	
	@Override
	public void drawBackPanel(int mx, int my, float partialTick)
	{
		this.drawDefaultBackground();
		
		currentTheme().getRenderer().drawThemedPanel(guiLeft + sizeX, guiTop, 96, sizeY);
		currentTheme().getRenderer().drawThemedPanel(guiLeft, guiTop, sizeX, sizeY);
		
		String tmp = I18n.format("betterquesting.title.designer");
		this.fontRendererObj.drawString(TextFormatting.BOLD + tmp, this.guiLeft + (sizeX/2) - this.fontRendererObj.getStringWidth(tmp)/2, this.guiTop + 18, getTextColor(), false);
		
		GlStateManager.color(1F, 1F, 1F, 1F);
		
		for(IGuiEmbedded e : embedded)
		{
			GlStateManager.pushMatrix();
			e.drawBackground(mx, my, partialTick);
			GlStateManager.popMatrix();
			
			GlStateManager.color(1F, 1F, 1F, 1F);
		}
		
		RenderUtils.DrawLine(guiLeft + sizeX + 16, guiTop + 32, guiLeft + sizeX + 80, guiTop + 32, partialTick, getTextColor());
		
		if(toolTab != null)
		{
			String tabTitle = TextFormatting.UNDERLINE + I18n.format(toolTab.getUnlocalisedName());
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

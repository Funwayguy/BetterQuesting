package betterquesting.client.gui.help;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.MathHelper;
import org.lwjgl.input.Mouse;
import betterquesting.client.gui.GuiQuesting;
import betterquesting.client.gui.misc.GuiButtonQuesting;
import betterquesting.client.gui.misc.GuiScrollingText;

public class GuiQuestingHelp extends GuiQuesting
{
	GuiScrollingText curPage = null;
	static ArrayList<HelpTopic> helpTopics = new ArrayList<HelpTopic>();
	int leftScroll = 0;
	int maxRows = 0;
	
	public GuiQuestingHelp(GuiScreen parent)
	{
		super(parent, "item.betterquesting.guide.name");
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public void initGui()
	{
		super.initGui();
		
		maxRows = (sizeY - 64)/20;
		
		for(int i = 0; i < maxRows; i++)
		{
			buttonList.add(new GuiButtonQuesting(i + 1, guiLeft + 16, guiTop + 32 + i*20, 100, 20, "NULL"));
		}
		
		RefreshColumns();
	}
	
	@Override
	public void drawScreen(int mx, int my, float partialTick)
	{
		super.drawScreen(mx, my, partialTick);

		// Left scroll bar
		this.drawTexturedModalRect(guiLeft + 116, this.guiTop + 32, 248, 0, 8, 20);
		int s = 20;
		while(s < (maxRows - 1) * 20)
		{
			this.drawTexturedModalRect(guiLeft + 116, this.guiTop + 32 + s, 248, 20, 8, 20);
			s += 20;
		}
		this.drawTexturedModalRect(guiLeft + 116, this.guiTop + 32 + s, 248, 40, 8, 20);
		this.drawTexturedModalRect(guiLeft + 116, this.guiTop + 32 + (int)Math.max(0, s * (float)leftScroll/(helpTopics.size() - maxRows)), 248, 60, 8, 20);
		
		if(curPage != null)
		{
			curPage.drawScreen(mx, my, partialTick);
		}
	}
	
	@Override
	public void actionPerformed(GuiButton btn)
	{
		super.actionPerformed(btn);
		
		if(btn.id > 0)
		{
			int n1 = btn.id - 1; // Line index
			int n2 = n1/maxRows; // Line listing (0 = line, 1 = delete)
			int n3 = n1%maxRows + leftScroll; // Quest list index
			
			if(n2 == 0)
			{
				if(n3 >= 0 && n3 < helpTopics.size())
				{
					curPage = new GuiScrollingText(this, sizeX - 148, sizeY - 64, guiTop + 32, guiLeft + 124, helpTopics.get(n3).getParagraph());
				} else
				{
					curPage = null;
				}
				
				RefreshColumns();
			}
		}
	}
	
    /**
     * Handles mouse input.
     */
	@Override
    public void handleMouseInput()
    {
		super.handleMouseInput();
		
        int mx = Mouse.getEventX() * this.width / this.mc.displayWidth;
        int my = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;
        int SDX = (int)-Math.signum(Mouse.getEventDWheel());
        
        if(SDX != 0 && isWithin(mx, my, this.guiLeft, this.guiTop, sizeX/2, sizeY))
        {
    		leftScroll = Math.max(0, MathHelper.clamp_int(leftScroll + SDX, 0, helpTopics.size() - maxRows));
    		RefreshColumns();
        }
    }
	
	public void RefreshColumns()
	{
		leftScroll = Math.max(0, MathHelper.clamp_int(leftScroll, 0, helpTopics.size() - maxRows));

		@SuppressWarnings("unchecked")
		List<GuiButton> btnList = this.buttonList;
		
		for(int i = 1; i < btnList.size(); i++)
		{
			GuiButton btn = btnList.get(i);
			int n1 = btn.id - 1; // Line index
			int n2 = n1/maxRows; // Line listing (0 = line, 1 = delete)
			int n3 = n1%maxRows + leftScroll; // Quest list index
			
			if(n2 == 0)
			{
				if(n3 >= 0 && n3 < helpTopics.size())
				{
					btn.displayString = I18n.format(helpTopics.get(n3).getButton());
					btn.enabled = btn.visible = true;
				} else
				{
					btn.displayString = "NULL";
					btn.enabled = btn.visible = false;
				}
			} else if(n2 == 1)
			{
				btn.enabled = btn.visible = n3 >= 0 && n3 < helpTopics.size();
			}
		}
	}
	
	public static void registerTopic(String btn, String paragraph)
	{
		helpTopics.add(new HelpTopic(btn, paragraph));
	}
	
	static
	{
		registerTopic("betterquesting.btn.help1", "betterquesting.help.page1");
		registerTopic("betterquesting.btn.help2", "betterquesting.help.page2");
		registerTopic("betterquesting.btn.help3", "betterquesting.help.page3");
		registerTopic("betterquesting.btn.help4", "betterquesting.help.page4");
		registerTopic("betterquesting.btn.help5", "betterquesting.help.page5");
		registerTopic("betterquesting.btn.help6", "betterquesting.help.page6");
		registerTopic("betterquesting.btn.help7", "betterquesting.help.page7");
		registerTopic("betterquesting.btn.help8", "betterquesting.help.page8");
	}
	
	public static class HelpTopic
	{
		String btn;
		String txt;
		
		public HelpTopic(String btn, String txt)
		{
			this.btn = btn;
			this.txt = txt;
		}
		
		public String getButton()
		{
			return I18n.format(btn);
		}
		
		public String getParagraph()
		{
			return I18n.format(txt);
		}
	}
}

package betterquesting.client.gui.help;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import betterquesting.client.gui.GuiQuesting;
import betterquesting.client.gui.misc.GuiButtonQuesting;
import betterquesting.client.gui.misc.GuiScrollingText;

public class GuiQuestingHelp extends GuiQuesting
{
	GuiScrollingText curPage = null;
	static ArrayList<Entry<String,String>> helpTopics = new ArrayList<Entry<String,String>>();
	
	public GuiQuestingHelp(GuiScreen parent)
	{
		super(parent, "item.betterquesting.guide.name");
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public void initGui()
	{
		super.initGui();
		
		for(int i = 0; i < helpTopics.size(); i++)
		{
			buttonList.add(new GuiButtonQuesting(i + 1, guiLeft + 16, guiTop + 32 + i*20, 100, 20, I18n.format(helpTopics.get(i).getKey())));
		}
	}
	
	@Override
	public void drawScreen(int mx, int my, float partialTick)
	{
		super.drawScreen(mx, my, partialTick);
		
		if(curPage != null)
		{
			curPage.drawScreen(mx, my, partialTick);
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void actionPerformed(GuiButton button)
	{
		super.actionPerformed(button);
		
		if(button.id > 0)
		{
			for(GuiButton btn : (ArrayList<GuiButton>)buttonList)
			{
				btn.enabled = true;
			}
			
			button.enabled = false;
			curPage = new GuiScrollingText(this, sizeX - 148, sizeY - 64, guiTop + 32, guiLeft + 124, I18n.format(helpTopics.get(button.id - 1).getValue()));
		}
	}
	
	static
	{
		HashMap<String,String> tmp = new HashMap<String,String>();
		tmp.put("betterquesting.btn.help1", "betterquesting.help.page1");
		tmp.put("betterquesting.btn.help2", "betterquesting.help.page2");
		tmp.put("betterquesting.btn.help3", "betterquesting.help.page3");
		tmp.put("betterquesting.btn.help4", "betterquesting.help.page4");
		tmp.put("betterquesting.btn.help5", "betterquesting.help.page5");
		tmp.put("betterquesting.btn.help6", "betterquesting.help.page6");
		tmp.put("betterquesting.btn.help7", "betterquesting.help.page7");
		helpTopics = new ArrayList<Entry<String,String>>(tmp.entrySet());
	}
}

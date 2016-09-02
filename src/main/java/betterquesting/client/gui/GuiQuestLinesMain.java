package betterquesting.client.gui;

import java.util.ArrayList;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.MathHelper;
import org.lwjgl.opengl.GL11;
import betterquesting.api.client.gui.INeedsRefresh;
import betterquesting.api.client.gui.premade.controls.GuiButtonQuestInstance;
import betterquesting.api.client.gui.premade.controls.GuiButtonThemed;
import betterquesting.api.client.gui.premade.screens.GuiScreenThemed;
import betterquesting.api.quests.IQuestLineContainer;
import betterquesting.client.gui.editors.GuiQuestLineEditorA;
import betterquesting.client.gui.misc.GuiButtonQuestLine;
import betterquesting.client.gui.misc.GuiScrollingText;
import betterquesting.quests.QuestLineDatabase;
import betterquesting.quests.QuestSettings;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiQuestLinesMain extends GuiScreenThemed implements INeedsRefresh
{
	/**
	 * Last opened quest screen from here
	 */
	public static GuiQuestInstance bookmarked;
	
	GuiButtonQuestLine selected;
	ArrayList<GuiButtonQuestLine> qlBtns = new ArrayList<GuiButtonQuestLine>();
	int listScroll = 0;
	int maxRows = 0;
	GuiQuestLinesEmbedded qlGui;
	GuiScrollingText qlDesc;
	
	public GuiQuestLinesMain(GuiScreen parent)
	{
		super(parent, I18n.format("betterquesting.title.quest_lines"));
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void initGui()
	{
		super.initGui();
		
		bookmarked = null;
		qlBtns.clear();
		
		listScroll = 0;
		maxRows = (sizeY - 64)/20;
		
		if(QuestSettings.INSTANCE.isEditMode())
		{
			((GuiButton)this.buttonList.get(0)).xPosition = this.width/2 - 100;
			((GuiButton)this.buttonList.get(0)).width = 100;
		}
		
		GuiButtonThemed btnEdit = new GuiButtonThemed(1, this.width/2, this.guiTop + this.sizeY - 16, 100, 20, I18n.format("betterquesting.btn.edit"), true);
		btnEdit.enabled = btnEdit.visible = QuestSettings.INSTANCE.isEditMode();
		this.buttonList.add(btnEdit);
		
		GuiQuestLinesEmbedded oldGui = qlGui;
		qlGui = new GuiQuestLinesEmbedded(guiLeft + 174, guiTop + 32, sizeX - (32 + 150 + 8), sizeY - 64 - 32);
		qlDesc = new GuiScrollingText(guiLeft + 174, guiTop + 32 + sizeY - 64 - 32, sizeX - (32 + 150 + 8), 48);
		
		boolean reset = true;
		
		int i = 0;
		for(int j = 0; j < QuestLineDatabase.INSTANCE.getAllValues().size(); j++)
		{
			IQuestLineContainer line = QuestLineDatabase.INSTANCE.getAllValues().get(j);
			GuiButtonQuestLine btnLine = new GuiButtonQuestLine(buttonList.size(), this.guiLeft + 16, this.guiTop + 32 + i, 142, 20, line);
			btnLine.enabled = line.size() <= 0 || QuestSettings.INSTANCE.isEditMode();
			
			if(selected != null && selected.getQuestLine().getUnlocalisedName().equals(line.getUnlocalisedName()))
			{
				reset = false;
				selected = btnLine;
			}
			
			if(!btnLine.enabled)
			{
				for(GuiButtonQuestInstance p : btnLine.getButtonTree().getButtonTree())
				{
					if((p.getQuest().isComplete(mc.thePlayer.getUniqueID()) || p.getQuest().isUnlocked(mc.thePlayer.getUniqueID())) && (selected == null || selected.getQuestLine() != line))
					{
						btnLine.enabled = true;
						break;
					}
				}
			}
			
			buttonList.add(btnLine);
			qlBtns.add(btnLine);
			i += 20;
		}
		
		if(reset || selected == null)
		{
			selected = null;
		} else
		{
			qlDesc.SetText(selected.getQuestLine().getUnlocalisedDescription());
			qlGui.setQuestLine(selected.getButtonTree(), true);
		}
		
		if(oldGui != null) // Preserve old settings
		{
			qlGui.copySettings(oldGui);
			this.embedded.remove(oldGui);
		}
		
		this.embedded.add(qlGui);
		
		UpdateScroll();
	}
	
	@Override
	public void refreshGui()
	{
		initGui();
	}
	
	@Override
	public void drawBackPanel(int mx, int my, float partialTick)
	{
		super.drawBackPanel(mx, my, partialTick);
		
		GL11.glColor4f(1F, 1F, 1F, 1F);
		
		this.mc.renderEngine.bindTexture(currentTheme().getGuiTexture());
		
		this.drawTexturedModalRect(this.guiLeft + 16 + 142, this.guiTop + 32, 248, 0, 8, 20);
		int i = 20;
		while(i < sizeY - 84)
		{
			this.drawTexturedModalRect(this.guiLeft + 16 + 142, this.guiTop + 32 + i, 248, 20, 8, 20);
			i += 20;
		}
		this.drawTexturedModalRect(this.guiLeft + 16 + 142, this.guiTop + 32 + i, 248, 40, 8, 20);
		this.drawTexturedModalRect(guiLeft + 16 + 142, this.guiTop + 32 + (int)Math.max(0, i * (float)listScroll/(float)(qlBtns.size() - maxRows)), 248, 60, 8, 20);
	}
	
	@Override
	public void actionPerformed(GuiButton button)
	{
		super.actionPerformed(button);
		
		if(button.id == 1)
		{
			mc.displayGuiScreen(new GuiQuestLineEditorA(this));
			// Quest line editor
		} else if(button instanceof GuiButtonQuestLine)
		{
			if(selected != null)
			{
				selected.enabled = true;
			}
			
			button.enabled = false;
			
			selected = (GuiButtonQuestLine)button;
			
			if(selected != null)
			{
				qlDesc.SetText(I18n.format(selected.getQuestLine().getUnlocalisedDescription()));
				qlGui.setQuestLine(selected.getButtonTree(), true);
			}
		}
	}
	
	@Override
	public void mouseScroll(int mx, int my, int scroll)
	{
		super.mouseScroll(mx, my, scroll);
		
		if(scroll != 0 && isWithin(mx, my, this.guiLeft, this.guiTop, 166, sizeY))
        {
    		listScroll = Math.max(0, MathHelper.clamp_int(listScroll + scroll, 0, qlBtns.size() - maxRows));
    		UpdateScroll();
        }
	}
	
	public void UpdateScroll()
	{
		// All buttons are required to be present due to the button trees
		// These are hidden and moved as necessary
		for(int i = 0; i < qlBtns.size(); i++)
		{
			GuiButtonQuestLine btn = qlBtns.get(i);
			int n = i - listScroll;
			
			if(n < 0 || n >= maxRows)
			{
				btn.visible = false;
			} else
			{
				btn.visible = true;
				btn.yPosition = this.guiTop + 32 + n*20;
			}
		}
	}
}

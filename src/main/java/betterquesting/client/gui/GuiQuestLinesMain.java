package betterquesting.client.gui;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import betterquesting.api.client.gui.GuiScreenThemed;
import betterquesting.api.client.gui.controls.GuiButtonQuestInstance;
import betterquesting.api.client.gui.controls.GuiButtonQuestLine;
import betterquesting.api.client.gui.controls.GuiButtonThemed;
import betterquesting.api.client.gui.lists.GuiScrollingButtons;
import betterquesting.api.client.gui.lists.GuiScrollingText;
import betterquesting.api.client.gui.misc.INeedsRefresh;
import betterquesting.api.client.gui.quest.QuestLineButtonTree;
import betterquesting.api.quests.IQuestLine;
import betterquesting.client.gui.editors.GuiQuestLineEditorA;
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
	
	private List<Integer> lineIDs = new ArrayList<Integer>();
	
	private GuiButtonQuestLine selected;
	private GuiScrollingButtons qlBtnList;
	private GuiQuestLinesEmbedded qlGui;
	private GuiScrollingText qlDesc;
	
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
		lineIDs = QuestLineDatabase.INSTANCE.getAllKeys();
		
		if(QuestSettings.INSTANCE.canUserEdit(mc.thePlayer))
		{
			((GuiButton)this.buttonList.get(0)).xPosition = this.width/2 - 100;
			((GuiButton)this.buttonList.get(0)).width = 100;
		}
		
		GuiButtonThemed btnEdit = new GuiButtonThemed(1, this.width/2, this.guiTop + this.sizeY - 16, 100, 20, I18n.format("betterquesting.btn.edit"), true);
		btnEdit.enabled = btnEdit.visible = QuestSettings.INSTANCE.canUserEdit(mc.thePlayer);
		this.buttonList.add(btnEdit);
		
		GuiQuestLinesEmbedded oldGui = qlGui;
		qlGui = new GuiQuestLinesEmbedded(guiLeft + 174, guiTop + 32, sizeX - (32 + 150 + 8), sizeY - 64 - 32);
		qlDesc = new GuiScrollingText(mc, guiLeft + 174, guiTop + 32 + sizeY - 64 - 32, sizeX - (32 + 150 + 8), 48);
		qlBtnList = new GuiScrollingButtons(mc, guiLeft + 16, guiTop + 32, 150, sizeY - 48);
		
		boolean reset = true;
		
		for(int j = 0; j < lineIDs.size(); j++)
		{
			int lID = lineIDs.get(j);
			IQuestLine line = QuestLineDatabase.INSTANCE.getValue(lID);
			
			if(line == null)
			{
				continue;
			}
			
			GuiButtonQuestLine btnLine = new GuiButtonQuestLine(2, 0, 0, 142, 20, line);
			btnLine.enabled = line.size() <= 0 || QuestSettings.INSTANCE.canUserEdit(mc.thePlayer);
			
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
			
			qlBtnList.addButtonRow(btnLine);
		}
		
		if(reset || selected == null)
		{
			selected = null;
		} else
		{
			qlDesc.SetText(I18n.format(selected.getQuestLine().getUnlocalisedDescription()));
			qlGui.setQuestLine(selected.getButtonTree(), true);
		}
		
		if(oldGui != null) // Preserve old settings
		{
			qlGui.copySettings(oldGui);
			this.embedded.remove(oldGui);
		}
		
		this.embedded.add(qlGui);
		this.embedded.add(qlDesc);
		this.embedded.add(qlBtnList);
	}
	
	@Override
	public void refreshGui()
	{
		initGui();
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
	public void mouseClicked(int mx, int my, int click)
	{
		super.mouseClicked(mx, my, click);
		
		if(click != 0)
		{
			return;
		}
		
		QuestLineButtonTree tree = qlGui.getQuestLine();
		
		if(tree != null)
		{
			GuiButtonQuestInstance btn = tree.getButtonAt(qlGui.getRelativeX(mx), qlGui.getRelativeY(my));
			
			if(btn != null)
			{
				btn.func_146113_a(mc.getSoundHandler());
				bookmarked = new GuiQuestInstance(this, btn.getQuest());
				mc.displayGuiScreen(bookmarked);
				return;
			}
		}
		
		GuiButtonThemed btn = qlBtnList.getButtonUnderMouse(mx, my);
		
		if(btn != null)
		{
			btn.func_146113_a(mc.getSoundHandler());
			this.actionPerformed(btn);
		}
	}
}

package betterquesting.client.gui;

import java.util.ArrayList;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.MathHelper;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import betterquesting.client.gui.editors.GuiQuestLineEditorA;
import betterquesting.client.gui.misc.GuiButtonQuestInstance;
import betterquesting.client.gui.misc.GuiButtonQuestLine;
import betterquesting.client.gui.misc.GuiButtonQuesting;
import betterquesting.client.gui.misc.GuiScrollingText;
import betterquesting.client.themes.ThemeRegistry;
import betterquesting.quests.QuestDatabase;
import betterquesting.quests.QuestLine;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiQuestLinesMain extends GuiQuesting
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
		
		if(QuestDatabase.editMode)
		{
			((GuiButton)this.buttonList.get(0)).xPosition = this.width/2 - 100;
			((GuiButton)this.buttonList.get(0)).width = 100;
		}
		
		GuiButtonQuesting btnEdit = new GuiButtonQuesting(1, this.width/2, this.guiTop + this.sizeY - 16, 100, 20, I18n.format("betterquesting.btn.edit"));
		btnEdit.enabled = btnEdit.visible = QuestDatabase.editMode;
		this.buttonList.add(btnEdit);
		
		GuiQuestLinesEmbedded oldGui = qlGui;
		qlGui = new GuiQuestLinesEmbedded(this, guiLeft + 174, guiTop + 32, sizeX - (32 + 150 + 8), sizeY - 64 - 32);
		qlDesc = new GuiScrollingText(this, sizeX - (32 + 150 + 8), 48, guiTop + 32 + sizeY - 64 - 32, guiLeft + 174);
		
		boolean reset = true;
		
		int i = 0;
		for(int j = 0; j < QuestDatabase.questLines.size(); j++)
		{
			QuestLine line = QuestDatabase.questLines.get(j);
			GuiButtonQuestLine btnLine = new GuiButtonQuestLine(buttonList.size(), this.guiLeft + 16, this.guiTop + 32 + i, 142, 20, line);
			btnLine.enabled = line.questList.size() <= 0 || QuestDatabase.editMode;
			
			if(selected != null && selected.line.name.equals(line.name))
			{
				reset = false;
				selected = btnLine;
			}
			
			if(!btnLine.enabled)
			{
				for(GuiButtonQuestInstance p : btnLine.tree.buttonTree)
				{
					if((p.quest.isComplete(mc.thePlayer.getUniqueID()) || p.quest.isUnlocked(mc.thePlayer.getUniqueID())) && (selected == null || selected.line != line))
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
			qlDesc.SetText(selected.line.description);
			qlGui.setQuestLine(selected.tree);
		}
		
		if(oldGui != null) // Preserve old settings
		{
			qlGui.copySettings(oldGui);
		}
		
		UpdateScroll();
	}
	
	@Override
	public void drawScreen(int mx, int my, float partialTick)
	{
		super.drawScreen(mx, my, partialTick);
		
		if(QuestDatabase.updateUI)
		{
			QuestDatabase.updateUI = false;
			this.initGui();
		}
		
		GL11.glColor4f(1F, 1F, 1F, 1F);
		
		this.mc.renderEngine.bindTexture(ThemeRegistry.curTheme().guiTexture());
		
		this.drawTexturedModalRect(this.guiLeft + 16 + 142, this.guiTop + 32, 248, 0, 8, 20);
		int i = 20;
		while(i < sizeY - 84)
		{
			this.drawTexturedModalRect(this.guiLeft + 16 + 142, this.guiTop + 32 + i, 248, 20, 8, 20);
			i += 20;
		}
		this.drawTexturedModalRect(this.guiLeft + 16 + 142, this.guiTop + 32 + i, 248, 40, 8, 20);
		this.drawTexturedModalRect(guiLeft + 16 + 142, this.guiTop + 32 + (int)Math.max(0, i * (float)listScroll/(float)(qlBtns.size() - maxRows)), 248, 60, 8, 20);
		
		
		if(qlGui != null && qlDesc != null)
		{
			GL11.glPushMatrix();
			GL11.glColor4f(1F, 1F, 1F, 1f);
			qlDesc.drawScreen(mx, my, partialTick);
			GL11.glPopMatrix();
			
			GL11.glPushMatrix();
			GL11.glColor4f(1F, 1F, 1F, 1f);
			GL11.glEnable(GL11.GL_DEPTH_TEST);
			qlGui.drawGui(mx, my, partialTick);
			GL11.glPopMatrix();
		}
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
				qlDesc.SetText(I18n.format(selected.line.description));
				qlGui.setQuestLine(selected.tree);
			}
		}
	}
	
	@Override
    protected void mouseClicked(int mx, int my, int type)
    {
		super.mouseClicked(mx, my, type);
		
		if(qlGui != null && type == 0)
		{
			GuiButtonQuestInstance qBtn = qlGui.getClickedQuest(mx, my);
			
			if(qBtn != null)
			{
				qBtn.func_146113_a(this.mc.getSoundHandler());
				bookmarked = new GuiQuestInstance(this, qBtn.quest);
				mc.displayGuiScreen(bookmarked);
			}
		}
    }
	
	@Override
	public void handleMouseInput()
    {
		super.handleMouseInput();
		
        int mx = Mouse.getEventX() * this.width / this.mc.displayWidth;
        int my = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;
        int SDX = (int)-Math.signum(Mouse.getEventDWheel());
        
        if(SDX != 0 && isWithin(mx, my, this.guiLeft, this.guiTop, 166, sizeY))
        {
    		listScroll = Math.max(0, MathHelper.clamp_int(listScroll + SDX, 0, qlBtns.size() - maxRows));
    		UpdateScroll();
        }
        
        if(qlGui != null)
        {
        	qlGui.handleMouse();
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

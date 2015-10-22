package betterquesting.client;

import java.awt.Color;
import java.util.ArrayList;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MathHelper;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import betterquesting.client.buttons.GuiButtonQuesting;
import betterquesting.core.BetterQuesting;
import betterquesting.importer.HQMImporter;
import betterquesting.network.PacketQuesting;
import betterquesting.quests.QuestDatabase;
import betterquesting.quests.QuestInstance;
import betterquesting.quests.QuestLine;
import betterquesting.utils.NBTConverter;
import betterquesting.utils.RenderUtils;
import com.google.gson.JsonObject;
import com.mojang.realmsclient.gui.ChatFormatting;

public class GuiQuestLineEditor extends GuiQuesting
{
	int lastID = -1;
	JsonObject lastEdit = new JsonObject();
	GuiTextField lineTitle;
	QuestLine selected;
	int selIndex = -1;
	int leftScroll = 0;
	int rightScroll = 0;
	int lsMax = 0;
	int lsMin = 0;
	int maxRows = 0;
	ArrayList<GuiButtonQuesting> qlBtns = new ArrayList<GuiButtonQuesting>();
	ArrayList<GuiButtonQuesting> qlBtnsDel = new ArrayList<GuiButtonQuesting>();
	ArrayList<GuiButtonQuesting> qiBtns = new ArrayList<GuiButtonQuesting>();
	ArrayList<GuiButtonQuesting> qiBtnsDel = new ArrayList<GuiButtonQuesting>();
	ArrayList<GuiButtonQuesting> qiBtnsAdd = new ArrayList<GuiButtonQuesting>();
	
	public GuiQuestLineEditor(GuiScreen parent)
	{
		super(parent, "Quest Line Editor");
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void initGui()
	{
		super.initGui();
		
		qlBtns.clear();
		qiBtns.clear();
		qlBtnsDel.clear();
		qiBtnsDel.clear();
		qiBtnsAdd.clear();
		
		if(lastEdit != null && lastID >= 0)
		{
			QuestDatabase.getQuest(lastID).readFromJSON(lastEdit); // This is just so we can represent it properly without waiting for re-sync
			NBTTagCompound tags = new NBTTagCompound();
			tags.setInteger("ID", 5);
			tags.setInteger("action", 0); // Action: Update data
			tags.setInteger("questID", lastID);
			tags.setTag("Data", NBTConverter.JSONtoNBT_Object(lastEdit, new NBTTagCompound()));
			BetterQuesting.instance.network.sendToServer(new PacketQuesting(tags));
			lastEdit = null;
			lastID = -1;
		}

		leftScroll = 0;
		rightScroll = 0;
		maxRows = (sizeY - 80)/20;
		int btnWidth = Math.min(sizeX/2 - 24, 198);
		
		lineTitle = new GuiTextField(mc.fontRenderer, guiLeft + sizeX/4*3 - (btnWidth/2 + 4) + 1, guiTop + 33, btnWidth + 8 - 2, 18);
		 
		this.buttonList.add(new GuiButtonQuesting(1, guiLeft + sizeX/4 - 50, guiTop + sizeY - 48, 100, 20, "Add New"));
		this.buttonList.add(new GuiButtonQuesting(2, guiLeft + sizeX/4*3 - 50, guiTop + sizeY - 48, 100, 20, "Add New"));
		this.buttonList.add(new GuiButtonQuesting(3, guiLeft + sizeX/2 - 50, guiTop + sizeY - 48, 100, 20, "Import"));
		
		// Quest Line - Main
		for(int i = 0; i < maxRows; i++)
		{
			GuiButtonQuesting btn = new GuiButtonQuesting(this.buttonList.size(), guiLeft + sizeX/4 - (btnWidth/2 + 4) + 20, guiTop + 32 + (i*20), btnWidth - 20, 20, "NULL");
			qlBtns.add(btn);
			this.buttonList.add(btn);
		}
		
		// Quest Line - Delete
		for(int i = 0; i < maxRows; i++)
		{
			GuiButtonQuesting btn = new GuiButtonQuesting(this.buttonList.size(), guiLeft + sizeX/4 - (btnWidth/2 + 4), guiTop + 32 + (i*20), 20, 20, "" + ChatFormatting.RED + ChatFormatting.BOLD + "x");
			qlBtnsDel.add(btn);
			this.buttonList.add(btn);
		}
		
		// Quest Instance - Main
		for(int i = 0; i < maxRows - 1; i++)
		{
			GuiButtonQuesting btn = new GuiButtonQuesting(this.buttonList.size(), guiLeft + sizeX/4*3 - (btnWidth/2 + 4) + 20, guiTop + 52 + (i*20), btnWidth - 40, 20, "NULL");
			qiBtns.add(btn);
			this.buttonList.add(btn);
		}
		
		// Quest Instance - Delete
		for(int i = 0; i < maxRows - 1; i++)
		{
			GuiButtonQuesting btn = new GuiButtonQuesting(this.buttonList.size(), guiLeft + sizeX/4*3 - (btnWidth/2 + 4) + 20 + btnWidth - 40, guiTop + 52 + (i*20), 20, 20, "" + ChatFormatting.RED + ChatFormatting.BOLD + "x");
			qiBtnsDel.add(btn);
			this.buttonList.add(btn);
		}
		
		// Quest Instance - Add/Remove
		for(int i = 0; i < maxRows - 1; i++)
		{
			GuiButtonQuesting btn = new GuiButtonQuesting(this.buttonList.size(), guiLeft + sizeX/4*3 - (btnWidth/2 + 4), guiTop + 52 + (i*20), 20, 20, "" + ChatFormatting.GREEN + ChatFormatting.BOLD + "+");
			qiBtnsAdd.add(btn);
			this.buttonList.add(btn);
		}
		
		RefreshColumns();
	}
	
	@Override
	public void drawScreen(int mx, int my, float partialTick)
	{
		super.drawScreen(mx, my, partialTick);
		
		if(QuestDatabase.updateUI)
		{
			RefreshColumns();
			QuestDatabase.updateUI = false;
		}
		
		GL11.glColor4f(1F, 1F, 1F, 1F);
		int btnWidth = Math.min(sizeX/2 - 24, 198);
		
		// Left scroll bar
		this.drawTexturedModalRect(guiLeft + sizeX/4 - 4 + btnWidth/2, this.guiTop + 32, 248, 0, 8, 20);
		int s = 20;
		while(s < (maxRows - 1) * 20)
		{
			this.drawTexturedModalRect(guiLeft + sizeX/4 - 4 + btnWidth/2, this.guiTop + 32 + s, 248, 20, 8, 20);
			s += 20;
		}
		this.drawTexturedModalRect(guiLeft + sizeX/4 - 4 + btnWidth/2, this.guiTop + 32 + s, 248, 40, 8, 20);
		this.drawTexturedModalRect(guiLeft + sizeX/4 - 4 + btnWidth/2, this.guiTop + 32 + (int)Math.max(0, s * (float)leftScroll/(QuestDatabase.questLines.size() - maxRows)), 248, 60, 8, 20);
		
		// Right scroll bar
		this.drawTexturedModalRect(guiLeft + sizeX/4*3 - 4 + btnWidth/2, this.guiTop + 52, 248, 0, 8, 20);
		s = 20;
		while(s < (maxRows - 2) * 20)
		{
			this.drawTexturedModalRect(guiLeft + sizeX/4*3 - 4 + btnWidth/2, this.guiTop + 52 + s, 248, 20, 8, 20);
			s += 20;
		}
		this.drawTexturedModalRect(guiLeft + sizeX/4*3 - 4 + btnWidth/2, this.guiTop + 52 + s, 248, 40, 8, 20);
		this.drawTexturedModalRect(guiLeft + sizeX/4*3 - 4 + btnWidth/2, this.guiTop + 52 + (int)Math.max(0, s * (float)rightScroll/(QuestDatabase.questDB.size() - maxRows + 1F)), 248, 60, 8, 20);
		
		RenderUtils.DrawLine(width/2, guiTop + 32, width/2, guiTop + sizeY - 28, 2F, Color.BLACK);
		
		lineTitle.drawTextBox();
	}
	
	public void DeleteQuest(int id)
	{
		NBTTagCompound tags = new NBTTagCompound();
		tags.setInteger("ID", 5);
		tags.setInteger("action", 1);
		tags.setInteger("questID", id);
		BetterQuesting.instance.network.sendToServer(new PacketQuesting(tags));
	}
	
	public void SendChanges(int action)
	{
		if(action < 0 || action > 2)
		{
			return;
		}
		
		NBTTagCompound tags = new NBTTagCompound();
		tags.setInteger("ID", 6);
		tags.setInteger("action", action);
		
		if(action == 2)
		{
			JsonObject json = new JsonObject();
			QuestDatabase.writeToJson_Lines(json);
			tags.setTag("Data", NBTConverter.JSONtoNBT_Object(json, new NBTTagCompound()));
		}
		
		BetterQuesting.instance.network.sendToServer(new PacketQuesting(tags));
	}
	
	@Override
	public void actionPerformed(GuiButton btn)
	{
		super.actionPerformed(btn);
		
		if(btn.id == 1) // Add quest line
		{
			SendChanges(0);
			RefreshColumns();
		} else if(btn.id == 2) // Add quest instance
		{
			SendChanges(1);
			RefreshColumns();
		} else if(btn.id == 3)
		{
			// Changes this to import screen
			HQMImporter.StartImport();
		} else if(btn.id > 0)
		{
			int bIndex = btn.id - 4;
			
			if(bIndex < maxRows*2)
			{
				// This is a quest line button conscious 
				
				if(bIndex < maxRows)
				{
					if(bIndex + leftScroll < QuestDatabase.questLines.size())
					{
						selected = QuestDatabase.questLines.get(bIndex + leftScroll);
						selIndex = bIndex + leftScroll;
						lineTitle.setText(selected.name);
						lineTitle.setEnabled(true);
					} else
					{
						selIndex = -1;
						selected = null;
						lineTitle.setEnabled(false);
					}
					
					rightScroll = 0;
				} else
				{
					bIndex -= maxRows;
					
					if(bIndex + leftScroll < QuestDatabase.questLines.size())
					{
						QuestDatabase.questLines.remove(bIndex + leftScroll);
						SendChanges(2);
					}
				}
				
				RefreshColumns();
			} else if(selected != null)
			{
				bIndex -= maxRows*2;
				int act = bIndex/(maxRows - 1);
				int index = bIndex%(maxRows - 1);
				
				if(act == 0)
				{
					QuestInstance quest = QuestDatabase.questDB.get(index + rightScroll);
					
					if(quest != null)
					{
						lastEdit = new JsonObject();
						lastID = quest.questID;
						quest.writeToJSON(lastEdit);
						mc.displayGuiScreen(new GuiJsonObject(this, lastEdit));
					}
				} else if(act == 1)
				{
					QuestInstance quest = QuestDatabase.questDB.get(index + rightScroll);
					
					if(quest != null)
					{
						System.out.println("Deleting quest " + quest.name);
						DeleteQuest(quest.questID);
					}
				} else if(act == 2)
				{
					QuestInstance quest = QuestDatabase.questDB.get(index + rightScroll);
					
					if(quest != null)
					{
						if(!selected.questList.contains(quest))
						{
							selected.questList.add(quest);
						} else
						{
							selected.questList.remove(quest);
						}
						
						SendChanges(2);
					}
				}
			}
		}
	}

    /**
     * Fired when a key is typed. This is the equivalent of KeyListener.keyTyped(KeyEvent e).
     */
	@Override
    protected void keyTyped(char character, int keyCode)
    {
        super.keyTyped(character, keyCode);
        
        if(selected != null)
        {
	        if(lineTitle.textboxKeyTyped(character, keyCode))
	        {
	        	selected.name = lineTitle.getText();
	        } else if(lineTitle.isFocused() && keyCode == Keyboard.KEY_RETURN)
	        {
	    		SendChanges(2);
	        }
        }
    }
    /**
     * Called when the mouse is clicked.
     */
	@Override
    protected void mouseClicked(int mx, int my, int click)
    {
		super.mouseClicked(mx, my, click);
		
		lineTitle.mouseClicked(mx, my, click);
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
        int SDX = (int)-Math.signum(Mouse.getDWheel());
        
        if(SDX != 0 && isWithin(mx, my, this.guiLeft, this.guiTop, sizeX/2, sizeY))
        {
    		leftScroll = Math.max(0, MathHelper.clamp_int(leftScroll + SDX, 0, QuestDatabase.questLines.size() - maxRows));
    		RefreshColumns();
        }
        
        if(SDX != 0 && isWithin(mx, my, this.guiLeft + sizeX/2, this.guiTop, sizeX/2, sizeY))
        {
        	if(selected != null)
        	{
        		rightScroll = Math.max(0, MathHelper.clamp_int(rightScroll + SDX, 0, QuestDatabase.questDB.size() - maxRows + 1));
        	} else
        	{
        		rightScroll = 0;
        	}
        	RefreshColumns();
        }
    }
	
	public void RefreshColumns()
	{
		leftScroll = Math.max(0, MathHelper.clamp_int(leftScroll, 0, QuestDatabase.questLines.size() - maxRows));
		rightScroll = Math.max(0, MathHelper.clamp_int(rightScroll, 0, QuestDatabase.questDB.size() - maxRows + 1));
		
		if(selected != null && !QuestDatabase.questLines.contains(selected))
		{
			if(selIndex >= 0 && selIndex < QuestDatabase.questLines.size())
			{
				selected = QuestDatabase.questLines.get(selIndex);
			} else
			{
				selected = null;
				selIndex = -1;
			}
		}
		
		for(int i = 0; i < qlBtns.size(); i++)
		{
			GuiButtonQuesting btn = qlBtns.get(i);
			GuiButtonQuesting btnD = qlBtnsDel.get(i);
			
			if(i + leftScroll >= QuestDatabase.questLines.size())
			{
				btn.displayString = "NULL " + (i + leftScroll);
				btn.visible = btn.enabled = false;
				btnD.visible = btnD.enabled = false;
			} else
			{
				QuestLine line = QuestDatabase.questLines.get(i + leftScroll);
				btn.displayString = line.name;
				btn.visible = true;
				btnD.visible = btnD.enabled = true;
				btn.enabled = line != selected;
			}
		}
		
		for(int i = 0; i < qiBtns.size(); i++)
		{
			GuiButtonQuesting btn = qiBtns.get(i);
			GuiButtonQuesting btnD = qiBtnsDel.get(i);
			GuiButtonQuesting btnA = qiBtnsAdd.get(i);
			
			if(selected == null)
			{
				btn.visible = btn.enabled = false;
				btnD.visible = btnD.enabled = false;
				btnA.visible = btnA.enabled = false;
			} else if(i + rightScroll >= QuestDatabase.questDB.size())
			{
				btn.displayString = "NULL " + (i + rightScroll);
				btn.visible = btn.enabled = false;
				btnD.visible = btnD.enabled = false;
				btnA.visible = btnA.enabled = false;
			} else
			{
				QuestInstance quest = QuestDatabase.questDB.get(i + rightScroll);
				btn.displayString = quest.name;
				btn.visible = btn.enabled = true;
				btnD.visible = btnD.enabled = true;
				btnA.visible = btnA.enabled = true;
				btnA.displayString = selected.questList.contains(quest)? "" + ChatFormatting.YELLOW + ChatFormatting.BOLD + "-" : "" + ChatFormatting.GREEN + ChatFormatting.BOLD + "+";
			}
		}
		
		if(selected == null)
		{
			lineTitle.setText("");
			lineTitle.setEnabled(false);
		} else
		{
			lineTitle.setText(selected.name);
			lineTitle.setEnabled(true);
		}
	}
	
	/* 
	 *      Quest Lines            Quest Instances
	 * 
	 * [X][ Quest Line 1 ] | <  Quest Line Title Field  >
	 * [X][ Quest Line 2 ] | [ Quest Instance 1 ][X][...]
	 * [X][ Quest Line 3 ] | [ Quest Instance 2 ][X][...]
	 * [X][ Quest Line 4 ] | [ Quest Instance 3 ][X][...]
	 * [X][ Quest Line 5 ] | [ Quest Instance 4 ][X][...]
	 * 
	 *       [Add New]             [Add New]
	 *                  [Done]
	 * 
	 * Used duel scroll wheels. Use mouse position to toggle scrolling
	 * Right menu appears when a quest line has been selected
	 */
}

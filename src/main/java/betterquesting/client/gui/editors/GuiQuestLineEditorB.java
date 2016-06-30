package betterquesting.client.gui.editors;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MathHelper;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import betterquesting.client.gui.GuiQuestInstance;
import betterquesting.client.gui.GuiQuesting;
import betterquesting.client.gui.misc.GuiBigTextField;
import betterquesting.client.gui.misc.GuiButtonQuesting;
import betterquesting.client.gui.misc.IVolatileScreen;
import betterquesting.client.themes.ThemeRegistry;
import betterquesting.network.PacketAssembly;
import betterquesting.network.PacketTypeRegistry.BQPacketType;
import betterquesting.quests.QuestDatabase;
import betterquesting.quests.QuestInstance;
import betterquesting.quests.QuestLine;
import betterquesting.quests.QuestLine.QuestLineEntry;
import betterquesting.utils.NBTConverter;
import betterquesting.utils.RenderUtils;
import com.google.gson.JsonObject;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiQuestLineEditorB extends GuiQuesting implements IVolatileScreen
{
	int selIndex = -1;
	QuestLine line;
	int leftScroll = 0;
	int rightScroll = 0;
	int maxRowsL = 0;
	int maxRowsR = 0;
	GuiBigTextField searchBox;
	ArrayList<QuestInstance> searchResults = new ArrayList<QuestInstance>();
	
	public GuiQuestLineEditorB(GuiScreen parent, QuestLine line)
	{
		super(parent, I18n.format("betterquesting.title.edit_line2", line == null? "?" : I18n.format(line.name)));
		this.line = line;
		selIndex = line == null? -1 : QuestDatabase.questLines.indexOf(line);
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public void initGui()
	{
		super.initGui();
		
		this.title = I18n.format("betterquesting.title.edit_line2", line == null? "?" : I18n.format(line.name));
		
		maxRowsL = (sizeY - 96)/20;
		maxRowsR = (sizeY - 116)/20;
		int btnWidth = sizeX/2 - 16;
		int sx = sizeX - 32;
		
		this.searchBox = new GuiBigTextField(mc.fontRenderer, guiLeft + sizeX/2 + 8, guiTop + 48, btnWidth - 16, 20);
		this.searchBox.setWatermark(I18n.format("betterquesting.gui.search"));
		this.buttonList.add(new GuiButtonQuesting(1, guiLeft + 16 + sx/4*3 - 50, guiTop + sizeY - 48, 100, 20, I18n.format("betterquesting.btn.new")));
		
		// Left main buttons
		for(int i = 0; i < maxRowsL; i++)
		{
			GuiButtonQuesting btn = new GuiButtonQuesting(this.buttonList.size(), guiLeft + 16, guiTop + 48 + (i*20), btnWidth - 36, 20, "NULL");
			this.buttonList.add(btn);
		}
		
		// Left delete buttons
		for(int i = 0; i < maxRowsL; i++)
		{
			GuiButtonQuesting btn = new GuiButtonQuesting(this.buttonList.size(), guiLeft + 16 + btnWidth - 36, guiTop + 48 + (i*20), 20, 20, "" + EnumChatFormatting.YELLOW + EnumChatFormatting.BOLD + ">");
			this.buttonList.add(btn);
		}
		
		// Right main buttons
		for(int i = 0; i < maxRowsR; i++)
		{
			GuiButtonQuesting btn = new GuiButtonQuesting(this.buttonList.size(), guiLeft + sizeX/2 + 28, guiTop + 68 + (i*20), btnWidth - 56, 20, "NULL");
			this.buttonList.add(btn);
		}
		
		// Right delete buttons
		for(int i = 0; i < maxRowsR; i++)
		{
			GuiButtonQuesting btn = new GuiButtonQuesting(this.buttonList.size(), guiLeft + sizeX/2 + 28 + btnWidth - 56, guiTop + 68 + (i*20), 20, 20, "" + EnumChatFormatting.RED + EnumChatFormatting.BOLD + "x");
			this.buttonList.add(btn);
		}
		
		// Right add buttons
		for(int i = 0; i < maxRowsR; i++)
		{
			GuiButtonQuesting btn = new GuiButtonQuesting(this.buttonList.size(), guiLeft + sizeX/2 + 8, guiTop + 68 + (i*20), 20, 20, "" + EnumChatFormatting.GREEN + EnumChatFormatting.BOLD + "<");
			this.buttonList.add(btn);
		}
		
		RefreshSearch();
		RefreshColumns();
	}
	
	@Override
	public void drawScreen(int mx, int my, float partialTick)
	{
		super.drawScreen(mx, my, partialTick);
		
		if(QuestDatabase.updateUI)
		{
			QuestDatabase.updateUI = false;
			RefreshSearch();
			RefreshColumns();
		}
		
		GL11.glColor4f(1F, 1F, 1F, 1F);
		mc.renderEngine.bindTexture(ThemeRegistry.curTheme().guiTexture());
		
		// Left scroll bar
		this.drawTexturedModalRect(guiLeft + sizeX/2 - 16, this.guiTop + 48, 248, 0, 8, 20);
		int s = 20;
		while(s < (maxRowsL - 1) * 20)
		{
			this.drawTexturedModalRect(guiLeft + sizeX/2 - 16, this.guiTop + 48 + s, 248, 20, 8, 20);
			s += 20;
		}
		this.drawTexturedModalRect(guiLeft + sizeX/2 - 16, this.guiTop + 48 + s, 248, 40, 8, 20);
		this.drawTexturedModalRect(guiLeft + sizeX/2 - 16, this.guiTop + 48 + (int)Math.max(0, s * (float)leftScroll/(line == null? 1 : line.questList.size() - maxRowsL)), 248, 60, 8, 20);
		
		// Right scroll bar
		this.drawTexturedModalRect(guiLeft + sizeX - 24, this.guiTop + 68, 248, 0, 8, 20);
		s = 20;
		while(s < (maxRowsR - 1) * 20)
		{
			this.drawTexturedModalRect(guiLeft + sizeX - 24, this.guiTop + 68 + s, 248, 20, 8, 20);
			s += 20;
		}
		
		this.drawTexturedModalRect(guiLeft + sizeX - 24, this.guiTop + 68 + s, 248, 40, 8, 20);
		this.drawTexturedModalRect(guiLeft + sizeX - 24, this.guiTop + 68 + (int)Math.max(0, s * (float)rightScroll/(searchResults.size() - maxRowsL)), 248, 60, 8, 20);
		
		RenderUtils.DrawLine(width/2, guiTop + 32, width/2, guiTop + sizeY - 32, 2F, ThemeRegistry.curTheme().textColor());
		
		int sx = sizeX - 32;
		String txt = I18n.format("betterquesting.gui.quest_line");
		mc.fontRenderer.drawString(txt, guiLeft + 16 + sx/4 - mc.fontRenderer.getStringWidth(txt)/2, guiTop + 32, ThemeRegistry.curTheme().textColor().getRGB(), false);
		txt = I18n.format("betterquesting.gui.database");
		mc.fontRenderer.drawString(txt, guiLeft + 16 + sx/4*3 - mc.fontRenderer.getStringWidth(txt)/2, guiTop + 32, ThemeRegistry.curTheme().textColor().getRGB(), false);
		
		searchBox.drawTextBox();
	}
	
	@Override
	public void actionPerformed(GuiButton button)
	{
		super.actionPerformed(button);
		
		if(button.id == 0)
		{
			SendChanges(2); // We do this upon exiting because the screen relies on this instance of the quest line and cannot recover if it is update early
		} else if(button.id == 1)
		{
			SendChanges(1);
		} else if(button.id > 1)
		{
			int n1 = button.id - 2; // Line index
			int n2 = n1/maxRowsL; // Line listing (0 = quest, 1 = quest delete, 2 = registry)
			int n3 = n1%maxRowsL + leftScroll; // Quest list index
			int n4 = n1%maxRowsL + rightScroll; // Registry list index
			
			if(n2 >= 2) // Right list needs some modifications to work properly
			{
				n1 -= maxRowsL*2;
				n2 = 2 + n1/maxRowsR;
				n4 = n1%maxRowsR + rightScroll;
			}
			
			if(n2 == 0) // Edit quest
			{
				if(line != null && n3 >= 0 && n3 < line.questList.size())
				{
					mc.displayGuiScreen(new GuiQuestInstance(this, line.questList.get(n3).quest));
				}
			} else if(n2 == 1) // Remove quest
			{
				if(!(line == null || n3 < 0 || n3 >= line.questList.size()))
				{
					line.questList.remove(n3);
					RefreshColumns();
				}
			} else if(n2 == 2) // Edit quest
			{
				if(!(n4 < 0 || n4 >= searchResults.size()))
				{
					mc.displayGuiScreen(new GuiQuestInstance(this, searchResults.get(n4)));
				}
			} else if(n2 == 3) // Delete quest
			{
				if(!(n4 < 0 || n4 >= searchResults.size()))
				{
					NBTTagCompound tags = new NBTTagCompound();
					tags.setInteger("action", 1); // Delete quest
					tags.setInteger("questID", searchResults.get(n4).questID);
					PacketAssembly.SendToServer(BQPacketType.QUEST_EDIT.GetLocation(), tags);
				}
			} else if(n2 == 4) // Add quest
			{
				if(!(line == null || n4 < 0 || n4 >= searchResults.size()))
				{
					QuestLineEntry qe = new QuestLineEntry(searchResults.get(n4), 0, 0);
					
					topLoop:
					while(true)
					{
						for(QuestLineEntry qe2 : line.questList)
						{
							if(qe.posX >= qe2.posX && qe.posX < qe2.posX + 24 && qe.posY >= qe2.posY && qe.posY < qe2.posY + 24)
							{
								qe.posX += 24;
								qe.posY += 24;
								continue topLoop; // We're in the way, move over and try again
							}
						}
						
						break;
					}
					
					if(qe.quest != null)
					{
						line.questList.add(qe);
						RefreshColumns();
					}
				}
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
    		leftScroll = line == null? 0 : Math.max(0, MathHelper.clamp_int(leftScroll + SDX, 0, line.questList.size() - maxRowsL));
    		RefreshColumns();
        }
        
        if(SDX != 0 && isWithin(mx, my, this.guiLeft + sizeX/2, this.guiTop, sizeX/2, sizeY))
        {
        	rightScroll = Math.max(0, MathHelper.clamp_int(rightScroll + SDX, 0, searchResults.size() - maxRowsR));
        	RefreshColumns();
        }
    }
	
	public void SendChanges(int action)
	{
		if(action < 0 || action > 2)
		{
			return;
		}
		
		NBTTagCompound tags = new NBTTagCompound();
		tags.setInteger("action", action);
		
		if(action == 2)
		{
			JsonObject json = new JsonObject();
			QuestDatabase.writeToJson_Lines(json);
			tags.setTag("Data", NBTConverter.JSONtoNBT_Object(json, new NBTTagCompound()));
		}
		
		PacketAssembly.SendToServer(BQPacketType.LINE_EDIT.GetLocation(), tags);
	}
	
	public void RefreshColumns()
	{
		leftScroll = line == null? 0 : Math.max(0, MathHelper.clamp_int(leftScroll, 0, line.questList.size() - maxRowsL));
    	rightScroll = Math.max(0, MathHelper.clamp_int(rightScroll, 0, searchResults.size() - maxRowsR));
    	
    	if(line != null && !QuestDatabase.questLines.contains(line))
		{
			if(selIndex >= 0 && selIndex < QuestDatabase.questLines.size())
			{
				line = QuestDatabase.questLines.get(selIndex);
			} else
			{
				mc.displayGuiScreen(parent);
			}
		}
    	
		@SuppressWarnings("unchecked")
		List<GuiButton> btnList = this.buttonList;
		
		for(int i = 2; i < btnList.size(); i++)
		{
			GuiButton btn = btnList.get(i);
			int n1 = i - 2;
			int n2 = n1/maxRowsL; // Button listing (0 = quest, 1 = quest delete, 2 = registry)
			int n3 = n1%maxRowsL + leftScroll; // Quest list index
			int n4 = n1%maxRowsL + rightScroll; // Registry list index
			
			if(n2 >= 2) // Right list needs some modifications to work properly
			{
				n1 -= maxRowsL*2;
				n2 = 2 + n1/maxRowsR;
				n4 = n1%maxRowsR + rightScroll;
			}
			
			if(n2 == 0) // Edit quest
			{
				if(line == null || n3 < 0 || n3 >= line.questList.size())
				{
					btn.displayString = "NULL";
					btn.visible = btn.enabled = false;
				} else
				{
					btn.visible = btn.enabled = true;
					btn.displayString = I18n.format(line.questList.get(n3).quest.name);
				}
			} else if(n2 == 1) // Remove quest
			{
				btn.visible = btn.enabled = line != null && !(n3 < 0 || n3 >= line.questList.size());
			} else if(n2 == 2) // Edit quest
			{
				if(n4 < 0 || n4 >= searchResults.size())
				{
					btn.displayString = "NULL";
					btn.visible = btn.enabled = false;
				} else
				{
					QuestInstance q = searchResults.get(n4);
					btn.visible = btn.enabled = true;
					btn.displayString = I18n.format(q.name);
				}
			} else if(n2 == 3) // Delete quest
			{
				btn.visible = btn.enabled = !(n4 < 0 || n4 >= searchResults.size());
			} else if(n2 == 4) // Add quest
			{
				if(n4 < 0 || n4 >= searchResults.size())
				{
					btn.visible = btn.enabled = false;
				} else
				{
					QuestInstance q = searchResults.get(n4);
					btn.visible = true;
					btn.enabled = line != null && line.getEntryByID(q.questID) == null;
				}
			}
		}
	}
	
    /**
     * Fired when a key is typed. This is the equivalent of KeyListener.keyTyped(KeyEvent e).
     */
	@Override
    protected void keyTyped(char character, int num)
    {
		super.keyTyped(character, num);
		String prevTxt = searchBox.getText();
		
		searchBox.textboxKeyTyped(character, num);
		
		if(!searchBox.getText().equalsIgnoreCase(prevTxt))
		{
			RefreshSearch();
			RefreshColumns();
		}
    }
	
	public void RefreshSearch()
	{
		searchResults = new ArrayList<QuestInstance>();
		String query = searchBox.getText().toLowerCase();
		
		for(QuestInstance q : QuestDatabase.questDB.values())
		{
			if(q == null)
			{
				continue;
			}
			
			if(q.name.toLowerCase().contains(query) || I18n.format(q.name).toLowerCase().contains(query) || query.equalsIgnoreCase("" + q.questID))
			{
				searchResults.add(q);
			}
		}
	}
	
	@Override
	public void mouseClicked(int mx, int my, int type)
	{
		super.mouseClicked(mx, my, type);
		this.searchBox.mouseClicked(mx, my, type);
	}
}

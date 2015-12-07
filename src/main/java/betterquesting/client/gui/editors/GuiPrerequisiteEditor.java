package betterquesting.client.gui.editors;

import java.util.List;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MathHelper;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import betterquesting.client.gui.GuiQuestInstance;
import betterquesting.client.gui.GuiQuesting;
import betterquesting.client.gui.misc.GuiButtonQuesting;
import betterquesting.client.themes.ThemeRegistry;
import betterquesting.core.BetterQuesting;
import betterquesting.network.PacketQuesting.PacketDataType;
import betterquesting.quests.QuestDatabase;
import betterquesting.quests.QuestInstance;
import betterquesting.utils.NBTConverter;
import betterquesting.utils.RenderUtils;
import com.google.gson.JsonObject;
import com.mojang.realmsclient.gui.ChatFormatting;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiPrerequisiteEditor extends GuiQuesting
{
	QuestInstance quest;
	int leftScroll = 0;
	int rightScroll = 0;
	int maxRows = 0;
	
	public GuiPrerequisiteEditor(GuiScreen parent, QuestInstance quest)
	{
		super(parent, "betterquesting.title.pre_requisites");
		this.quest = quest;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public void initGui()
	{
		super.initGui();
		
		maxRows = (sizeY - 96)/20;
		int btnWidth = Math.min(sizeX/2 - 24, 198);
		
		this.buttonList.add(new GuiButtonQuesting(1, guiLeft + sizeX/4*3 - 50, guiTop + sizeY - 48, 100, 20, I18n.format("betterquesting.btn.new")));
		
		// Left main buttons
		for(int i = 0; i < maxRows; i++)
		{
			GuiButtonQuesting btn = new GuiButtonQuesting(this.buttonList.size(), guiLeft + sizeX/4 - (btnWidth/2 + 4) + 20, guiTop + 48 + (i*20), btnWidth - 20, 20, "NULL");
			this.buttonList.add(btn);
		}
		
		// Left delete buttons
		for(int i = 0; i < maxRows; i++)
		{
			GuiButtonQuesting btn = new GuiButtonQuesting(this.buttonList.size(), guiLeft + sizeX/4 - (btnWidth/2 + 4), guiTop + 48 + (i*20), 20, 20, "" + ChatFormatting.YELLOW + ChatFormatting.BOLD + "-");
			this.buttonList.add(btn);
		}
		
		// Right main buttons
		for(int i = 0; i < maxRows; i++)
		{
			GuiButtonQuesting btn = new GuiButtonQuesting(this.buttonList.size(), guiLeft + sizeX/4*3 - (btnWidth/2 + 4) + 40, guiTop + 48 + (i*20), btnWidth - 40, 20, "NULL");
			this.buttonList.add(btn);
		}
		
		// Right delete buttons
		for(int i = 0; i < maxRows; i++)
		{
			GuiButtonQuesting btn = new GuiButtonQuesting(this.buttonList.size(), guiLeft + sizeX/4*3 - (btnWidth/2 + 4) + 20, guiTop + 48 + (i*20), 20, 20, "" + ChatFormatting.RED + ChatFormatting.BOLD + "x");
			this.buttonList.add(btn);
		}
		
		// Right add buttons
		for(int i = 0; i < maxRows; i++)
		{
			GuiButtonQuesting btn = new GuiButtonQuesting(this.buttonList.size(), guiLeft + sizeX/4*3 - (btnWidth/2 + 4), guiTop + 48 + (i*20), 20, 20, "" + ChatFormatting.GREEN + ChatFormatting.BOLD + "+");
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
			QuestDatabase.updateUI = false;
			RefreshColumns();
		}
		
		GL11.glColor4f(1F, 1F, 1F, 1F);
		mc.renderEngine.bindTexture(ThemeRegistry.curTheme().guiTexture());
		
		int btnWidth = Math.min(sizeX/2 - 24, 198);
		
		// Left scroll bar
		this.drawTexturedModalRect(guiLeft + sizeX/4 - 4 + btnWidth/2, this.guiTop + 48, 248, 0, 8, 20);
		int s = 20;
		while(s < (maxRows - 1) * 20)
		{
			this.drawTexturedModalRect(guiLeft + sizeX/4 - 4 + btnWidth/2, this.guiTop + 48 + s, 248, 20, 8, 20);
			s += 20;
		}
		this.drawTexturedModalRect(guiLeft + sizeX/4 - 4 + btnWidth/2, this.guiTop + 48 + s, 248, 40, 8, 20);
		this.drawTexturedModalRect(guiLeft + sizeX/4 - 4 + btnWidth/2, this.guiTop + 48 + (int)Math.max(0, s * (float)leftScroll/(quest == null? 1 : quest.preRequisites.size() - maxRows)), 248, 60, 8, 20);
		
		// Right scroll bar
		this.drawTexturedModalRect(guiLeft + sizeX/4*3 - 4 + btnWidth/2, this.guiTop + 48, 248, 0, 8, 20);
		s = 20;
		while(s < (maxRows - 1) * 20)
		{
			this.drawTexturedModalRect(guiLeft + sizeX/4*3 - 4 + btnWidth/2, this.guiTop + 48 + s, 248, 20, 8, 20);
			s += 20;
		}
		
		this.drawTexturedModalRect(guiLeft + sizeX/4*3 - 4 + btnWidth/2, this.guiTop + 48 + s, 248, 40, 8, 20);
		this.drawTexturedModalRect(guiLeft + sizeX/4*3 - 4 + btnWidth/2, this.guiTop + 48 + (int)Math.max(0, s * (float)rightScroll/(QuestDatabase.questDB.size() - maxRows + 1F)), 248, 60, 8, 20);
		
		RenderUtils.DrawLine(width/2, guiTop + 32, width/2, guiTop + sizeY - 32, 2F, ThemeRegistry.curTheme().textColor());
		
		String txt = I18n.format("betterquesting.gui.quest_line");
		mc.fontRenderer.drawString(txt, guiLeft + sizeX/4 - mc.fontRenderer.getStringWidth(txt)/2, guiTop + 32, ThemeRegistry.curTheme().textColor().getRGB(), false);
		txt = I18n.format("betterquesting.gui.database");
		mc.fontRenderer.drawString(txt, guiLeft + sizeX/4*3 - mc.fontRenderer.getStringWidth(txt)/2, guiTop + 32, ThemeRegistry.curTheme().textColor().getRGB(), false);
	}
	
	@Override
	public void actionPerformed(GuiButton button)
	{
		super.actionPerformed(button);
		
		if(button.id == 1)
		{
			NBTTagCompound tags = new NBTTagCompound();
			//tags.setInteger("ID", 6);
			tags.setInteger("action", 1);
			//BetterQuesting.instance.network.sendToServer(new PacketQuesting(tags));
			BetterQuesting.instance.network.sendToServer(PacketDataType.LINE_EDIT.makePacket(tags));
		} else if(button.id > 1)
		{
			int n1 = button.id - 2; // Line index
			int n2 = n1/maxRows; // Line listing (0 = quest, 1 = quest delete, 2 = registry)
			int n3 = n1%maxRows + leftScroll; // Quest list index
			int n4 = n1%maxRows + rightScroll; // Registry list index
			
			if(n2 == 0) // Edit quest
			{
				if(quest != null && n3 >= 0 && n3 < quest.preRequisites.size())
				{
					mc.displayGuiScreen(new GuiQuestInstance(this, quest.preRequisites.get(n3)));
				}
			} else if(n2 == 1) // Remove quest
			{
				if(!(quest == null || n3 < 0 || n3 >= quest.preRequisites.size()))
				{
					quest.preRequisites.remove(n3);
					SendChanges();
				}
			} else if(n2 == 2) // Edit quest
			{
				if(!(n4 < 0 || n4 >= QuestDatabase.questDB.size()))
				{
					mc.displayGuiScreen(new GuiQuestInstance(this, QuestDatabase.getQuestByOrder(n4)));
				}
			} else if(n2 == 3) // Delete quest
			{
				if(!(n4 < 0 || n4 >= QuestDatabase.questDB.size()))
				{
					NBTTagCompound tags = new NBTTagCompound();
					//tags.setInteger("ID", 5);
					tags.setInteger("action", 1); // Delete quest
					tags.setInteger("questID", QuestDatabase.getQuestByOrder(n4).questID);
					//BetterQuesting.instance.network.sendToServer(new PacketQuesting(tags));
					BetterQuesting.instance.network.sendToServer(PacketDataType.QUEST_EDIT.makePacket(tags));
				}
			} else if(n2 == 4) // Add quest
			{
				if(!(quest == null || n4 < 0 || n4 >= QuestDatabase.questDB.size()))
				{
					quest.preRequisites.add(QuestDatabase.getQuestByOrder(n4));
					SendChanges();
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
        int SDX = (int)-Math.signum(Mouse.getDWheel());
        
        if(SDX != 0 && isWithin(mx, my, this.guiLeft, this.guiTop, sizeX/2, sizeY))
        {
    		leftScroll = quest == null? 0 : Math.max(0, MathHelper.clamp_int(leftScroll + SDX, 0, quest.preRequisites.size() - maxRows));
    		RefreshColumns();
        }
        
        if(SDX != 0 && isWithin(mx, my, this.guiLeft + sizeX/2, this.guiTop, sizeX/2, sizeY))
        {
        	rightScroll = Math.max(0, MathHelper.clamp_int(rightScroll + SDX, 0, QuestDatabase.questDB.size() - maxRows));
        	RefreshColumns();
        }
    }
	
	public void SendChanges()
	{
		JsonObject json = new JsonObject();
		quest.writeToJSON(json);
		NBTTagCompound tags = new NBTTagCompound();
		//tags.setInteger("ID", 5);
		tags.setInteger("action", 0); // Action: Update data
		tags.setInteger("questID", quest.questID);
		tags.setTag("Data", NBTConverter.JSONtoNBT_Object(json, new NBTTagCompound()));
		//BetterQuesting.instance.network.sendToServer(new PacketQuesting(tags));
		BetterQuesting.instance.network.sendToServer(PacketDataType.QUEST_EDIT.makePacket(tags));
	}
	
	public void RefreshColumns()
	{
		leftScroll = quest == null? 0 : Math.max(0, MathHelper.clamp_int(leftScroll, 0, quest.preRequisites.size() - maxRows));
    	rightScroll = Math.max(0, MathHelper.clamp_int(rightScroll, 0, QuestDatabase.questDB.size() - maxRows));
    	
    	if(quest != null && !QuestDatabase.questDB.containsValue(quest))
		{
    		quest = QuestDatabase.getQuestByID(quest.questID);
    		
			if(quest == null)
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
			int n2 = n1/maxRows; // Button listing (0 = quest, 1 = quest delete, 2 = registry)
			int n3 = n1%maxRows + leftScroll; // Quest list index
			int n4 = n1%maxRows + rightScroll; // Registry list index
			
			if(n2 == 0) // Edit quest
			{
				if(quest == null || n3 < 0 || n3 >= quest.preRequisites.size())
				{
					btn.displayString = "NULL";
					btn.visible = btn.enabled = false;
				} else
				{
					btn.visible = btn.enabled = true;
					btn.displayString = quest.preRequisites.get(n3).name;
				}
			} else if(n2 == 1) // Remove quest
			{
				btn.visible = btn.enabled = quest != null && !(n3 < 0 || n3 >= quest.preRequisites.size());
			} else if(n2 == 2) // Edit quest
			{
				if(n4 < 0 || n4 >= QuestDatabase.questDB.size())
				{
					btn.displayString = "NULL";
					btn.visible = btn.enabled = false;
				} else
				{
					QuestInstance q = QuestDatabase.getQuestByOrder(n4);
					btn.visible = btn.enabled = true;
					btn.displayString = q.name;
				}
			} else if(n2 == 3) // Delete quest
			{
				btn.visible = btn.enabled = !(n3 < 0 || n3 >= QuestDatabase.questDB.size());
			} else if(n2 == 4) // Add quest
			{
				if(n4 < 0 || n4 >= QuestDatabase.questDB.size())
				{
					btn.visible = btn.enabled = false;
				} else
				{
					QuestInstance q = QuestDatabase.getQuestByOrder(n4);
					btn.visible = true;
					btn.enabled = quest != null && !quest.preRequisites.contains(q);
				}
			}
		}
	}
}

package betterquesting.client.gui.editors;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MathHelper;
import org.lwjgl.opengl.GL11;
import betterquesting.api.client.gui.INeedsRefresh;
import betterquesting.api.client.gui.IVolatileScreen;
import betterquesting.api.client.gui.premade.controls.GuiButtonThemed;
import betterquesting.api.client.gui.premade.screens.GuiScreenThemed;
import betterquesting.api.enums.EnumPacketAction;
import betterquesting.api.enums.EnumSaveType;
import betterquesting.api.network.PacketTypeNative;
import betterquesting.api.network.PreparedPayload;
import betterquesting.api.quests.IQuestLine;
import betterquesting.api.quests.properties.NativePropertyTypes;
import betterquesting.api.utils.NBTConverter;
import betterquesting.api.utils.RenderUtils;
import betterquesting.client.gui.misc.GuiBigTextField;
import betterquesting.client.gui.misc.ITextCallback;
import betterquesting.importers.ImporterRegistry;
import betterquesting.network.PacketSender;
import betterquesting.quests.QuestLineDatabase;
import com.google.gson.JsonObject;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiQuestLineEditorA extends GuiScreenThemed implements ITextCallback, IVolatileScreen, INeedsRefresh
{
	private List<Integer> questList = new ArrayList<Integer>();
	private GuiButtonThemed btnDesign;
	private GuiTextField lineTitle;
	private GuiBigTextField lineDesc;
	private IQuestLine selected;
	//int selIndex = -1;
	int selID = -1;
	int leftScroll = 0;
	int maxRows = 0;
	
	public GuiQuestLineEditorA(GuiScreen parent)
	{
		super(parent, "betterquesting.title.edit_line1");
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void initGui()
	{
		super.initGui();
		
		maxRows = (sizeY - 80)/20;
		int btnWidth = sizeX/2 - 16;
		int sx = sizeX - 32;
		
		lineTitle = new GuiTextField(mc.fontRenderer, guiLeft + sizeX/2 + 9, guiTop + sizeY/2 - 59, btnWidth - 18, 18);
		lineTitle.setMaxStringLength(Integer.MAX_VALUE);
		
		lineDesc = new GuiBigTextField(mc.fontRenderer, guiLeft + sizeX/2 + 9, guiTop + sizeY/2 - 19, btnWidth - 18, 18).enableBigEdit(this, 0);
		lineDesc.setMaxStringLength(Integer.MAX_VALUE);
		 
		this.buttonList.add(new GuiButtonThemed(1, guiLeft + 16, guiTop + sizeY - 48, (btnWidth - 16)/2, 20, I18n.format("betterquesting.btn.new"), true));
		GuiButtonThemed btnImport = new GuiButtonThemed(3, guiLeft + 16 + (btnWidth - 16)/2, guiTop + sizeY - 48, (btnWidth - 16)/2, 20, I18n.format("betterquesting.btn.import"), true);
		btnImport.enabled = ImporterRegistry.INSTANCE.getImporters().size() > 0 && mc.isIntegratedServerRunning();
		this.buttonList.add(btnImport);
		this.buttonList.add(new GuiButtonThemed(2, guiLeft + 16 + sx/4*3 - 75, guiTop + sizeY/2 + 20, 150, 20, I18n.format("betterquesting.btn.add_remove_quests"), true));
		btnDesign = new GuiButtonThemed(4, guiLeft + 16 + sx/4*3 - 75, guiTop + sizeY/2 + 40, 150, 20, I18n.format("betterquesting.btn.designer"), true);
		this.buttonList.add(btnDesign);
		
		// Quest Line - Main
		for(int i = 0; i < maxRows; i++)
		{
			GuiButtonThemed btn = new GuiButtonThemed(this.buttonList.size(), guiLeft + 16 + 20, guiTop + 32 + (i*20), btnWidth - 56, 20, "NULL", true);
			this.buttonList.add(btn);
		}
		
		// Quest Line - Delete
		for(int i = 0; i < maxRows; i++)
		{
			GuiButtonThemed btn = new GuiButtonThemed(this.buttonList.size(), guiLeft + 16, guiTop + 32 + (i*20), 20, 20, "" + EnumChatFormatting.RED + EnumChatFormatting.BOLD + "x", true);
			this.buttonList.add(btn);
		}
		
		// Quest Line - Shift Up
		for(int i = 0; i < maxRows; i++)
		{
			GuiButtonThemed btn = new GuiButtonThemed(this.buttonList.size(), guiLeft + 16 + 20 + btnWidth - 56, guiTop + 32 + (i*20), 20, 20, "" + EnumChatFormatting.YELLOW + EnumChatFormatting.BOLD + "^", true);
			this.buttonList.add(btn);
		}
		
		RefreshColumns();
	}
	
	@Override
	public void refreshGui()
	{
		selected = QuestLineDatabase.INSTANCE.getValue(selID);
		
		RefreshColumns();
	}
	
	@Override
	public void drawScreen(int mx, int my, float partialTick)
	{
		super.drawScreen(mx, my, partialTick);
		
		GL11.glColor4f(1F, 1F, 1F, 1F);
		mc.renderEngine.bindTexture(currentTheme().getGuiTexture());
		
		// Left scroll bar
		this.drawTexturedModalRect(guiLeft + sizeX/2 - 16, this.guiTop + 32, 248, 0, 8, 20);
		int s = 20;
		while(s < (maxRows - 1) * 20)
		{
			this.drawTexturedModalRect(guiLeft + sizeX/2 - 16, this.guiTop + 32 + s, 248, 20, 8, 20);
			s += 20;
		}
		this.drawTexturedModalRect(guiLeft + sizeX/2 - 16, this.guiTop + 32 + s, 248, 40, 8, 20);
		this.drawTexturedModalRect(guiLeft + sizeX/2 - 16, this.guiTop + 32 + (int)Math.max(0, s * (float)leftScroll/(QuestLineDatabase.INSTANCE.size() - maxRows)), 248, 60, 8, 20);
		
		RenderUtils.DrawLine(width/2, guiTop + 32, width/2, guiTop + sizeY - 48, 2F, getTextColor());
		
		mc.fontRenderer.drawString(I18n.format("betterquesting.gui.name"), guiLeft + sizeX/2 + 8, guiTop + sizeY/2 - 72, getTextColor(), false);
		mc.fontRenderer.drawString(I18n.format("betterquesting.gui.description"), guiLeft + sizeX/2 + 8, guiTop + sizeY/2 - 32, getTextColor(), false);
		
		lineTitle.drawTextBox();
		lineDesc.drawTextBox(mx, my, partialTick);
	}
	
	public void DeleteQuest(int id)
	{
		NBTTagCompound tags = new NBTTagCompound();
		tags.setInteger("action", 1);
		tags.setInteger("questID", id);
		PacketSender.INSTANCE.sendToServer(new PreparedPayload(PacketTypeNative.QUEST_EDIT.GetLocation(), tags));
	}
	
	public void SendChanges(EnumPacketAction action, IQuestLine questLine)
	{
		SendChanges(action, QuestLineDatabase.INSTANCE.getKey(questLine));
	}
	
	public void SendChanges(EnumPacketAction action, int lineID)
	{
		IQuestLine questLine = QuestLineDatabase.INSTANCE.getValue(lineID);
		
		if(action == null)
		{
			return;
		}
		
		NBTTagCompound tags = new NBTTagCompound();
		
		if(action == EnumPacketAction.EDIT && questLine != null)
		{
			JsonObject base = new JsonObject();
			base.add("line", questLine.writeToJson(new JsonObject(), EnumSaveType.CONFIG));
			tags.setTag("data", NBTConverter.JSONtoNBT_Object(base, new NBTTagCompound()));
		}
		
		tags.setInteger("lineID", questLine == null? -1 : QuestLineDatabase.INSTANCE.getKey(questLine));
		tags.setInteger("action", action.ordinal());
		
		PacketSender.INSTANCE.sendToServer(new PreparedPayload(PacketTypeNative.LINE_EDIT.GetLocation(), tags));
	}
	
	@Override
	public void actionPerformed(GuiButton btn)
	{
		super.actionPerformed(btn);
		
		if(btn.id == 1) // Add quest line
		{
			SendChanges(EnumPacketAction.ADD, -1);
			RefreshColumns();
		} else if(btn.id == 2)
		{
			// Editor B will function just fine even if selected is null.
			// This is so users can add/edit/remove quests without having to make a quest line first
			mc.displayGuiScreen(new GuiQuestLineEditorB(this, selected));
		} else if(btn.id == 3)
		{
			// Changes this to import screen
			mc.displayGuiScreen(new GuiImporters(this));
		} else if(btn.id == 4 && selected != null)
		{
			// Changes this to import screen
			mc.displayGuiScreen(new GuiQuestLineDesigner(this, selected));
		} else if(btn.id > 4)
		{
			int n1 = btn.id - 5; // Line index
			int n2 = n1/maxRows; // Line listing (0 = line, 1 = delete)
			int n3 = n1%maxRows + leftScroll; // Quest list index
			
			if(n2 == 0)
			{
				if(n3 >= 0 && n3 < questList.size())
				{
					selected = QuestLineDatabase.INSTANCE.getValue(questList.get(n3));
					selID = QuestLineDatabase.INSTANCE.getKey(selected);
				} else
				{
					selected = null;
					selID = -1;
				}
				
				RefreshColumns();
			} else if(n2 == 1)
			{
				if(n3 >= 0 && n3 < questList.size())
				{
					SendChanges(EnumPacketAction.REMOVE, questList.get(n3));
				}
			} else if(n2 == 2)
			{
				if(n3 >= 1 && n3 < questList.size())
				{
					// Order changing may not work with HashMaps
					//QuestDatabase.questLines.add(n3 - 1, QuestDatabase.questLines.remove(n3));
					//SendChanges(2);
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
        	lineDesc.textboxKeyTyped(character, keyCode);
        	lineTitle.textboxKeyTyped(character, keyCode);
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
		lineDesc.mouseClicked(mx, my, click);
		
		if(selected != null)
		{
			boolean flag = false;
			
			if(!lineTitle.isFocused() && !lineTitle.getText().equals(selected.getUnlocalisedName()))
			{
				selected.getProperties().setProperty(NativePropertyTypes.NAME, lineTitle.getText());
				flag = true;
			}
			
			if(!lineDesc.isFocused() && !lineDesc.getText().equals(selected.getUnlocalisedDescription()))
			{
				selected.getProperties().setProperty(NativePropertyTypes.DESC, lineDesc.getText());
				flag = true;
			}
			
			if(flag)
			{
				SendChanges(EnumPacketAction.EDIT, selected);
			}
		}
    }
	
	@Override
	public void mouseScroll(int mx, int my, int scroll)
	{
		super.mouseScroll(mx, my, scroll);
        
        if(scroll != 0 && isWithin(mx, my, this.guiLeft, this.guiTop, sizeX/2, sizeY))
        {
    		leftScroll = Math.max(0, MathHelper.clamp_int(leftScroll + scroll, 0, questList.size() - maxRows));
    		RefreshColumns();
        }
	}
	
	public void RefreshColumns()
	{
		questList = QuestLineDatabase.INSTANCE.getAllKeys();
		
		leftScroll = Math.max(0, MathHelper.clamp_int(leftScroll, 0, questList.size() - maxRows));
		
		if(btnDesign != null)
		{
			btnDesign.enabled = selected != null;
		}

		@SuppressWarnings("unchecked")
		List<GuiButton> btnList = this.buttonList;
		
		for(int i = 5; i < btnList.size(); i++)
		{
			GuiButton btn = btnList.get(i);
			int n1 = btn.id - 5; // Line index
			int n2 = n1/maxRows; // Line listing (0 = line, 1 = delete, 2 = move up)
			int n3 = n1%maxRows + leftScroll; // Quest list index
			
			if(n2 == 0)
			{
				if(n3 >= 0 && n3 < questList.size())
				{
					btn.displayString = I18n.format(QuestLineDatabase.INSTANCE.getValue(questList.get(n3)).getUnlocalisedName());
					btn.visible = true;
					btn.enabled = questList.get(n3) != selID;
				} else
				{
					btn.displayString = "NULL";
					btn.enabled = btn.visible = false;
				}
			} else if(n2 == 1 || n2 == 2)
			{
				btn.enabled = btn.visible = n3 >= 0 && n3 < questList.size();
			}
		}
		
		if(selected == null)
		{
			lineTitle.setText("");
			lineTitle.setEnabled(false);
			lineDesc.setText("");
			lineDesc.setEnabled(false);
		} else
		{
			lineTitle.setText(selected.getUnlocalisedName());
			lineTitle.setEnabled(true);
			lineDesc.setText(selected.getUnlocalisedDescription());
			lineDesc.setEnabled(true);
		}
	}

	@Override
	public void setText(int id, String text)
	{
		if(id == 0)
		{
			if(lineDesc != null)
			{
				lineDesc.setText(text);
			}
			
			if(selected != null)
			{
				selected.getProperties().setProperty(NativePropertyTypes.DESC, text);
				SendChanges(EnumPacketAction.EDIT, selected);
			}
		}
	}
}

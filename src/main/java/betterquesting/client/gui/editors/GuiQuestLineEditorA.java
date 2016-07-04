package betterquesting.client.gui.editors;

import java.util.List;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MathHelper;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import betterquesting.client.gui.GuiQuesting;
import betterquesting.client.gui.misc.GuiBigTextField;
import betterquesting.client.gui.misc.GuiButtonQuesting;
import betterquesting.client.gui.misc.ITextEditor;
import betterquesting.client.gui.misc.IVolatileScreen;
import betterquesting.client.themes.ThemeRegistry;
import betterquesting.importers.ImporterRegistry;
import betterquesting.network.PacketAssembly;
import betterquesting.network.PacketTypeRegistry.BQPacketType;
import betterquesting.quests.QuestDatabase;
import betterquesting.quests.QuestLine;
import betterquesting.utils.NBTConverter;
import betterquesting.utils.RenderUtils;
import com.google.gson.JsonObject;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiQuestLineEditorA extends GuiQuesting implements ITextEditor, IVolatileScreen
{
	GuiButtonQuesting btnDesign;
	GuiTextField lineTitle;
	GuiBigTextField lineDesc;
	QuestLine selected;
	int selIndex = -1;
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
		 
		this.buttonList.add(new GuiButtonQuesting(1, guiLeft + 16, guiTop + sizeY - 48, (btnWidth - 16)/2, 20, I18n.format("betterquesting.btn.new")));
		GuiButtonQuesting btnImport = new GuiButtonQuesting(3, guiLeft + 16 + (btnWidth - 16)/2, guiTop + sizeY - 48, (btnWidth - 16)/2, 20, I18n.format("betterquesting.btn.import"));
		btnImport.enabled = ImporterRegistry.getImporters().size() > 0 && mc.isIntegratedServerRunning();
		this.buttonList.add(btnImport);
		this.buttonList.add(new GuiButtonQuesting(2, guiLeft + 16 + sx/4*3 - 75, guiTop + sizeY/2 + 20, 150, 20, I18n.format("betterquesting.btn.add_remove_quests")));
		btnDesign = new GuiButtonQuesting(4, guiLeft + 16 + sx/4*3 - 75, guiTop + sizeY/2 + 40, 150, 20, I18n.format("betterquesting.btn.designer"));
		this.buttonList.add(btnDesign);
		
		// Quest Line - Main
		for(int i = 0; i < maxRows; i++)
		{
			GuiButtonQuesting btn = new GuiButtonQuesting(this.buttonList.size(), guiLeft + 16 + 20, guiTop + 32 + (i*20), btnWidth - 56, 20, "NULL");
			this.buttonList.add(btn);
		}
		
		// Quest Line - Delete
		for(int i = 0; i < maxRows; i++)
		{
			GuiButtonQuesting btn = new GuiButtonQuesting(this.buttonList.size(), guiLeft + 16, guiTop + 32 + (i*20), 20, 20, "" + EnumChatFormatting.RED + EnumChatFormatting.BOLD + "x");
			this.buttonList.add(btn);
		}
		
		// Quest Line - Shift Up
		for(int i = 0; i < maxRows; i++)
		{
			GuiButtonQuesting btn = new GuiButtonQuesting(this.buttonList.size(), guiLeft + 16 + 20 + btnWidth - 56, guiTop + 32 + (i*20), 20, 20, "" + EnumChatFormatting.YELLOW + EnumChatFormatting.BOLD + "^");
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
		mc.renderEngine.bindTexture(ThemeRegistry.curTheme().guiTexture());
		
		// Left scroll bar
		this.drawTexturedModalRect(guiLeft + sizeX/2 - 16, this.guiTop + 32, 248, 0, 8, 20);
		int s = 20;
		while(s < (maxRows - 1) * 20)
		{
			this.drawTexturedModalRect(guiLeft + sizeX/2 - 16, this.guiTop + 32 + s, 248, 20, 8, 20);
			s += 20;
		}
		this.drawTexturedModalRect(guiLeft + sizeX/2 - 16, this.guiTop + 32 + s, 248, 40, 8, 20);
		this.drawTexturedModalRect(guiLeft + sizeX/2 - 16, this.guiTop + 32 + (int)Math.max(0, s * (float)leftScroll/(QuestDatabase.questLines.size() - maxRows)), 248, 60, 8, 20);
		
		RenderUtils.DrawLine(width/2, guiTop + 32, width/2, guiTop + sizeY - 48, 2F, ThemeRegistry.curTheme().textColor());
		
		mc.fontRenderer.drawString(I18n.format("betterquesting.gui.name"), guiLeft + sizeX/2 + 8, guiTop + sizeY/2 - 72, ThemeRegistry.curTheme().textColor().getRGB(), false);
		mc.fontRenderer.drawString(I18n.format("betterquesting.gui.description"), guiLeft + sizeX/2 + 8, guiTop + sizeY/2 - 32, ThemeRegistry.curTheme().textColor().getRGB(), false);
		
		lineTitle.drawTextBox();
		lineDesc.drawTextBox();
	}
	
	public void DeleteQuest(int id)
	{
		NBTTagCompound tags = new NBTTagCompound();
		tags.setInteger("action", 1);
		tags.setInteger("questID", id);
		PacketAssembly.SendToServer(BQPacketType.QUEST_EDIT.GetLocation(), tags);
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
	
	@Override
	public void actionPerformed(GuiButton btn)
	{
		super.actionPerformed(btn);
		
		if(btn.id == 1) // Add quest line
		{
			SendChanges(0);
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
				if(n3 >= 0 && n3 < QuestDatabase.questLines.size())
				{
					selected = QuestDatabase.questLines.get(n3);
					selIndex = n3;
				} else
				{
					selected = null;
					selIndex = -1;
				}
				
				RefreshColumns();
			} else if(n2 == 1)
			{
				if(n3 >= 0 && n3 < QuestDatabase.questLines.size())
				{
					QuestDatabase.questLines.remove(n3);
					SendChanges(2);
				}
			} else if(n2 == 2)
			{
				if(n3 >= 1 && n3 < QuestDatabase.questLines.size())
				{
					QuestDatabase.questLines.add(n3 - 1, QuestDatabase.questLines.remove(n3));
					SendChanges(2);
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
			
			if(!lineTitle.isFocused() && !lineTitle.getText().equals(selected.name))
			{
				selected.name = lineTitle.getText();
				flag = true;
			}
			
			if(!lineDesc.isFocused() && !lineDesc.getText().equals(selected.description))
			{
				selected.description = lineDesc.getText();
				flag = true;
			}
			
			if(flag)
			{
				SendChanges(2);
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
    		leftScroll = Math.max(0, MathHelper.clamp_int(leftScroll + SDX, 0, QuestDatabase.questLines.size() - maxRows));
    		RefreshColumns();
        }
    }
	
	public void RefreshColumns()
	{
		leftScroll = Math.max(0, MathHelper.clamp_int(leftScroll, 0, QuestDatabase.questLines.size() - maxRows));
		
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
			int n2 = n1/maxRows; // Line listing (0 = line, 1 = delete)
			int n3 = n1%maxRows + leftScroll; // Quest list index
			
			if(n2 == 0)
			{
				if(n3 >= 0 && n3 < QuestDatabase.questLines.size())
				{
					btn.displayString = I18n.format(QuestDatabase.questLines.get(n3).name);
					btn.enabled = btn.visible = true;
				} else
				{
					btn.displayString = "NULL";
					btn.enabled = btn.visible = false;
				}
			} else if(n2 == 1 || n2 == 2)
			{
				btn.enabled = btn.visible = n3 >= 0 && n3 < QuestDatabase.questLines.size();
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
			lineTitle.setText(selected.name);
			lineTitle.setEnabled(true);
			lineDesc.setText(selected.description);
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
				selected.description = text;
				SendChanges(2);
			}
		}
	}
}

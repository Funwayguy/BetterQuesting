package betterquesting.client.gui.editors;

import java.awt.Color;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.nbt.NBTTagCompound;
import betterquesting.client.gui.GuiQuesting;
import betterquesting.client.gui.editors.json.GuiJsonObject;
import betterquesting.client.gui.misc.GuiButtonQuesting;
import betterquesting.core.BetterQuesting;
import betterquesting.network.PacketQuesting;
import betterquesting.quests.QuestInstance;
import betterquesting.utils.NBTConverter;
import com.google.gson.JsonObject;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiQuestEditor extends GuiQuesting
{
	JsonObject lastEdit;
	QuestInstance quest;
	
	GuiTextField titleField;
	GuiTextField descField;
	
	public GuiQuestEditor(GuiScreen parent, QuestInstance quest)
	{
		super(parent, "Quest Editor");
		this.quest = quest;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public void initGui()
	{
		super.initGui();
		
		if(lastEdit != null)
		{
			quest.readFromJSON(lastEdit);
			lastEdit = null;
			SendChanges();
		}
		
		titleField = new GuiTextField(this.fontRendererObj, width/2 - 99, height/2 - 68 + 1, 198, 18);
		titleField.setMaxStringLength(Integer.MAX_VALUE);
		titleField.setText(quest.name);
		descField = new GuiTextField(this.fontRendererObj, width/2 - 99, height/2 - 28 + 1, 198, 18);
		descField.setMaxStringLength(Integer.MAX_VALUE);
		descField.setText(quest.description);
		
		GuiButtonQuesting btn = new GuiButtonQuesting(1, width/2 - 100, height/2 + 8, 200, 20, "Edit Rewards");
		this.buttonList.add(btn);
		btn = new GuiButtonQuesting(2, width/2 - 100, height/2 + 28, 200, 20, "Edit Tasks");
		this.buttonList.add(btn);
		btn = new GuiButtonQuesting(3, width/2 - 100, height/2 + 48, 200, 20, "Edit Prerequisites");
		this.buttonList.add(btn);
		btn = new GuiButtonQuesting(4, width/2 - 100, height/2 + 68, 200, 20, "Advanced Editor");
		this.buttonList.add(btn);
	}
	
	@Override
	public void drawScreen(int mx, int my, float partialTick)
	{
		super.drawScreen(mx, my, partialTick);
		
		titleField.drawTextBox();
		descField.drawTextBox();

		mc.fontRenderer.drawString("Name:", width/2 - 100, height/2 - 80, Color.BLACK.getRGB(), false);
		mc.fontRenderer.drawString("Description: ", width/2 - 100, height/2 - 40, Color.BLACK.getRGB(), false);
	}
	
	@Override
	public void actionPerformed(GuiButton button)
	{
		super.actionPerformed(button);
		
		if(button.id == 1) // Rewards
		{
			mc.displayGuiScreen(new GuiRewardEditor(this, quest));
		} else if(button.id == 2) // Tasks
		{
			mc.displayGuiScreen(new GuiTaskEditor(this, quest));
		} else if(button.id == 3) // Prerequisites
		{
			mc.displayGuiScreen(new GuiPrerequisiteEditor(this, quest));
		} else if(button.id == 4) // Raw JSON
		{
			this.lastEdit = new JsonObject();
			quest.writeToJSON(lastEdit);
			mc.displayGuiScreen(new GuiJsonObject(this, lastEdit));
		}
	}

    /**
     * Fired when a key is typed. This is the equivalent of KeyListener.keyTyped(KeyEvent e).
     */
	@Override
    protected void keyTyped(char character, int keyCode)
    {
        super.keyTyped(character, keyCode);
        
        titleField.textboxKeyTyped(character, keyCode);
        descField.textboxKeyTyped(character, keyCode);
    }
    /**
     * Called when the mouse is clicked.
     */
	@Override
    protected void mouseClicked(int mx, int my, int click)
    {
		super.mouseClicked(mx, my, click);
		
		titleField.mouseClicked(mx, my, click);
		descField.mouseClicked(mx, my, click);
		
		boolean flag = false; // Just in case measure to prevent multiple update calls
		
		if(!titleField.isFocused() && !titleField.getText().equals(quest.name))
		{
			// Apply changes, this way is automatic and doesn't require pressing Enter
			quest.name = titleField.getText();
			flag = true;
		}
		
		if(!descField.isFocused() && !descField.getText().equals(quest.description))
		{
			// Apply changes, this way is automatic and doesn't require pressing Enter
			quest.description = descField.getText();
			flag = true;
		}
		
		if(flag)
		{
			SendChanges();
		}
    }
	
	// If the changes are approved by the server, it will be broadcast to all players including the editor
	public void SendChanges()
	{
		JsonObject json = new JsonObject();
		quest.writeToJSON(json);
		NBTTagCompound tags = new NBTTagCompound();
		tags.setInteger("ID", 5);
		tags.setInteger("action", 0); // Action: Update data
		tags.setInteger("questID", quest.questID);
		tags.setTag("Data", NBTConverter.JSONtoNBT_Object(json, new NBTTagCompound()));
		BetterQuesting.instance.network.sendToServer(new PacketQuesting(tags));
	}
}

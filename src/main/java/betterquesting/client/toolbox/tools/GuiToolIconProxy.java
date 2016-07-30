package betterquesting.client.toolbox.tools;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.nbt.NBTTagCompound;
import com.google.gson.JsonObject;
import betterquesting.client.gui.GuiQuesting;
import betterquesting.client.gui.editors.json.GuiJsonItemSelection;
import betterquesting.network.PacketAssembly;
import betterquesting.network.PacketTypeRegistry.BQPacketType;
import betterquesting.quests.QuestInstance;
import betterquesting.utils.JsonHelper;
import betterquesting.utils.NBTConverter;

public class GuiToolIconProxy extends GuiQuesting
{
	QuestInstance quest;
	JsonObject jProg = new JsonObject();
	JsonObject json = new JsonObject();
	boolean flag = false;
	
	public GuiToolIconProxy(GuiScreen parent, QuestInstance quest)
	{
		super(parent, "");
		this.quest = quest;
		quest.writeToJSON(json);
		quest.writeProgressToJSON(jProg);
	}
	
	@Override
	public void initGui()
	{
		if(flag)
		{
			quest.readFromJSON(json);
			quest.readProgressFromJSON(jProg);
			SendChanges();
			this.mc.displayGuiScreen(parent);
			return;
		} else
		{
			flag = true;
			mc.displayGuiScreen(new GuiJsonItemSelection(this, JsonHelper.GetObject(json, "icon")));
		}
	}
	
	// If the changes are approved by the server, it will be broadcast to all players including the editor
	public void SendChanges()
	{
		JsonObject json1 = new JsonObject();
		quest.writeToJSON(json1);
		JsonObject json2 = new JsonObject();
		quest.writeProgressToJSON(json2);
		NBTTagCompound tags = new NBTTagCompound();
		tags.setInteger("action", 0); // Action: Update data
		tags.setInteger("questID", quest.questID);
		tags.setTag("Data", NBTConverter.JSONtoNBT_Object(json1, new NBTTagCompound()));
		tags.setTag("Progress", NBTConverter.JSONtoNBT_Object(json2, new NBTTagCompound()));
		PacketAssembly.SendToServer(BQPacketType.QUEST_EDIT.GetLocation(), tags);
	}
}

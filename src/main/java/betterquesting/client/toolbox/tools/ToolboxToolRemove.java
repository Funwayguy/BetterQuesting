package betterquesting.client.toolbox.tools;

import com.google.gson.JsonObject;
import net.minecraft.nbt.NBTTagCompound;
import betterquesting.client.gui.GuiQuesting;
import betterquesting.client.gui.misc.GuiButtonQuestInstance;
import betterquesting.client.toolbox.ToolboxTool;
import betterquesting.network.PacketAssembly;
import betterquesting.network.PacketTypeRegistry.BQPacketType;
import betterquesting.quests.QuestDatabase;
import betterquesting.quests.QuestLine;
import betterquesting.quests.QuestLine.QuestLineEntry;
import betterquesting.utils.NBTConverter;

public class ToolboxToolRemove extends ToolboxTool
{
	public ToolboxToolRemove(GuiQuesting screen)
	{
		super(screen);
	}
	
	@Override
	public void onMouseClick(int mx, int my, int click)
	{
		if(click != 0 || ui.getQuestLine() == null)
		{
			return;
		}
		
		QuestLine line = ui.getQuestLine();
		GuiButtonQuestInstance btn = ui.getClickedQuest(mx, my);
		
		if(line != null && btn != null)
		{
			QuestLineEntry entry = line.getEntryByID(btn.quest.questID);
			line.questList.remove(entry);
			
			NBTTagCompound tags = new NBTTagCompound();
			tags.setInteger("action", 2);
			JsonObject json = new JsonObject();
			QuestDatabase.writeToJson_Lines(json);
			tags.setTag("Data", NBTConverter.JSONtoNBT_Object(json, new NBTTagCompound()));
			PacketAssembly.SendToServer(BQPacketType.LINE_EDIT.GetLocation(), tags);
		}
	}
}

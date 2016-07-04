package betterquesting.client.toolbox.tools;

import net.minecraft.nbt.NBTTagCompound;
import betterquesting.client.gui.GuiQuesting;
import betterquesting.client.gui.misc.GuiButtonQuestInstance;
import betterquesting.client.toolbox.ToolboxTool;
import betterquesting.network.PacketAssembly;
import betterquesting.network.PacketTypeRegistry.BQPacketType;

public class ToolboxToolComplete extends ToolboxTool
{
	public ToolboxToolComplete(GuiQuesting screen)
	{
		super(screen);
	}
	
	@Override
	public void onMouseClick(int mx, int my, int click)
	{
		if(click != 0)
		{
			return;
		}
		
		GuiButtonQuestInstance btn = ui.getClickedQuest(mx, my);
		
		if(btn != null)
		{
			NBTTagCompound tags = new NBTTagCompound();
			tags.setInteger("action", 3); // Complete quest
			tags.setInteger("questID", btn.quest.questID);
			PacketAssembly.SendToServer(BQPacketType.QUEST_EDIT.GetLocation(), tags);
		}
	}
}

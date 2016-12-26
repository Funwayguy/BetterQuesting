package betterquesting.client.toolbox.tools;

import net.minecraft.nbt.NBTTagCompound;
import betterquesting.api.client.gui.controls.GuiButtonQuestInstance;
import betterquesting.api.client.gui.misc.IGuiQuestLine;
import betterquesting.api.client.toolbox.IToolboxTool;
import betterquesting.api.enums.EnumPacketAction;
import betterquesting.api.network.QuestingPacket;
import betterquesting.network.PacketSender;
import betterquesting.network.PacketTypeNative;
import betterquesting.questing.QuestDatabase;

public class ToolboxToolDelete implements IToolboxTool
{
	IGuiQuestLine gui;
	
	@Override
	public void initTool(IGuiQuestLine gui)
	{
		this.gui = gui;
	}

	@Override
	public void disableTool()
	{
	}

	@Override
	public void drawTool(int mx, int my, float partialTick)
	{
	}
	
	@Override
	public void onMouseClick(int mx, int my, int click)
	{
		if(click != 0)
		{
			return;
		}
		
		GuiButtonQuestInstance btn = gui.getQuestLine().getButtonAt(mx, my);
		
		if(btn != null)
		{
			NBTTagCompound tags = new NBTTagCompound();
			tags.setInteger("action", EnumPacketAction.REMOVE.ordinal()); // Delete quest
			tags.setInteger("questID", QuestDatabase.INSTANCE.getKey(btn.getQuest()));
			PacketSender.INSTANCE.sendToServer(new QuestingPacket(PacketTypeNative.QUEST_EDIT.GetLocation(), tags));
		}
	}

	@Override
	public void onMouseScroll(int mx, int my, int scroll)
	{
	}

	@Override
	public void onKeyPressed(char c, int key)
	{
	}

	@Override
	public boolean allowTooltips()
	{
		return true;
	}

	@Override
	public boolean allowScrolling(int click)
	{
		return true;
	}

	@Override
	public boolean allowZoom()
	{
		return true;
	}

	@Override
	public boolean clampScrolling()
	{
		return true;
	}
}

package adv_director.client.toolbox.tools;

import net.minecraft.nbt.NBTTagCompound;
import adv_director.api.client.gui.controls.GuiButtonQuestInstance;
import adv_director.api.client.gui.misc.IGuiQuestLine;
import adv_director.api.client.toolbox.IToolboxTool;
import adv_director.api.enums.EnumPacketAction;
import adv_director.api.network.QuestingPacket;
import adv_director.network.PacketSender;
import adv_director.network.PacketTypeNative;
import adv_director.questing.QuestDatabase;

public class ToolboxToolReset implements IToolboxTool
{
	private IGuiQuestLine gui;
	
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
			tags.setInteger("action", EnumPacketAction.SET.ordinal()); // Complete quest
			tags.setInteger("questID", QuestDatabase.INSTANCE.getKey(btn.getQuest()));
			tags.setBoolean("status", false);
			
			PacketSender.INSTANCE.sendToServer(new QuestingPacket(PacketTypeNative.QUEST_EDIT.GetLocation(), tags));
		}
	}

	@Override
	public void drawTool(int mx, int my, float partialTick)
	{
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

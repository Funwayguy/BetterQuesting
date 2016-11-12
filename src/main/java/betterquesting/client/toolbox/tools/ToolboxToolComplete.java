package betterquesting.client.toolbox.tools;

import net.minecraft.nbt.NBTTagCompound;
import betterquesting.api.client.gui.controls.GuiButtonQuestInstance;
import betterquesting.api.client.gui.quest.IGuiQuestLine;
import betterquesting.api.client.toolbox.IToolboxTool;
import betterquesting.api.enums.EnumPacketAction;
import betterquesting.api.network.PacketTypeNative;
import betterquesting.api.network.PreparedPayload;
import betterquesting.database.QuestDatabase;
import betterquesting.network.PacketSender;

public class ToolboxToolComplete implements IToolboxTool
{
	IGuiQuestLine gui = null;
	
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
			tags.setInteger("action", EnumPacketAction.SET.ordinal()); // Complete quest
			tags.setInteger("questID", QuestDatabase.INSTANCE.getKey(btn.getQuest()));
			tags.setBoolean("state", true);
			PacketSender.INSTANCE.sendToServer(new PreparedPayload(PacketTypeNative.QUEST_EDIT.GetLocation(), tags));
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

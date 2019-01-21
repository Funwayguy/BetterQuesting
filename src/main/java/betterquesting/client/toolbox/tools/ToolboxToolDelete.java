package betterquesting.client.toolbox.tools;

import betterquesting.api.client.toolbox.IToolboxTool;
import betterquesting.api.enums.EnumPacketAction;
import betterquesting.api.network.QuestingPacket;
import betterquesting.api2.client.gui.controls.PanelButtonQuest;
import betterquesting.client.gui2.CanvasQuestLine;
import betterquesting.network.PacketSender;
import betterquesting.network.PacketTypeNative;
import betterquesting.questing.QuestDatabase;
import net.minecraft.nbt.NBTTagCompound;

import java.util.List;

public class ToolboxToolDelete implements IToolboxTool
{
	private CanvasQuestLine gui;
	
	@Override
	public void initTool(CanvasQuestLine gui)
	{
		this.gui = gui;
	}

	@Override
	public void disableTool()
	{
	}

	@Override
	public void drawCanvas(int mx, int my, float partialTick)
	{
	}
	
	@Override
    public void drawOverlay(int mx, int my, float partialTick)
    {
    }
    
    @Override
    public List<String> getTooltip(int mx, int my)
    {
        return null;
    }
	
	@Override
	public boolean onMouseClick(int mx, int my, int click)
	{
		if(click != 0)
		{
			return false;
		}
		
		PanelButtonQuest btn = gui.getButtonAt(mx, my);
		
		if(btn != null)
		{
			NBTTagCompound tags = new NBTTagCompound();
			tags.setInteger("action", EnumPacketAction.REMOVE.ordinal()); // Delete quest
			tags.setInteger("questID", QuestDatabase.INSTANCE.getID(btn.getStoredValue()));
			PacketSender.INSTANCE.sendToServer(new QuestingPacket(PacketTypeNative.QUEST_EDIT.GetLocation(), tags));
			return true;
		}
		
		return false;
	}
	
	@Override
    public boolean onMouseRelease(int mx, int my, int click)
    {
        return false;
    }

	@Override
	public boolean onMouseScroll(int mx, int my, int scroll)
	{
	    return false;
	}

	@Override
	public boolean onKeyPressed(char c, int key)
	{
	    return false;
	}

	@Override
	public boolean clampScrolling()
	{
		return true;
	}
}

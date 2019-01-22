package betterquesting.client.toolbox.tools;

import betterquesting.api.client.toolbox.IToolboxTool;
import betterquesting.api.enums.EnumPacketAction;
import betterquesting.api.network.QuestingPacket;
import betterquesting.api.properties.NativeProps;
import betterquesting.api2.client.gui.controls.PanelButtonQuest;
import betterquesting.client.gui2.CanvasQuestLine;
import betterquesting.client.gui2.editors.nbt.GuiItemSelection;
import betterquesting.network.PacketSender;
import betterquesting.network.PacketTypeNative;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagCompound;

import java.util.List;

public class ToolboxToolIcon implements IToolboxTool
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
    public void refresh(CanvasQuestLine gui)
    {
    }
	
	@Override
	public boolean onMouseClick(int mx, int my, int click)
	{
		if(click != 0 || !gui.getTransform().contains(mx, my)) return false;
		
		PanelButtonQuest btn = gui.getButtonAt(mx, my);
		
		if(btn != null)
		{
		    Minecraft mc = Minecraft.getMinecraft();
            mc.displayGuiScreen(new GuiItemSelection(mc.currentScreen, btn.getStoredValue().getValue().getProperty(NativeProps.ICON), value -> {
                btn.getStoredValue().getValue().setProperty(NativeProps.ICON, value);
                NBTTagCompound base = new NBTTagCompound();
                base.setTag("config", btn.getStoredValue().getValue().writeToNBT(new NBTTagCompound()));
                base.setTag("progress", btn.getStoredValue().getValue().writeProgressToNBT(new NBTTagCompound(), null));
                NBTTagCompound tags = new NBTTagCompound();
                tags.setInteger("action", EnumPacketAction.EDIT.ordinal()); // Action: Update data
                tags.setInteger("questID", btn.getStoredValue().getID());
                tags.setTag("data", base);
                PacketSender.INSTANCE.sendToServer(new QuestingPacket(PacketTypeNative.QUEST_EDIT.GetLocation(), tags));
            }));
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
	public boolean onKeyPressed(char c, int keyCode)
	{
	    return false;
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
	public boolean clampScrolling()
	{
		return true;
	}
}

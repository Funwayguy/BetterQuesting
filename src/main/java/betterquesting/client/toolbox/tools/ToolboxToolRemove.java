package betterquesting.client.toolbox.tools;

import net.minecraft.nbt.NBTTagCompound;
import betterquesting.api.client.gui.premade.controls.GuiButtonQuestInstance;
import betterquesting.api.client.gui.quest.IGuiQuestLine;
import betterquesting.api.client.toolbox.IToolboxTool;
import betterquesting.api.enums.EnumPacketAction;
import betterquesting.api.enums.EnumSaveType;
import betterquesting.api.network.PacketTypeNative;
import betterquesting.api.network.PreparedPayload;
import betterquesting.api.quests.IQuestLineContainer;
import betterquesting.api.utils.NBTConverter;
import betterquesting.network.PacketSender;
import betterquesting.quests.QuestDatabase;
import betterquesting.quests.QuestLineDatabase;
import com.google.gson.JsonObject;

public class ToolboxToolRemove implements IToolboxTool
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
		
		IQuestLineContainer line = gui.getQuestLine().getQuestLine();
		GuiButtonQuestInstance btn = gui.getQuestLine().getButtonAt(mx, my);
		
		if(line != null && btn != null)
		{
			int qID = QuestDatabase.INSTANCE.getKey(btn.getQuest());
			line.remove(qID);
			
			NBTTagCompound tags = new NBTTagCompound();
			tags.setInteger("action", EnumPacketAction.EDIT.ordinal());
			JsonObject base = new JsonObject();
			base.add("line", line.writeToJson(new JsonObject(), EnumSaveType.CONFIG));
			tags.setTag("data", NBTConverter.JSONtoNBT_Object(base, new NBTTagCompound()));
			tags.setInteger("lineID", QuestLineDatabase.INSTANCE.getKey(line));
			PacketSender.INSTANCE.sendToServer(new PreparedPayload(PacketTypeNative.LINE_EDIT.GetLocation(), tags));
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

package betterquesting.client.toolbox;

import betterquesting.api.api.ApiReference;
import betterquesting.api.api.QuestingAPI;
import betterquesting.api.client.gui.GuiScreenThemed;
import betterquesting.api.enums.EnumPacketAction;
import betterquesting.api.misc.ICallback;
import betterquesting.api.network.QuestingPacket;
import betterquesting.api.questing.IQuestLine;
import betterquesting.network.PacketSender;
import betterquesting.network.PacketTypeNative;
import betterquesting.questing.QuestLineDatabase;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.nbt.NBTTagCompound;

@Deprecated
public class GuiQuestLineEditProxy extends GuiScreenThemed implements ICallback<NBTTagCompound>
{
	private final IQuestLine line;
	private boolean flag = false;
	
	public GuiQuestLineEditProxy(GuiScreen parent, IQuestLine questLine)
	{
		super(parent, questLine.getUnlocalisedName());
		this.line = questLine;
	}
	
	public void initGui()
	{
		if(flag)
		{
			mc.displayGuiScreen(parent);
		} else
		{
			flag = true;
			QuestingAPI.getAPI(ApiReference.GUI_HELPER).openJsonEditor(this, this, line.writeToNBT(new NBTTagCompound(), null), null);
		}
	}
	
	public void SendChanges(EnumPacketAction action)
	{
		if(action == null)
		{
			return;
		}
		
		NBTTagCompound tags = new NBTTagCompound();
		
		if(action == EnumPacketAction.EDIT && line != null)
		{
			NBTTagCompound base = new NBTTagCompound();
			base.setTag("line", line.writeToNBT(new NBTTagCompound(), null));
			tags.setTag("data", base);
		}
		
		tags.setInteger("action", action.ordinal());
		tags.setInteger("lineID", QuestLineDatabase.INSTANCE.getID(line));
		
		PacketSender.INSTANCE.sendToServer(new QuestingPacket(PacketTypeNative.LINE_EDIT.GetLocation(), tags));
	}

	@Override
	public void setValue(NBTTagCompound value)
	{
		line.readFromNBT(value, false);
		SendChanges(EnumPacketAction.EDIT);
	}
}

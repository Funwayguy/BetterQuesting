package adv_director.client.toolbox;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.init.Items;
import net.minecraft.nbt.NBTTagCompound;
import adv_director.api.client.gui.GuiScreenThemed;
import adv_director.api.enums.EnumPacketAction;
import adv_director.api.enums.EnumSaveType;
import adv_director.api.misc.ICallback;
import adv_director.api.network.QuestingPacket;
import adv_director.api.properties.NativeProps;
import adv_director.api.questing.IQuest;
import adv_director.api.utils.BigItemStack;
import adv_director.api.utils.NBTConverter;
import adv_director.client.gui.editors.json.GuiJsonItemSelection;
import adv_director.network.PacketSender;
import adv_director.network.PacketTypeNative;
import adv_director.questing.QuestDatabase;
import com.google.gson.JsonObject;

public class GuiToolIconProxy extends GuiScreenThemed implements ICallback<BigItemStack>
{
	private final IQuest quest;
	boolean flag = false;
	
	public GuiToolIconProxy(GuiScreen parent, IQuest quest)
	{
		super(parent, "");
		this.quest = quest;
	}
	
	@Override
	public void initGui()
	{
		if(flag)
		{
			this.mc.displayGuiScreen(parent);
			return;
		} else
		{
			flag = true;
			mc.displayGuiScreen(new GuiJsonItemSelection(this, this, quest.getItemIcon()));
		}
	}
	
	// If the changes are approved by the server, it will be broadcast to all players including the editor
	public void SendChanges()
	{
		JsonObject base = new JsonObject();
		base.add("config", quest.writeToJson(new JsonObject(), EnumSaveType.CONFIG));
		base.add("progress", quest.writeToJson(new JsonObject(), EnumSaveType.PROGRESS));
		NBTTagCompound tags = new NBTTagCompound();
		tags.setTag("data", NBTConverter.JSONtoNBT_Object(base, new NBTTagCompound()));
		tags.setInteger("action", EnumPacketAction.EDIT.ordinal());
		tags.setInteger("questID", QuestDatabase.INSTANCE.getKey(quest));
		
		PacketSender.INSTANCE.sendToServer(new QuestingPacket(PacketTypeNative.QUEST_EDIT.GetLocation(), tags));
	}

	@Override
	public void setValue(BigItemStack value)
	{
		BigItemStack stack = value != null? value : new BigItemStack(Items.NETHER_STAR);
		quest.getProperties().setProperty(NativeProps.ICON, stack);
		SendChanges();
	}
}

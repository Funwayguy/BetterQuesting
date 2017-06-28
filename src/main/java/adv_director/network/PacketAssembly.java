package adv_director.network;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.GZIPInputStream;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTSizeTracker;
import net.minecraft.nbt.NBTTagByteArray;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.MathHelper;
import org.apache.logging.log4j.Level;
import adv_director.core.AdvDirector;

public final class PacketAssembly
{
	public static final PacketAssembly INSTANCE = new PacketAssembly();
	
	// Player assigned packet buffers
	private final ConcurrentHashMap<UUID,byte[]> buffer = new ConcurrentHashMap<UUID,byte[]>();
	// Internal server packet buffer (server to server or client side)
	private byte[] serverBuf = null;
	private int id = 0;
	
	private PacketAssembly()
	{
	}
	
	public ArrayList<NBTTagCompound> splitPacket(NBTTagCompound tags)
	{
		ArrayList<NBTTagCompound> pkts = new ArrayList<NBTTagCompound>();
		
		try
		{
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			CompressedStreamTools.writeCompressed(tags, baos);
			byte[] data = baos.toByteArray();
			baos.close();
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
				
				container.setInteger("size", data.length); // If the buffer isn't yet created, how big is it
				container.setInteger("index", idx); // Where should this piece start writing too
				container.setBoolean("end", p == req - 1);
				container.setTag("data", new NBTTagByteArray(part)); // The raw byte data to write
				
				pkts.add(container);
				
			}
		} catch(Exception e)
		{
			AdvDirector.logger.log(Level.INFO, "Unable to build packet", e);
		}
		
		id = (id + 1)%100; // Cycle the index
		
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
		
		if(tmp == null || tmp.length != size)
		{
			tmp = new byte[size];
			setBuffer(owner, tmp);
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
				NBTTagCompound tag = CompressedStreamTools.read(dis , NBTSizeTracker.INFINITE);
				dis.close();
				return tag;
			} catch(Exception e)
			{
				AdvDirector.logger.log(Level.INFO, "Unable to assemble packet", e);
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
			return buffer.get(owner);
		}
	}
	
	public void setBuffer(UUID owner, byte[] value)
	{
		if(owner == null)
		{
			serverBuf = value;
		} else
		{
			buffer.put(owner, value);
		}
	}
	
	public void clearBuffer(UUID owner)
	{
		if(owner == null)
		{
			serverBuf = null;
		} else
		{
			buffer.remove(owner);
		}
	}
}

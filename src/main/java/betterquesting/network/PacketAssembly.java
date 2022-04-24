package betterquesting.network;

import betterquesting.core.BetterQuesting;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTSizeTracker;
import net.minecraft.nbt.NBTTagByteArray;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.MathHelper;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.util.*;
import java.util.zip.GZIPInputStream;

public final class PacketAssembly {
    public static final PacketAssembly INSTANCE = new PacketAssembly();

    // TODO: Allow for simultaneous packet assembly (may not be necessary)
    // TODO: Implement PROPER thread safety that doesn't cause dirty read/writes
    // TODO: Add a scheduler to bulk up multiple data packets to send on the next tick (also may be unnecessary)
    // Player assigned packet buffers
    private final HashMap<UUID, byte[]> buffer = new HashMap<>();

    // Internal server packet buffer (server to server or client side)
    private byte[] serverBuf = null;
    //private int id = 0;

    private static final int bufSize = 20480; // 20KB

    public List<NBTTagCompound> splitPacket(NBTTagCompound tags) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            CompressedStreamTools.writeCompressed(tags, baos);
            baos.flush();
            byte[] data = baos.toByteArray();
            baos.close();
            int req = MathHelper.ceil(data.length / (float) bufSize);
            List<NBTTagCompound> pkts = new ArrayList<>(req);

            for (int p = 0; p < req; p++) {
                int idx = p * bufSize;
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
        } catch (Exception e) {
            BetterQuesting.logger.error("Unable to split build packet!", e);
            return Collections.emptyList();
        }
    }

    /**
     * Appends a packet onto the buffer and returns an assembled NBTTagCompound when complete
     */
    public NBTTagCompound assemblePacket(UUID owner, NBTTagCompound tags) {
        int size = tags.getInteger("size");
        int index = tags.getInteger("index");
        boolean end = tags.getBoolean("end");
        byte[] data = tags.getByteArray("data");

        byte[] tmp = getBuffer(owner);

        if (tmp == null) {
            tmp = new byte[size];
            setBuffer(owner, tmp);
        } else if (tmp.length != size) {
            BetterQuesting.logger.error("Unexpected change in BQ packet byte length: " + size + " > " + tmp.length);
            clearBuffer(owner);
            return null;
        }

        System.arraycopy(data, 0, tmp, index, data.length);
		/*for(int i = 0; i < data.length && index + i < size; i++)
		{
			tmp[index + i] = data[i];
		}*/

        if (end) {
            clearBuffer(owner);

            try {
                DataInputStream dis = new DataInputStream(new BufferedInputStream(new GZIPInputStream(new ByteArrayInputStream(tmp))));
                NBTTagCompound tag = CompressedStreamTools.read(dis, NBTSizeTracker.INFINITE);
                dis.close();
                return tag;
            } catch (Exception e) {
                throw new RuntimeException("Unable to assemble BQ packet", e);
            }
        }

        return null;
    }

    public byte[] getBuffer(UUID owner) {
        if (owner == null) {
            return serverBuf;
        } else {
            synchronized (buffer) {
                return buffer.get(owner);
            }
        }
    }

    public void setBuffer(UUID owner, byte[] value) {
        if (owner == null) {
            serverBuf = value;
        } else {
            synchronized (buffer) {
                if (buffer.containsKey(owner)) {
                    throw new IllegalStateException("Attepted to start more than one BQ packet assembly for UUID " + owner.toString());
                }

                buffer.put(owner, value);
            }
        }
    }

    public void clearBuffer(UUID owner) {
        if (owner == null) {
            serverBuf = null;
        } else {
            synchronized (buffer) {
                buffer.remove(owner);
            }
        }
    }
}

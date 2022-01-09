package betterquesting.questing.tasks;

import betterquesting.api.questing.IQuest;
import betterquesting.api2.client.gui.misc.IGuiRect;
import betterquesting.api2.client.gui.panels.IGuiPanel;
import betterquesting.api2.storage.DBEntry;
import betterquesting.api2.utils.ParticipantInfo;
import betterquesting.core.BetterQuesting;
import betterquesting.client.gui2.tasks.PanelTaskLocation;
import betterquesting.questing.tasks.factory.FactoryTaskLocation;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StringUtils;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import org.apache.logging.log4j.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public class TaskLocation implements ITaskTickable
{
	private final Set<UUID> completeUsers = new TreeSet<>();
	public String name = "New Location";
	public String structure = "";
	public String biome = "";
	public int x = 0;
	public int y = 0;
	public int z = 0;
	public int dim = 0;
	public int range = -1;
	public boolean visible = false;
	public boolean hideInfo = false;
	public boolean invert = false;
	public boolean taxiCab = false;
	
	@Override
	public ResourceLocation getFactoryID()
	{
		return FactoryTaskLocation.INSTANCE.getRegistryName();
	}
	
	@Override
	public String getUnlocalisedName()
	{
		return "bq_standard.task.location";
	}
	
	@Override
	public boolean isComplete(UUID uuid)
	{
		return completeUsers.contains(uuid);
	}
	
	@Override
	public void setComplete(UUID uuid)
	{
		completeUsers.add(uuid);
	}
 
	@Override
	public void resetUser(@Nullable UUID uuid)
	{
	    if(uuid == null)
        {
		    completeUsers.clear();
        } else
        {
            completeUsers.remove(uuid);
        }
	}
	
	@Override
	public void tickTask(@Nonnull ParticipantInfo pInfo, DBEntry<IQuest> quest)
	{
		if(pInfo.PLAYER.ticksExisted%100 == 0) internalDetect(pInfo, quest);
	}
	
	@Override
	public void detect(@Nonnull ParticipantInfo pInfo, DBEntry<IQuest> quest)
	{
		internalDetect(pInfo, quest);
	}
	
	private void internalDetect(@Nonnull ParticipantInfo pInfo, DBEntry<IQuest> quest)
    {
		if(!pInfo.PLAYER.isEntityAlive() || !(pInfo.PLAYER instanceof EntityPlayerMP)) return;
		
		EntityPlayerMP playerMP = (EntityPlayerMP)pInfo.PLAYER;
		
		boolean flag = false;
		
		if(playerMP.dimension == dim && (range <= 0 || getDistance(playerMP) <= range))
		{
		    if(!StringUtils.isNullOrEmpty(biome) && !new ResourceLocation(biome).equals(playerMP.getServerWorld().getBiome(playerMP.getPosition()).getRegistryName()))
            {
                if(!invert) return;
            } else if(!StringUtils.isNullOrEmpty(structure) && !playerMP.getServerWorld().getChunkProvider().isInsideStructure(playerMP.world, structure, playerMP.getPosition()))
            {
                if(!invert) return;
            } else if(visible && range > 0) // Do not do ray casting with infinite range!
			{
				Vec3d pPos = new Vec3d(playerMP.posX, playerMP.posY + playerMP.getEyeHeight(), playerMP.posZ);
				Vec3d tPos = new Vec3d(x, y, z);
				RayTraceResult mop = playerMP.world.rayTraceBlocks(pPos, tPos, false, true, false);
				
				if(mop == null || mop.typeOfHit != RayTraceResult.Type.BLOCK)
				{
					flag = true;
				}
			} else
			{
				flag = true;
			}
		}
		
		if(flag != invert)
        {
            pInfo.ALL_UUIDS.forEach((uuid) -> {
                if(!isComplete(uuid)) setComplete(uuid);
            });
            pInfo.markDirtyParty(Collections.singletonList(quest.getID()));
        }
    }
	
	private double getDistance(EntityPlayer player)
    {
        if(!taxiCab)
        {
            return player.getDistance(x, y, z);
        } else
        {
            BlockPos pPos = player.getPosition();
            return Math.abs(pPos.getX() - x) + Math.abs(pPos.getY() - y) + Math.abs(pPos.getZ() - z);
        }
    }
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt)
	{
		nbt.setString("name", name);
		nbt.setInteger("posX", x);
		nbt.setInteger("posY", y);
		nbt.setInteger("posZ", z);
		nbt.setInteger("dimension", dim);
		nbt.setString("biome", biome);
		nbt.setString("structure", structure);
		nbt.setInteger("range", range);
		nbt.setBoolean("visible", visible);
		nbt.setBoolean("hideInfo", hideInfo);
		nbt.setBoolean("invert", invert);
		nbt.setBoolean("taxiCabDist", taxiCab);
		
		return nbt;
	}
	
	@Override
	public void readFromNBT(NBTTagCompound nbt)
	{
		name = nbt.getString("name");
		x = nbt.getInteger("posX");
		y = nbt.getInteger("posY");
		z = nbt.getInteger("posZ");
		dim = nbt.getInteger("dimension");
		biome = nbt.getString("biome");
		structure = nbt.getString("structure");
		range = nbt.getInteger("range");
		visible = nbt.getBoolean("visible");
		hideInfo = nbt.getBoolean("hideInfo");
		invert = nbt.getBoolean("invert") || nbt.getBoolean("invertDistance");
		taxiCab = nbt.getBoolean("taxiCabDist");
	}
	
	@Override
	public NBTTagCompound writeProgressToNBT(NBTTagCompound nbt, @Nullable List<UUID> users)
	{
		NBTTagList jArray = new NBTTagList();
		
		completeUsers.forEach((uuid) -> {
		    if(users == null || users.contains(uuid)) jArray.appendTag(new NBTTagString(uuid.toString()));
		});
		
		nbt.setTag("completeUsers", jArray);
		
		return nbt;
	}
 
	@Override
	public void readProgressFromNBT(NBTTagCompound nbt, boolean merge)
	{
		if(!merge) completeUsers.clear();
		NBTTagList cList = nbt.getTagList("completeUsers", 8);
		for(int i = 0; i < cList.tagCount(); i++)
		{
			try
			{
				completeUsers.add(UUID.fromString(cList.getStringTagAt(i)));
			} catch(Exception e)
			{
                BetterQuesting.logger.log(Level.ERROR, "Unable to load UUID for task", e);
			}
		}
	}
 
	@Override
	public IGuiPanel getTaskGui(IGuiRect rect, DBEntry<IQuest> quest)
	{
	    return new PanelTaskLocation(rect, this);
	}
 
	@Override
	public GuiScreen getTaskEditor(GuiScreen parent, DBEntry<IQuest> quest)
	{
		return null;
	}
}

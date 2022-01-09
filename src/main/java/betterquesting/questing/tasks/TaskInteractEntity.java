package betterquesting.questing.tasks;

import betterquesting.api.questing.IQuest;
import betterquesting.api.questing.tasks.ITask;
import betterquesting.api.utils.BigItemStack;
import betterquesting.api.utils.ItemComparison;
import betterquesting.api2.client.gui.misc.IGuiRect;
import betterquesting.api2.client.gui.panels.IGuiPanel;
import betterquesting.api2.storage.DBEntry;
import betterquesting.api2.utils.ParticipantInfo;
import betterquesting.core.BetterQuesting;
import betterquesting.client.gui2.tasks.PanelTaskInteractEntity;
import betterquesting.questing.tasks.factory.FactoryTaskInteractEntity;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public class TaskInteractEntity implements ITask
{
	private final Set<UUID> completeUsers = new TreeSet<>();
	private final TreeMap<UUID, Integer> userProgress = new TreeMap<>();
	
    public BigItemStack targetItem = new BigItemStack(Items.AIR);
	public boolean ignoreItemNBT = false;
	public boolean partialItemMatch = true;
	public boolean useMainHand = true;
	public boolean useOffHand = true;
    
    public String entityID = "minecraft:villager";
    public NBTTagCompound entityTags = new NBTTagCompound();
    public boolean entitySubtypes = true;
	public boolean ignoreEntityNBT = true;
	
	public boolean onInteract = true;
	public boolean onHit = false;
	public int required = 1;
    
    @Override
    public String getUnlocalisedName()
    {
        return BetterQuesting.MODID_STD + ".task.interact_entity";
    }
    
    @Override
    public ResourceLocation getFactoryID()
    {
        return FactoryTaskInteractEntity.INSTANCE.getRegistryName();
    }
    
    public void onInteract(ParticipantInfo pInfo, DBEntry<IQuest> quest, EnumHand hand, ItemStack item, Entity entity, boolean isHit)
    {
        if((!onHit && isHit) || (!onInteract && !isHit)) return;
        if((!useMainHand && hand == EnumHand.MAIN_HAND) || (!useOffHand && hand == EnumHand.OFF_HAND)) return;
        
        ResourceLocation targetRes = new ResourceLocation(entityID);
        Class<? extends Entity> targetClass = EntityList.getClass(targetRes);
        if(targetClass == null) return; // No idea what we're looking for
        
        Class<? extends Entity> subjectClass = entity.getClass();
        ResourceLocation subjectRes = EntityList.getKey(entity);
        if(subjectRes == null) return; // This isn't a registered entity!
        
        if(entitySubtypes ? !targetClass.isAssignableFrom(subjectClass) : !subjectRes.equals(targetRes)) return;
        
        if(!ignoreEntityNBT)
        {
            NBTTagCompound subjectTags = new NBTTagCompound();
            entity.writeToNBTOptional(subjectTags);
            if(!ItemComparison.CompareNBTTag(entityTags, subjectTags, true)) return;
        }
        
        if(targetItem.getBaseStack().getItem() != Items.AIR)
        {
            if(targetItem.hasOreDict() && !ItemComparison.OreDictionaryMatch(targetItem.getOreIngredient(), targetItem.GetTagCompound(), item, !ignoreItemNBT, partialItemMatch))
            {
                return;
            } else if(!ItemComparison.StackMatch(targetItem.getBaseStack(), item, !ignoreItemNBT, partialItemMatch))
            {
                return;
            }
        }
		
        final List<Tuple<UUID, Integer>> progress = getBulkProgress(pInfo.ALL_UUIDS);
        
        progress.forEach((value) -> {
            if(isComplete(value.getFirst())) return;
            int np = Math.min(required, value.getSecond() + 1);
            setUserProgress(value.getFirst(), np);
            if(np >= required) setComplete(value.getFirst());
        });
        
		pInfo.markDirtyParty(Collections.singletonList(quest.getID()));
    }
    
    @Override
    public void detect(ParticipantInfo pInfo, DBEntry<IQuest> quest)
    {
        final List<Tuple<UUID, Integer>> progress = getBulkProgress(pInfo.ALL_UUIDS);
        
        progress.forEach((value) -> {
            if(value.getSecond() >= required) setComplete(value.getFirst());
        });
        
		pInfo.markDirtyParty(Collections.singletonList(quest.getID()));
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
            userProgress.clear();
        } else
        {
            completeUsers.remove(uuid);
            userProgress.remove(uuid);
        }
	}
    
    @Override
	@SideOnly(Side.CLIENT)
    public IGuiPanel getTaskGui(IGuiRect rect, DBEntry<IQuest> quest)
    {
        return new PanelTaskInteractEntity(rect, this);
    }
    
    @Override
    @Nullable
	@SideOnly(Side.CLIENT)
    public GuiScreen getTaskEditor(GuiScreen parent, DBEntry<IQuest> quest)
    {
        return null;
    }
	
	@Override
	public void readProgressFromNBT(NBTTagCompound nbt, boolean merge)
	{
		if(!merge)
        {
            completeUsers.clear();
            userProgress.clear();
        }
		
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
		
		NBTTagList pList = nbt.getTagList("userProgress", 10);
		for(int n = 0; n < pList.tagCount(); n++)
		{
			try
			{
                NBTTagCompound pTag = pList.getCompoundTagAt(n);
                UUID uuid = UUID.fromString(pTag.getString("uuid"));
                userProgress.put(uuid, pTag.getInteger("value"));
			} catch(Exception e)
			{
                BetterQuesting.logger.log(Level.ERROR, "Unable to load user progress for task", e);
			}
		}
	}
	
	@Override
	public NBTTagCompound writeProgressToNBT(NBTTagCompound nbt, @Nullable List<UUID> users)
	{
		NBTTagList jArray = new NBTTagList();
		NBTTagList progArray = new NBTTagList();
		
		if(users != null)
        {
            users.forEach((uuid) -> {
                if(completeUsers.contains(uuid)) jArray.appendTag(new NBTTagString(uuid.toString()));
                
                Integer data = userProgress.get(uuid);
                if(data != null)
                {
                    NBTTagCompound pJson = new NBTTagCompound();
                    pJson.setString("uuid", uuid.toString());
                    pJson.setInteger("value", data);
                    progArray.appendTag(pJson);
                }
            });
        } else
        {
            completeUsers.forEach((uuid) -> jArray.appendTag(new NBTTagString(uuid.toString())));
            
            userProgress.forEach((uuid, data) -> {
                NBTTagCompound pJson = new NBTTagCompound();
			    pJson.setString("uuid", uuid.toString());
                pJson.setInteger("value", data);
                progArray.appendTag(pJson);
            });
        }
		
		nbt.setTag("completeUsers", jArray);
		nbt.setTag("userProgress", progArray);
		
		return nbt;
	}
    
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt)
    {
        nbt.setTag("item", targetItem.writeToNBT(new NBTTagCompound()));
        nbt.setBoolean("ignoreItemNBT", ignoreItemNBT);
        nbt.setBoolean("partialItemMatch", partialItemMatch);
        
        nbt.setString("targetID", entityID);
        nbt.setTag("targetNBT", entityTags);
        nbt.setBoolean("ignoreTargetNBT", ignoreEntityNBT);
        nbt.setBoolean("targetSubtypes", entitySubtypes);
        
        nbt.setBoolean("allowMainHand", useMainHand);
        nbt.setBoolean("allowOffHand", useOffHand);
        nbt.setInteger("requiredUses", required);
        nbt.setBoolean("onInteract", onInteract);
        nbt.setBoolean("onHit", onHit);
        return nbt;
    }
    
    @Override
    public void readFromNBT(NBTTagCompound nbt)
    {
        targetItem = new BigItemStack(nbt.getCompoundTag("item"));
        ignoreItemNBT = nbt.getBoolean("ignoreItemNBT");
        partialItemMatch = nbt.getBoolean("partialItemMatch");
        
        entityID = nbt.getString("targetID");
        entityTags = nbt.getCompoundTag("targetNBT");
        ignoreEntityNBT = nbt.getBoolean("ignoreTargetNBT");
        entitySubtypes = nbt.getBoolean("targetSubtypes");
        
        useMainHand = nbt.getBoolean("allowMainHand");
        useOffHand = nbt.getBoolean("allowOffHand");
        required = nbt.getInteger("requiredUses");
        onInteract = nbt.getBoolean("onInteract");
        onHit = nbt.getBoolean("onHit");
    }
	
	private void setUserProgress(UUID uuid, int progress)
	{
		userProgress.put(uuid, progress);
	}
	
	public int getUsersProgress(UUID uuid)
	{
        Integer n = userProgress.get(uuid);
        return n == null? 0 : n;
	}
	
	private List<Tuple<UUID, Integer>> getBulkProgress(@Nonnull List<UUID> uuids)
    {
        if(uuids.size() <= 0) return Collections.emptyList();
        List<Tuple<UUID, Integer>> list = new ArrayList<>();
        uuids.forEach((key) -> list.add(new Tuple<>(key, getUsersProgress(key))));
        return list;
    }
}

package betterquesting.quests.tasks.advanced;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.UUID;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHealEvent;
import net.minecraftforge.event.entity.player.EntityInteractEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerPickupXpEvent;
import net.minecraftforge.event.entity.player.PlayerUseItemEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.NoteBlockEvent;
import betterquesting.quests.QuestDatabase;
import betterquesting.quests.QuestInstance;
import betterquesting.quests.tasks.TaskBase;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent.ItemPickupEvent;

/**
 * In charge of sending event data to all AdvancedTasks
 */
public class AdvancedEventHandler
{
	@SubscribeEvent
	public void onLivingAttacked(LivingAttackEvent event)
	{
		if(event.entity.worldObj.isRemote)
		{
			return;
		}
		
		if(event.entityLiving instanceof EntityPlayer)
		{
			EntityPlayer player = (EntityPlayer)event.entityLiving;
			
			for(Entry<AdvancedTaskBase,QuestInstance> set : GetAdvancedTasks(player.getUniqueID()).entrySet())
			{
				set.getKey().onPlayerAttacked(set.getValue(), player, event.source, event.ammount);
			}
		} else if(event.source.getEntity() instanceof EntityPlayer)
		{
			EntityPlayer player = (EntityPlayer)event.source.getEntity();
			
			for(Entry<AdvancedTaskBase,QuestInstance> set : GetAdvancedTasks(player.getUniqueID()).entrySet())
			{
				set.getKey().onAttackedByPlayer(set.getValue(), event.entityLiving, event.source, event.ammount);
			}
		}
	}
	
	@SubscribeEvent
	public void onLivingDeath(LivingDeathEvent event)
	{
		if(event.entity.worldObj.isRemote)
		{
			return;
		}
		
		if(event.entityLiving instanceof EntityPlayer)
		{
			EntityPlayer player = (EntityPlayer)event.entityLiving;
			
			for(Entry<AdvancedTaskBase,QuestInstance> set : GetAdvancedTasks(player.getUniqueID()).entrySet())
			{
				set.getKey().onPlayerKilled(set.getValue(), player, event.source);
			}
		} else if(event.source.getEntity() instanceof EntityPlayer)
		{
			EntityPlayer player = (EntityPlayer)event.source.getEntity();
			
			for(Entry<AdvancedTaskBase,QuestInstance> set : GetAdvancedTasks(player.getUniqueID()).entrySet())
			{
				set.getKey().onKilledByPlayer(set.getValue(), event.entityLiving, event.source);
			}
		}
	}
	
	@SubscribeEvent
	public void onLivingHeal(LivingHealEvent event)
	{
		if(!(event.entityLiving instanceof EntityPlayer) || event.entity.worldObj.isRemote)
		{
			return;
		}
		
		EntityPlayer player = (EntityPlayer)event.entityLiving;
		
		for(Entry<AdvancedTaskBase,QuestInstance> set : GetAdvancedTasks(player.getUniqueID()).entrySet())
		{
			set.getKey().onPlayerHeal(set.getValue(), player, event.amount);
		}
	}
	
	@SubscribeEvent
	public void onPickupXP(PlayerPickupXpEvent event)
	{
		if(event.entity.worldObj.isRemote)
		{
			return;
		}
		
		EntityPlayer player = (EntityPlayer)event.entityLiving;
		
		for(Entry<AdvancedTaskBase,QuestInstance> set : GetAdvancedTasks(player.getUniqueID()).entrySet())
		{
			set.getKey().onXpPickup(set.getValue(), player, event.orb.xpValue);
		}
	}
	
	@SubscribeEvent
	public void onChunkEnter(EntityEvent.EnteringChunk event)
	{
		if(!(event.entity instanceof EntityPlayer) || event.entity.worldObj.isRemote)
		{
			return;
		}
		
		EntityPlayer player = (EntityPlayer)event.entity;
		
		for(Entry<AdvancedTaskBase,QuestInstance> set : GetAdvancedTasks(player.getUniqueID()).entrySet())
		{
			set.getKey().onEnterChunk(set.getValue(), player, event.newChunkX, event.newChunkZ);
		}
	}
	
	@SubscribeEvent
	public void onItemCrafted(PlayerEvent.ItemCraftedEvent event)
	{
		if(event.player.worldObj.isRemote || event.player == null)
		{
			return;
		}
		
		EntityPlayer player = event.player;
		
		// Because this is horribly broken with shift-clicking for some stupid reason we have to perform this hack job of code to get the true stack size
		ItemStack actStack = event.crafting.copy();
		
		if(event.craftMatrix instanceof InventoryCrafting)
		{
			actStack = CraftingManager.getInstance().findMatchingRecipe((InventoryCrafting)event.craftMatrix, player.worldObj);
		}
		
		for(Entry<AdvancedTaskBase,QuestInstance> set : GetAdvancedTasks(player.getUniqueID()).entrySet())
		{
			set.getKey().onItemCrafted(set.getValue(), player, actStack);
		}
	}
	
	@SubscribeEvent
	public void onItemDropped(ItemTossEvent event)
	{
		if(event.player.worldObj.isRemote)
		{
			return;
		}
		
		EntityPlayer player = event.player;
		
		for(Entry<AdvancedTaskBase,QuestInstance> set : GetAdvancedTasks(player.getUniqueID()).entrySet())
		{
			set.getKey().onItemDropped(set.getValue(), player, event.entityItem);
		}
	}
	
	@SubscribeEvent
	public void onItemPickup(ItemPickupEvent event)
	{
		if(event.player.worldObj.isRemote)
		{
			return;
		}
		
		EntityPlayer player = event.player;
		
		for(Entry<AdvancedTaskBase,QuestInstance> set : GetAdvancedTasks(player.getUniqueID()).entrySet())
		{
			set.getKey().onItemPickup(set.getValue(), player, event.pickedUp);
		}
	}
	
	@SubscribeEvent
	public void onItemUseStart(PlayerUseItemEvent.Start event)
	{
		if(event.entity.worldObj.isRemote)
		{
			return;
		}
		
		EntityPlayer player = event.entityPlayer;
		
		for(Entry<AdvancedTaskBase,QuestInstance> set : GetAdvancedTasks(player.getUniqueID()).entrySet())
		{
			set.getKey().onItemUseStart(set.getValue(), player, event.item, event.duration);
		}
	}
	
	@SubscribeEvent
	public void onItemUseEnd(PlayerUseItemEvent.Finish event)
	{
		if(event.entity.worldObj.isRemote)
		{
			return;
		}
		
		EntityPlayer player = event.entityPlayer;
		
		for(Entry<AdvancedTaskBase,QuestInstance> set : GetAdvancedTasks(player.getUniqueID()).entrySet())
		{
			set.getKey().onItemUseEnd(set.getValue(), player, event.item, event.duration);
		}
	}
	
	@SubscribeEvent
	public void onItemSmelted(PlayerEvent.ItemSmeltedEvent event)
	{
		if(event.player.worldObj.isRemote)
		{
			return;
		}
		
		EntityPlayer player = event.player;
		
		for(Entry<AdvancedTaskBase,QuestInstance> set : GetAdvancedTasks(player.getUniqueID()).entrySet())
		{
			set.getKey().onItemSmelted(set.getValue(), player, event.smelting);
		}
	}
	
	@SubscribeEvent
	public void onBlockBreak(BlockEvent.BreakEvent event)
	{
		if(event.world.isRemote)
		{
			return;
		}
		
		EntityPlayer player = event.getPlayer();
		
		for(Entry<AdvancedTaskBase,QuestInstance> set : GetAdvancedTasks(player.getUniqueID()).entrySet())
		{
			set.getKey().onBlockBreak(set.getValue(), player, event.block, event.blockMetadata, event.x, event.y, event.z);
		}
	}
	
	@SubscribeEvent
	public void onBlockPlace(BlockEvent.PlaceEvent event)
	{
		if(event.world.isRemote)
		{
			return;
		}
		
		EntityPlayer player = event.player;
		
		for(Entry<AdvancedTaskBase,QuestInstance> set : GetAdvancedTasks(player.getUniqueID()).entrySet())
		{
			set.getKey().onBlockPlace(set.getValue(), player, event.block, event.blockMetadata, event.x, event.y, event.z);
		}
	}
	
	@SubscribeEvent
	public void onBlockInteract(PlayerInteractEvent event)
	{
		if(event.world.isRemote)
		{
			return;
		}
		
		EntityPlayer player = event.entityPlayer;
		
		if(event.action == PlayerInteractEvent.Action.LEFT_CLICK_BLOCK)
		{
			for(Entry<AdvancedTaskBase,QuestInstance> set : GetAdvancedTasks(player.getUniqueID()).entrySet())
			{
				set.getKey().onBlockInteract(set.getValue(), player, event.world.getBlock(event.x, event.y, event.z), event.world.getBlockMetadata(event.x, event.y, event.z), event.x, event.y, event.z, true);
			}
		} else if(event.action == PlayerInteractEvent.Action.RIGHT_CLICK_AIR)
		{
			for(Entry<AdvancedTaskBase,QuestInstance> set : GetAdvancedTasks(player.getUniqueID()).entrySet())
			{
				set.getKey().onBlockInteract(set.getValue(), player, event.world.getBlock(event.x, event.y, event.z), event.world.getBlockMetadata(event.x, event.y, event.z), event.x, event.y, event.z, false);
			}
		}
	}
	
	@SubscribeEvent
	public void onEntityInteract(EntityInteractEvent event)
	{
		if(event.entity.worldObj.isRemote && event.target != null)
		{
			return;
		}
		
		EntityPlayer player = event.entityPlayer;
		
		for(Entry<AdvancedTaskBase,QuestInstance> set : GetAdvancedTasks(player.getUniqueID()).entrySet())
		{
			set.getKey().onEntityInteract(set.getValue(), player, event.target);
		}
	}
	
	@SubscribeEvent
	public void onNotePlayed(NoteBlockEvent.Play event) // Notifies everyone in a 64 block range about a played note
	{
		if(event.world.isRemote)
		{
			return;
		}
		
		@SuppressWarnings("unchecked")
		ArrayList<EntityPlayer> pList = (ArrayList<EntityPlayer>)event.world.getEntitiesWithinAABB(EntityPlayer.class, AxisAlignedBB.getBoundingBox(event.x, event.y, event.z, event.x + 1D, event.y + 1D, event.z + 1D).expand(64D, 64D, 64D));
		for(EntityPlayer player : pList)
		{
			for(Entry<AdvancedTaskBase,QuestInstance> set : GetAdvancedTasks(player.getUniqueID()).entrySet())
			{
				set.getKey().onNotePlayed(set.getValue(), event.world, event.x, event.y, event.z, event.instrument, event.getNote(), event.getOctave());
			}
		}
	}
	
	HashMap<AdvancedTaskBase, QuestInstance> GetAdvancedTasks(UUID uuid)
	{
		HashMap<AdvancedTaskBase, QuestInstance> map = new HashMap<AdvancedTaskBase, QuestInstance>();
		
		for(QuestInstance quest : QuestDatabase.getActiveQuests(uuid))
		{
			for(TaskBase task : quest.tasks)
			{
				if(task instanceof AdvancedTaskBase && !task.isComplete(uuid))
				{
					map.put((AdvancedTaskBase)task, quest);
				}
			}
		}
		
		return map;
	}
}

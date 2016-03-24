package betterquesting.quests.tasks.advanced;

import java.util.ArrayList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.living.LivingHealEvent;
import net.minecraftforge.event.entity.player.EntityInteractEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerPickupXpEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.NoteBlockEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.ItemPickupEvent;
import betterquesting.quests.QuestDatabase;
import betterquesting.quests.tasks.TaskBase;

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
			
			for(TaskBase task : QuestDatabase.getActiveTasks(player.getUniqueID()))
			{
				if(!(task instanceof AdvancedTaskBase))
				{
					continue;
				}
				
				((AdvancedTaskBase)task).onPlayerAttacked(player, event.source, event.ammount);
			}
		} else if(event.source.getEntity() instanceof EntityPlayer)
		{
			EntityPlayer player = (EntityPlayer)event.source.getEntity();
			
			for(TaskBase task : QuestDatabase.getActiveTasks(player.getUniqueID()))
			{
				if(!(task instanceof AdvancedTaskBase))
				{
					continue;
				}
				
				((AdvancedTaskBase)task).onAttackedByPlayer(event.entityLiving, event.source, event.ammount);
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
			
			for(TaskBase task : QuestDatabase.getActiveTasks(player.getUniqueID()))
			{
				if(!(task instanceof AdvancedTaskBase))
				{
					continue;
				}
				
				((AdvancedTaskBase)task).onPlayerKilled(player, event.source);
			}
		} else if(event.source.getEntity() instanceof EntityPlayer)
		{
			EntityPlayer player = (EntityPlayer)event.source.getEntity();
			
			for(TaskBase task : QuestDatabase.getActiveTasks(player.getUniqueID()))
			{
				if(!(task instanceof AdvancedTaskBase))
				{
					continue;
				}
				
				((AdvancedTaskBase)task).onKilledByPlayer(event.entityLiving, event.source);
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
		
		for(TaskBase task : QuestDatabase.getActiveTasks(player.getUniqueID()))
		{
			if(!(task instanceof AdvancedTaskBase))
			{
				continue;
			}
			
			((AdvancedTaskBase)task).onPlayerHeal(player, event.amount);
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
		
		for(TaskBase task : QuestDatabase.getActiveTasks(player.getUniqueID()))
		{
			if(!(task instanceof AdvancedTaskBase))
			{
				continue;
			}
			
			((AdvancedTaskBase)task).onXpPickup(player, event.orb.xpValue);
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
		
		for(TaskBase task : QuestDatabase.getActiveTasks(player.getUniqueID()))
		{
			if(!(task instanceof AdvancedTaskBase))
			{
				continue;
			}
			
			((AdvancedTaskBase)task).onEnterChunk(player, event.newChunkX, event.newChunkZ);
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
		
		for(TaskBase task : QuestDatabase.getActiveTasks(player.getUniqueID()))
		{
			if(!(task instanceof AdvancedTaskBase))
			{
				continue;
			}
			
			((AdvancedTaskBase)task).onItemCrafted(player, actStack);
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
		
		for(TaskBase task : QuestDatabase.getActiveTasks(player.getUniqueID()))
		{
			if(!(task instanceof AdvancedTaskBase))
			{
				continue;
			}
			
			((AdvancedTaskBase)task).onItemDropped(player, event.entityItem);
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
		
		for(TaskBase task : QuestDatabase.getActiveTasks(player.getUniqueID()))
		{
			if(!(task instanceof AdvancedTaskBase))
			{
				continue;
			}
			
			((AdvancedTaskBase)task).onItemPickup(player, event.pickedUp);
		}
	}
	
	@SubscribeEvent
	public void onItemUseStart(LivingEntityUseItemEvent.Start event)
	{
		if(event.entity.worldObj.isRemote && event.entityLiving instanceof EntityPlayer)
		{
			return;
		}
		
		EntityPlayer player = (EntityPlayer)event.entityLiving;
		
		for(TaskBase task : QuestDatabase.getActiveTasks(player.getUniqueID()))
		{
			if(!(task instanceof AdvancedTaskBase))
			{
				continue;
			}
			
			((AdvancedTaskBase)task).onItemUseStart(player, event.item, event.duration);
		}
	}
	
	@SubscribeEvent
	public void onItemUseEnd(LivingEntityUseItemEvent.Finish event)
	{
		if(event.entity.worldObj.isRemote && event.entityLiving instanceof EntityPlayer)
		{
			return;
		}
		
		EntityPlayer player = (EntityPlayer)event.entityLiving;
		
		for(TaskBase task : QuestDatabase.getActiveTasks(player.getUniqueID()))
		{
			if(!(task instanceof AdvancedTaskBase))
			{
				continue;
			}
			
			((AdvancedTaskBase)task).onItemUseEnd(player, event.item, event.duration);
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
		
		for(TaskBase task : QuestDatabase.getActiveTasks(player.getUniqueID()))
		{
			if(!(task instanceof AdvancedTaskBase))
			{
				continue;
			}
			
			((AdvancedTaskBase)task).onItemSmelted(player, event.smelting);
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
		
		for(TaskBase task : QuestDatabase.getActiveTasks(player.getUniqueID()))
		{
			if(!(task instanceof AdvancedTaskBase))
			{
				continue;
			}
			
			((AdvancedTaskBase)task).onBlockBreak(player, event.state, event.pos);
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
		
		for(TaskBase task : QuestDatabase.getActiveTasks(player.getUniqueID()))
		{
			if(!(task instanceof AdvancedTaskBase))
			{
				continue;
			}
			
			((AdvancedTaskBase)task).onBlockPlace(player, event.state, event.pos);
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
			for(TaskBase task : QuestDatabase.getActiveTasks(player.getUniqueID()))
			{
				if(!(task instanceof AdvancedTaskBase))
				{
					continue;
				}
				
				((AdvancedTaskBase)task).onBlockInteract(player, event.world.getBlockState(event.pos), event.pos, true);
			}
		} else if(event.action == PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK || event.action == PlayerInteractEvent.Action.RIGHT_CLICK_AIR)
		{
			for(TaskBase task : QuestDatabase.getActiveTasks(player.getUniqueID()))
			{
				if(!(task instanceof AdvancedTaskBase))
				{
					continue;
				}
				
				((AdvancedTaskBase)task).onBlockInteract(player, event.world.getBlockState(event.pos), event.pos, false);
			}
		}
	}
	
	@SubscribeEvent
	public void onEntityInteract(EntityInteractEvent event)
	{
		if(event.entity.worldObj.isRemote)
		{
			return;
		}
		
		EntityPlayer player = event.entityPlayer;
		
		for(TaskBase task : QuestDatabase.getActiveTasks(player.getUniqueID()))
		{
			if(!(task instanceof AdvancedTaskBase))
			{
				continue;
			}
			
			((AdvancedTaskBase)task).onEntityInteract(player, event.entityLiving);
		}
	}
	
	@SubscribeEvent
	public void onNotePlayed(NoteBlockEvent.Play event) // Notifies everyone in a 64 block range about a played note
	{
		if(event.world.isRemote)
		{
			return;
		}
		
		ArrayList<EntityPlayer> pList = (ArrayList<EntityPlayer>)event.world.getEntitiesWithinAABB(EntityPlayer.class, new AxisAlignedBB(event.pos.getX(), event.pos.getY(), event.pos.getZ(), event.pos.getX() + 1D, event.pos.getY() + 1D, event.pos.getZ() + 1D).expand(64D, 64D, 64D));
		for(EntityPlayer player : pList)
		{
			for(TaskBase task : QuestDatabase.getActiveTasks(player.getUniqueID()))
			{
				if(!(task instanceof AdvancedTaskBase))
				{
					continue;
				}
				
				((AdvancedTaskBase)task).onNotePlayed(event.world, event.pos, event.instrument, event.getNote(), event.getOctave());
			}
		}
	}
}

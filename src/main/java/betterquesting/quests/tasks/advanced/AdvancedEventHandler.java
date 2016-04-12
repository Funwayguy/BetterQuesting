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
import net.minecraftforge.event.entity.player.PlayerInteractEvent.EntityInteract;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.LeftClickBlock;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
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
		if(event.getEntityLiving().worldObj.isRemote)
		{
			return;
		}
		
		if(event.getEntityLiving() instanceof EntityPlayer)
		{
			EntityPlayer player = (EntityPlayer)event.getEntityLiving();
			
			for(TaskBase task : QuestDatabase.getActiveTasks(player.getUniqueID()))
			{
				if(!(task instanceof AdvancedTaskBase))
				{
					continue;
				}
				
				((AdvancedTaskBase)task).onPlayerAttacked(player, event.getSource(), event.getAmount());
			}
		} else if(event.getSource().getEntity() instanceof EntityPlayer)
		{
			EntityPlayer player = (EntityPlayer)event.getSource().getEntity();
			
			for(TaskBase task : QuestDatabase.getActiveTasks(player.getUniqueID()))
			{
				if(!(task instanceof AdvancedTaskBase))
				{
					continue;
				}
				
				((AdvancedTaskBase)task).onAttackedByPlayer(event.getEntityLiving(), event.getSource(), event.getAmount());
			}
		}
	}
	
	@SubscribeEvent
	public void onLivingDeath(LivingDeathEvent event)
	{
		if(event.getEntityLiving().worldObj.isRemote)
		{
			return;
		}
		
		if(event.getEntityLiving() instanceof EntityPlayer)
		{
			EntityPlayer player = (EntityPlayer)event.getEntityLiving();
			
			for(TaskBase task : QuestDatabase.getActiveTasks(player.getUniqueID()))
			{
				if(!(task instanceof AdvancedTaskBase))
				{
					continue;
				}
				
				((AdvancedTaskBase)task).onPlayerKilled(player, event.getSource());
			}
		} else if(event.getSource().getEntity() instanceof EntityPlayer)
		{
			EntityPlayer player = (EntityPlayer)event.getSource().getEntity();
			
			for(TaskBase task : QuestDatabase.getActiveTasks(player.getUniqueID()))
			{
				if(!(task instanceof AdvancedTaskBase))
				{
					continue;
				}
				
				((AdvancedTaskBase)task).onKilledByPlayer(event.getEntityLiving(), event.getSource());
			}
		}
	}
	
	@SubscribeEvent
	public void onLivingHeal(LivingHealEvent event)
	{
		if(!(event.getEntityLiving() instanceof EntityPlayer) || event.getEntityLiving().worldObj.isRemote)
		{
			return;
		}
		
		EntityPlayer player = (EntityPlayer)event.getEntityLiving();
		
		for(TaskBase task : QuestDatabase.getActiveTasks(player.getUniqueID()))
		{
			if(!(task instanceof AdvancedTaskBase))
			{
				continue;
			}
			
			((AdvancedTaskBase)task).onPlayerHeal(player, event.getAmount());
		}
	}
	
	@SubscribeEvent
	public void onPickupXP(PlayerPickupXpEvent event)
	{
		if(event.getEntityLiving().worldObj.isRemote)
		{
			return;
		}
		
		EntityPlayer player = (EntityPlayer)event.getEntityLiving();
		
		for(TaskBase task : QuestDatabase.getActiveTasks(player.getUniqueID()))
		{
			if(!(task instanceof AdvancedTaskBase))
			{
				continue;
			}
			
			((AdvancedTaskBase)task).onXpPickup(player, event.getOrb().xpValue);
		}
	}
	
	@SubscribeEvent
	public void onChunkEnter(EntityEvent.EnteringChunk event)
	{
		if(!(event.getEntity() instanceof EntityPlayer) || event.getEntity().worldObj.isRemote)
		{
			return;
		}
		
		EntityPlayer player = (EntityPlayer)event.getEntity();
		
		for(TaskBase task : QuestDatabase.getActiveTasks(player.getUniqueID()))
		{
			if(!(task instanceof AdvancedTaskBase))
			{
				continue;
			}
			
			((AdvancedTaskBase)task).onEnterChunk(player, event.getNewChunkX(), event.getNewChunkZ());
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
		if(event.getPlayer().worldObj.isRemote)
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
			
			((AdvancedTaskBase)task).onItemDropped(player, event.getEntityItem());
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
		if(event.getEntityLiving().worldObj.isRemote || !(event.getEntityLiving() instanceof EntityPlayer))
		{
			return;
		}
		
		EntityPlayer player = (EntityPlayer)event.getEntityLiving();
		
		for(TaskBase task : QuestDatabase.getActiveTasks(player.getUniqueID()))
		{
			if(!(task instanceof AdvancedTaskBase))
			{
				continue;
			}
			
			((AdvancedTaskBase)task).onItemUseStart(player, event.getItem(), event.getDuration());
		}
	}
	
	@SubscribeEvent
	public void onItemUseEnd(LivingEntityUseItemEvent.Finish event)
	{
		if(event.getEntityLiving().worldObj.isRemote || !(event.getEntityLiving() instanceof EntityPlayer))
		{
			return;
		}
		
		EntityPlayer player = (EntityPlayer)event.getEntityLiving();
		
		for(TaskBase task : QuestDatabase.getActiveTasks(player.getUniqueID()))
		{
			if(!(task instanceof AdvancedTaskBase))
			{
				continue;
			}
			
			((AdvancedTaskBase)task).onItemUseEnd(player, event.getItem(), event.getDuration());
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
		if(event.getWorld().isRemote)
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
			
			((AdvancedTaskBase)task).onBlockBreak(player, event.getState(), event.getPos());
		}
	}
	
	@SubscribeEvent
	public void onBlockPlace(BlockEvent.PlaceEvent event)
	{
		if(event.getWorld().isRemote)
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
			
			((AdvancedTaskBase)task).onBlockPlace(player, event.getState(), event.getPos());
		}
	}
	
	@SubscribeEvent
	public void onBlockPunch(LeftClickBlock event)
	{
		if(event.getWorld().isRemote)
		{
			return;
		}
		
		EntityPlayer player = event.getEntityPlayer();
		
		for(TaskBase task : QuestDatabase.getActiveTasks(player.getUniqueID()))
		{
			if(!(task instanceof AdvancedTaskBase))
			{
				continue;
			}
			
			((AdvancedTaskBase)task).onBlockInteract(player, event.getWorld().getBlockState(event.getPos()), event.getPos(), event.getHand(), true);
		}
	}
	
	@SubscribeEvent
	public void onBlockInteract(RightClickBlock event)
	{
		if(event.getWorld().isRemote)
		{
			return;
		}
		
		EntityPlayer player = event.getEntityPlayer();
		
		for(TaskBase task : QuestDatabase.getActiveTasks(player.getUniqueID()))
		{
			if(!(task instanceof AdvancedTaskBase))
			{
				continue;
			}
			
			((AdvancedTaskBase)task).onBlockInteract(player, event.getWorld().getBlockState(event.getPos()), event.getPos(), event.getHand(), false);
		}
	}
	
	@SubscribeEvent
	public void onEntityInteract(EntityInteract event)
	{
		if(event.getEntityPlayer().worldObj.isRemote && event.getTarget() != null)
		{
			return;
		}
		
		EntityPlayer player = event.getEntityPlayer();
		
		for(TaskBase task : QuestDatabase.getActiveTasks(player.getUniqueID()))
		{
			if(!(task instanceof AdvancedTaskBase))
			{
				continue;
			}
			
			((AdvancedTaskBase)task).onEntityInteract(player, event.getTarget(), event.getHand());
		}
	}
	
	@SubscribeEvent
	public void onNotePlayed(NoteBlockEvent.Play event) // Notifies everyone in a 64 block range about a played note
	{
		if(event.getWorld().isRemote)
		{
			return;
		}
		
		ArrayList<EntityPlayer> pList = (ArrayList<EntityPlayer>)event.getWorld().getEntitiesWithinAABB(EntityPlayer.class, new AxisAlignedBB(event.getPos().getX(), event.getPos().getY(), event.getPos().getZ(), event.getPos().getX() + 1D, event.getPos().getY() + 1D, event.getPos().getZ() + 1D).expand(64D, 64D, 64D));
		for(EntityPlayer player : pList)
		{
			for(TaskBase task : QuestDatabase.getActiveTasks(player.getUniqueID()))
			{
				if(!(task instanceof AdvancedTaskBase))
				{
					continue;
				}
				
				((AdvancedTaskBase)task).onNotePlayed(event.getWorld(), event.getPos(), event.getInstrument(), event.getNote(), event.getOctave());
			}
		}
	}
}

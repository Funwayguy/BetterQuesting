package betterquesting.quests.tasks.advanced;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import java.util.Map.Entry;
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
import betterquesting.quests.QuestInstance;
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
			
			for(Entry<AdvancedTaskBase,QuestInstance> set : GetAdvancedTasks(player.getUniqueID()).entrySet())
			{
				set.getKey().onPlayerAttacked(set.getValue(), player, event.getSource(), event.getAmount());
			}
		} else if(event.getSource().getEntity() instanceof EntityPlayer)
		{
			EntityPlayer player = (EntityPlayer)event.getSource().getEntity();
			
			for(Entry<AdvancedTaskBase,QuestInstance> set : GetAdvancedTasks(player.getUniqueID()).entrySet())
			{
				set.getKey().onAttackedByPlayer(set.getValue(), event.getEntityLiving(), event.getSource(), event.getAmount());
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
			
			for(Entry<AdvancedTaskBase,QuestInstance> set : GetAdvancedTasks(player.getUniqueID()).entrySet())
			{
				set.getKey().onPlayerKilled(set.getValue(), player, event.getSource());
			}
		} else if(event.getSource().getEntity() instanceof EntityPlayer)
		{
			EntityPlayer player = (EntityPlayer)event.getSource().getEntity();
			
			for(Entry<AdvancedTaskBase,QuestInstance> set : GetAdvancedTasks(player.getUniqueID()).entrySet())
			{
				set.getKey().onKilledByPlayer(set.getValue(), event.getEntityLiving(), event.getSource());
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
		
		for(Entry<AdvancedTaskBase,QuestInstance> set : GetAdvancedTasks(player.getUniqueID()).entrySet())
		{
			set.getKey().onPlayerHeal(set.getValue(), player, event.getAmount());
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
		
		for(Entry<AdvancedTaskBase,QuestInstance> set : GetAdvancedTasks(player.getUniqueID()).entrySet())
		{
			set.getKey().onXpPickup(set.getValue(), player, event.getOrb().xpValue);
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
		
		for(Entry<AdvancedTaskBase,QuestInstance> set : GetAdvancedTasks(player.getUniqueID()).entrySet())
		{
			set.getKey().onEnterChunk(set.getValue(), player, event.getNewChunkX(), event.getNewChunkZ());
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
		if(event.getPlayer().worldObj.isRemote)
		{
			return;
		}
		
		EntityPlayer player = event.getPlayer();
		
		for(Entry<AdvancedTaskBase,QuestInstance> set : GetAdvancedTasks(player.getUniqueID()).entrySet())
		{
			set.getKey().onItemDropped(set.getValue(), player, event.getEntityItem());
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
	public void onItemUseStart(LivingEntityUseItemEvent.Start event)
	{
		if(event.getEntityLiving().worldObj.isRemote || !(event.getEntityLiving() instanceof EntityPlayer))
		{
			return;
		}
		
		EntityPlayer player = (EntityPlayer)event.getEntityLiving();
		
		for(Entry<AdvancedTaskBase,QuestInstance> set : GetAdvancedTasks(player.getUniqueID()).entrySet())
		{
			set.getKey().onItemUseStart(set.getValue(), player, event.getItem(), event.getDuration());
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
		
		for(Entry<AdvancedTaskBase,QuestInstance> set : GetAdvancedTasks(player.getUniqueID()).entrySet())
		{
			set.getKey().onItemUseEnd(set.getValue(), player, event.getItem(), event.getDuration());
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
		if(event.getWorld().isRemote)
		{
			return;
		}
		
		EntityPlayer player = event.getPlayer();
		
		for(Entry<AdvancedTaskBase,QuestInstance> set : GetAdvancedTasks(player.getUniqueID()).entrySet())
		{
			set.getKey().onBlockBreak(set.getValue(), player, event.getState(), event.getPos());
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
		
		for(Entry<AdvancedTaskBase,QuestInstance> set : GetAdvancedTasks(player.getUniqueID()).entrySet())
		{
			set.getKey().onBlockPlace(set.getValue(), player, event.getState(), event.getPos());
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
		
		for(Entry<AdvancedTaskBase,QuestInstance> set : GetAdvancedTasks(player.getUniqueID()).entrySet())
		{
			set.getKey().onBlockInteract(set.getValue(), player, event.getWorld().getBlockState(event.getPos()), event.getPos(), event.getHand(), true);
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
		
		for(Entry<AdvancedTaskBase,QuestInstance> set : GetAdvancedTasks(player.getUniqueID()).entrySet())
		{
			set.getKey().onBlockInteract(set.getValue(), player, event.getWorld().getBlockState(event.getPos()), event.getPos(), event.getHand(), false);
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
		
		for(Entry<AdvancedTaskBase,QuestInstance> set : GetAdvancedTasks(player.getUniqueID()).entrySet())
		{
			set.getKey().onEntityInteract(set.getValue(), player, event.getTarget(), event.getHand());
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
			for(Entry<AdvancedTaskBase,QuestInstance> set : GetAdvancedTasks(player.getUniqueID()).entrySet())
			{
				set.getKey().onNotePlayed(set.getValue(), event.getWorld(), event.getPos(), event.getInstrument(), event.getNote(), event.getOctave());
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

package betterquesting.quests.tasks.advanced;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import net.minecraftforge.event.world.NoteBlockEvent;
import betterquesting.quests.QuestInstance;
import betterquesting.quests.tasks.TaskBase;

/**
 * A base class for most event based quest tasks. Feel free to make your own class and handler if this one doesn't suit your needs
 */
public abstract class AdvancedTaskBase extends TaskBase
{
	public void onPlayerKilled(QuestInstance quest, EntityPlayer player, DamageSource source)
	{
		onPlayerKilled(player, source);
	}
	
	@Deprecated
	public void onPlayerKilled(EntityPlayer player, DamageSource source){}
	
	public void onKilledByPlayer(QuestInstance quest, EntityLivingBase entity, DamageSource source)
	{
		onKilledByPlayer(entity, source);
	}
	
	@Deprecated
	public void onKilledByPlayer(EntityLivingBase entity, DamageSource source){}
	
	public void onPlayerAttacked(QuestInstance quest, EntityPlayer player, DamageSource source, float damage)
	{
		onPlayerAttacked(player, source, damage);
	}
	
	@Deprecated
	public void onPlayerAttacked(EntityPlayer player, DamageSource source, float damage){}
	
	public void onAttackedByPlayer(QuestInstance quest, EntityLivingBase entity, DamageSource source, float damage)
	{
		onAttackedByPlayer(entity, source, damage);
	}
	
	@Deprecated
	public void onAttackedByPlayer(EntityLivingBase entity, DamageSource source, float damage){}
	
	public void onPlayerHeal(QuestInstance quest, EntityPlayer player, float heal)
	{
		onPlayerHeal(player, heal);
	}
	
	@Deprecated
	public void onPlayerHeal(EntityPlayer player, float heal){}
	
	public void onXpPickup(QuestInstance quest, EntityPlayer player, int experience)
	{
		onXpPickup(player, experience);
	}
	
	@Deprecated
	public void onXpPickup(EntityPlayer player, int experience){}
	
	public void onEnterChunk(QuestInstance quest, EntityPlayer player, int chunkX, int chunkZ)
	{
		onEnterChunk(player, chunkX, chunkZ);
	}
	
	@Deprecated
	public void onEnterChunk(EntityPlayer player, int chunkX, int chunkZ){}
	
	public void onItemCrafted(QuestInstance quest, EntityPlayer player, ItemStack stack)
	{
		onItemCrafted(player, stack);
	}
	
	@Deprecated
	public void onItemCrafted(EntityPlayer player, ItemStack stack){}
	
	public void onItemSmelted(QuestInstance quest, EntityPlayer player, ItemStack stack)
	{
		onItemSmelted(player, stack);
	}
	
	@Deprecated
	public void onItemSmelted(EntityPlayer player, ItemStack stack){}
	
	public void onItemDropped(QuestInstance quest, EntityPlayer player, EntityItem item)
	{
		onItemDropped(player, item);
	}
	
	@Deprecated
	public void onItemDropped(EntityPlayer player, EntityItem item){}
	
	public void onItemPickup(QuestInstance quest, EntityPlayer player, EntityItem item){}
	
	@Deprecated
	public void onItemPickup(EntityPlayer player, EntityItem item){}
	
	public void onItemUseStart(QuestInstance quest, EntityPlayer player, ItemStack stack, int duration)
	{
		onItemUseStart(player, stack, duration);
	}
	
	@Deprecated
	public void onItemUseStart(EntityPlayer player, ItemStack stack, int duration){}
	
	public void onItemUseEnd(QuestInstance quest, EntityPlayer player, ItemStack stack, int duration)
	{
		onItemUseEnd(player, stack, duration);
	}
	
	@Deprecated
	public void onItemUseEnd(EntityPlayer player, ItemStack stack, int duration){}
	
	public void onBlockBreak(QuestInstance quest, EntityPlayer player, Block block, int meta, int x, int y, int z)
	{
		onBlockBreak(player, block, meta, x, y, z);
	}
	
	@Deprecated
	public void onBlockBreak(EntityPlayer player, Block block, int meta, int x, int y, int z){}
	
	public void onBlockPlace(QuestInstance quest, EntityPlayer player, Block block, int meta, int x, int y, int z)
	{
		onBlockPlace(player, block, meta, x, y, z);
	}
	
	@Deprecated
	public void onBlockPlace(EntityPlayer player, Block block, int meta, int x, int y, int z){}
	
	public void onBlockInteract(QuestInstance quest, EntityPlayer player, Block block, int meta, int x, int y, int z, boolean isPunch)
	{
		onBlockInteract(player, block, meta, x, y, z, isPunch);
	}
	
	@Deprecated
	public void onBlockInteract(EntityPlayer player, Block block, int meta, int x, int y, int z, boolean isPunch){}
	
	public void onEntityInteract(QuestInstance quest, EntityPlayer player, Entity target)
	{
		onEntityInteract(player, target);
	}
	
	@Deprecated
	public void onEntityInteract(EntityPlayer player, Entity target){}
	
	public void onNotePlayed(QuestInstance quest, World world, int x, int y, int z, NoteBlockEvent.Instrument instrument, NoteBlockEvent.Note note, NoteBlockEvent.Octave octave) // Special event, combination lock?
	{
		onNotePlayed(world, x, y, z, instrument, note, octave);
	}
	
	@Deprecated
	public void onNotePlayed(World world, int x, int y, int z, NoteBlockEvent.Instrument instrument, NoteBlockEvent.Note note, NoteBlockEvent.Octave octave){} // Special event, combination lock?
}

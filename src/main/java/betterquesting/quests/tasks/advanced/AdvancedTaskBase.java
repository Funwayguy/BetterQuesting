package betterquesting.quests.tasks.advanced;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.world.NoteBlockEvent;
import betterquesting.quests.tasks.TaskBase;

/**
 * A base class for most event based quest tasks. Feel free to make your own class and handler if this one doesn't suit your needs
 */
public abstract class AdvancedTaskBase extends TaskBase
{
	public void onPlayerKilled(EntityPlayer player, DamageSource source){}
	
	public void onKilledByPlayer(EntityLivingBase entity, DamageSource source){}
	
	public void onPlayerAttacked(EntityPlayer player, DamageSource source, float damage){}
	
	public void onAttackedByPlayer(EntityLivingBase entity, DamageSource source, float damage){}
	
	public void onPlayerHeal(EntityPlayer player, float heal){}
	
	public void onXpPickup(EntityPlayer player, int experience){}
	
	public void onEnterChunk(EntityPlayer player, int chunkX, int chunkZ){}
	
	public void onItemCrafted(EntityPlayer player, ItemStack stack){}
	
	public void onItemSmelted(EntityPlayer player, ItemStack stack){}
	
	public void onItemDropped(EntityPlayer player, EntityItem item){}
	
	public void onItemPickup(EntityPlayer player, EntityItem item){}
	
	public void onItemUseStart(EntityPlayer player, ItemStack stack, int duration){}
	
	public void onItemUseEnd(EntityPlayer player, ItemStack stack, int duration){}
	
	public void onBlockBreak(EntityPlayer player, IBlockState state, BlockPos pos){}
	
	public void onBlockPlace(EntityPlayer player, IBlockState state, BlockPos pos){}
	
	public void onBlockInteract(EntityPlayer player, IBlockState state, BlockPos pos, boolean isPunch){}
	
	public void onEntityInteract(EntityPlayer player, EntityLivingBase entity){}
	
	public void onNotePlayed(World world, BlockPos pos, NoteBlockEvent.Instrument instrument, NoteBlockEvent.Note note, NoteBlockEvent.Octave octave){} // Special event, combination lock?
}

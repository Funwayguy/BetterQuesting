package betterquesting.api.placeholders;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.play.server.SSpawnObjectPacket;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

public class EntityPlaceholder extends Entity
{
	private final ItemEntity eItem;
	private CompoundNBT original = new CompoundNBT();
	
	public EntityPlaceholder(World world)
	{
		super(EntityType.ITEM, world);
		eItem = new ItemEntity(EntityType.ITEM, world);
		eItem.setItem(new ItemStack(ItemPlaceholder.placeholder));
	}
	
	public EntityPlaceholder SetOriginalTags(CompoundNBT tags)
	{
		this.original = tags;
		return this;
	}
	
	public CompoundNBT GetOriginalTags()
	{
		return this.original;
	}
	
	public ItemEntity GetItemEntity()
	{
		return eItem;
	}
	
	@Override
	protected void registerData()
	{
	}
	
	@Override
	protected void readAdditional(@Nonnull CompoundNBT tags)
	{
		original = tags.getCompound("original");
	}
	
	@Override
	protected void writeAdditional(@Nonnull CompoundNBT tags)
	{
		tags.put("original", this.original);
	}
 
	@Nonnull
    @Override
    public IPacket<?> createSpawnPacket()
    {
        return new SSpawnObjectPacket(this);
    }
}

package betterquesting.lives;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.IExtendedEntityProperties;

public class BQ_LifeTracker implements IExtendedEntityProperties
{
	public static String ID = "BQ_LIVES";
	
	public static void register(EntityPlayer player)
	{
		player.registerExtendedProperties(ID, new BQ_LifeTracker());
	}
	
	public static BQ_LifeTracker get(EntityPlayer player)
	{
		IExtendedEntityProperties prop = player.getExtendedProperties(ID);
		
		if(prop != null && prop instanceof BQ_LifeTracker)
		{
			return (BQ_LifeTracker)prop;
		} else
		{
			return null;
		}
	}
	
	// === INSTANCE ===
	
	public int lives = 1;
	
	public BQ_LifeTracker()
	{
		lives = LifeManager.defLives;
	}
	
	@Override
	public void saveNBTData(NBTTagCompound compound)
	{
		NBTTagCompound baseTag = new NBTTagCompound();
		baseTag.setInteger("lives", lives);
		compound.setTag(ID, baseTag);
	}

	@Override
	public void loadNBTData(NBTTagCompound compound)
	{
		NBTTagCompound lTag = compound.getCompoundTag(ID);
		
		if(lTag.hasKey("lives"))
		{
			lives = lTag.getInteger("lives");
		} else
		{
			lives = LifeManager.defLives;
		}
	}

	@Override
	public void init(Entity entity, World world)
	{
	}
}

package betterquesting.api.registry;

import net.minecraft.entity.player.EntityPlayer;
import betterquesting.api.other.IDataSync;
import betterquesting.api.properties.IPropertyContainer;


public interface IQuestSettings extends IPropertyContainer, IDataSync
{
	public boolean canUserEdit(EntityPlayer player);
}

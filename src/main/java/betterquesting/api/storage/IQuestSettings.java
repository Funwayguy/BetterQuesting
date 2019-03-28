package betterquesting.api.storage;

import net.minecraft.entity.player.EntityPlayer;
import betterquesting.api.misc.IDataSync;
import betterquesting.api.properties.IPropertyContainer;


public interface IQuestSettings extends IPropertyContainer, IDataSync
{
	boolean canUserEdit(EntityPlayer player);
}

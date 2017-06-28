package adv_director.api.storage;

import net.minecraft.entity.player.EntityPlayer;
import adv_director.api.misc.IDataSync;
import adv_director.api.properties.IPropertyContainer;


public interface IQuestSettings extends IPropertyContainer, IDataSync
{
	public boolean canUserEdit(EntityPlayer player);
}

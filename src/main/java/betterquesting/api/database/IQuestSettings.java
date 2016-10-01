package betterquesting.api.database;

import net.minecraft.entity.player.EntityPlayer;
import betterquesting.api.quests.properties.IPropertyContainer;


public interface IQuestSettings extends IPropertyContainer, IDataSync
{
	public boolean canUserEdit(EntityPlayer player);
}

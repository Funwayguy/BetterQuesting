package betterquesting.api.storage;

import net.minecraft.entity.player.EntityPlayer;
import betterquesting.api.properties.IPropertyContainer;


public interface IQuestSettings extends IPropertyContainer
{
	boolean canUserEdit(EntityPlayer player);
    void reset();
}

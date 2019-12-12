package betterquesting.api.storage;

import betterquesting.api.properties.IPropertyContainer;
import net.minecraft.entity.player.PlayerEntity;


public interface IQuestSettings extends IPropertyContainer
{
	boolean canUserEdit(PlayerEntity player); // TODO: Is it necessary to even have this interface just for this one method?
    void reset();
}

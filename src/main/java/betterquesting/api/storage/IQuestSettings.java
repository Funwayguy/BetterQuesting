package betterquesting.api.storage;

import betterquesting.api.properties.IPropertyContainer;
import net.minecraft.entity.player.EntityPlayer;


public interface IQuestSettings extends IPropertyContainer {
    boolean canUserEdit(EntityPlayer player);

    void reset();
}

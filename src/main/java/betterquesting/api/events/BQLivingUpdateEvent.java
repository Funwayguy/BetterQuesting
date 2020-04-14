package betterquesting.api.events;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;

public class BQLivingUpdateEvent extends LivingUpdateEvent
{
    public BQLivingUpdateEvent(EntityPlayerMP player)
    {
        super(player);
    }
}

package betterquesting.advancement;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.FMLCommonHandler;

import java.util.ArrayList;
import java.util.List;

public class AdvListenerManager
{
    public static final AdvListenerManager INSTANCE = new AdvListenerManager();
    
    private final List<BqsAdvListener<?>> listenerList = new ArrayList<>();
    
    public void registerListener(final BqsAdvListener<?> listener)
    {
        MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
        if(server != null) server.addScheduledTask(() -> listenerList.add(listener));
    }
    
    public void updateAll()
    {
        MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
        
        for(EntityPlayerMP player : server.getPlayerList().getPlayers())
        {
            for(BqsAdvListener<?> advl : listenerList)
            {
                advl.unregisterSelf(player.getAdvancements());
                if(advl.verify()) advl.registerSelf(player.getAdvancements());
            }
        }
        
        listenerList.removeIf(advl -> !advl.verify());
    }
}

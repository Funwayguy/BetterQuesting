package betterquesting.network;

import betterquesting.api.network.IPacketRegistry;
import betterquesting.network.handlers.*;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.function.Consumer;

public class PacketTypeRegistry implements IPacketRegistry
{
	public static final PacketTypeRegistry INSTANCE = new PacketTypeRegistry();
	
	private final HashMap<ResourceLocation, Consumer<Tuple<CompoundNBT, ServerPlayerEntity>>> serverHandlers = new HashMap<>();
	private final HashMap<ResourceLocation, Consumer<CompoundNBT>> clientHandlers = new HashMap<>();
 
	public void initCommon()
	{
        NetQuestSync.registerHandler();
        NetQuestEdit.registerHandler();
        NetQuestAction.registerHandler();
        
        NetChapterSync.registerHandler();
        NetChapterEdit.registerHandler();
        
        NetPartySync.registerHandler();
        NetPartyAction.registerHandler();
		NetInviteSync.registerHandler();
		
		NetLifeSync.registerHandler();
		NetNameSync.registerHandler();
		NetNotices.registerHandler();
		NetStationEdit.registerHandler();
		NetImport.registerHandler();
		NetSettingSync.registerHandler();
		
		NetCacheSync.registerHandler();
		NetBulkSync.registerHandler();
	}
	
	/*public void initClient()
    {
    
    }*/
	
	@Override
	public void registerServerHandler(@Nonnull ResourceLocation idName, @Nonnull Consumer<Tuple<CompoundNBT,ServerPlayerEntity>> method)
    {
        if(serverHandlers.containsKey(idName))
        {
			throw new IllegalArgumentException("Cannot register dupliate packet handler: " + idName);
        }
        
        serverHandlers.put(idName, method);
    }
	
	@Override
    @OnlyIn(Dist.CLIENT)
	public void registerClientHandler(@Nonnull ResourceLocation idName, @Nonnull Consumer<CompoundNBT> method)
    {
        if(clientHandlers.containsKey(idName))
        {
			throw new IllegalArgumentException("Cannot register dupliate packet handler: " + idName);
        }
        
        clientHandlers.put(idName, method);
    }
	
	@Nullable
	public Consumer<Tuple<CompoundNBT, ServerPlayerEntity>> getServerHandler(@Nonnull ResourceLocation idName)
    {
        return serverHandlers.get(idName);
    }
	
	@Nullable
    @OnlyIn(Dist.CLIENT)
	public Consumer<CompoundNBT> getClientHandler(@Nonnull ResourceLocation idName)
    {
        return clientHandlers.get(idName);
    }
}

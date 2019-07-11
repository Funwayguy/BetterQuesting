package betterquesting.network;

import betterquesting.api.network.IPacketHandler;
import betterquesting.api.network.IPacketRegistry;
import betterquesting.core.BetterQuesting;
import betterquesting.network.handlers.*;
import betterquesting.network.handlers.quests.*;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.function.Consumer;

public class PacketTypeRegistry implements IPacketRegistry
{
	public static final PacketTypeRegistry INSTANCE = new PacketTypeRegistry();
	
	private final HashMap<ResourceLocation, Consumer<Tuple<NBTTagCompound, EntityPlayerMP>>> serverHandlers = new HashMap<>();
	private final HashMap<ResourceLocation, Consumer<NBTTagCompound>> clientHandlers = new HashMap<>();
 
	public void init()
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
		registerHandler(new PktHandlerNotification());
		registerHandler(new PktHandlerTileEdit());
		registerHandler(new PktHandlerImport());
		registerHandler(PktHandlerSettings.INSTANCE);
		
		if(BetterQuesting.proxy.isClient()) registerHandler(new PktHandlerCacheSync());
	}
	
	@Deprecated
    private void registerHandler(@Nonnull IPacketHandler handler)
	{
		if(handler.getRegistryName() == null)
		{
			throw new IllegalArgumentException("Tried to register a packet handler with a null name: " + handler.getClass());
		}
        
        if(BetterQuesting.proxy.isClient())
        {
            registerClientHandler(handler.getRegistryName(), handler::handleClient);
        }
        
        registerServerHandler(handler.getRegistryName(), (message) -> handler.handleServer(message.getFirst(), message.getSecond()));
	}
	
	@Override
	public void registerServerHandler(@Nonnull ResourceLocation idName, @Nonnull Consumer<Tuple<NBTTagCompound,EntityPlayerMP>> method)
    {
        if(serverHandlers.containsKey(idName))
        {
			throw new IllegalArgumentException("Cannot register dupliate packet handler: " + idName);
        }
        
        serverHandlers.put(idName, method);
    }
	
	@Override
    @SideOnly(Side.CLIENT)
	public void registerClientHandler(@Nonnull ResourceLocation idName, @Nonnull Consumer<NBTTagCompound> method)
    {
        if(clientHandlers.containsKey(idName))
        {
			throw new IllegalArgumentException("Cannot register dupliate packet handler: " + idName);
        }
        
        clientHandlers.put(idName, method);
    }
	
	@Nullable
	public Consumer<Tuple<NBTTagCompound,EntityPlayerMP>> getServerHandler(@Nonnull ResourceLocation idName)
    {
        return serverHandlers.get(idName);
    }
	
	@Nullable
    @SideOnly(Side.CLIENT)
	public Consumer<NBTTagCompound> getClientHandler(@Nonnull ResourceLocation idName)
    {
        return clientHandlers.get(idName);
    }
}

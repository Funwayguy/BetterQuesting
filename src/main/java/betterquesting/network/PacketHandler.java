package betterquesting.network;

import betterquesting.api.api.QuestingAPI;
import betterquesting.core.BetterQuesting;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.NetworkEvent.Context;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import org.apache.logging.log4j.Level;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class PacketHandler implements BiConsumer<PacketQuesting, Supplier<Context>>
{
    public static final PacketHandler INSTANCE = new PacketHandler();
    
    @Override
    public void accept(PacketQuesting packet, Supplier<Context> context)
    {
        switch(context.get().getDirection().getOriginationSide())
        {
            case CLIENT: // Sent from the client to the server
                acceptServer(packet, context);
                context.get().setPacketHandled(true);
                break;
            case SERVER: // Sent from the server to the client
                acceptClient(packet, context);
                context.get().setPacketHandled(true);
                break;
        }
    }
    
    private void acceptServer(PacketQuesting packet, Supplier<Context> context)
    {
        if(packet == null || packet.getTags() == null)
        {
            BetterQuesting.logger.log(Level.ERROR, "A critical NPE error occured during while handling a BetterQuesting packet server side", new NullPointerException());
            return;
        }
        
        final ServerPlayerEntity sender = context.get().getSender();
        final CompoundNBT message = PacketAssembly.INSTANCE.assemblePacket(sender == null ? null : QuestingAPI.getQuestingUUID(sender), packet.getTags());
        
        if(message == null)
        {
            return;
        } else if(!message.contains("ID"))
        {
            BetterQuesting.logger.log(Level.WARN, "Recieved a packet server side without an ID");
            return;
        }
        
        final Consumer<Tuple<CompoundNBT, ServerPlayerEntity>> method = PacketTypeRegistry.INSTANCE.getServerHandler(new ResourceLocation(message.getString("ID")));
        
        if(method == null)
        {
            BetterQuesting.logger.log(Level.WARN, "Recieved a packet server side with an invalid ID: " + message.getString("ID"));
        } else if(sender != null)
        {
            ServerLifecycleHooks.getCurrentServer().deferTask(() -> method.accept(new Tuple<>(message, sender)));
        }
    }
    
    @OnlyIn(Dist.CLIENT)
    private void acceptClient(PacketQuesting packet, Supplier<Context> context)
    {
        if(packet == null || packet.getTags() == null)
        {
            BetterQuesting.logger.log(Level.ERROR, "A critical NPE error occured during while handling a BetterQuesting packet client side", new NullPointerException());
            return;
        }
        
        final CompoundNBT message = PacketAssembly.INSTANCE.assemblePacket(null, packet.getTags());
        
        if(message == null)
        {
            return;
        } else if(!message.contains("ID"))
        {
            BetterQuesting.logger.log(Level.WARN, "Recieved a packet server side without an ID");
            return;
        }
        
        final Consumer<CompoundNBT> method = PacketTypeRegistry.INSTANCE.getClientHandler(new ResourceLocation(message.getString("ID")));
        
        if(method == null)
        {
            BetterQuesting.logger.log(Level.WARN, "Recieved a packet server side with an invalid ID: " + message.getString("ID"));
        } else
        {
            Minecraft.getInstance().deferTask(() -> method.accept(message));
        }
    }
}

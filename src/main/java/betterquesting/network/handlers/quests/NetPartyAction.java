package betterquesting.network.handlers.quests;

import betterquesting.api.api.QuestingAPI;
import betterquesting.api.enums.EnumPartyStatus;
import betterquesting.api.questing.party.IParty;
import betterquesting.core.BetterQuesting;
import betterquesting.network.PacketTypeRegistry;
import betterquesting.questing.party.PartyManager;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.UUID;

public class NetPartyAction
{
    private static final ResourceLocation ID_NAME = new ResourceLocation("betterquesting:party_action");
    
    public static void registerHandler()
    {
        PacketTypeRegistry.INSTANCE.registerServerHandler(ID_NAME, NetPartyAction::onServer);
        
        if(BetterQuesting.proxy.isClient())
        {
            PacketTypeRegistry.INSTANCE.registerClientHandler(ID_NAME, NetPartyAction::onClient);
        }
    }
    
    private static void onServer(Tuple<NBTTagCompound, EntityPlayerMP> message)
    {
        EntityPlayerMP sender = message.getSecond();
        MinecraftServer server = sender.getServer();
        if(server == null) return; // Here mostly just to keep intellisense happy
        
		int action = !message.getFirst().hasKey("action", 99) ? -1 : message.getFirst().getInteger("action");
		int partyID = !message.getFirst().hasKey("partyID", 99) ? -1 : message.getFirst().getInteger("partyID");
        IParty party = PartyManager.INSTANCE.getValue(partyID);
        int permission = party == null ? 0 : checkPermission(server, sender, party);
		
		switch(action)
        {
            case 0:
            {
                createParty(sender, message.getFirst().getString("name"));
                break;
            }
            case 1:
            {
                deleteParty(message.getFirst().getInteger("partyID"));
                break;
            }
            case 2:
            {
            
            }
        }
    }
    
    private static void createParty(EntityPlayerMP sender, String name)
    {
        UUID playerID = QuestingAPI.getQuestingUUID(sender);
        if(PartyManager.INSTANCE.getParty(playerID) != null) return;
        
        int partyID = PartyManager.INSTANCE.nextID();
        IParty party = PartyManager.INSTANCE.createNew(partyID);
        party.setStatus(playerID, EnumPartyStatus.OWNER);
        NetPartySync.sendSync(new EntityPlayerMP[]{sender}, new int[]{partyID});
    }
    
    private static void deleteParty(int partyID)
    {
    
    }
    
    private static void editParty(int partyID, NBTTagCompound settings)
    {
    
    }
    
    private static void inviteUser(int partyID, String username)
    {
    
    }
    
    private static void acceptInvite(int partyID, UUID uuid)
    {
    
    }
    
    private static void kickUser(int partyID, String username) // Is also the leave action (self kick if you will)
    {
    
    }
    
    private static int checkPermission(MinecraftServer server, EntityPlayerMP player, IParty party)
    {
        if(server.getPlayerList().canSendCommands(player.getGameProfile())) return 4; // Can kick owners or force invites without needing to be a member of the party
		UUID playerID = QuestingAPI.getQuestingUUID(player);
        EnumPartyStatus status = party.getStatus(playerID);
        if(status == null) return 0; // Only OPs can edit parties they aren't a member of
        
        switch(status)
        {
            case MEMBER:
                return 1;
            case ADMIN:
                return 2;
            case OWNER:
                return 3;
            default:
                return 0;
        }
    }
    
    // TODO: Include client side handling when leaving a party (sync wouldn't pickup on it after a user has left)
    // TODO: Include client side deletion when parties are disbanded
    @SideOnly(Side.CLIENT)
    private static void onClient(NBTTagCompound message)
    {
    }
}

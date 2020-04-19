package betterquesting.questing.party;

import betterquesting.api.enums.EnumPartyStatus;
import betterquesting.api.properties.NativeProps;
import betterquesting.api.questing.IQuest;
import betterquesting.api.questing.party.IParty;
import betterquesting.api.questing.party.IPartyDatabase;
import betterquesting.api2.storage.DBEntry;
import betterquesting.api2.storage.SimpleDatabase;
import betterquesting.core.BetterQuesting;
import betterquesting.network.handlers.NetQuestSync;
import betterquesting.questing.QuestDatabase;
import betterquesting.storage.QuestSettings;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.server.MinecraftServer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public class PartyManager extends SimpleDatabase<IParty> implements IPartyDatabase
{
	public static final PartyManager INSTANCE = new PartyManager();

	public static void SyncPartyQuests(IParty party, UUID targetPlayer) {
		ArrayList<UUID> uuids = new ArrayList<>();
		uuids.add(targetPlayer);
		SyncPartyQuests(party, uuids);
	}
	public static void SyncPartyQuests(IParty party) {
		SyncPartyQuests(party, party.getMembers());
	}

	private static void SyncPartyQuests(IParty party, List<UUID> targets) {
		new Thread(() -> {
			BetterQuesting.logger.info("Start force party quest sync");
			long current = System.currentTimeMillis();
			List<UUID> partyMembers = party.getMembers();

			Map<UUID, List<Integer>> syncQueue = new HashMap<>();
			for (UUID target : targets) {
				syncQueue.put(target, new ArrayList<>());
			}

			for (DBEntry<IQuest> entry : QuestDatabase.INSTANCE.getEntries()) {
				IQuest value = entry.getValue();
				boolean isCompleted = false;
				for (UUID member : partyMembers) {
					if (value.isComplete(member)){
						isCompleted = true;
						break;
					}
				}

				if (isCompleted){
					for (UUID target : targets) {
						if (!value.isComplete(target)) {
							value.setComplete(target, current);
							syncQueue.get(target).add(entry.getID());
						}
					}
				}
			}

			for (Map.Entry<UUID, List<Integer>> uuidListEntry : syncQueue.entrySet()) {
				EntityPlayerMP player = getPlayer(uuidListEntry.getKey());
				BetterQuesting.logger.info("Force party quest sync: Completed " + uuidListEntry.getValue().size() + " quests for " +
						(player != null ? player.getDisplayName() : uuidListEntry.getKey().toString()));
				if (player == null) continue;
				NetQuestSync.sendSync(player, uuidListEntry.getValue().stream().mapToInt(i -> i).toArray(), false, true);
			}
		}).start();
	}

	private static EntityPlayerMP getPlayer(UUID uuid){
		Optional onlinePlayer = MinecraftServer.getServer().getConfigurationManager().playerEntityList.stream().filter(i -> i instanceof EntityPlayerMP)
				.filter(o -> ((EntityPlayerMP) o).getPersistentID() == uuid).findFirst();
		return onlinePlayer.isPresent() ? (EntityPlayerMP) onlinePlayer.get() : null;
	}

	private final HashMap<UUID,Integer> partyCache = new HashMap<>();

	@Override
    public synchronized IParty createNew(int id)
    {
        IParty party = new PartyInstance();
        if(id >= 0) this.add(id, party);
        return party;
    }

    @Nullable
	@Override
	public synchronized DBEntry<IParty> getParty(@Nonnull UUID uuid)
	{
	    if(!QuestSettings.INSTANCE.getProperty(NativeProps.PARTY_ENABLE)) return null; // We're merely preventing access. Not erasing data

        Integer cachedID = partyCache.get(uuid);
        IParty cachedParty = cachedID == null ? null : getValue(cachedID);

        if(cachedID != null && cachedParty == null) // Disbanded party
        {
            partyCache.remove(uuid);
        } else if(cachedParty != null) // Active party. Check validity...
        {
            EnumPartyStatus status = cachedParty.getStatus(uuid);
            if(status != null) return new DBEntry<>(cachedID, cachedParty);
            partyCache.remove(uuid); // User isn't a party member anymore
        }

	    // NOTE: A server with a lot of solo players may still hammer this loop. Optimise further?
		for(DBEntry<IParty> entry : getEntries())
		{
			EnumPartyStatus status = entry.getValue().getStatus(uuid);

			if(status != null)
			{
			    partyCache.put(uuid, entry.getID());
				return entry;
			}
		}

		return null;
	}

	@Override
	public NBTTagList writeToNBT(NBTTagList json, @Nullable List<Integer> subset)
	{
		for(DBEntry<IParty> entry : getEntries())
		{
		    if(subset != null && !subset.contains(entry.getID())) continue;
			NBTTagCompound jp = entry.getValue().writeToNBT(new NBTTagCompound());
			jp.setInteger("partyID", entry.getID());
			json.appendTag(jp);
		}

		return json;
	}

	@Override
	public void readFromNBT(NBTTagList json, boolean merge)
	{
		if(!merge) reset();

		for(int i = 0; i < json.tagCount(); i++)
		{
			NBTTagCompound jp = json.getCompoundTagAt(i);

			int partyID = jp.hasKey("partyID", 99) ? jp.getInteger("partyID") : -1;
			if(partyID < 0) continue;

			IParty party = new PartyInstance();
			party.readFromNBT(jp);

			if(party.getMembers().size() > 0)
			{
				add(partyID, party);
			}
		}
	}

	@Override
    public synchronized void reset()
    {
        super.reset();
        partyCache.clear();
    }
}

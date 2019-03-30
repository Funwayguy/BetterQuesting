package betterquesting.api.questing.party;

import betterquesting.api.enums.EnumPartyStatus;
import betterquesting.api.misc.IDataSync;
import betterquesting.api.properties.IPropertyContainer;
import betterquesting.api2.storage.INBTSaveLoad;
import net.minecraft.nbt.NBTTagCompound;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

public interface IParty extends INBTSaveLoad<NBTTagCompound>, IDataSync
{
	String getName();
	
	IPropertyContainer getProperties();
	
	/**
	 * Invites a user to this party if they are not currently a member.
	 * If this party has no members, the invited user will be promoted to owner.
	 */
	void inviteUser(UUID uuid);
	
	/**
	 * Kicks an existing member from the party. If the owner is kicked than a host
	 * migration will take place to the next administrator or member in line.
	 */
	void kickUser(UUID uuid);
	
	/**
	 * Sets the privilege level of an existing party member.
	 * Can be used to confirm an invite, promote to administrator or migrate hosts
	 */
	void setStatus(UUID uuid, @Nonnull EnumPartyStatus priv);
	@Nullable
	EnumPartyStatus getStatus(UUID uuid);
	
	List<UUID> getMembers();
}

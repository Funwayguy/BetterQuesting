package betterquesting.api.questing.party;

import java.util.List;
import java.util.UUID;
import betterquesting.api.enums.EnumPartyStatus;
import betterquesting.api.misc.IDataSync;
import betterquesting.api.misc.IJsonSaveLoad;
import betterquesting.api.properties.IPropertyContainer;
import com.google.gson.JsonObject;

public interface IParty extends IJsonSaveLoad<JsonObject>, IDataSync
{
	public String getName();
	
	public boolean getShareLives();
	public boolean getShareReward();
	
	
	public IPropertyContainer getProperties();
	
	/**
	 * Invites a user to this party if they are not currently a member.
	 * If this party has no members, the invited user will be promoted to owner.
	 */
	public void inviteUser(UUID uuid);
	
	/**
	 * Kicks an existing member from the party. If the owner is kicked than a host
	 * migration will take place to the next administrator or member in line.
	 */
	public void kickUser(UUID uuid);
	
	/**
	 * Sets the privilege level of an existing party member.
	 * Can be used to confirm an invite, promote to administrator or migrate hosts
	 */
	public void setStatus(UUID uuid, EnumPartyStatus priv);
	public EnumPartyStatus getStatus(UUID uuid);
	
	public List<UUID> getMembers();
	
	// Replaced by IPropertyContainer
	@Deprecated
	public void setShareLives(boolean state);
	@Deprecated
	public void setShareReward(boolean state);
	@Deprecated
	public void setName(String name);
}

package betterquesting.quests.tasks.party;

import java.util.UUID;
import betterquesting.party.PartyInstance;
import betterquesting.party.PartyInstance.PartyMember;
import betterquesting.party.PartyManager;
import betterquesting.quests.tasks.TaskBase;

public abstract class PartyTaskBase<T> extends TaskBase implements IPartyTask<T>
{
	@Override
	public void ResetPartyProgress(UUID uuid)
	{
		PartyInstance party = PartyManager.GetParty(uuid);
		
		if(party == null)
		{
			ResetProgress(uuid);
		} else
		{
			for(PartyMember mem : party.GetMembers())
			{
				if(mem == null || mem.GetPrivilege() <= 0)
				{
					continue;
				}
				
				ResetProgress(mem.userID);
			}
		}
	}
	
	@Override
	public void SetPartyCompletion(UUID uuid, boolean state)
	{
		PartyInstance party = PartyManager.GetParty(uuid);
		
		if(party == null)
		{
			setCompletion(uuid, state);
		} else
		{
			for(PartyMember mem : party.GetMembers())
			{
				if(mem == null || mem.GetPrivilege() <= 0)
				{
					continue;
				}
				
				setCompletion(mem.userID, state);
			}
		}
	}
}

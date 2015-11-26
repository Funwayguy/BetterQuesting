package betterquesting.client.gui.party;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import betterquesting.client.gui.GuiQuesting;
import betterquesting.party.PartyInstance;

public class GuiPartyInvite extends GuiQuesting
{
	PartyInstance party;
	
	public GuiPartyInvite(GuiScreen parent, PartyInstance party)
	{
		super(parent, "Invite To Party");
		this.party = party;
	}
	
	public void initGui()
	{
		super.initGui();
	}
	
	public void drawScreen(int mx, int my, float partialTick)
	{
		super.drawScreen(mx, my, partialTick);
	}
	
	/*                      Invite Members
	 * 
	 * Username [Invite] | Username [Invite] | Username [Invite]
	 * Username [Invite] | Username [Invite] | Username [Invite]
	 * Username [Invite] | Username [Invite] | Username [Invite]
	 * Username [Invite] | Username [Invite] | Username [Invite]
	 * Username [Invite] | Username [Invite] | Username [Invite]
	 * 
	 *                          [Done]
	 */
	
	public void actionPerformed(GuiButton button)
	{
		super.actionPerformed(button);
	}
	
	public void RefreshColumns()
	{
		
	}
}

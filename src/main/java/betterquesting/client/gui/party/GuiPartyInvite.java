package betterquesting.client.gui.party;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MathHelper;
import org.lwjgl.input.Mouse;
import betterquesting.client.gui.GuiQuesting;
import betterquesting.client.gui.misc.GuiButtonQuesting;
import betterquesting.core.BetterQuesting;
import betterquesting.network.PacketQuesting.PacketDataType;
import betterquesting.party.PartyInstance;

public class GuiPartyInvite extends GuiQuesting
{
	int scroll = 0;
	int maxRows = 0;
	PartyInstance party;
	List<NetworkPlayerInfo> playerList;
	
	public GuiPartyInvite(GuiScreen parent, PartyInstance party)
	{
		super(parent, "betterquesting.title.party_invite");
		this.party = party;
	}
	
	public void initGui()
	{
		super.initGui();
		maxRows = (sizeY - 72)/20;
		
        NetHandlerPlayClient nethandlerplayclient = mc.thePlayer.sendQueue;
		playerList = new ArrayList<NetworkPlayerInfo>(nethandlerplayclient.getPlayerInfoMap());
		
		for(int i = 0; i < maxRows * 3; i++)
		{
			GuiButtonQuesting btn = new GuiButtonQuesting(this.buttonList.size(), guiLeft + sizeX/2 - 150 + ((i%3)*100), guiTop + 48 + (i/3*20), 100, 20, "Username");
			this.buttonList.add(btn);
		}
		
		RefreshColumns();
	}
	
	public void drawScreen(int mx, int my, float partialTick)
	{
		super.drawScreen(mx, my, partialTick);
	}
	
	public void actionPerformed(GuiButton button)
	{
		super.actionPerformed(button);
		
		if(button.id > 0)
		{
			int n1 = button.id - 1; // Button index
			int n2 = n1/(maxRows*3); // Column listing (0 = line)
			int n3 = n1%(maxRows*3) + scroll; // Format index
			
			if(n2 == 0)
			{
				if(n3 >= 0 && n3 < playerList.size())
				{
					NBTTagCompound tags = new NBTTagCompound();
					//tags.setInteger("ID", 7);
					tags.setInteger("action", 4);
					tags.setString("Party", party.name);
					tags.setString("Member", button.displayString);
					//BetterQuesting.instance.network.sendToServer(new PacketQuesting(tags));
					BetterQuesting.instance.network.sendToServer(PacketDataType.PARTY_ACTION.makePacket(tags));
				}
			}
		}
	}
	
	@Override
	public void handleMouseInput() throws IOException
	{
		super.handleMouseInput();
		
        int mx = Mouse.getEventX() * this.width / this.mc.displayWidth;
        int my = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;
        int SDX = (int)-Math.signum(Mouse.getEventDWheel());
        
        if(SDX != 0 && isWithin(mx, my, this.guiLeft + sizeX/2, this.guiTop, sizeX/2, sizeY))
        {
    		scroll = Math.max(0, MathHelper.clamp_int(scroll + SDX*3, 0, playerList.size() - maxRows*3));
    		RefreshColumns();
        }
	}
	
	public void RefreshColumns()
	{
		scroll = Math.max(0, MathHelper.clamp_int(scroll, 0, playerList.size() - maxRows*3));

		List<GuiButton> btnList = this.buttonList;
		
		for(int i = 1; i < btnList.size(); i++)
		{
			GuiButton btn = btnList.get(i);
			int n1 = btn.id - 1; // Button index
			int n2 = n1/(maxRows*3); // Column listing (0 = line)
			int n3 = n1%(maxRows*3) + scroll; // Format index
			
			if(n2 == 0)
			{
				if(n3 >= 0 && n3 < playerList.size())
				{
					btn.visible = btn.enabled = true;
					btn.displayString = playerList.get(n3).getGameProfile().getName();
				} else
				{
					btn.visible = btn.enabled = false;
					btn.displayString = "#" + n3;
				}
			}
		}
	}
}

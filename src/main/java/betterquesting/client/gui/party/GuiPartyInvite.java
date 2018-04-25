package betterquesting.client.gui.party;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.MathHelper;
import betterquesting.api.client.gui.GuiScreenThemed;
import betterquesting.api.client.gui.controls.GuiBigTextField;
import betterquesting.api.client.gui.controls.GuiButtonThemed;
import betterquesting.api.client.gui.misc.INeedsRefresh;
import betterquesting.api.enums.EnumPacketAction;
import betterquesting.api.network.QuestingPacket;
import betterquesting.api.questing.party.IParty;
import betterquesting.network.PacketSender;
import betterquesting.network.PacketTypeNative;
import betterquesting.questing.party.PartyManager;
import betterquesting.storage.NameCache;

@Deprecated
public class GuiPartyInvite extends GuiScreenThemed implements INeedsRefresh
{
	private final int partyID;
	private IParty party;
	
	private int listScroll = 0;
	private int maxRows = 0;
	
	private final List<String> playerList = new ArrayList<String>();
	private GuiBigTextField txtManual;
	private GuiButtonThemed btnManual;
	
	public GuiPartyInvite(GuiScreen parent, IParty party)
	{
		super(parent, "betterquesting.title.party_invite");
		this.party = party;
		this.partyID = PartyManager.INSTANCE.getKey(party);
	}
	
	@Override
	public void initGui()
	{
		super.initGui();
		maxRows = (sizeY - 92)/20;
		
        NetHandlerPlayClient nethandlerplayclient = mc.player.connection;
        
        playerList.clear();
        playerList.addAll(NameCache.INSTANCE.getAllNames());
        
        for(NetworkPlayerInfo info : nethandlerplayclient.getPlayerInfoMap())
        {
        	if(!playerList.contains(info.getGameProfile().getName()))
        	{
        		playerList.add(info.getGameProfile().getName());
        	}
        }
		
		this.txtManual = new GuiBigTextField(this.fontRenderer, guiLeft + sizeX/2 - 149, guiTop + 33, 198, 18);
		this.txtManual.setWatermark("Username");
		this.btnManual = new GuiButtonThemed(this.buttonList.size(), guiLeft + sizeX/2 + 50, guiTop + 32, 100, 20, I18n.format("betterquesting.btn.party_invite"), true);
		this.buttonList.add(btnManual);
		
		for(int i = 0; i < maxRows * 3; i++)
		{
			GuiButtonThemed btn = new GuiButtonThemed(this.buttonList.size(), guiLeft + sizeX/2 - 150 + ((i%3)*100), guiTop + 68 + (i/3*20), 100, 20, "Username", true);
			this.buttonList.add(btn);
		}
		
		RefreshColumns();
	}
	
	@Override
	public void refreshGui()
	{
		this.party = PartyManager.INSTANCE.getValue(partyID);
		
        NetHandlerPlayClient nethandlerplayclient = mc.player.connection;
        
        playerList.clear();
        playerList.addAll(NameCache.INSTANCE.getAllNames());
        
        for(NetworkPlayerInfo info : nethandlerplayclient.getPlayerInfoMap())
        {
        	if(!playerList.contains(info.getGameProfile().getName()))
        	{
        		playerList.add(info.getGameProfile().getName());
        	}
        }
		
		RefreshColumns();
	}
	
	@Override
	public void drawScreen(int mx, int my, float partialTick)
	{
		super.drawScreen(mx, my, partialTick);
		
		if(txtManual != null)
		{
			txtManual.drawTextBox(mx, my, partialTick);
		}
		
		GlStateManager.color(1F, 1F, 1F, 1F);
		mc.renderEngine.bindTexture(currentTheme().getGuiTexture());
		
		// Scroll bar
		this.drawTexturedModalRect(guiLeft + sizeX/2 + 150, this.guiTop + 68, 248, 0, 8, 20);
		int s = 20;
		while(s < (maxRows - 1) * 20)
		{
			this.drawTexturedModalRect(guiLeft + sizeX/2 + 150, this.guiTop + 68 + s, 248, 20, 8, 20);
			s += 20;
		}
		
		this.drawTexturedModalRect(guiLeft + sizeX/2 + 150, this.guiTop + 68 + s, 248, 40, 8, 20);
		this.drawTexturedModalRect(guiLeft + sizeX/2 + 150, this.guiTop + 68 + (int)Math.max(0, s * (float)listScroll/(playerList.size() - maxRows * 3)), 248, 60, 8, 20);
	}
	
	@Override
	public void actionPerformed(GuiButton button)
	{
		super.actionPerformed(button);
		
		if(button.id == 1)
		{
			NBTTagCompound tags = new NBTTagCompound();
			tags.setInteger("action", EnumPacketAction.INVITE.ordinal());
			tags.setInteger("partyID", PartyManager.INSTANCE.getKey(party));
			tags.setString("target", txtManual.getText());
			PacketSender.INSTANCE.sendToServer(new QuestingPacket(PacketTypeNative.PARTY_EDIT.GetLocation(), tags));
		} else if(button.id > 1)
		{
			int n1 = button.id - 2; // Button index
			int n2 = n1/(maxRows*3); // Column listing (0 = line)
			int n3 = n1%(maxRows*3) + listScroll; // Format index
			
			if(n2 == 0)
			{
				if(n3 >= 0 && n3 < playerList.size())
				{
					NBTTagCompound tags = new NBTTagCompound();
					tags.setInteger("action", EnumPacketAction.INVITE.ordinal());
					tags.setInteger("partyID", PartyManager.INSTANCE.getKey(party));
					tags.setString("target", button.displayString);
					PacketSender.INSTANCE.sendToServer(new QuestingPacket(PacketTypeNative.PARTY_EDIT.GetLocation(), tags));
				}
			}
		}
	}
	
	@Override
	public void mouseClicked(int mx, int my, int type) throws IOException
	{
		super.mouseClicked(mx, my, type);
		
		if(this.txtManual != null)
		{
			this.txtManual.mouseClicked(mx, my, type);
		}
	}
	
	@Override
	public void mouseScroll(int mx, int my, int scroll)
	{
		super.mouseScroll(mx, my, scroll);
        
        if(scroll != 0 && isWithin(mx, my, guiLeft, guiTop, sizeX, sizeY))
        {
    		listScroll = Math.max(0, MathHelper.clamp(listScroll + scroll*3, 0, playerList.size() - maxRows*3));
    		RefreshColumns();
        }
	}
	
	@Override
    public void keyTyped(char character, int num) throws IOException
    {
		super.keyTyped(character, num);
		
		if(this.txtManual != null)
		{
			this.txtManual.textboxKeyTyped(character, num);
			
			if(this.txtManual.getText() != null && this.txtManual.getText().length() > 0)
			{
				this.btnManual.enabled = true;
			} else
			{
				this.btnManual.enabled = false;
			}
		} else
		{
			this.btnManual.enabled = false;
		}
    }
	
	public void RefreshColumns()
	{
		listScroll = Math.max(0, MathHelper.clamp(listScroll, 0, playerList.size() - maxRows*3));

		List<GuiButton> btnList = this.buttonList;
		
		for(int i = 2; i < btnList.size(); i++)
		{
			GuiButton btn = btnList.get(i);
			int n1 = btn.id - 2; // Button index
			int n2 = n1/(maxRows*3); // Column listing (0 = line)
			int n3 = n1%(maxRows*3) + listScroll; // Format index
			
			if(n2 == 0)
			{
				if(n3 >= 0 && n3 < playerList.size())
				{
					btn.visible = btn.enabled = true;
					btn.displayString = playerList.get(n3);
				} else
				{
					btn.visible = true;
					btn.enabled = false;
					btn.displayString = "-";
				}
			}
		}
	}
}

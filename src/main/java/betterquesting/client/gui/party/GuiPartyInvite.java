package betterquesting.client.gui.party;

import java.util.List;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiPlayerInfo;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MathHelper;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import betterquesting.client.gui.GuiQuesting;
import betterquesting.client.gui.misc.GuiBigTextField;
import betterquesting.client.gui.misc.GuiButtonQuesting;
import betterquesting.client.themes.ThemeRegistry;
import betterquesting.network.PacketAssembly;
import betterquesting.network.PacketTypeRegistry.BQPacketType;
import betterquesting.party.PartyInstance;

public class GuiPartyInvite extends GuiQuesting
{
	int scroll = 0;
	int maxRows = 0;
	PartyInstance party;
	List<GuiPlayerInfo> playerList;
	GuiBigTextField txtManual;
	GuiButtonQuesting btnManual;
	
	public GuiPartyInvite(GuiScreen parent, PartyInstance party)
	{
		super(parent, "betterquesting.title.party_invite");
		this.party = party;
	}
	
	@SuppressWarnings("unchecked")
	public void initGui()
	{
		super.initGui();
		maxRows = (sizeY - 92)/20;
		
        NetHandlerPlayClient nethandlerplayclient = mc.thePlayer.sendQueue;
		playerList = nethandlerplayclient.playerInfoList;
		
		this.txtManual = new GuiBigTextField(this.fontRendererObj, guiLeft + sizeX/2 - 149, guiTop + 33, 198, 18);
		this.txtManual.setWatermark("Username");
		this.btnManual = new GuiButtonQuesting(this.buttonList.size(), guiLeft + sizeX/2 + 50, guiTop + 32, 100, 20, I18n.format("betterquesting.btn.party_invite"));
		this.buttonList.add(btnManual);
		
		for(int i = 0; i < maxRows * 3; i++)
		{
			GuiButtonQuesting btn = new GuiButtonQuesting(this.buttonList.size(), guiLeft + sizeX/2 - 150 + ((i%3)*100), guiTop + 68 + (i/3*20), 100, 20, "Username");
			this.buttonList.add(btn);
		}
		
		RefreshColumns();
	}
	
	public void drawScreen(int mx, int my, float partialTick)
	{
		super.drawScreen(mx, my, partialTick);
		
		if(txtManual != null)
		{
			txtManual.drawTextBox();
		}
		
		GL11.glColor4f(1F, 1F, 1F, 1F);
		mc.renderEngine.bindTexture(ThemeRegistry.curTheme().guiTexture());
		
		// Scroll bar
		this.drawTexturedModalRect(guiLeft + sizeX/2 + 150, this.guiTop + 68, 248, 0, 8, 20);
		int s = 20;
		while(s < (maxRows - 1) * 20)
		{
			this.drawTexturedModalRect(guiLeft + sizeX/2 + 150, this.guiTop + 68 + s, 248, 20, 8, 20);
			s += 20;
		}
		
		this.drawTexturedModalRect(guiLeft + sizeX/2 + 150, this.guiTop + 68 + s, 248, 40, 8, 20);
		this.drawTexturedModalRect(guiLeft + sizeX/2 + 150, this.guiTop + 68 + (int)Math.max(0, s * (float)scroll/(playerList.size() - maxRows * 3)), 248, 60, 8, 20);
	}
	
	public void actionPerformed(GuiButton button)
	{
		super.actionPerformed(button);
		
		if(button.id == 1)
		{
			NBTTagCompound tags = new NBTTagCompound();
			tags.setInteger("action", 4);
			tags.setString("Party", party.name);
			tags.setString("Member", txtManual.getText());
			PacketAssembly.SendToServer(BQPacketType.PARTY_ACTION.GetLocation(), tags);
		} else if(button.id > 1)
		{
			int n1 = button.id - 2; // Button index
			int n2 = n1/(maxRows*3); // Column listing (0 = line)
			int n3 = n1%(maxRows*3) + scroll; // Format index
			
			if(n2 == 0)
			{
				if(n3 >= 0 && n3 < playerList.size())
				{
					NBTTagCompound tags = new NBTTagCompound();
					tags.setInteger("action", 4);
					tags.setString("Party", party.name);
					tags.setString("Member", button.displayString);
					PacketAssembly.SendToServer(BQPacketType.PARTY_ACTION.GetLocation(), tags);
				}
			}
		}
	}
	
	@Override
	public void mouseClicked(int mx, int my, int type)
	{
		super.mouseClicked(mx, my, type);
		
		if(this.txtManual != null)
		{
			this.txtManual.mouseClicked(mx, my, type);
		}
	}
	
	@Override
	public void handleMouseInput()
	{
		super.handleMouseInput();
		
        int mx = Mouse.getEventX() * this.width / this.mc.displayWidth;
        int my = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;
        int SDX = (int)-Math.signum(Mouse.getEventDWheel());
        
        if(SDX != 0 && isWithin(mx, my, guiLeft, guiTop, sizeX, sizeY))
        {
    		scroll = Math.max(0, MathHelper.clamp_int(scroll + SDX*3, 0, playerList.size() - maxRows*3));
    		RefreshColumns();
        }
	}
	
	@Override
    public void keyTyped(char character, int num)
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
		scroll = Math.max(0, MathHelper.clamp_int(scroll, 0, playerList.size() - maxRows*3));

		@SuppressWarnings("unchecked")
		List<GuiButton> btnList = this.buttonList;
		
		for(int i = 2; i < btnList.size(); i++)
		{
			GuiButton btn = btnList.get(i);
			int n1 = btn.id - 2; // Button index
			int n2 = n1/(maxRows*3); // Column listing (0 = line)
			int n3 = n1%(maxRows*3) + scroll; // Format index
			
			if(n2 == 0)
			{
				if(n3 >= 0 && n3 < playerList.size())
				{
					btn.visible = btn.enabled = true;
					btn.displayString = playerList.get(n3).name;
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

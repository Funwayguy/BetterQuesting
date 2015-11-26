package betterquesting.client.gui.party;

import java.util.List;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MathHelper;
import org.lwjgl.input.Mouse;
import betterquesting.client.gui.GuiQuesting;
import betterquesting.client.gui.misc.GuiButtonQuesting;
import betterquesting.client.themes.ThemeRegistry;
import betterquesting.core.BetterQuesting;
import betterquesting.network.PacketQuesting;
import betterquesting.party.PartyInstance;
import betterquesting.party.PartyInstance.PartyMember;
import betterquesting.party.PartyManager;
import betterquesting.utils.NBTConverter;
import betterquesting.utils.RenderUtils;
import com.google.gson.JsonObject;

public class GuiManageParty extends GuiQuesting
{
	PartyInstance party;
	PartyMember member;
	int rightScroll = 0; // Member list
	int maxRows = 0;
	GuiTextField fieldName;
	
	public GuiManageParty(GuiScreen parent, PartyInstance party)
	{
		super(parent, "Party - " + party.name);
		this.party = party;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public void initGui()
	{
		super.initGui();
		
		title = "Party - " + party.name;
		party = PartyManager.GetParty(mc.thePlayer.getUniqueID());
		member = party == null? null : party.GetMemberData(mc.thePlayer.getUniqueID());
		
		if(member == null)
		{
			mc.displayGuiScreen(new GuiNoParty(parent));
			return;
		}
		
		rightScroll = 0;
		maxRows = (sizeY - 72)/20;
		
		this.buttonList.add(new GuiButtonQuesting(1, guiLeft + sizeX/4 - 75, height/2 + 40, 70, 20, "Leave"));
		GuiButtonQuesting lootBtn = new GuiButtonQuesting(2, guiLeft + sizeX/4 - 75, height/2 - 20, 150, 20, "Loot Share: " + party.lootShare);
		lootBtn.enabled = member.GetPrivilege() == 2;
		this.buttonList.add(lootBtn);
		GuiButtonQuesting lifeBtn = new GuiButtonQuesting(3, guiLeft + sizeX/4 - 75, height/2, 150, 20, "Life Share: " + party.lifeShare);
		lifeBtn.enabled = member.GetPrivilege() == 2;
		this.buttonList.add(lifeBtn);
		GuiButtonQuesting invBtn = new GuiButtonQuesting(4, guiLeft + sizeX/4 + 5, height/2 + 40, 70, 20, "Invite");
		invBtn.enabled = member.GetPrivilege() == 2;
		this.buttonList.add(invBtn);
		
		fieldName = new GuiTextField(mc.fontRenderer, guiLeft + sizeX/4 - 74, height/2 - 59, 148, 18);
		fieldName.setText(party.name);
		fieldName.setEnabled(member.GetPrivilege() == 2);
		
		// Quest Line - Main
		for(int i = 0; i < maxRows; i++)
		{
			GuiButtonQuesting btn = new GuiButtonQuesting(this.buttonList.size(), guiLeft + sizeX - 74, guiTop + 48 + (i*20), 50, 20, "Kick");
			this.buttonList.add(btn);
		}
		
		RefreshColumns();
	}
	
	@Override
	public void drawScreen(int mx, int my, float partialTick)
	{
		super.drawScreen(mx, my, partialTick);
		
		if(PartyManager.updateUI)
		{
			PartyManager.updateUI = false;
			this.initGui();
			return;
		}
		
		if(party == null)
		{ 
			return;
		}
		
		String memTitle = EnumChatFormatting.UNDERLINE + "Party Members";
		mc.fontRenderer.drawString(memTitle, guiLeft + sizeX/4*3 - mc.fontRenderer.getStringWidth(memTitle)/2, guiTop + 32, ThemeRegistry.curTheme().textColor().getRGB(), false);
		
		int dotL = mc.fontRenderer.getStringWidth("...");
		
		for(int i = 0; i < party.GetMembers().size(); i++)
		{
			int n = i + rightScroll;
			
			if(n < 0 || n >= party.GetMembers().size() || i >= maxRows)
			{
				continue;
			}
			
			PartyMember m = party.GetMembers().get(n);
			String name = PartyManager.GetUsername(m.userID);
			if(mc.fontRenderer.getStringWidth(name) > sizeX/2 - 32 - 58) // Prevents overlap onto left side, especially when rendering unresolved UUIDs
			{
				name = mc.fontRenderer.trimStringToWidth(name, sizeX/2 - 32 - 58 - dotL) + "...";
			}
			mc.fontRenderer.drawString(name, guiLeft + sizeX - 82 - mc.fontRenderer.getStringWidth(name), guiTop + 48 + (i*20) + 4, ThemeRegistry.curTheme().textColor().getRGB(), false);
		}
		
		mc.renderEngine.bindTexture(ThemeRegistry.curTheme().guiTexture());
		
		this.drawTexturedModalRect(guiLeft + sizeX - 24, this.guiTop + 48, 248, 0, 8, 20);
		int s = 20;
		while(s < (maxRows - 1) * 20)
		{
			this.drawTexturedModalRect(guiLeft + sizeX - 24, this.guiTop + 48 + s, 248, 20, 8, 20);
			s += 20;
		}
		this.drawTexturedModalRect(guiLeft + sizeX - 24, this.guiTop + 48 + s, 248, 40, 8, 20);
		this.drawTexturedModalRect(guiLeft + sizeX - 24, this.guiTop + 48 + (int)Math.max(0, s * (float)rightScroll/(party.GetMembers().size() - maxRows)), 248, 60, 8, 20);
		
		mc.fontRenderer.drawString("Name:", guiLeft + sizeX/4 - 75, height/2 - 70, ThemeRegistry.curTheme().textColor().getRGB(), false);
		
		fieldName.drawTextBox();
		
		RenderUtils.DrawLine(width/2, guiTop + 32, width/2, guiTop + sizeY - 32, 2F, ThemeRegistry.curTheme().textColor());
	}
	
	@Override
	public void actionPerformed(GuiButton button)
	{
		super.actionPerformed(button); // Finish the button functionality
		
		if(button.id == 1) // Leave party
		{
			NBTTagCompound tags = new NBTTagCompound();
			tags.setInteger("ID", 7);
			tags.setInteger("action", 1);
			tags.setString("Party", party.name);
			tags.setString("Member", member.userID.toString());
			BetterQuesting.instance.network.sendToServer(new PacketQuesting(tags));
		} else if(button.id == 2 && member.GetPrivilege() == 2) // Share loot
		{
			party.lootShare = !party.lootShare;
			SendChanges();
		} else if(button.id == 3 && member.GetPrivilege() == 2) // Share life
		{
			party.lifeShare = !party.lifeShare;
			SendChanges();
		} else if(button.id == 4 && member.GetPrivilege() == 2) // Invite
		{
			mc.displayGuiScreen(new GuiPartyInvite(this, party));
		} else if(button.id > 4) // Kick
		{
			int n1 = button.id - 5; // Button index
			int n2 = n1/maxRows; // Column listing (0 = line)
			int n3 = n1%maxRows + rightScroll; // Format index
			
			if(n2 == 0)
			{
				if(n3 >= 0 && n3 < party.GetMembers().size())
				{
					PartyMember pMem = party.GetMembers().get(n3);
					System.out.println("Kicking user " + pMem.userID.toString() + " from party " + party.name);
					NBTTagCompound tags = new NBTTagCompound();
					tags.setInteger("ID", 7);
					tags.setInteger("action", 1);
					tags.setString("Party", party.name);
					tags.setString("Member", pMem.userID.toString());
					BetterQuesting.instance.network.sendToServer(new PacketQuesting(tags));
				}
			}
		}
	}

    /**
     * Fired when a key is typed. This is the equivalent of KeyListener.keyTyped(KeyEvent e).
     */
	@Override
    protected void keyTyped(char character, int keyCode)
    {
        super.keyTyped(character, keyCode);
        
        fieldName.textboxKeyTyped(character, keyCode);
    }
	
    /**
     * Called when the mouse is clicked.
     */
	@Override
    protected void mouseClicked(int mx, int my, int click)
    {
		super.mouseClicked(mx, my, click);
		
		if(member.GetPrivilege() == 2)
		{
			fieldName.mouseClicked(mx, my, click);
			
			if(!fieldName.isFocused() && !fieldName.getText().equals(party.name))
			{
				String oldName = party.name;
				party.name = fieldName.getText();
				SendChanges(oldName);
			}
		} else
		{
			fieldName.setFocused(false);
		}
    }
	
	@Override
	public void handleMouseInput()
	{
		super.handleMouseInput();
		
        int mx = Mouse.getEventX() * this.width / this.mc.displayWidth;
        int my = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;
        int SDX = (int)-Math.signum(Mouse.getDWheel());
        
        if(SDX != 0 && isWithin(mx, my, this.guiLeft + sizeX/2, this.guiTop, sizeX/2, sizeY))
        {
    		rightScroll = Math.max(0, MathHelper.clamp_int(rightScroll + SDX, 0, party.GetMembers().size() - maxRows));
        }
	}
	public void SendChanges()
	{
		SendChanges(party.name);
	}
	
	public void SendChanges(String name) // Use this if the name is being edited
	{
		if(member == null || member.GetPrivilege() != 2)
		{
			return; // Not allowed to edit the party
		}
		
		NBTTagCompound tags = new NBTTagCompound();
		tags.setInteger("ID", 7);
		tags.setInteger("action", 2);
		tags.setString("Party", name);
		tags.setString("Member", member.userID.toString());
		JsonObject pJson = new JsonObject();
		party.writeToJson(pJson);
		tags.setTag("Data", NBTConverter.JSONtoNBT_Object(pJson, new NBTTagCompound()));
		BetterQuesting.instance.network.sendToServer(new PacketQuesting(tags));
	}
	
	public void RefreshColumns()
	{
		rightScroll = Math.max(0, MathHelper.clamp_int(rightScroll, 0, party.GetMembers().size() - maxRows));

		@SuppressWarnings("unchecked")
		List<GuiButton> btnList = this.buttonList;
		
		for(int i = 5; i < btnList.size(); i++)
		{
			GuiButton btn = btnList.get(i);
			int n1 = btn.id - 5; // Button index
			int n2 = n1/maxRows; // Column listing (0 = line)
			int n3 = n1%maxRows + rightScroll; // Party index
			
			if(n2 == 0)
			{
				if(n3 >= 0 && n3 < party.GetMembers().size())
				{
					btn.visible = true;
					btn.enabled = member.GetPrivilege() == 2;
					// Kick #n3 member of the party
				} else
				{
					btn.visible = btn.enabled = false;
				}
			}
		}
	}
}

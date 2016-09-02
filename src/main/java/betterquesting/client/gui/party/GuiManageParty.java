package betterquesting.client.gui.party;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MathHelper;
import betterquesting.api.client.gui.INeedsRefresh;
import betterquesting.api.client.gui.premade.controls.GuiButtonThemed;
import betterquesting.api.client.gui.premade.screens.GuiScreenThemed;
import betterquesting.api.enums.EnumPacketAction;
import betterquesting.api.enums.EnumPartyStatus;
import betterquesting.api.enums.EnumSaveType;
import betterquesting.api.network.PacketTypeNative;
import betterquesting.api.network.PreparedPayload;
import betterquesting.api.party.IParty;
import betterquesting.api.utils.NBTConverter;
import betterquesting.api.utils.RenderUtils;
import betterquesting.core.BetterQuesting;
import betterquesting.lives.LifeDatabase;
import betterquesting.network.PacketSender;
import betterquesting.party.PartyManager;
import betterquesting.quests.NameCache;
import betterquesting.quests.QuestSettings;
import com.google.gson.JsonObject;

public class GuiManageParty extends GuiScreenThemed implements INeedsRefresh
{
	ItemStack heart;
	int lives = 1;
	int partyID = -1;
	IParty party;
	EnumPartyStatus status;
	int rightScroll = 0; // Member list
	int maxRows = 0;
	GuiTextField fieldName;
	List<UUID> memList = new ArrayList<UUID>();
	
	public GuiManageParty(GuiScreen parent, IParty party)
	{
		super(parent, I18n.format("betterquesting.title.party", party.getName()));
		this.party = party;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public void initGui()
	{
		super.initGui();
		
		party = PartyManager.INSTANCE.getUserParty(mc.thePlayer.getUniqueID());
		
		if(party == null)
		{
			mc.displayGuiScreen(new GuiNoParty(parent));
			return;
		}
		
		status = NameCache.INSTANCE.isOP(mc.thePlayer.getUniqueID())? EnumPartyStatus.OWNER : party.getStatus(mc.thePlayer.getUniqueID());
		heart = new ItemStack(BetterQuesting.extraLife);
		lives = LifeDatabase.INSTANCE.getLives(mc.thePlayer.getUniqueID());
		memList = party.getMembers();
		
		setTitle(I18n.format("betterquesting.title.party", party.getName()));
		
		rightScroll = 0;
		maxRows = (sizeY - 72)/20;
		
		this.buttonList.add(new GuiButtonThemed(1, guiLeft + sizeX/4 - 75, height/2 + 40, 70, 20, I18n.format("betterquesting.btn.party_leave"), true));
		GuiButtonThemed lootBtn = new GuiButtonThemed(2, guiLeft + sizeX/4 - 75, height/2 - 20, 150, 20, I18n.format("betterquesting.btn.party_share_loot") + ": " + party.getShareReward(), true);
		lootBtn.enabled = status.ordinal() >= 3;
		this.buttonList.add(lootBtn);
		GuiButtonThemed lifeBtn = new GuiButtonThemed(3, guiLeft + sizeX/4 - 75, height/2, 150, 20, I18n.format("betterquesting.btn.party_share_lives") + ": " + party.getShareLives(), true);
		lifeBtn.enabled = status.ordinal() >= 3;
		this.buttonList.add(lifeBtn);
		GuiButtonThemed invBtn = new GuiButtonThemed(4, guiLeft + sizeX/4 + 5, height/2 + 40, 70, 20, I18n.format("betterquesting.btn.party_invite"), true);
		invBtn.enabled = status.ordinal() >= 2;
		this.buttonList.add(invBtn);
		
		fieldName = new GuiTextField(mc.fontRenderer, guiLeft + sizeX/4 - 74, height/2 - 59, 148, 18);
		fieldName.setText(party.getName());
		fieldName.setEnabled(status.ordinal() >= 3);
		
		// Quest Line - Main
		for(int i = 0; i < maxRows; i++)
		{
			GuiButtonThemed btn = new GuiButtonThemed(this.buttonList.size(), guiLeft + sizeX - 74, guiTop + 48 + (i*20), 50, 20, I18n.format("betterquesting.btn.party_kick"), true);
			this.buttonList.add(btn);
		}
		
		RefreshColumns();
	}
	
	@Override
	public void refreshGui()
	{
		this.initGui();
	}
	
	@Override
	public void drawScreen(int mx, int my, float partialTick)
	{
		super.drawScreen(mx, my, partialTick);
		
		if(QuestSettings.INSTANCE.isHardcore())
		{
			RenderUtils.RenderItemStack(mc, heart, guiLeft + 16, guiTop + sizeY - 32, "");
			mc.fontRenderer.drawString("x " + lives, guiLeft + 36, guiTop + sizeY - 28, getTextColor());
		}
		
		String memTitle = EnumChatFormatting.UNDERLINE + I18n.format("betterquesting.gui.party_members");
		mc.fontRenderer.drawString(memTitle, guiLeft + sizeX/4*3 - mc.fontRenderer.getStringWidth(memTitle)/2, guiTop + 32, getTextColor(), false);
		
		int dotL = mc.fontRenderer.getStringWidth("...");
		
		for(int i = 0; i < memList.size(); i++)
		{
			int n = i + rightScroll;
			
			if(n < 0 || n >= memList.size() || i >= maxRows)
			{
				continue;
			}
			
			String name = NameCache.INSTANCE.getName(memList.get(n));
			if(mc.fontRenderer.getStringWidth(name) > sizeX/2 - 32 - 58) // Prevents overlap onto left side, especially when rendering unresolved UUIDs
			{
				name = mc.fontRenderer.trimStringToWidth(name, sizeX/2 - 32 - 58 - dotL) + "...";
			}
			mc.fontRenderer.drawString(name, guiLeft + sizeX - 82 - mc.fontRenderer.getStringWidth(name), guiTop + 48 + (i*20) + 4, getTextColor(), false);
		}
		
		mc.renderEngine.bindTexture(currentTheme().getGuiTexture());
		
		this.drawTexturedModalRect(guiLeft + sizeX - 24, this.guiTop + 48, 248, 0, 8, 20);
		int s = 20;
		while(s < (maxRows - 1) * 20)
		{
			this.drawTexturedModalRect(guiLeft + sizeX - 24, this.guiTop + 48 + s, 248, 20, 8, 20);
			s += 20;
		}
		this.drawTexturedModalRect(guiLeft + sizeX - 24, this.guiTop + 48 + s, 248, 40, 8, 20);
		this.drawTexturedModalRect(guiLeft + sizeX - 24, this.guiTop + 48 + (int)Math.max(0, s * (float)rightScroll/(memList.size() - maxRows)), 248, 60, 8, 20);
		
		mc.fontRenderer.drawString(I18n.format("betterquesting.gui.name"), guiLeft + sizeX/4 - 75, height/2 - 70, getTextColor(), false);
		
		fieldName.drawTextBox();
		
		RenderUtils.DrawLine(width/2, guiTop + 32, width/2, guiTop + sizeY - 32, 2F, getTextColor());
	}
	
	@Override
	public void actionPerformed(GuiButton button)
	{
		super.actionPerformed(button); // Finish the button functionality
		
		if(button.id == 1) // Leave party
		{
			NBTTagCompound tags = new NBTTagCompound();
			tags.setInteger("action", EnumPacketAction.KICK.ordinal());
			tags.setInteger("partyID", PartyManager.INSTANCE.getKey(party));
			tags.setString("target", mc.thePlayer.getUniqueID().toString());
			PacketSender.INSTANCE.sendToServer(new PreparedPayload(PacketTypeNative.PARTY_EDIT.GetLocation(), tags));
		} else if(button.id == 2 && status.ordinal() >= 3) // Share loot
		{
			party.setShareReward(!party.getShareReward());
			SendChanges();
		} else if(button.id == 3 && status.ordinal() >= 3) // Share life
		{
			party.setShareLives(!party.getShareLives());
			SendChanges();
		} else if(button.id == 4 && status.ordinal() >= 3) // Invite
		{
			mc.displayGuiScreen(new GuiPartyInvite(this, party));
		} else if(button.id > 4) // Kick
		{
			int n1 = button.id - 5; // Button index
			int n2 = n1/maxRows; // Column listing (0 = line)
			int n3 = n1%maxRows + rightScroll; // Format index
			
			if(n2 == 0)
			{
				if(n3 >= 0 && n3 < memList.size())
				{
					NBTTagCompound tags = new NBTTagCompound();
					tags.setInteger("action", EnumPacketAction.KICK.ordinal());
					tags.setInteger("partyID", PartyManager.INSTANCE.getKey(party));
					tags.setString("target", memList.get(n3).toString());
					PacketSender.INSTANCE.sendToServer(new PreparedPayload(PacketTypeNative.PARTY_EDIT.GetLocation(), tags));
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
		
		if(status.ordinal() >= 3)
		{
			fieldName.mouseClicked(mx, my, click);
			
			if(!fieldName.isFocused() && !fieldName.getText().equals(party.getName()))
			{
				party.setName(fieldName.getText());
				SendChanges();
			}
		} else
		{
			fieldName.setFocused(false);
		}
    }
	
	@Override
	public void mouseScroll(int mx, int my, int scroll)
	{
		super.mouseScroll(mx, my, scroll);
        
        if(scroll != 0 && isWithin(mx, my, this.guiLeft + sizeX/2, this.guiTop, sizeX/2, sizeY))
        {
    		rightScroll = Math.max(0, MathHelper.clamp_int(rightScroll + scroll, 0, memList.size() - maxRows));
    		RefreshColumns();
        }
	}
	
	public void SendChanges() // Use this if the name is being edited
	{
		if(status != EnumPartyStatus.OWNER && !NameCache.INSTANCE.isOP(mc.thePlayer.getUniqueID()))
		{
			return; // Not allowed to edit the party (Operators may force edit)
		}
		
		NBTTagCompound tags = new NBTTagCompound();
		tags.setInteger("action", EnumPacketAction.EDIT.ordinal());
		tags.setInteger("partyID", PartyManager.INSTANCE.getKey(party));
		//tags.setString("target", mc.thePlayer.getUniqueID().toString());
		JsonObject base = new JsonObject();
		base.add("party", party.writeToJson(new JsonObject(), EnumSaveType.CONFIG));
		tags.setTag("data", NBTConverter.JSONtoNBT_Object(base, new NBTTagCompound()));
		PacketSender.INSTANCE.sendToServer(new PreparedPayload(PacketTypeNative.PARTY_EDIT.GetLocation(), tags));
	}
	
	public void RefreshColumns()
	{
		rightScroll = Math.max(0, MathHelper.clamp_int(rightScroll, 0, memList.size() - maxRows));

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
				if(n3 >= 0 && n3 < memList.size())
				{
					btn.visible = true;
					btn.enabled = status.ordinal() >= 2;
					// Kick #n3 member of the party
				} else
				{
					btn.visible = btn.enabled = false;
				}
			}
		}
	}
}

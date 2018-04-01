package betterquesting.client.gui.party;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextFormatting;
import betterquesting.api.api.QuestingAPI;
import betterquesting.api.client.gui.GuiScreenThemed;
import betterquesting.api.client.gui.controls.GuiButtonThemed;
import betterquesting.api.client.gui.misc.INeedsRefresh;
import betterquesting.api.enums.EnumPacketAction;
import betterquesting.api.network.QuestingPacket;
import betterquesting.api.questing.party.IParty;
import betterquesting.api.utils.RenderUtils;
import betterquesting.core.BetterQuesting;
import betterquesting.network.PacketSender;
import betterquesting.network.PacketTypeNative;
import betterquesting.questing.party.PartyManager;
import betterquesting.storage.LifeDatabase;
import betterquesting.storage.QuestSettings;

public class GuiNoParty extends GuiScreenThemed implements INeedsRefresh
{
	ItemStack heart;
	int lives = 1;
	
	int rightScroll = 0; // Invite list
	int maxRows = 0;
	List<IParty> invites = new ArrayList<IParty>();
	GuiTextField fieldName;
	GuiButton btnCreate;
	
	public GuiNoParty(GuiScreen parent)
	{
		super(parent, "betterquesting.title.party_none");
	}
	
	@Override
	public void initGui()
	{
		super.initGui();
		
		UUID playerID = QuestingAPI.getQuestingUUID(mc.player);
		
		IParty party = PartyManager.INSTANCE.getUserParty(playerID);
		if(party != null)
		{
			mc.displayGuiScreen(new GuiManageParty(parent, party));
			return;
		}
		
		heart = new ItemStack(BetterQuesting.extraLife);
		lives = LifeDatabase.INSTANCE.getLives(playerID);
		invites.clear();
		for(int i : PartyManager.INSTANCE.getPartyInvites(playerID))
		{
			invites.add(PartyManager.INSTANCE.getValue(i));
		}
		
		rightScroll = 0;
		maxRows = (sizeY - 72)/20;
		
		btnCreate = new GuiButtonThemed(1, guiLeft + sizeX/4 - 75, height/2 + 00, 150, 20, I18n.format("betterquesting.btn.party_new"), true);
		this.buttonList.add(btnCreate);
		
		fieldName = new GuiTextField(0, mc.fontRenderer, guiLeft + sizeX/4 - 74, height/2 - 19, 148, 18);
		fieldName.setText("New Party");
		
		// Party Invites
		for(int i = 0; i < maxRows; i++)
		{
			GuiButtonThemed btn = new GuiButtonThemed(this.buttonList.size(), guiLeft + sizeX - 74, guiTop + 48 + (i*20), 50, 20, I18n.format("betterquesting.btn.party_join"), true);
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
		
		if(QuestSettings.INSTANCE.canUserEdit(mc.player))
		{
			RenderUtils.RenderItemStack(mc, heart, guiLeft + 16, guiTop + sizeY - 32, "");
			mc.fontRenderer.drawString("x " + lives, guiLeft + 36, guiTop + sizeY - 28, getTextColor());
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
		this.drawTexturedModalRect(guiLeft + sizeX - 24, this.guiTop + 48 + (int)Math.max(0, s * (float)rightScroll/(invites.size() - maxRows)), 248, 60, 8, 20);
		
		String memTitle = TextFormatting.UNDERLINE + I18n.format("betterquesting.gui.party_invites");
		mc.fontRenderer.drawString(memTitle, guiLeft + sizeX/4*3 - mc.fontRenderer.getStringWidth(memTitle)/2, guiTop + 32, getTextColor(), false);
		
		int dotL = mc.fontRenderer.getStringWidth("...");
		
		for(int i = 0; i < invites.size(); i++)
		{
			int n = i + rightScroll;
			
			IParty party = invites.get(i);
			
			if(n < 0 || n >= invites.size() || i >= maxRows)
			{
				continue;
			}
			
			String name = party.getName();
			if(mc.fontRenderer.getStringWidth(name) > sizeX/2 - 32 - 58) // Prevents overlap onto left side, especially when rendering unresolved UUIDs
			{
				name = mc.fontRenderer.trimStringToWidth(name, sizeX/2 - 32 - 58 - dotL) + "...";
			}
			mc.fontRenderer.drawString(name, guiLeft + sizeX - 82 - mc.fontRenderer.getStringWidth(name), guiTop + 48 + (i*20) + 4, getTextColor(), false);
		}
		
		mc.fontRenderer.drawString(I18n.format("betterquesting.gui.name"), guiLeft + sizeX/4 - 75, height/2 - 30, getTextColor(), false);
		
		fieldName.drawTextBox();
		
		RenderUtils.DrawLine(width/2, guiTop + 32, width/2, guiTop + sizeY - 32, 2F, getTextColor());
	}
	
	@Override
	public void actionPerformed(GuiButton button)
	{
		super.actionPerformed(button); // Finish the button functionality
		
		if(button.id == 1) // Create party
		{
			NBTTagCompound tags = new NBTTagCompound();
			tags.setInteger("action", EnumPacketAction.ADD.ordinal());
			tags.setString("name", fieldName.getText());
			PacketSender.INSTANCE.sendToServer(new QuestingPacket(PacketTypeNative.PARTY_EDIT.GetLocation(), tags));
		} else if(button.id > 1) // Join party
		{
			int n1 = button.id - 2; // Button index
			int n2 = n1/maxRows; // Column listing (0 = line)
			int n3 = n1%maxRows + rightScroll; // Party index
			
			if(n2 == 0)
			{
				if(n3 >= 0 && n3 < invites.size())
				{
					NBTTagCompound tags = new NBTTagCompound();
					tags.setInteger("action", EnumPacketAction.JOIN.ordinal());
					tags.setInteger("partyID", PartyManager.INSTANCE.getKey(invites.get(n3)));
					PacketSender.INSTANCE.sendToServer(new QuestingPacket(PacketTypeNative.PARTY_EDIT.GetLocation(), tags));
				}
			}
		}
	}

    /**
     * Fired when a key is typed. This is the equivalent of KeyListener.keyTyped(KeyEvent e).
     */
	@Override
    protected void keyTyped(char character, int keyCode) throws IOException
    {
        super.keyTyped(character, keyCode);
        
        fieldName.textboxKeyTyped(character, keyCode);
        
        btnCreate.enabled = fieldName.getText().length() >= 0;
    }
	
    /**
     * Called when the mouse is clicked.
     */
	@Override
    protected void mouseClicked(int mx, int my, int click) throws IOException
    {
		super.mouseClicked(mx, my, click);
		
		fieldName.mouseClicked(mx, my, click);
    }
	
	@Override
	public void mouseScroll(int mx, int my, int scroll)
	{
		super.mouseScroll(mx, my, scroll);
        
        if(scroll != 0 && isWithin(mx, my, this.guiLeft + sizeX/2, this.guiTop, sizeX/2, sizeY))
        {
    		rightScroll = Math.max(0, MathHelper.clamp(rightScroll + scroll, 0, invites.size() - maxRows));
    		RefreshColumns();
        }
	}
	
	public void RefreshColumns()
	{
		rightScroll = Math.max(0, MathHelper.clamp(rightScroll, 0, invites.size() - maxRows));

		List<GuiButton> btnList = this.buttonList;
		
		for(int i = 2; i < btnList.size(); i++)
		{
			GuiButton btn = btnList.get(i);
			int n1 = btn.id - 2; // Button index
			int n2 = n1/maxRows; // Column listing (0 = line)
			int n3 = n1%maxRows + rightScroll; // Format index
			
			if(n2 == 0)
			{
				if(n3 >= 0 && n3 < invites.size())
				{
					btn.visible = btn.enabled = true;
				} else
				{
					btn.visible = btn.enabled = false;
				}
			}
		}
	}
}

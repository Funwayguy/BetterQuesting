package betterquesting.client.gui.party;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MathHelper;
import org.lwjgl.input.Mouse;
import betterquesting.client.gui.GuiQuesting;
import betterquesting.client.gui.misc.GuiButtonQuesting;
import betterquesting.client.themes.ThemeRegistry;
import betterquesting.core.BetterQuesting;
import betterquesting.lives.LifeManager;
import betterquesting.network.PacketAssembly;
import betterquesting.network.PacketTypeRegistry.BQPacketType;
import betterquesting.party.PartyInstance;
import betterquesting.party.PartyManager;
import betterquesting.quests.QuestDatabase;
import betterquesting.utils.RenderUtils;

public class GuiNoParty extends GuiQuesting
{
	ItemStack heart;
	int lives = 1;
	
	int rightScroll = 0; // Invite list
	int maxRows = 0;
	ArrayList<PartyInstance> invites = new ArrayList<PartyInstance>();
	GuiTextField fieldName;
	GuiButton btnCreate;
	
	public GuiNoParty(GuiScreen parent)
	{
		super(parent, "betterquesting.title.party_none");
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public void initGui()
	{
		super.initGui();
		
		PartyInstance party = PartyManager.GetParty(mc.thePlayer.getUniqueID());
		if(party != null)
		{
			mc.displayGuiScreen(new GuiManageParty(parent, party));
			return;
		}
		
		heart = new ItemStack(BetterQuesting.extraLife);
		lives = LifeManager.getLives(mc.thePlayer);
		
		invites = PartyManager.getInvites(mc.thePlayer.getUniqueID());
		
		rightScroll = 0;
		maxRows = (sizeY - 72)/20;
		
		btnCreate = new GuiButtonQuesting(1, guiLeft + sizeX/4 - 75, height/2 + 00, 150, 20, I18n.format("betterquesting.btn.party_new"));
		this.buttonList.add(btnCreate);
		
		fieldName = new GuiTextField(mc.fontRenderer, guiLeft + sizeX/4 - 74, height/2 - 19, 148, 18);
		fieldName.setText("New Party");
		
		// Party Invites
		for(int i = 0; i < maxRows; i++)
		{
			GuiButtonQuesting btn = new GuiButtonQuesting(this.buttonList.size(), guiLeft + sizeX - 74, guiTop + 48 + (i*20), 50, 20, I18n.format("betterquesting.btn.party_join"));
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
		
		if(QuestDatabase.bqHardcore)
		{
			RenderUtils.RenderItemStack(mc, heart, guiLeft + 16, guiTop + sizeY - 32, "");
			mc.fontRenderer.drawString("x " + lives, guiLeft + 36, guiTop + sizeY - 28, ThemeRegistry.curTheme().textColor().getRGB());
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
		this.drawTexturedModalRect(guiLeft + sizeX - 24, this.guiTop + 48 + (int)Math.max(0, s * (float)rightScroll/(invites.size() - maxRows)), 248, 60, 8, 20);
		
		String memTitle = EnumChatFormatting.UNDERLINE + I18n.format("betterquesting.gui.party_invites");
		mc.fontRenderer.drawString(memTitle, guiLeft + sizeX/4*3 - mc.fontRenderer.getStringWidth(memTitle)/2, guiTop + 32, ThemeRegistry.curTheme().textColor().getRGB(), false);
		
		int dotL = mc.fontRenderer.getStringWidth("...");
		
		for(int i = 0; i < invites.size(); i++)
		{
			int n = i + rightScroll;
			
			PartyInstance party = invites.get(i);
			
			if(n < 0 || n >= invites.size() || i >= maxRows)
			{
				continue;
			}
			
			String name = party.name;
			if(mc.fontRenderer.getStringWidth(name) > sizeX/2 - 32 - 58) // Prevents overlap onto left side, especially when rendering unresolved UUIDs
			{
				name = mc.fontRenderer.trimStringToWidth(name, sizeX/2 - 32 - 58 - dotL) + "...";
			}
			mc.fontRenderer.drawString(name, guiLeft + sizeX - 82 - mc.fontRenderer.getStringWidth(name), guiTop + 48 + (i*20) + 4, ThemeRegistry.curTheme().textColor().getRGB(), false);
		}
		
		mc.fontRenderer.drawString(I18n.format("betterquesting.gui.name"), guiLeft + sizeX/4 - 75, height/2 - 30, ThemeRegistry.curTheme().textColor().getRGB(), false);
		
		fieldName.drawTextBox();
		
		RenderUtils.DrawLine(width/2, guiTop + 32, width/2, guiTop + sizeY - 32, 2F, ThemeRegistry.curTheme().textColor());
	}
	
	@Override
	public void actionPerformed(GuiButton button)
	{
		super.actionPerformed(button); // Finish the button functionality
		
		if(button.id == 1) // Create party
		{
			NBTTagCompound tags = new NBTTagCompound();
			tags.setInteger("action", 0);
			tags.setString("Party", fieldName.getText());
			PacketAssembly.SendToServer(BQPacketType.PARTY_ACTION.GetLocation(), tags);
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
					tags.setInteger("action", 3);
					tags.setString("Party", invites.get(n3).name);
					PacketAssembly.SendToServer(BQPacketType.PARTY_ACTION.GetLocation(), tags);
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
        
        btnCreate.enabled = PartyManager.GetPartyByName(fieldName.getText()) == null && fieldName.getText().length() >= 0;
    }
	
    /**
     * Called when the mouse is clicked.
     */
	@Override
    protected void mouseClicked(int mx, int my, int click)
    {
		super.mouseClicked(mx, my, click);
		
		fieldName.mouseClicked(mx, my, click);
    }
	
	@Override
	public void handleMouseInput()
	{
		super.handleMouseInput();
		
        int mx = Mouse.getEventX() * this.width / this.mc.displayWidth;
        int my = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;
        int SDX = (int)-Math.signum(Mouse.getEventDWheel());
        
        if(SDX != 0 && isWithin(mx, my, this.guiLeft + sizeX/2, this.guiTop, sizeX/2, sizeY))
        {
    		rightScroll = Math.max(0, MathHelper.clamp_int(rightScroll + SDX, 0, invites.size() - maxRows));
    		RefreshColumns();
        }
	}
	
	public void RefreshColumns()
	{
		rightScroll = Math.max(0, MathHelper.clamp_int(rightScroll, 0, invites.size() - maxRows));

		@SuppressWarnings("unchecked")
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

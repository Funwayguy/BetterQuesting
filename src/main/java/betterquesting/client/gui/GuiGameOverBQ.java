package betterquesting.client.gui;

import java.util.Iterator;
import java.util.UUID;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiGameOver;
import net.minecraft.client.gui.GuiLabel;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiYesNo;
import net.minecraft.client.gui.GuiYesNoCallback;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import betterquesting.api.api.QuestingAPI;
import betterquesting.api.properties.NativeProps;
import betterquesting.api.questing.party.IParty;
import betterquesting.questing.party.PartyManager;
import betterquesting.storage.LifeDatabase;
import betterquesting.storage.QuestSettings;

@SideOnly(Side.CLIENT)
public class GuiGameOverBQ extends GuiGameOver implements GuiYesNoCallback
{
	ITextComponent causeOfDeath;
	int lifeCache = -1;
    private int cooldown;
    
	public GuiGameOverBQ(ITextComponent causeOfDeath)
	{
		super(causeOfDeath);
		this.causeOfDeath = causeOfDeath;
	}
	
    /**
     * Adds the buttons (and other controls) to the screen in question.
     */
    public void initGui()
    {
    	UUID playerID = QuestingAPI.getQuestingUUID(mc.player);
    	
    	IParty party = PartyManager.INSTANCE.getUserParty(playerID);
    	
    	if(party == null || !party.getProperties().getProperty(NativeProps.PARTY_LIVES))
    	{
    		lifeCache = LifeDatabase.INSTANCE.getLives(playerID);
    	} else
    	{
    		lifeCache = LifeDatabase.INSTANCE.getLives(party);
    	}
    	
    	this.buttonList.clear();

        if (this.mc.world.getWorldInfo().isHardcoreModeEnabled() || (QuestSettings.INSTANCE.getProperty(NativeProps.HARDCORE) && lifeCache <= 0))
        {
            this.buttonList.add(new GuiButton(0, this.width / 2 - 100, this.height / 4 + 72, I18n.format("deathScreen.spectate")));
            this.buttonList.add(new GuiButton(1, this.width / 2 - 100, this.height / 4 + 96, I18n.format("deathScreen." + (this.mc.isIntegratedServerRunning() ? "deleteWorld" : "leaveServer"))));
        }
        else
        {
            this.buttonList.add(new GuiButton(0, this.width / 2 - 100, this.height / 4 + 72, I18n.format("deathScreen.respawn")));
            this.buttonList.add(new GuiButton(1, this.width / 2 - 100, this.height / 4 + 96, I18n.format("deathScreen.titleScreen")));

            if (this.mc.getSession() == null)
            {
                ((GuiButton)this.buttonList.get(1)).enabled = false;
            }
        }
        
        for (GuiButton guibutton : this.buttonList)
        {
            guibutton.enabled = false;
        }
    }

    /**
     * Fired when a key is typed. This is the equivalent of KeyListener.keyTyped(KeyEvent e).
     */
    protected void keyTyped(char r, int num) {}

    protected void actionPerformed(GuiButton button)
    {
        switch (button.id)
        {
            case 0:
                this.mc.player.respawnPlayer();
                this.mc.displayGuiScreen((GuiScreen)null);
                break;
            case 1:

                if (this.mc.world.getWorldInfo().isHardcoreModeEnabled())
                {
                    this.mc.displayGuiScreen(new GuiMainMenu());
                }
                else
                {
                    GuiYesNo guiyesno = new GuiYesNo(this, I18n.format("deathScreen.quit.confirm"), "", I18n.format("deathScreen.titleScreen"), I18n.format("deathScreen.respawn"), 0);
                    this.mc.displayGuiScreen(guiyesno);
                    guiyesno.setButtonDelay(20);
                }
        }
    }

    public void confirmClicked(boolean p_73878_1_, int p_73878_2_)
    {
        if (p_73878_1_)
        {
            this.mc.world.sendQuittingDisconnectingPacket();
        	// Send a custom BetterQuesting packet
            this.mc.loadWorld((WorldClient)null);
            this.mc.displayGuiScreen(new GuiMainMenu());
        }
        else
        {
            this.mc.player.respawnPlayer();
            this.mc.displayGuiScreen((GuiScreen)null);
        }
    }

    /**
     * Draws the screen and all the components in it.
     */
    public void drawScreen(int mx, int my, float partialTick)
    {
        this.drawGradientRect(0, 0, this.width, this.height, 1615855616, -1602211792);
        GlStateManager.pushMatrix();
        GlStateManager.scale(2.0F, 2.0F, 2.0F);
        boolean flag = this.mc.world.getWorldInfo().isHardcoreModeEnabled() || (QuestSettings.INSTANCE.getProperty(NativeProps.HARDCORE) && lifeCache <= 0);
        String s = flag ? I18n.format("deathScreen.title.hardcore") : I18n.format("deathScreen.title");
        this.drawCenteredString(this.fontRendererObj, s, this.width / 2 / 2, 30, 16777215);
        GlStateManager.popMatrix();

        if (flag)
        {
            this.drawCenteredString(this.fontRendererObj, I18n.format("deathScreen.hardcoreInfo"), this.width / 2, 144, 16777215);
        }

        this.drawCenteredString(this.fontRendererObj, I18n.format("deathScreen.score") + ": " + TextFormatting.YELLOW + this.mc.player.getScore(), this.width / 2, 100, 16777215);
        
        if(QuestSettings.INSTANCE.getProperty(NativeProps.HARDCORE))
        {
        	this.drawCenteredString(this.fontRendererObj, I18n.format("betterquesting.gui.remaining_lives", TextFormatting.YELLOW + "" + lifeCache), this.width / 2, 112, 16777215);
        }
        
        if (this.causeOfDeath != null && my > 85 && my < 85 + this.fontRendererObj.FONT_HEIGHT)
        {
            ITextComponent itextcomponent = this.getClickedComponentAt(mx);

            if (itextcomponent != null && itextcomponent.getStyle().getHoverEvent() != null)
            {
                this.handleComponentHover(itextcomponent, mx, my);
            }
        }

        int k;

        for (k = 0; k < this.buttonList.size(); ++k)
        {
            ((GuiButton)this.buttonList.get(k)).drawButton(this.mc, mx, my);
        }

        for (k = 0; k < this.labelList.size(); ++k)
        {
            ((GuiLabel)this.labelList.get(k)).drawLabel(this.mc, mx, my);
        }
    }

    /**
     * Returns true if this GUI should pause the game when it is displayed in single-player
     */
    public boolean doesGuiPauseGame()
    {
        return false;
    }

    /**
     * Called from the main game loop to update the screen.
     */
    public void updateScreen()
    {
        super.updateScreen();
        ++this.cooldown;
        GuiButton guibutton;

        if (this.cooldown >= 20)
        {
            for (Iterator<GuiButton> iterator = this.buttonList.iterator(); iterator.hasNext(); guibutton.enabled = true)
            {
                guibutton = iterator.next();
            }
        }
    }
}
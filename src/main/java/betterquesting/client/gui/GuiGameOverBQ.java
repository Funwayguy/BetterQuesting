package betterquesting.client.gui;

import java.util.Iterator;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiGameOver;
import net.minecraft.client.gui.GuiLabel;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiYesNo;
import net.minecraft.client.gui.GuiYesNoCallback;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;
import betterquesting.lives.LifeManager;
import betterquesting.quests.QuestDatabase;

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
    	lifeCache = LifeManager.getLives(mc.thePlayer);
        this.buttonList.clear();

        if (this.mc.theWorld.getWorldInfo().isHardcoreModeEnabled() || (QuestDatabase.bqHardcore && LifeManager.getLives(mc.thePlayer) <= 0))
        {
            this.buttonList.add(new GuiButton(0, this.width / 2 - 100, this.height / 4 + 72, I18n.translateToLocal("deathScreen.spectate")));
            this.buttonList.add(new GuiButton(1, this.width / 2 - 100, this.height / 4 + 96, I18n.translateToLocal("deathScreen." + (this.mc.isIntegratedServerRunning() ? "deleteWorld" : "leaveServer"))));
        }
        else
        {
            this.buttonList.add(new GuiButton(0, this.width / 2 - 100, this.height / 4 + 72, I18n.translateToLocal("deathScreen.respawn")));
            this.buttonList.add(new GuiButton(1, this.width / 2 - 100, this.height / 4 + 96, I18n.translateToLocal("deathScreen.titleScreen")));

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
                this.mc.thePlayer.respawnPlayer();
                this.mc.displayGuiScreen((GuiScreen)null);
                break;
            case 1:

                if (this.mc.theWorld.getWorldInfo().isHardcoreModeEnabled())
                {
                    this.mc.displayGuiScreen(new GuiMainMenu());
                }
                else
                {
                    GuiYesNo guiyesno = new GuiYesNo(this, I18n.translateToLocal("deathScreen.quit.confirm"), "", I18n.translateToLocal("deathScreen.titleScreen"), I18n.translateToLocal("deathScreen.respawn"), 0);
                    this.mc.displayGuiScreen(guiyesno);
                    guiyesno.setButtonDelay(20);
                }
        }
    }

    public void confirmClicked(boolean result, int id)
    {
        if (result)
        {
            if (this.mc.theWorld != null)
            {
                this.mc.theWorld.sendQuittingDisconnectingPacket();
            }
            
            this.mc.loadWorld((WorldClient)null);
            this.mc.displayGuiScreen(new GuiMainMenu());
        }
        else
        {
            this.mc.thePlayer.respawnPlayer();
            this.mc.displayGuiScreen((GuiScreen)null);
        }
    }

    /**
     * Draws the screen and all the components in it.
     */
    public void drawScreen(int mx, int my, float partialTick)
    {
        this.drawGradientRect(0, 0, this.width, this.height, 1615855616, -1602211792);
        GL11.glPushMatrix();
        GL11.glScalef(2.0F, 2.0F, 2.0F);
        boolean flag = this.mc.theWorld.getWorldInfo().isHardcoreModeEnabled() || (QuestDatabase.bqHardcore && LifeManager.getLives(mc.thePlayer) <= 0);
        String s = flag ? I18n.translateToLocal("deathScreen.title.hardcore") : I18n.translateToLocal("deathScreen.title");
        this.drawCenteredString(this.fontRendererObj, s, this.width / 2 / 2, 30, 16777215);
        GL11.glPopMatrix();

        if (flag)
        {
            this.drawCenteredString(this.fontRendererObj, I18n.translateToLocal("deathScreen.hardcoreInfo"), this.width / 2, 144, 16777215);
        }

        this.drawCenteredString(this.fontRendererObj, I18n.translateToLocal("deathScreen.score") + ": " + TextFormatting.YELLOW + this.mc.thePlayer.getScore(), this.width / 2, 100, 16777215);
        
        if(QuestDatabase.bqHardcore)
        {
        	this.drawCenteredString(this.fontRendererObj, I18n.translateToLocalFormatted("betterquesting.gui.remaining_lives", TextFormatting.YELLOW + "" + LifeManager.getLives(mc.thePlayer)), this.width / 2, 112, 16777215);
        }
        
        if (this.causeOfDeath != null && my > 85 && my < 85 + this.fontRendererObj.FONT_HEIGHT)
        {
            ITextComponent itextcomponent = this.func_184870_b(mx);

            if (itextcomponent != null && itextcomponent.getChatStyle().getChatHoverEvent() != null)
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
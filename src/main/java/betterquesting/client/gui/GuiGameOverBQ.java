package betterquesting.client.gui;

import betterquesting.lives.LifeManager;
import betterquesting.quests.QuestDatabase;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.Iterator;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiGameOver;
import net.minecraft.client.gui.GuiLabel;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiYesNo;
import net.minecraft.client.gui.GuiYesNoCallback;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.EnumChatFormatting;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class GuiGameOverBQ extends GuiGameOver implements GuiYesNoCallback
{
	int lifeCache = -1;
    private int cooldown;
    /**
     * Adds the buttons (and other controls) to the screen in question.
     */
    @SuppressWarnings("unchecked")
	public void initGui()
    {
    	lifeCache = LifeManager.getLives(mc.thePlayer);
        this.buttonList.clear();

        if (this.mc.theWorld.getWorldInfo().isHardcoreModeEnabled() || (QuestDatabase.bqHardcore && LifeManager.getLives(mc.thePlayer) <= 0))
        {
        	int i = this.mc.theWorld.getWorldInfo().isHardcoreModeEnabled()? 1 : 0;
            if (this.mc.isIntegratedServerRunning())
            {
                this.buttonList.add(new GuiButton(i, this.width / 2 - 100, this.height / 4 + 96, I18n.format("deathScreen.deleteWorld", new Object[0])));
            }
            else
            {
                this.buttonList.add(new GuiButton(i, this.width / 2 - 100, this.height / 4 + 96, I18n.format("deathScreen.leaveServer", new Object[0])));
            }
        }
        else
        {
            this.buttonList.add(new GuiButton(0, this.width / 2 - 100, this.height / 4 + 72, I18n.format("deathScreen.respawn", new Object[0])));
            this.buttonList.add(new GuiButton(1, this.width / 2 - 100, this.height / 4 + 96, I18n.format("deathScreen.titleScreen", new Object[0])));

            if (this.mc.getSession() == null)
            {
                ((GuiButton)this.buttonList.get(1)).enabled = false;
            }
        }

        GuiButton guibutton;

        for (Iterator<GuiButton> iterator = this.buttonList.iterator(); iterator.hasNext(); guibutton.enabled = false)
        {
            guibutton = iterator.next();
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
                GuiYesNo guiyesno = new GuiYesNo(this, I18n.format("deathScreen.quit.confirm", new Object[0]), "", I18n.format("deathScreen.titleScreen", new Object[0]), I18n.format("deathScreen.respawn", new Object[0]), 0);
                this.mc.displayGuiScreen(guiyesno);
                guiyesno.func_146350_a(20);
        }
    }

    public void confirmClicked(boolean p_73878_1_, int p_73878_2_)
    {
        if (p_73878_1_)
        {
            this.mc.theWorld.sendQuittingDisconnectingPacket();
        	// Send a custom BetterQuesting packet
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
        String s = flag ? I18n.format("deathScreen.title.hardcore", new Object[0]) : I18n.format("deathScreen.title", new Object[0]);
        this.drawCenteredString(this.fontRendererObj, s, this.width / 2 / 2, 30, 16777215);
        GL11.glPopMatrix();

        if (flag)
        {
            this.drawCenteredString(this.fontRendererObj, I18n.format("deathScreen.hardcoreInfo", new Object[0]), this.width / 2, 144, 16777215);
        }

        this.drawCenteredString(this.fontRendererObj, I18n.format("deathScreen.score", new Object[0]) + ": " + EnumChatFormatting.YELLOW + this.mc.thePlayer.getScore(), this.width / 2, 100, 16777215);
        
        if(QuestDatabase.bqHardcore)
        {
        	this.drawCenteredString(this.fontRendererObj, I18n.format("betterquesting.gui.remaining_lives", EnumChatFormatting.YELLOW + "" + LifeManager.getLives(mc.thePlayer)), this.width / 2, 112, 16777215);
        }

        int k;

        for (k = 0; k < this.buttonList.size(); ++k)
        {
            ((GuiButton)this.buttonList.get(k)).drawButton(this.mc, mx, my);
        }

        for (k = 0; k < this.labelList.size(); ++k)
        {
            ((GuiLabel)this.labelList.get(k)).func_146159_a(this.mc, mx, my);
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

        if (this.cooldown == 20)
        {
            for (@SuppressWarnings("unchecked")
			Iterator<GuiButton> iterator = this.buttonList.iterator(); iterator.hasNext(); guibutton.enabled = true)
            {
                guibutton = iterator.next();
            }
        }
    }
}
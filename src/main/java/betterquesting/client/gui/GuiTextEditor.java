package betterquesting.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ChatAllowedCharacters;
import net.minecraft.util.EnumChatFormatting;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;
import betterquesting.client.gui.misc.ITextEditor;
import betterquesting.client.themes.ThemeRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiTextEditor extends GuiQuesting
{
	int hostID;
	ITextEditor host;
	public String text = "";
	
    public GuiTextEditor(GuiScreen parent, String text)
    {
    	super(parent, "Text Editor");
    	
    	this.text = text;
    }
    
    public GuiTextEditor setHost(ITextEditor host, int hostID)
    {
    	this.host = host;
    	this.hostID = hostID;
    	return this;
    }

    /**
     * Adds the buttons (and other controls) to the screen in question.
     */
    public void initGui()
    {
    	super.initGui();
    	
        Keyboard.enableRepeatEvents(true);
    }

    /**
     * Called when the screen is unloaded. Used to disable keyboard repeat events
     */
    public void onGuiClosed()
    {
        Keyboard.enableRepeatEvents(false);
        
        if(host != null)
        {
        	host.setText(0, text);
        }
    }

    /**
     * Fired when a key is typed. This is the equivalent of KeyListener.keyTyped(KeyEvent e).
     */
    protected void keyTyped(char c, int num)
    {
        super.keyTyped(c, num);
        
        switch (c)
        {
            case 22: // Paste
                this.writeText(GuiScreen.getClipboardString());
                return;
            default:
                switch (num)
                {
                    case 14: // Backspace
                        if (text.length() > 0)
                        {
                            text = text.substring(0, text.length() - 1);
                        }

                        return;
                    case 28:
                    case 156: // New line
                        this.writeText("\n");
                        return;
                    default:
                        if (ChatAllowedCharacters.isAllowedCharacter(c))
                        {
                            this.writeText(Character.toString(c));
                        }
                }
        }
    }

    private void writeText(String string)
    {
        String s1 = text;
        String s2 = s1 + string;
        int i = this.fontRendererObj.splitStringWidth(s2 + "" + EnumChatFormatting.BLACK + "_", sizeX - 32);

        if (i <= sizeX - 32)
        {
            text = s2;
        }
    }

    /**
     * Draws the screen and all the components in it.
     */
    public void drawScreen(int mx, int my, float partialTick)
    {
        super.drawScreen(mx, my, partialTick);
        
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        
        String s1 = text;

        if (this.fontRendererObj.getBidiFlag())
        {
            s1 = s1 + "_";
        }
        else if((Minecraft.getSystemTime()/500)%2 == 0)
        {
            s1 = s1 + "" + EnumChatFormatting.BLACK + "_";
        }
        else
        {
            s1 = s1 + "" + EnumChatFormatting.WHITE + "_";
        }
        
        this.fontRendererObj.drawSplitString(s1, this.guiLeft + 16, this.guiTop + 32, this.sizeX - 32, ThemeRegistry.curTheme().textColor().getRGB());
    }
}
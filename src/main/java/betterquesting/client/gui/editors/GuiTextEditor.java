package betterquesting.client.gui.editors;

import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ChatAllowedCharacters;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MathHelper;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import betterquesting.client.gui.GuiQuesting;
import betterquesting.client.gui.misc.GuiButtonQuesting;
import betterquesting.client.gui.misc.GuiScrollingText;
import betterquesting.client.gui.misc.ITextEditor;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiTextEditor extends GuiQuesting
{
	int hostID;
	ITextEditor host;
	public String text = "";
	int listScroll = 0;
	int maxRows = 0;
	GuiScrollingText scrollingText;
	
    public GuiTextEditor(GuiScreen parent, String text)
    {
    	super(parent, "betterquesting.title.edit_text");
    	
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
    @SuppressWarnings("unchecked")
	public void initGui()
    {
    	super.initGui();
    	
        Keyboard.enableRepeatEvents(true);

		maxRows = (sizeY - 48)/20;
		
		for(int i = 0; i < maxRows; i++)
		{
			GuiButtonQuesting btn = new GuiButtonQuesting(i + 1, guiLeft + 16, guiTop + 32 + (i*20), 100, 20, "NULL");
			this.buttonList.add(btn);
		}
		
		scrollingText = new GuiScrollingText(this, sizeX - 148, sizeY - 64, guiTop + 32, guiLeft + 132);
		scrollingText.SetText(text);
		
		RefreshColumns();
    }
    
    @Override
    public void actionPerformed(GuiButton btn)
    {
    	super.actionPerformed(btn);
    	
    	if(btn.id > 0)
    	{
			int n1 = btn.id - 1; // Line index
			int n2 = n1/maxRows; // Line listing (0 = line, 1 = delete)
			int n3 = n1%maxRows + listScroll; // Quest list index
			
			if(n2 == 0)
			{
				if(n3 >= 0 && n3 < EnumChatFormatting.values().length)
				{
					String tmp = EnumChatFormatting.values()[n3].toString();
					writeText(tmp);
				}
			}
    	}
    }

    /**
     * Called when the screen is unloaded. Used to disable keyboard repeat events
     */
    public void onGuiClosed()
    {
        Keyboard.enableRepeatEvents(false);
        
        if(host != null)
        {
        	host.setText(hostID, text);
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
                        scrollingText.SetText(text + "_");
                        scrollingText.SetScroll(1F);
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
        text = s2;
        scrollingText.SetText(text + "_");
        scrollingText.SetScroll(1F);
    }

    /**
     * Draws the screen and all the components in it.
     */
    public void drawScreen(int mx, int my, float partialTick)
    {
        super.drawScreen(mx, my, partialTick);
        
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        
		this.drawTexturedModalRect(guiLeft + 116, this.guiTop + 32, 248, 0, 8, 20);
		int s = 20;
		while(s < (maxRows - 1) * 20)
		{
			this.drawTexturedModalRect(guiLeft + 116, this.guiTop + 32 + s, 248, 20, 8, 20);
			s += 20;
		}
		this.drawTexturedModalRect(guiLeft + 116, this.guiTop + 32 + s, 248, 40, 8, 20);
		this.drawTexturedModalRect(guiLeft + 116, this.guiTop + 32 + (int)Math.max(0, s * (float)listScroll/(EnumChatFormatting.values().length - maxRows)), 248, 60, 8, 20);
        
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
        
        scrollingText.SetText(s1);
        scrollingText.drawScreen(mx, my, partialTick);
        //RenderUtils.drawSplitString(fontRendererObj, s1, this.guiLeft + 132, this.guiTop + 32, this.sizeX - 140, ThemeRegistry.curTheme().textColor().getRGB(), false);
    }
	
    /**
     * Handles mouse input.
     */
	@Override
    public void handleMouseInput()
    {
		super.handleMouseInput();
		
        int mx = Mouse.getEventX() * this.width / this.mc.displayWidth;
        int my = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;
        int SDX = (int)-Math.signum(Mouse.getEventDWheel());
        
        if(SDX != 0 && isWithin(mx, my, this.guiLeft, this.guiTop, 116, sizeY))
        {
        	listScroll = Math.max(0, MathHelper.clamp_int(listScroll + SDX, 0, EnumChatFormatting.values().length - maxRows));
    		RefreshColumns();
        }
    }
	
	public void RefreshColumns()
	{
		listScroll = Math.max(0, MathHelper.clamp_int(listScroll, 0, EnumChatFormatting.values().length - maxRows));

		@SuppressWarnings("unchecked")
		List<GuiButton> btnList = this.buttonList;
		
		for(int i = 1; i < btnList.size(); i++)
		{
			GuiButton btn = btnList.get(i);
			int n1 = btn.id - 1; // Button index
			int n2 = n1/maxRows; // Column listing (0 = line)
			int n3 = n1%maxRows + listScroll; // Format index
			
			if(n2 == 0)
			{
				if(n3 >= 0 && n3 < EnumChatFormatting.values().length)
				{
					btn.displayString = EnumChatFormatting.values()[n3].getFriendlyName();
					btn.enabled = btn.visible = true;
				} else
				{
					btn.displayString = "NULL";
					btn.enabled = btn.visible = false;
				}
			}
		}
	}
}
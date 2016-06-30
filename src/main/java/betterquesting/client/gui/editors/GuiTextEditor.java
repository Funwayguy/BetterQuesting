package betterquesting.client.gui.editors;

import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ChatAllowedCharacters;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MathHelper;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import betterquesting.client.gui.GuiQuesting;
import betterquesting.client.gui.misc.GuiButtonQuesting;
import betterquesting.client.gui.misc.GuiScrollingText;
import betterquesting.client.gui.misc.ITextEditor;
import betterquesting.client.gui.misc.IVolatileScreen;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiTextEditor extends GuiQuesting implements IVolatileScreen
{
	int hostID;
	ITextEditor host;
	public String text = "";
	int listScroll = 0;
	int maxRows = 0;
	GuiScrollingText scrollingText;
	
    private int cursorPosition;
	
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

		maxRows = (sizeY - 48)/20;
		
		for(int i = 0; i < maxRows; i++)
		{
			GuiButtonQuesting btn = new GuiButtonQuesting(i + 1, guiLeft + 16, guiTop + 32 + (i*20), 100, 20, "NULL");
			this.buttonList.add(btn);
		}
		
		scrollingText = new GuiScrollingText(this, sizeX - 148, sizeY - 64, guiTop + 32, guiLeft + 132);
		scrollingText.SetText(text);
    	cursorPosition = text.length();
		
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
    
    @Override
    public void onGuiClosed()
    {
        super.onGuiClosed();
        
        if(host != null)
        {
        	host.setText(hostID, text);
        }
    }

    /**
     * Moves the text cursor by a specified number of characters and clears the selection
     */
    public void moveCursorBy(int p_146182_1_)
    {
        this.setCursorPosition(this.cursorPosition + p_146182_1_);
    }

    /**
     * sets the position of the cursor to the provided index
     */
    public void setCursorPosition(int p_146190_1_)
    {
        this.cursorPosition = p_146190_1_;
        int j = this.text.length();

        if (this.cursorPosition < 0)
        {
            this.cursorPosition = 0;
        }

        if (this.cursorPosition > j)
        {
            this.cursorPosition = j;
        }

        this.setSelectionPos(this.cursorPosition);
    }

    /**
     * sets the cursors position to the beginning
     */
    public void setCursorPositionZero()
    {
        this.setCursorPosition(0);
    }

    /**
     * sets the cursors position to after the text
     */
    public void setCursorPositionEnd()
    {
        this.setCursorPosition(this.text.length());
    }

    /**
     * returns the current position of the cursor
     */
    public int getCursorPosition()
    {
        return this.cursorPosition;
    }

    /**
     * Sets the position of the selection anchor (i.e. position the selection was started at)
     */
    public void setSelectionPos(int p_146199_1_)
    {
        int j = this.text.length();

        if (p_146199_1_ > j)
        {
            p_146199_1_ = j;
        }

        if (p_146199_1_ < 0)
        {
            p_146199_1_ = 0;
        }
    }

    /**
     * delete the selected text, otherwsie deletes characters from either side of the cursor. params: delete num
     */
    public void deleteFromCursor(int p_146175_1_)
    {
        if (this.text.length() != 0)
        {
            boolean flag = p_146175_1_ < 0;
            int j = flag ? this.cursorPosition + p_146175_1_ : this.cursorPosition;
            int k = flag ? this.cursorPosition : this.cursorPosition + p_146175_1_;
            String s = "";

            if (j >= 0)
            {
                s = this.text.substring(0, j);
            }

            if (k < this.text.length())
            {
                s = s + this.text.substring(k);
            }

            this.text = s;

            if (flag)
            {
                this.moveCursorBy(p_146175_1_);
            }
        }
    }
    
    @Override
    public void keyTyped(char p_146201_1_, int p_146201_2_)
    {
            switch (p_146201_1_)
            {
                case 1:
                    this.setCursorPositionEnd();
                    this.setSelectionPos(0);
                    return;
                case 22:
                    this.writeText(GuiScreen.getClipboardString());
                    return;
                default:
                    switch (p_146201_2_)
                    {
                        case 14:
                            this.deleteFromCursor(-1);
                            return;
                        case 28:
                        case 156:
                            this.writeText("\n");
                            return;
                        case 199:
                            if (GuiScreen.isShiftKeyDown())
                            {
                                this.setSelectionPos(0);
                            }
                            else
                            {
                                this.setCursorPositionZero();
                            }

                            return;
                        case 203:
                            this.moveCursorBy(-1);
                            return;
                        case 205:
                           this.moveCursorBy(1);
                            return;
                        case 207:
                            if (GuiScreen.isShiftKeyDown())
                            {
                                this.setSelectionPos(this.text.length());
                            }
                            else
                            {
                                this.setCursorPositionEnd();
                            }

                            return;
                        case 211:
                            this.deleteFromCursor(1);
                            return;
                        default:
                            this.writeText(ChatAllowedCharacters.filerAllowedCharacters(Character.toString(p_146201_1_)));
                            return;
                    }
            }
    }
    
    public void writeText(String raw)
    {
        String s1 = "";
        String s2 = raw;
        int i = this.cursorPosition;

        if (this.text.length() > 0)
        {
            s1 = s1 + this.text.substring(0, i);
        }

        int l;
        s1 = s1 + s2;
        l = s2.length();

        if (this.text.length() > 0 && i < this.text.length())
        {
            s1 = s1 + this.text.substring(i);
        }

        this.text = s1;
        this.moveCursorBy(l);
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
        
        String s1 = text.substring(0, cursorPosition);
        String s2 = text.substring(cursorPosition);

        if (this.fontRendererObj.getBidiFlag())
        {
            s1 = s1 + "_";
        }
        else if((Minecraft.getSystemTime()/500)%2 == 0)
        {
        	s1.substring(0, cursorPosition);
            s1 = s1 + "_";
        }
        else
        {
            s1 = s1 + " ";
        }
        
        scrollingText.SetText(s1 + s2);
        scrollingText.drawScreen(mx, my, partialTick);
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
package betterquesting.api2.client.gui.controls;

import betterquesting.api.utils.RenderUtils;
import betterquesting.api2.client.gui.misc.IGuiRect;
import betterquesting.api2.client.gui.panels.IGuiPanel;
import betterquesting.api2.client.gui.resources.textures.IGuiTexture;
import betterquesting.api2.client.gui.themes.presets.PresetColor;
import betterquesting.api2.client.gui.themes.presets.PresetTexture;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatAllowedCharacters;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.input.Mouse;

import java.util.List;

public class PanelTextField implements IGuiPanel
{
	private final IGuiRect transform;
	private boolean enabled = true;
	
	private IGuiTexture[] texState = new IGuiTexture[3];
    private boolean isFocused = false;
    private boolean isActive = true;
    private boolean canWrap = false;
    private int maxLength = 32;
    
    private String text;
    
    private int selectStart = 0;
    private int selectEnd = 0; // WARNING: Selection end can be before selection start!
    private boolean dragging = false;
    
    // Yep... we're supporting this without a scrolling canvas (we don't need the zooming and mouse dragging but the scrolling bounds change much more often)
    private IValueIO<Float> scrollX;
    private IValueIO<Float> scrollY;
    
    public PanelTextField(IGuiRect rect, String text)
    {
        this.transform = rect;
        
        this.setTextures(PresetTexture.TEXT_BOX_0.getTexture(), PresetTexture.TEXT_BOX_1.getTexture(), PresetTexture.TEXT_BOX_2.getTexture());
        
        this.setText(text);
		
		// Dummy value drivers
		
		scrollX = new IValueIO<Float>()
		{
			private float v = 0F;
			
			@Override
			public Float readValue()
			{
				return v;
			}
			
			@Override
			public void writeValue(Float value)
			{
				this.v = MathHelper.clamp(value, 0F, 1F);
			}
		};
		
		scrollY = new IValueIO<Float>()
		{
			private float v = 0F;
			
			@Override
			public Float readValue()
			{
				return v;
			}
			
			@Override
			public void writeValue(Float value)
			{
				this.v = MathHelper.clamp(value, 0F, 1F);
			}
		};
    }
    
    public PanelTextField setTextures(IGuiTexture disabled, IGuiTexture idle, IGuiTexture focused)
    {
        this.texState[0] = disabled;
        this.texState[1] = idle;
        this.texState[2] = focused;
        return this;
    }
    
    public PanelTextField setMaxLength(int size)
    {
        this.maxLength = size;
        return this;
    }
    
    /**
     * Enables text wrapping for multi-line editing
     */
    public PanelTextField enableWrapping(boolean state)
    {
        this.canWrap = state;
        return this;
    }
    
    public PanelTextField setScrollDriverX(IValueIO<Float> driver)
    {
        this.scrollX = driver;
        return this;
    }
    
    public PanelTextField setScrollDriverY(IValueIO<Float> driver)
    {
        this.scrollY = driver;
        return this;
    }
    
    public void setActive(boolean state)
    {
        this.isActive = state;
    }
    
    public boolean isActive()
    {
        return this.isActive;
    }
    
    public void setText(String text)
    {
        this.text = text;
    }
    
    public String getText()
    {
        return this.text;
    }
    
    /**
     * Writes text to the current cursor position replacing any current selection
     */
    public void writeText(String s)
    {
        String out = "";
        String in = ChatAllowedCharacters.filterAllowedCharacters(s);
        int l = Math.min(selectStart, selectEnd);
        int r = Math.max(selectStart, selectEnd);
        int space = this.maxLength - text.length() - (l - r);
        
        if(!text.isEmpty())
        {
            out = text.substring(0, l);
        }
        
        if(space < in.length())
        {
        
        }
    }
    
    @Override
    public IGuiRect getTransform()
    {
        return transform;
    }
    
    @Override
    public void initPanel()
    {
    
    }
    
    @Override
    public void setEnabled(boolean state)
    {
        this.enabled = state;
    }
    
    @Override
    public boolean isEnabled()
    {
        return this.enabled;
    }
    
    @Override
    public void drawPanel(int mx, int my, float partialTick)
    {
        if(isActive && dragging && Mouse.isButtonDown(0))
        {
            selectEnd = RenderUtils.getCursorPos(text, mx - (transform.getX() + 4), my - (transform.getY() + 4), transform.getWidth() - 8, Minecraft.getMinecraft().fontRenderer);
        } else if(dragging)
        {
            dragging = false;
        }
        
        IGuiRect bounds = this.getTransform();
        int state = !this.isActive() ? 0 : (isFocused ? 2 : 1);
        Minecraft mc = Minecraft.getMinecraft();
        IGuiTexture t = texState[state];
        
        if(t != null) // Full screen text editors probably don't need the backgrounds
        {
            t.drawTexture(bounds.getX(), bounds.getY(), bounds.getWidth(), bounds.getHeight(), 0F, partialTick);
        }
        
        if(!canWrap)
        {
            RenderUtils.drawHighlightedString(mc.fontRenderer, text, bounds.getX() + 4, bounds.getY() + 4, PresetColor.TEXT_AUX_1.getColor().getRGB(), false, PresetColor.TEXT_HIGHLIGHT.getColor().getRGB(), selectStart, selectEnd);
        } else
        {
            RenderUtils.drawHighlightedSplitString(mc.fontRenderer, text, bounds.getX() + 4, bounds.getY() + 4, bounds.getWidth() - 8, PresetColor.TEXT_AUX_1.getColor().getRGB(), false, PresetColor.TEXT_HIGHLIGHT.getColor().getRGB(), selectStart, selectEnd);
        }
    }
    
    @Override
    public boolean onMouseClick(int mx, int my, int button)
    {
        if(transform.contains(mx, my))
        {
            this.isFocused = true;
            
            selectStart = RenderUtils.getCursorPos(text, mx - (transform.getX() + 4), my - (transform.getY() + 4), transform.getWidth() - 8, Minecraft.getMinecraft().fontRenderer);
            selectEnd = selectStart;
            dragging = true;
            
            return true;
        }
        
        this.isFocused = false;
        
        return false;
    }
    
    @Override
    public boolean onMouseRelease(int mx, int my, int button)
    {
        if(isFocused)
        {
            //selectEnd = RenderUtils.getCursorPos(text, mx - (transform.getX() + 4), my - (transform.getY() + 4), transform.getWidth() - 8, Minecraft.getMinecraft().fontRenderer);
            
            return true;
        }
        
        return false;
    }
    
    @Override
    public boolean onMouseScroll(int mx, int my, int scroll)
    {
        // Scroll X/Y depending on wrapping mode
        return false;
    }
    
    @Override
    public boolean onKeyTyped(char c, int keycode)
    {
        if(!isFocused)
        {
            return false;
        }
        
        // Process ALLLLLLLLL the keyboard commands. Probably should refer to the GuiTextField class for a lot of this
        /*
        - Control + C
        - Control + X
        - Control + P
        - Control + A
        - Control + Shift + A
        - Home
        - End
        - Arrow Key (vertical movement too)
        - Letter (unicode) (also Caps Lock)
        - Shift + Letter (upper case) (invert Caps Lock)
        - Shift + Arrow Key (move cursor end)
         */
        
        return true;
    }
    
    @Override
    public List<String> getTooltip(int mx, int my)
    {
        return null;
    }
}

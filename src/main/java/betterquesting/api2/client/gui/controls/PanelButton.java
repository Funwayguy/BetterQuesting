package betterquesting.api2.client.gui.controls;

import java.awt.Color;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.init.SoundEvents;
import betterquesting.api2.client.gui.events.PEventBroadcaster;
import betterquesting.api2.client.gui.events.types.PEventButton;
import betterquesting.api2.client.gui.misc.IGuiRect;
import betterquesting.api2.client.gui.panels.IGuiPanel;
import betterquesting.api2.client.gui.resources.IGuiTexture;
import betterquesting.api2.client.gui.themes.TexturePreset;
import betterquesting.api2.client.gui.themes.ThemeRegistry;

public class PanelButton implements IGuiPanel
{
	private final IGuiRect transform;
	
	private final IGuiTexture[] texStates = new IGuiTexture[3];
	private int[] colStates = new int[]{Color.GRAY.getRGB(), Color.WHITE.getRGB(), 16777120};
	private IGuiTexture texIcon = null;
	private List<String> tooltip = null;
	private boolean txtShadow = true;
	private String btnText = "";
	private int btnState = 1;
	private int btnID = -1;
	
	public PanelButton(IGuiRect rect, int id, String txt)
	{
		this.transform = rect;
		this.btnText = txt;
		this.btnID = id;
		
		this.setTextures(ThemeRegistry.INSTANCE.getTexture(TexturePreset.BTN_NORMAL_0), ThemeRegistry.INSTANCE.getTexture(TexturePreset.BTN_NORMAL_1), ThemeRegistry.INSTANCE.getTexture(TexturePreset.BTN_NORMAL_2));
	}
	
	public PanelButton setTextHighlight(int disabled, int idle, int hover)
	{
		this.colStates[0] = disabled;
		this.colStates[1] = idle;
		this.colStates[2] = hover;
		return this;
	}
	
	public PanelButton setTextShadow(boolean enabled)
	{
		this.txtShadow = enabled;
		return this;
	}
	
	public PanelButton setTextures(IGuiTexture disabled, IGuiTexture idle, IGuiTexture hover)
	{
		this.texStates[0] = disabled;
		this.texStates[1] = idle;
		this.texStates[2] = hover;
		return this;
	}
	
	public PanelButton setIcon(IGuiTexture icon)
	{
		this.texIcon = icon;
		return this;
	}
	
	public PanelButton setTooltip(List<String> tooltip)
	{
		this.tooltip = tooltip;
		return this;
	}
	
	public int getButtonID()
	{
		return this.btnID;
	}
	
	public int getButtonState()
	{
		return this.btnState;
	}
	
	public String getText()
	{
		return this.btnText;
	}
	
	public boolean isEnabled()
	{
		return this.btnState > 0;
	}
	
	public PanelButton setEnabled(boolean state)
	{
		this.btnState = state? 1 : 0;
		return this;
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
	public void drawPanel(int mx, int my, float partialTick)
	{
		IGuiRect bounds = this.getTransform();
		GlStateManager.pushMatrix();
		GlStateManager.color(1F, 1F, 1F, 1F);
		this.btnState = !isEnabled()? 0 : (bounds.contains(mx, my)? 2 : 1);
		
		IGuiTexture t = texStates[btnState];
		
		if(t != null) // Support for text or icon only buttons in one or more states.
		{
			t.drawTexture(bounds.getX(), bounds.getY(), bounds.getWidth(), bounds.getHeight(), 0F);
		}
		
		if(texIcon != null)
		{
			int isz = Math.min(bounds.getHeight(), bounds.getWidth());
			texIcon.drawTexture(bounds.getX() + (bounds.getWidth()/2) - (isz/2), bounds.getY() + (bounds.getHeight()/2) - (isz/2), isz, isz, 0F);
		}
		
		if(btnText != null && btnText.length() > 0)
		{
			drawCenteredString(Minecraft.getMinecraft().fontRendererObj, btnText, bounds.getX() + bounds.getWidth()/2, bounds.getY() + bounds.getHeight()/2 - 4, colStates[btnState], txtShadow);
		}
		
		GlStateManager.popMatrix();
	}
	
    private static void drawCenteredString(FontRenderer font, String text, int x, int y, int color, boolean shadow)
    {
        font.drawString(text, x - font.getStringWidth(text) / 2, y, color, shadow);
    }
    
    /*private static void drawString(FontRenderer font, String text, int x, int y, int color, boolean shadow)
    {
        font.drawString(text, x, y, color, shadow);
    }*/
    
	@Override
	public boolean onMouseClick(int mx, int my, int click)
	{
		IGuiRect bounds = this.getTransform();
		boolean clicked = click == 0 && bounds.contains(mx, my) && PEventBroadcaster.INSTANCE.postEvent(new PEventButton(this));
		
		if(clicked)
		{
	        Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0F));
		}
		
		return clicked;
	}
	
	@Override
	public boolean onMouseScroll(int mx, int my, int scroll)
	{
		return false;
	}
	
	@Override
	public void onKeyTyped(char c, int keycode)
	{
	}
	
	@Override
	public List<String> getTooltip(int mx, int my)
	{
		return tooltip;
	}
}

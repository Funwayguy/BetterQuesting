package betterquesting.client.gui2;

import java.util.List;
import org.lwjgl.util.vector.Vector4f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ResourceLocation;
import betterquesting.api.client.themes.ITheme;
import betterquesting.api.utils.BigItemStack;
import betterquesting.api2.client.gui.GuiScreenCanvas;
import betterquesting.api2.client.gui.controls.PanelButton;
import betterquesting.api2.client.gui.controls.PanelButtonStorage;
import betterquesting.api2.client.gui.events.IPEventListener;
import betterquesting.api2.client.gui.events.PEventBroadcaster;
import betterquesting.api2.client.gui.events.PanelEvent;
import betterquesting.api2.client.gui.events.types.PEventButton;
import betterquesting.api2.client.gui.misc.GuiAlign;
import betterquesting.api2.client.gui.misc.GuiPadding;
import betterquesting.api2.client.gui.misc.GuiRectangle;
import betterquesting.api2.client.gui.misc.GuiTransform;
import betterquesting.api2.client.gui.panels.CanvasEmpty;
import betterquesting.api2.client.gui.panels.CanvasTextured;
import betterquesting.api2.client.gui.panels.bars.PanelVScrollBar;
import betterquesting.api2.client.gui.panels.content.PanelItem;
import betterquesting.api2.client.gui.panels.content.PanelTextBox;
import betterquesting.api2.client.gui.panels.lists.CanvasScrolling;
import betterquesting.api2.client.gui.themes.presets.PresetColor;
import betterquesting.api2.client.gui.themes.presets.PresetTexture;
import betterquesting.core.BetterQuesting;

public class GuiThemes extends GuiScreenCanvas implements IPEventListener
{
	// Last value of the scrollbar before loading new theme
	private PanelVScrollBar scrollPanel;
	
	public GuiThemes(GuiScreen parent)
	{
		super(parent);
	}
	
	@Override
	public void initPanel()
	{
		super.initPanel();
		
		PEventBroadcaster.INSTANCE.register(this, PEventButton.class);
		
		// Background panel
		CanvasTextured bgCan = new CanvasTextured(new GuiTransform(GuiAlign.FULL_BOX, new GuiPadding(16, 16, 16, 16), 0), PresetTexture.PANEL_MAIN.getTexture());
		this.addPanel(bgCan);
		
		// Inner canvas bounds
		CanvasEmpty inCan = new CanvasEmpty(new GuiTransform(GuiAlign.FULL_BOX, new GuiPadding(16, 16, 16, 16), 0));
		bgCan.addPanel(inCan); 
		
		PanelTextBox panTxt = new PanelTextBox(new GuiTransform(GuiAlign.TOP_EDGE, new GuiPadding(0, 0, 0, -16), 0), "Themes").setAlignment(1);
		panTxt.setColor(PresetColor.TEXT_HEADER.getColor());
		inCan.addPanel(panTxt);
		
		PanelButton btnExit = new PanelButton(new GuiTransform(GuiAlign.BOTTOM_CENTER, new GuiPadding(-100, -16, -100, 0), 0), 0, "Exit");
		bgCan.addPanel(btnExit);
		
		CanvasScrolling canScroll = new CanvasScrolling(new GuiTransform(GuiAlign.HALF_LEFT, new GuiPadding(0, 16, 16, 16), 0));
		inCan.addPanel(canScroll);
		
		List<ITheme> themes = betterquesting.client.themes.ThemeRegistry.INSTANCE.getAllThemes();
		//List<IGuiTheme> themes = ThemeRegistry.INSTANCE.getAllThemes();
		int width = canScroll.getTransform().getWidth();
		
		for(int i = 0; i < themes.size(); i++)
		{
			GuiRectangle trans = new GuiRectangle(0, i * 24, width, 24, 0);
			ITheme theme = themes.get(i);
			PanelButtonStorage<ResourceLocation> pbs = new PanelButtonStorage<ResourceLocation>(trans, 1, theme.getDisplayName(), theme.getThemeID());
			canScroll.addPanel(pbs);
			
			if(betterquesting.client.themes.ThemeRegistry.INSTANCE.getCurrentTheme() == theme)
			{
				pbs.setEnabled(false);
			}
		}
		
		PanelVScrollBar vsb = new PanelVScrollBar(new GuiTransform(GuiAlign.RIGHT_EDGE, new GuiPadding(0, 0, -8, 0), 0));
		inCan.addPanel(vsb);
		vsb.getTransform().setParent(canScroll.getTransform());
		canScroll.setScrollDriverY(vsb);
		
		scrollPanel = vsb;
		
		// Preview panel/canvas
		CanvasEmpty preCan = new CanvasEmpty(new GuiTransform(GuiAlign.HALF_RIGHT, new GuiPadding(8, 16, 0, 16), 0));
		inCan.addPanel(preCan);
		
		CanvasTextured preCanIn0 = new CanvasTextured(new GuiTransform(new Vector4f(0F, 0F, 0.5F, 0.5F), new GuiPadding(0, 0, 0, 0), 0), PresetTexture.PANEL_MAIN.getTexture());
		preCan.addPanel(preCanIn0);
		
		PanelTextBox tBox1 = new PanelTextBox(new GuiTransform(GuiAlign.MID_CENTER, -32, -8, 64, 16, 0), "EXAMPLE").setAlignment(1).setColor(PresetColor.TEXT_MAIN.getColor());
		preCanIn0.addPanel(tBox1);
		
		CanvasTextured preCanIn1 = new CanvasTextured(new GuiTransform(new Vector4f(0.5F, 0F, 1F, 0.5F), new GuiPadding(0, 0, 0, 0), 0), PresetTexture.PANEL_INNER.getTexture());
		preCan.addPanel(preCanIn1);
		
		PanelTextBox tBox2 = new PanelTextBox(new GuiTransform(GuiAlign.MID_CENTER, -32, -8, 64, 16, 0), "EXAMPLE").setAlignment(1).setColor(PresetColor.TEXT_AUX_0.getColor());
		preCanIn1.addPanel(tBox2);
		
		CanvasTextured preCanIn2 = new CanvasTextured(new GuiTransform(GuiAlign.HALF_BOTTOM, new GuiPadding(0, 0, 0, 0), 0), PresetTexture.AUX_FRAME_0.getTexture());
		preCan.addPanel(preCanIn2);
		
		PanelQuestPreview pqp = new PanelQuestPreview(new GuiTransform(new Vector4f(0.25F, 0.5F, 0.25F, 0.5F), -12, -12, 24, 24, 0));
		preCanIn2.addPanel(pqp);
		
		PanelItem pi = new PanelItem(new GuiTransform(new Vector4f(0.75F, 0.5F, 0.75F, 0.5F), -12, -12, 24, 24, 0), new BigItemStack(BetterQuesting.guideBook), true, true, true);
		preCanIn2.addPanel(pi);
		
		PanelLinePreview pl = new PanelLinePreview(pqp.getTransform(), pi.getTransform(), 4, 1);
		preCanIn2.addPanel(pl);
		
		PanelTextBox tBox3 = new PanelTextBox(new GuiTransform(GuiAlign.FULL_BOX, new GuiPadding(8, 8, 8, 8), 0), "EXAMPLE").setAlignment(1).setColor(PresetColor.TEXT_AUX_1.getColor());
		preCanIn2.addPanel(tBox3);
		
		
	}
	
	@Override
	public void onPanelEvent(PanelEvent event)
	{
		if(event == null)
		{
			return;
		} else if(PEventButton.class.isAssignableFrom(event.getClass()))
		{
			onButtonPress((PEventButton)event);
		}
	}
	
	@Override
	public void drawPanel(int mx, int my, float partialTick)
	{
		this.drawDefaultBackground();
		
		super.drawPanel(mx, my, partialTick);
	}
	
	@SuppressWarnings("unchecked")
	private void onButtonPress(PEventButton event)
	{
		Minecraft mc = Minecraft.getMinecraft();
		PanelButton btn = event.getButton();
		
		if(btn.getButtonID() == 0) // Exit
		{
			mc.displayGuiScreen(this.parent);
		} else if(btn.getButtonID() == 1 && btn instanceof PanelButtonStorage)
		{
			ResourceLocation res = ((PanelButtonStorage<ResourceLocation>)btn).getStoredValue();
			
			float scroll = scrollPanel.readValue();
			betterquesting.client.themes.ThemeRegistry.INSTANCE.setCurrentTheme(res);
			//ThemeRegistry.INSTANCE.setTheme(res);
			this.initGui();
			scrollPanel.writeValue(scroll);
		}
	}
}

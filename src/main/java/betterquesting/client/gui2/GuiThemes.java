package betterquesting.client.gui2;

import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ResourceLocation;
import betterquesting.api.client.themes.ITheme;
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
import betterquesting.api2.client.gui.panels.content.PanelTextBox;
import betterquesting.api2.client.gui.panels.lists.CanvasScrolling;
import betterquesting.api2.client.gui.themes.presets.PresetColor;
import betterquesting.api2.client.gui.themes.presets.PresetTexture;

public class GuiThemes extends GuiScreenCanvas implements IPEventListener
{
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
		}
		
		PanelVScrollBar vsb = new PanelVScrollBar(new GuiTransform(GuiAlign.RIGHT_EDGE, new GuiPadding(0, 0, -8, 0), 0));
		inCan.addPanel(vsb);
		vsb.getTransform().setParent(canScroll.getTransform());
		canScroll.setScrollDriverY(vsb);
		
		// Preview panel/canvas
		CanvasTextured preCan = new CanvasTextured(new GuiTransform(GuiAlign.HALF_RIGHT, new GuiPadding(8, 16, 0, 16), 0), PresetTexture.PANEL_MAIN.getTexture());
		inCan.addPanel(preCan);
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
	
	@SuppressWarnings({"deprecation", "unchecked"})
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

			betterquesting.client.themes.ThemeRegistry.INSTANCE.setCurrentTheme(res);
			//ThemeRegistry.INSTANCE.setTheme(res);
			this.initGui();
		}
	}
}

package betterquesting.client.gui2;

import org.lwjgl.util.vector.Vector4f;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ResourceLocation;
import betterquesting.api.properties.NativeProps;
import betterquesting.api2.client.gui.GuiScreenCanvas;
import betterquesting.api2.client.gui.controls.PanelButton;
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
import betterquesting.api2.client.gui.resources.IGuiTexture;
import betterquesting.api2.client.gui.resources.SimpleTexture;
import betterquesting.api2.client.gui.themes.TexturePreset;
import betterquesting.api2.client.gui.themes.ThemeRegistry;
import betterquesting.core.BetterQuesting;
import betterquesting.storage.QuestSettings;

public class GuiHome extends GuiScreenCanvas implements IPEventListener
{
	private ResourceLocation homeGui;
	private IGuiTexture homeSplashBG;
	private IGuiTexture homeSplashTitle;
	private float ancX = 0.5F;
	private float ancY = 0.5F;
	private int offX = 0;
	private int offY = 0;
	
	public GuiHome(GuiScreen parent)
	{
		super(parent);
	}
	
	@Override
	public void initGui()
	{
		super.initGui();
		
		PEventBroadcaster.INSTANCE.register(this, PEventButton.class);
		
		homeGui = new ResourceLocation(QuestSettings.INSTANCE.getProperty(NativeProps.HOME_IMAGE));
		homeSplashBG = new SimpleTexture(homeGui, new GuiRectangle(0, 0, 256, 128));
		homeSplashTitle = new SimpleTexture(homeGui, new GuiRectangle(0, 128, 256, 128));
		ancX = QuestSettings.INSTANCE.getProperty(NativeProps.HOME_ANC_X).floatValue();
		ancY = QuestSettings.INSTANCE.getProperty(NativeProps.HOME_ANC_Y).floatValue();
		offX = QuestSettings.INSTANCE.getProperty(NativeProps.HOME_OFF_X).intValue();
		offY = QuestSettings.INSTANCE.getProperty(NativeProps.HOME_OFF_Y).intValue();
		
		// Background panel
		CanvasTextured bgCan = new CanvasTextured(new GuiTransform(GuiAlign.FULL_BOX, new GuiPadding(16, 16, 16, 16), 0), ThemeRegistry.INSTANCE.getTexture(TexturePreset.PANEL_MAIN));
		this.addPanel(bgCan);
		
		// Inner canvas bounds
		CanvasEmpty inCan = new CanvasEmpty(new GuiTransform(GuiAlign.FULL_BOX, new GuiPadding(16, 16, 16, 16), 0));
		bgCan.addPanel(inCan);
		
		CanvasTextured splashCan = new CanvasTextured(new GuiTransform(GuiAlign.FULL_BOX, new GuiPadding(0, 0, 0, 32), 0), homeSplashBG);
		inCan.addPanel(splashCan);
		CanvasTextured splashTitle = new CanvasTextured(new GuiTransform(new Vector4f(ancX, ancY, ancX, ancY), new GuiPadding(offX, offY, -256 - offX, -128 - offY), 0), homeSplashTitle);
		splashCan.addPanel(splashTitle);
		
		PanelButton btnExit = new PanelButton(new GuiTransform(new Vector4f(0F, 1F, 0.25F, 1F), new GuiPadding(0, -32, 0, 0), 0), 0, "Exit");
		inCan.addPanel(btnExit);
		PanelButton btnQuests = new PanelButton(new GuiTransform(new Vector4f(0.25F, 1F, 0.5F, 1F), new GuiPadding(0, -32, 0, 0), 0), 1, "Quests");
		inCan.addPanel(btnQuests);
		PanelButton btnParty = new PanelButton(new GuiTransform(new Vector4f(0.5F, 1F, 0.75F, 1F), new GuiPadding(0, -32, 0, 0), 0), 2, "Party");
		inCan.addPanel(btnParty);
		PanelButton btnTheme = new PanelButton(new GuiTransform(new Vector4f(0.75F, 1F, 1F, 1F), new GuiPadding(0, -32, 0, 0), 0), 3, "Theme");
		inCan.addPanel(btnTheme);
		
		IGuiTexture txGear = new SimpleTexture(new ResourceLocation(BetterQuesting.MODID, "textures/gui/editor_icons.png"), new GuiRectangle(0, 16, 16, 16));
		PanelButton btnEdit = new PanelButton(new GuiTransform(GuiAlign.TOP_LEFT, new GuiPadding(0, 0, -16, -16), 0), 4, "").setIcon(txGear);
		inCan.addPanel(btnEdit);
	}
	
	@Override
	public void drawScreen(int mx, int my, float partialTick)
	{
		this.drawDefaultBackground();
		
		super.drawScreen(mx, my, partialTick);
	}
	
	@Override
	public void onPanelEvent(PanelEvent event)
	{
		if(event == null)
		{
			// I expect someone to do something stupid eventually...
			return;
		} else if(PEventButton.class.isAssignableFrom(event.getClass()))
		{
			onButtonPress((PEventButton)event);
		}
	}
	
	private void onButtonPress(PEventButton event)
	{
		//System.out.println("Pressed button #" + event.getButton().getButtonID() + " : " + event.getButton().getText());
	}
}

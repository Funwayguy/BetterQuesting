package betterquesting.client.gui2;

import java.util.Arrays;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.util.vector.Vector4f;
import betterquesting.api.api.QuestingAPI;
import betterquesting.api.enums.EnumSaveType;
import betterquesting.api.misc.ICallback;
import betterquesting.api.properties.NativeProps;
import betterquesting.api.questing.party.IParty;
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
import betterquesting.api2.client.gui.themes.presets.PresetIcon;
import betterquesting.api2.client.gui.themes.presets.PresetTexture;
import betterquesting.client.gui.GuiQuestLinesMain;
import betterquesting.client.gui.editors.json.scrolling.GuiJsonEditor;
import betterquesting.client.gui.party.GuiManageParty;
import betterquesting.client.gui.party.GuiNoParty;
import betterquesting.network.PacketSender;
import betterquesting.questing.party.PartyManager;
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
	public void initPanel()
	{
		super.initPanel();
		
		PEventBroadcaster.INSTANCE.register(this, PEventButton.class);
		
		homeGui = new ResourceLocation(QuestSettings.INSTANCE.getProperty(NativeProps.HOME_IMAGE));
		homeSplashBG = new SimpleTexture(homeGui, new GuiRectangle(0, 0, 256, 128));
		homeSplashTitle = new SimpleTexture(homeGui, new GuiRectangle(0, 128, 256, 128)).maintainAspect(true);
		ancX = QuestSettings.INSTANCE.getProperty(NativeProps.HOME_ANC_X);
		ancY = QuestSettings.INSTANCE.getProperty(NativeProps.HOME_ANC_Y);
		offX = QuestSettings.INSTANCE.getProperty(NativeProps.HOME_OFF_X);
		offY = QuestSettings.INSTANCE.getProperty(NativeProps.HOME_OFF_Y);
		
		// Background panel
		CanvasTextured bgCan = new CanvasTextured(new GuiTransform(GuiAlign.FULL_BOX, new GuiPadding(16, 16, 16, 16), 0), PresetTexture.PANEL_MAIN.getTexture());
		this.addPanel(bgCan);
		
		// Inner canvas bounds
		CanvasEmpty inCan = new CanvasEmpty(new GuiTransform(GuiAlign.FULL_BOX, new GuiPadding(16, 16, 16, 16), 0));
		bgCan.addPanel(inCan);
		
		CanvasTextured splashCan = new CanvasTextured(new GuiTransform(GuiAlign.FULL_BOX, new GuiPadding(0, 0, 0, 32), 0), homeSplashBG);
		inCan.addPanel(splashCan);
		CanvasTextured splashTitle = new CanvasTextured(new GuiTransform(new Vector4f(ancX, ancY, ancX, ancY), new GuiPadding(offX, offY, -256 - offX, -128 - offY), 0), homeSplashTitle);
		splashCan.addPanel(splashTitle);
		
		PanelButton btnExit = new PanelButton(new GuiTransform(new Vector4f(0F, 1F, 0.25F, 1F), new GuiPadding(0, -32, 0, 0), 0), 0, "Exit");//.setTextures(ThemeRegistry.INSTANCE.getTexture(TexturePreset.BTN_CLEAN_0), ThemeRegistry.INSTANCE.getTexture(TexturePreset.BTN_CLEAN_1), ThemeRegistry.INSTANCE.getTexture(TexturePreset.BTN_CLEAN_2));
		btnExit.setTooltip(Arrays.asList(new String[]{I18n.format("betterquesting.home.exit")}));
		inCan.addPanel(btnExit);
		
		PanelButton btnQuests = new PanelButton(new GuiTransform(new Vector4f(0.25F, 1F, 0.5F, 1F), new GuiPadding(0, -32, 0, 0), 0), 1, "Quests");//.setTextures(ThemeRegistry.INSTANCE.getTexture(TexturePreset.BTN_CLEAN_0), ThemeRegistry.INSTANCE.getTexture(TexturePreset.BTN_CLEAN_1), ThemeRegistry.INSTANCE.getTexture(TexturePreset.BTN_CLEAN_2));
		inCan.addPanel(btnQuests);
		PanelButton btnParty = new PanelButton(new GuiTransform(new Vector4f(0.5F, 1F, 0.75F, 1F), new GuiPadding(0, -32, 0, 0), 0), 2, "Party");//.setTextures(ThemeRegistry.INSTANCE.getTexture(TexturePreset.BTN_CLEAN_0), ThemeRegistry.INSTANCE.getTexture(TexturePreset.BTN_CLEAN_1), ThemeRegistry.INSTANCE.getTexture(TexturePreset.BTN_CLEAN_2));
		inCan.addPanel(btnParty);
		PanelButton btnTheme = new PanelButton(new GuiTransform(new Vector4f(0.75F, 1F, 1F, 1F), new GuiPadding(0, -32, 0, 0), 0), 3, "Theme");//.setTextures(ThemeRegistry.INSTANCE.getTexture(TexturePreset.BTN_CLEAN_0), ThemeRegistry.INSTANCE.getTexture(TexturePreset.BTN_CLEAN_1), ThemeRegistry.INSTANCE.getTexture(TexturePreset.BTN_CLEAN_2));
		inCan.addPanel(btnTheme);
		
		PanelButton btnEdit = new PanelButton(new GuiTransform(GuiAlign.TOP_LEFT, new GuiPadding(0, 0, -16, -16), 0), 4, "").setIcon(PresetIcon.ICON_GEAR.getTexture());
		inCan.addPanel(btnEdit);
	}
	
	@Override
	public void drawPanel(int mx, int my, float partialTick)
	{
		//this.drawDefaultBackground();
		
		super.drawPanel(mx, my, partialTick);
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
	
	private void onButtonPress(PEventButton event)
	{
		Minecraft mc = Minecraft.getMinecraft();
		PanelButton btn = event.getButton();
		
		if(btn.getButtonID() == 0) // Exit
		{
			mc.displayGuiScreen(this.parent);
		} else if(btn.getButtonID() == 1) // Quests
		{
			mc.displayGuiScreen(new GuiQuestLinesMain(this));
		} else if(btn.getButtonID() == 2) // Party
		{
			IParty party = PartyManager.INSTANCE.getUserParty(QuestingAPI.getQuestingUUID(mc.player));
			
			if(party != null)
			{
				mc.displayGuiScreen(new GuiManageParty(this, party));
			} else
			{
				mc.displayGuiScreen(new GuiNoParty(this));
			}
		} else if(btn.getButtonID() == 3) // Theme
		{
			//mc.displayGuiScreen(new GuiThemeSelect(this));
			mc.displayGuiScreen(new GuiThemes(this)); // UNFINISHED
		} else if(btn.getButtonID() == 4) // Editor
		{
			mc.displayGuiScreen(new GuiJsonEditor(this, QuestSettings.INSTANCE.writeToNBT(new NBTTagCompound(), EnumSaveType.CONFIG), null, new ICallback<NBTTagCompound>()
			{
				@Override
				public void setValue(NBTTagCompound value)
				{
					QuestSettings.INSTANCE.readFromNBT(value, EnumSaveType.CONFIG);
					PacketSender.INSTANCE.sendToServer(QuestSettings.INSTANCE.getSyncPacket());
				}
			}));
		}
	}
}

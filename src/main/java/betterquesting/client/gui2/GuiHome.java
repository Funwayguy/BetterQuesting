package betterquesting.client.gui2;

import betterquesting.api.api.ApiReference;
import betterquesting.api2.utils.QuestTranslation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.util.vector.Vector4f;
import betterquesting.api.api.QuestingAPI;
import betterquesting.api.enums.EnumSaveType;
import betterquesting.api.properties.NativeProps;
import betterquesting.api.questing.party.IParty;
import betterquesting.api2.client.gui.GuiScreenCanvas;
import betterquesting.api2.client.gui.controls.PanelButton;
import betterquesting.api2.client.gui.controls.IPanelButton;
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
import betterquesting.api2.client.gui.resources.textures.IGuiTexture;
import betterquesting.api2.client.gui.resources.textures.SimpleTexture;
import betterquesting.api2.client.gui.themes.presets.PresetIcon;
import betterquesting.api2.client.gui.themes.presets.PresetTexture;
import betterquesting.client.gui.editors.json.scrolling.GuiJsonEditor;
import betterquesting.client.gui.party.GuiManageParty;
import betterquesting.network.PacketSender;
import betterquesting.questing.party.PartyManager;
import betterquesting.storage.QuestSettings;

@SideOnly(Side.CLIENT)
public class GuiHome extends GuiScreenCanvas implements IPEventListener
{
	public static GuiScreen bookmark;
	
	public GuiHome(GuiScreen parent)
	{
		super(parent);
	}
	
	@Override
	public void initPanel()
	{
		super.initPanel();
		
		PEventBroadcaster.INSTANCE.register(this, PEventButton.class);
		
		ResourceLocation homeGui = new ResourceLocation(QuestSettings.INSTANCE.getProperty(NativeProps.HOME_IMAGE));
		IGuiTexture homeSplashBG = new SimpleTexture(homeGui, new GuiRectangle(0, 0, 256, 128));
		IGuiTexture homeSplashTitle = new SimpleTexture(homeGui, new GuiRectangle(0, 128, 256, 128)).maintainAspect(true);
		float ancX = QuestSettings.INSTANCE.getProperty(NativeProps.HOME_ANC_X);
		float ancY = QuestSettings.INSTANCE.getProperty(NativeProps.HOME_ANC_Y);
		int offX = QuestSettings.INSTANCE.getProperty(NativeProps.HOME_OFF_X);
		int offY = QuestSettings.INSTANCE.getProperty(NativeProps.HOME_OFF_Y);
		
		// Background panel
		CanvasTextured bgCan = new CanvasTextured(new GuiTransform(GuiAlign.FULL_BOX, new GuiPadding(0, 0, 0, 0), 0), PresetTexture.PANEL_MAIN.getTexture());
		this.addPanel(bgCan);
		
		// Inner canvas bounds
		CanvasEmpty inCan = new CanvasEmpty(new GuiTransform(GuiAlign.FULL_BOX, new GuiPadding(16, 16, 16, 16), 0));
		bgCan.addPanel(inCan);
		
		CanvasTextured splashCan = new CanvasTextured(new GuiTransform(GuiAlign.FULL_BOX, new GuiPadding(0, 0, 0, 32), 0), homeSplashBG);
		inCan.addPanel(splashCan);
		CanvasTextured splashTitle = new CanvasTextured(new GuiTransform(new Vector4f(ancX, ancY, ancX, ancY), new GuiPadding(offX, offY, -256 - offX, -128 - offY), 0), homeSplashTitle);
		splashCan.addPanel(splashTitle);
		
		PanelButton btnExit = new PanelButton(new GuiTransform(new Vector4f(0F, 1F, 0.25F, 1F), new GuiPadding(0, -32, 0, 0), 0), 0, QuestTranslation.translate("betterquesting.home.exit"));
		inCan.addPanel(btnExit);
		
		PanelButton btnQuests = new PanelButton(new GuiTransform(new Vector4f(0.25F, 1F, 0.5F, 1F), new GuiPadding(0, -32, 0, 0), 0), 1, QuestTranslation.translate("betterquesting.home.quests"));
		inCan.addPanel(btnQuests);
		PanelButton btnParty = new PanelButton(new GuiTransform(new Vector4f(0.5F, 1F, 0.75F, 1F), new GuiPadding(0, -32, 0, 0), 0), 2, QuestTranslation.translate("betterquesting.home.party"));
		inCan.addPanel(btnParty);
		PanelButton btnTheme = new PanelButton(new GuiTransform(new Vector4f(0.75F, 1F, 1F, 1F), new GuiPadding(0, -32, 0, 0), 0), 3, QuestTranslation.translate("betterquesting.home.theme"));
		inCan.addPanel(btnTheme);
		
		if(QuestingAPI.getAPI(ApiReference.SETTINGS).canUserEdit(mc.player))
		{
			PanelButton btnEdit = new PanelButton(new GuiTransform(GuiAlign.TOP_LEFT, new GuiPadding(0, 0, -16, -16), 0), 4, "").setIcon(PresetIcon.ICON_GEAR.getTexture());
			inCan.addPanel(btnEdit);
		}
		
		/*PanelButton tstBtn = new PanelButton(new GuiTransform(GuiAlign.TOP_RIGHT, -16, 0, 16, 16, 0), 5, "?");
		inCan.addPanel(tstBtn);*/
	}
	
	@Override
	public void onPanelEvent(PanelEvent event)
	{
		if(event instanceof PEventButton)
		{
			onButtonPress((PEventButton)event);
		}
	}
	
	private void onButtonPress(PEventButton event)
	{
		Minecraft mc = Minecraft.getMinecraft();
		IPanelButton btn = event.getButton();
		
		if(btn.getButtonID() == 0) // Exit
		{
			mc.displayGuiScreen(this.parent);
		} else if(btn.getButtonID() == 1) // Quests
		{
			mc.displayGuiScreen(new GuiQuestLines(this));
		} else if(btn.getButtonID() == 2) // Party
		{
			IParty party = PartyManager.INSTANCE.getUserParty(QuestingAPI.getQuestingUUID(mc.player));
			
			if(party != null)
			{
				mc.displayGuiScreen(new GuiManageParty(this, party));
			} else
			{
				mc.displayGuiScreen(new GuiPartyCreate(this));
			}
		} else if(btn.getButtonID() == 3) // Theme
		{
			mc.displayGuiScreen(new GuiThemes(this));
		} else if(btn.getButtonID() == 4) // Editor
		{
			mc.displayGuiScreen(new GuiJsonEditor(this, QuestSettings.INSTANCE.writeToNBT(new NBTTagCompound(), EnumSaveType.CONFIG), null, (NBTTagCompound value) ->
			{
				QuestSettings.INSTANCE.readFromNBT(value, EnumSaveType.CONFIG);
				PacketSender.INSTANCE.sendToServer(QuestSettings.INSTANCE.getSyncPacket());
			}));
		} else if(btn.getButtonID() == 5)
		{
			//mc.displayGuiScreen(new GuiPartyCreate(this));
		}
	}
}

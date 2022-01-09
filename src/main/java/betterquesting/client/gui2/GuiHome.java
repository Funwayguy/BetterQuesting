package betterquesting.client.gui2;

import betterquesting.api.api.ApiReference;
import betterquesting.api.api.QuestingAPI;
import betterquesting.api.properties.NativeProps;
import betterquesting.api.questing.party.IParty;
import betterquesting.api.storage.BQ_Settings;
import betterquesting.api.utils.JsonHelper;
import betterquesting.api.utils.NBTConverter;
import betterquesting.api2.client.gui.GuiScreenCanvas;
import betterquesting.api2.client.gui.controls.IPanelButton;
import betterquesting.api2.client.gui.controls.PanelButton;
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
import betterquesting.api2.client.gui.themes.presets.PresetColor;
import betterquesting.api2.client.gui.themes.presets.PresetIcon;
import betterquesting.api2.client.gui.themes.presets.PresetTexture;
import betterquesting.api2.storage.DBEntry;
import betterquesting.api2.utils.QuestTranslation;
import betterquesting.client.gui2.editors.nbt.GuiNbtEditor;
import betterquesting.client.gui2.party.GuiPartyCreate;
import betterquesting.client.gui2.party.GuiPartyManage;
import betterquesting.client.gui3.GuiStatus;
import betterquesting.handlers.SaveLoadHandler;
import betterquesting.network.handlers.NetChapterSync;
import betterquesting.network.handlers.NetQuestSync;
import betterquesting.network.handlers.NetSettingSync;
import betterquesting.questing.QuestDatabase;
import betterquesting.questing.QuestLineDatabase;
import betterquesting.questing.party.PartyManager;
import betterquesting.storage.QuestSettings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.util.vector.Vector4f;

import java.io.File;
import java.util.Collections;
import java.util.function.Consumer;

@SideOnly(Side.CLIENT)
public class GuiHome extends GuiScreenCanvas
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
		
		PEventBroadcaster.INSTANCE.register((Consumer<PanelEvent>)this::onButtonPress, PEventButton.class);
		
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
		btnParty.setActive(QuestSettings.INSTANCE.getProperty(NativeProps.PARTY_ENABLE));
		inCan.addPanel(btnParty);
		PanelButton btnTheme = new PanelButton(new GuiTransform(new Vector4f(0.75F, 1F, 1F, 1F), new GuiPadding(0, -32, 0, 0), 0), 3, QuestTranslation.translate("betterquesting.home.theme"));
		inCan.addPanel(btnTheme);

        PanelButton btnNotif = new PanelButton(new GuiTransform(GuiAlign.BOTTOM_RIGHT, -140, -52, 136, 16, 0), 420, (BQ_Settings.questNotices ? QuestTranslation.translate("betterquesting.notification.enabled") : QuestTranslation.translate("betterquesting.notification.disabled")))
        {
            @Override
            public void onButtonClick(){
                BQ_Settings.questNotices = !BQ_Settings.questNotices;
                if(betterquesting.handlers.ConfigHandler.config != null) {
                    betterquesting.handlers.ConfigHandler.config.get(Configuration.CATEGORY_GENERAL, "Quest Notices", true).set(BQ_Settings.questNotices);
                    betterquesting.handlers.ConfigHandler.config.save();
                }
                this.setText(BQ_Settings.questNotices ? QuestTranslation.translate("betterquesting.notification.enabled") : QuestTranslation.translate("betterquesting.notification.disabled"));
            }
        };
        btnNotif.setTooltip(Collections.singletonList(QuestTranslation.translate("betterquesting.notification.tooltip")));
        inCan.addPanel(btnNotif);
		
		if(QuestingAPI.getAPI(ApiReference.SETTINGS).canUserEdit(mc.player))
		{
			PanelButton btnEdit = new PanelButton(new GuiTransform(GuiAlign.TOP_LEFT, new GuiPadding(0, 0, -16, -16), 0), 4, "").setIcon(PresetIcon.ICON_GEAR.getTexture());
			inCan.addPanel(btnEdit);
		}
		
		if(Minecraft.getMinecraft().isIntegratedServerRunning() && SaveLoadHandler.INSTANCE.hasUpdate())
		{
			PanelButton tstBtn = new PanelButton(new GuiTransform(GuiAlign.TOP_RIGHT, -16, 0, 16, 16, 0), 5, "");
			tstBtn.setIcon(PresetIcon.ICON_NOTICE.getTexture(), PresetColor.UPDATE_NOTICE.getColor(), 0);
			tstBtn.setTooltip(Collections.singletonList(QuestTranslation.translateTrimmed("betterquesting.tooltip.update_quests", true)));
			inCan.addPanel(tstBtn);
		}
		
		if((Boolean)Launch.blackboard.get("fml.deobfuscatedEnvironment"))
        {
		    PanelButton tstBtn = new PanelButton(new GuiTransform(GuiAlign.TOP_LEFT, 0, 16, 16, 16, 0), -2, "?")
            {
                @Override
                public void onButtonClick()
                {
                    mc.displayGuiScreen(new GuiStatus(GuiHome.this));
                    //mc.displayGuiScreen(new GuiBuilderMain(GuiHome.this));
                }
            }; // Test screen
		    inCan.addPanel(tstBtn);
        }
	}
	
	private void onButtonPress(PanelEvent event)
	{
	    if(!(event instanceof PEventButton)) return;
	    
		Minecraft mc = Minecraft.getMinecraft();
		IPanelButton btn = ((PEventButton)event).getButton();
		
		if(btn.getButtonID() == 0) // Exit
		{
			mc.displayGuiScreen(this.parent);
		} else if(btn.getButtonID() == 1) // Quests
		{
			mc.displayGuiScreen(new GuiQuestLines(this));
		} else if(btn.getButtonID() == 2) // Party
		{
			DBEntry<IParty> party = PartyManager.INSTANCE.getParty(QuestingAPI.getQuestingUUID(mc.player));
			
			if(party != null)
			{
				mc.displayGuiScreen(new GuiPartyManage(this));
			} else
			{
				mc.displayGuiScreen(new GuiPartyCreate(this));
			}
		} else if(btn.getButtonID() == 3) // Theme
		{
			mc.displayGuiScreen(new GuiThemes(this));
		} else if(btn.getButtonID() == 4) // Editor
		{
			mc.displayGuiScreen(new GuiNbtEditor(this, QuestSettings.INSTANCE.writeToNBT(new NBTTagCompound()), (value) ->
			{
				QuestSettings.INSTANCE.readFromNBT(value);
                NetSettingSync.requestEdit();
			}));
		} else if(btn.getButtonID() == 5) // Update me
		{
			final File qFile = new File(BQ_Settings.defaultDir, "DefaultQuests.json");
			
			if(qFile.exists())
			{
				FMLCommonHandler.instance().getMinecraftServerInstance().addScheduledTask(() -> {
					boolean editMode = QuestSettings.INSTANCE.getProperty(NativeProps.EDIT_MODE);
					boolean hardMode = QuestSettings.INSTANCE.getProperty(NativeProps.HARDCORE);
					
					NBTTagList jsonP = QuestDatabase.INSTANCE.writeProgressToNBT(new NBTTagList(), null);
					NBTTagCompound j1 = NBTConverter.JSONtoNBT_Object(JsonHelper.ReadFromFile(qFile), new NBTTagCompound(), true);
					QuestSettings.INSTANCE.readFromNBT(j1.getCompoundTag("questSettings"));
					QuestDatabase.INSTANCE.readFromNBT(j1.getTagList("questDatabase", 10), false);
					QuestLineDatabase.INSTANCE.readFromNBT(j1.getTagList("questLines", 10), false);
					QuestDatabase.INSTANCE.readProgressFromNBT(jsonP, false);
					
					QuestSettings.INSTANCE.setProperty(NativeProps.EDIT_MODE, editMode);
					QuestSettings.INSTANCE.setProperty(NativeProps.HARDCORE, hardMode);
					
					NetSettingSync.sendSync(null);
                    NetQuestSync.quickSync(-1, true, true);
                    NetChapterSync.sendSync(null, null);
					
					SaveLoadHandler.INSTANCE.resetUpdate();
					SaveLoadHandler.INSTANCE.markDirty();
				});
				
				//this.initGui(); // Reset the whole thing
				mc.displayGuiScreen(null);
			}
		}
	}
}

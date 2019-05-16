package betterquesting.client.gui2;

import betterquesting.api.api.QuestingAPI;
import betterquesting.api.questing.party.IParty;
import betterquesting.api.utils.BigItemStack;
import betterquesting.api2.client.gui.GuiScreenCanvas;
import betterquesting.api2.client.gui.controls.PanelButton;
import betterquesting.api2.client.gui.controls.io.ValueFuncIO;
import betterquesting.api2.client.gui.misc.GuiAlign;
import betterquesting.api2.client.gui.misc.GuiPadding;
import betterquesting.api2.client.gui.misc.GuiRectangle;
import betterquesting.api2.client.gui.misc.GuiTransform;
import betterquesting.api2.client.gui.panels.CanvasEmpty;
import betterquesting.api2.client.gui.panels.CanvasTextured;
import betterquesting.api2.client.gui.panels.content.PanelEntityPreview;
import betterquesting.api2.client.gui.panels.content.PanelItemSlot;
import betterquesting.api2.client.gui.panels.lists.CanvasScrolling;
import betterquesting.api2.client.gui.themes.presets.PresetIcon;
import betterquesting.api2.client.gui.themes.presets.PresetTexture;
import betterquesting.client.gui2.party.GuiPartyCreate;
import betterquesting.client.gui2.party.GuiPartyManage;
import betterquesting.questing.party.PartyManager;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Keyboard;
import org.lwjgl.util.vector.Vector4f;

public class GuiDebugTest extends GuiScreenCanvas
{
	public GuiDebugTest(GuiScreen parent)
	{
		super(parent);
	}
	
	@Override
	public void initPanel()
	{
		super.initPanel();
    
        //PEventBroadcaster.INSTANCE.register(this, PEventButton.class);
        
        // === Tab Bar ===
        
        CanvasScrolling cvTabBar = new CanvasScrolling(new GuiTransform(GuiAlign.TOP_EDGE, new GuiPadding(0, 0, 16, -16), 0));
        this.addPanel(cvTabBar);
        
        cvTabBar.addPanel(new PanelButton(new GuiRectangle(0, 0, 100, 16), -1, "Home")
        {
            @Override
            public void onButtonClick()
            {
                mc.displayGuiScreen(new GuiDebugTest(parent));
            }
        });
        cvTabBar.addPanel(new PanelButton(new GuiRectangle(100, 0, 100, 16), -1, "Quests")
        {
            @Override
            public void onButtonClick()
            {
                mc.displayGuiScreen(new GuiQuestLines(GuiDebugTest.this));
            }
        });
        cvTabBar.addPanel(new PanelButton(new GuiRectangle(200, 0, 100, 16), -1, "Party")
        {
            @Override
            public void onButtonClick()
            {
                IParty party = PartyManager.INSTANCE.getUserParty(QuestingAPI.getQuestingUUID(mc.player));
                mc.displayGuiScreen(party == null ? new GuiPartyCreate(GuiDebugTest.this) : new GuiPartyManage(GuiDebugTest.this));
            }
        });
        cvTabBar.addPanel(new PanelButton(new GuiRectangle(300, 0, 100, 16), -1, "Theme")
        {
            @Override
            public void onButtonClick()
            {
                mc.displayGuiScreen(new GuiThemes(GuiDebugTest.this));
            }
        });
        
        // === Player Status ===
        
        CanvasEmpty cvLeft = new CanvasEmpty(new GuiTransform(new Vector4f(0F, 0F, 0.3F, 1F), new GuiPadding(0, 16, 0, 0), 0));
        this.addPanel(cvLeft);
        
        CanvasTextured cvPreview = new CanvasTextured(new GuiTransform(GuiAlign.FULL_BOX, new GuiPadding(0, 0, 0, 64), 0), PresetTexture.PANEL_MAIN.getTexture());
        cvLeft.addPanel(cvPreview);
        
        cvPreview.addPanel(new PanelEntityPreview(new GuiTransform(GuiAlign.FULL_BOX, new GuiPadding(0, 0, 32, 0), 0), mc.player).setRotationDriven(new ValueFuncIO<>(() -> 15F), new ValueFuncIO<>(() -> 30F)));
        
        cvPreview.addPanel(new PanelItemSlot(new GuiTransform(GuiAlign.MID_RIGHT, -30, -48, 24, 24, 0), -1, new BigItemStack(mc.player.inventory.armorItemInSlot(3))));
        cvPreview.addPanel(new PanelItemSlot(new GuiTransform(GuiAlign.MID_RIGHT, -30, -24, 24, 24, 0), -1, new BigItemStack(mc.player.inventory.armorItemInSlot(2))));
        cvPreview.addPanel(new PanelItemSlot(new GuiTransform(GuiAlign.MID_RIGHT, -30, 0, 24, 24, 0), -1, new BigItemStack(mc.player.inventory.armorItemInSlot(1))));
        cvPreview.addPanel(new PanelItemSlot(new GuiTransform(GuiAlign.MID_RIGHT, -30, 24, 24, 24, 0), -1, new BigItemStack(mc.player.inventory.armorItemInSlot(0))));
        
        cvPreview.addPanel(new PanelItemSlot(new GuiTransform(GuiAlign.BOTTOM_CENTER, -32, -30, 24, 24, 0), -1, new BigItemStack(mc.player.getHeldItemMainhand())));
        cvPreview.addPanel(new PanelItemSlot(new GuiTransform(GuiAlign.BOTTOM_CENTER, 8, -30, 24, 24, 0), -1, new BigItemStack(mc.player.getHeldItemOffhand())));
        
        cvLeft.addPanel(new CanvasTextured(new GuiTransform(GuiAlign.BOTTOM_EDGE, new GuiPadding(0, -64, 0, 0), 0), PresetTexture.PANEL_MAIN.getTexture()));
        
        // === Info Panel ===
        
        CanvasTextured cvRight = new CanvasTextured(new GuiTransform(new Vector4f(0.3F, 0F, 1F, 1F), new GuiPadding(0, 16, 0, 0), 0), PresetTexture.PANEL_MAIN.getTexture());
        this.addPanel(cvRight);
        
        this.addPanel(new PanelButton(new GuiTransform(GuiAlign.TOP_RIGHT, -16, 0, 16, 16, -1), -1, "")
        {
          @Override
          public void onButtonClick()
          {
              mc.displayGuiScreen(parent);
          }
        }.setIcon(PresetIcon.ICON_CROSS.getTexture()));
	}
	
	@Override
    public boolean onKeyTyped(char c, int keyCode)
    {
        if(!super.onKeyTyped(c, keyCode))
        {
            if(keyCode == Keyboard.KEY_BACK)
            {
                mc.displayGuiScreen(parent);
                return true;
            }
            
            return false;
        }
        
        return true;
    }
}

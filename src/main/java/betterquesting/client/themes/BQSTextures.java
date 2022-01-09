package betterquesting.client.themes;

import betterquesting.api.api.ApiReference;
import betterquesting.api.api.QuestingAPI;
import betterquesting.api2.client.gui.misc.GuiRectangle;
import betterquesting.api2.client.gui.resources.textures.IGuiTexture;
import betterquesting.api2.client.gui.resources.textures.SimpleTexture;
import betterquesting.api2.client.gui.themes.IThemeRegistry;
import betterquesting.core.BetterQuesting;
import net.minecraft.util.ResourceLocation;

public enum BQSTextures
{
    LOOT_CHEST("loot_chest"),
    LOOT_GLOW("loot_glow"),
    
    HAND_LEFT("hand_left"),
    HAND_RIGHT("hand_right"),
    ATK_SYMB("attack_symb"),
    USE_SYMB("use_symb");
    
    public static final ResourceLocation TX_UI_ELEMENTS = new ResourceLocation(BetterQuesting.MODID_STD, "textures/gui/gui_elements.png");
    
    private final ResourceLocation key;
	
	BQSTextures(String key)
	{
		this.key = new ResourceLocation(BetterQuesting.MODID_STD, key);
	}
	
	public IGuiTexture getTexture()
	{
		return QuestingAPI.getAPI(ApiReference.THEME_REG).getTexture(this.key);
	}
	
	public ResourceLocation getKey()
	{
		return this.key;
	}
	
	public static void registerTextures()
    {
        IThemeRegistry tReg = QuestingAPI.getAPI(ApiReference.THEME_REG);
        tReg.setDefaultTexture(LOOT_CHEST.key, new SimpleTexture(TX_UI_ELEMENTS, new GuiRectangle(0, 0, 128, 68)));
        tReg.setDefaultTexture(LOOT_GLOW.key, new SimpleTexture(TX_UI_ELEMENTS, new GuiRectangle(128, 0, 32, 32)));
        
        tReg.setDefaultTexture(HAND_LEFT.key, new SimpleTexture(TX_UI_ELEMENTS, new GuiRectangle(0, 80, 16, 16)));
        tReg.setDefaultTexture(HAND_RIGHT.key, new SimpleTexture(TX_UI_ELEMENTS, new GuiRectangle(16, 80, 16, 16)));
        tReg.setDefaultTexture(ATK_SYMB.key, new SimpleTexture(TX_UI_ELEMENTS, new GuiRectangle(32, 80, 16, 16)));
        tReg.setDefaultTexture(USE_SYMB.key, new SimpleTexture(TX_UI_ELEMENTS, new GuiRectangle(48, 80, 16, 16)));
    }
}

package betterquesting.api2.client.gui.themes.presets;

import net.minecraft.util.ResourceLocation;
import betterquesting.api2.client.gui.misc.GuiRectangle;
import betterquesting.api2.client.gui.resources.textures.IGuiTexture;
import betterquesting.api2.client.gui.resources.textures.SimpleTexture;
import betterquesting.api2.client.gui.themes.ThemeRegistry;
import betterquesting.core.BetterQuesting;

public enum PresetIcon
{
	ICON_TICK("icon_tick"),
	ICON_CROSS("icon_cross"),
	
	ICON_POSITIVE("icon_positive"),
	ICON_NEGATIVE("icon_negative"),
	
	ICON_LEFT("icon_left"),
	ICON_RIGHT("icon_right"),
	ICON_UP("icon_up"),
	ICON_DOWN("icon_down"),
	
	ICON_PROPS("icon_props"),
	ICON_GEAR("icon_gear"),
	ICON_TRASH("icon_trash"),
	ICON_SELECTION("icon_selection"),
	ICON_COPY("icon_copy"),
	ICON_GRAB("icon_grab"),
	ICON_NEW("icon_new"),
	ICON_GRID("icon_grid"),
	ICON_TWO_WAY("icon_two_way"),
	ICON_LINK("icon_link"),
	ICON_SORT("icon_sort"),
	ICON_VIEW("icon_view"),
	ICON_FUNC("icon_func"),
	ICON_CURSOR("icon_cursor"),
	ICON_REFRESH("icon_refresh"),
	ICON_ITEM("icon_item"),
	ICON_SCALE("icon_scale"),
	
	ICON_EXIT("icon_exit"),
	ICON_NOTICE("icon_notice"),
	ICON_PARTY("icon_party"),
	ICON_THEME("icon_theme"),
	ICON_HOME("icon_home");
	
	
	public static final ResourceLocation TX_ICONS = new ResourceLocation(BetterQuesting.MODID, "textures/gui/editor_icons.png");
	
	private final ResourceLocation key;
	
	private PresetIcon(String key)
	{
		this.key = new ResourceLocation(BetterQuesting.MODID, key);
	}
	
	public IGuiTexture getTexture()
	{
		return ThemeRegistry.INSTANCE.getTexture(this.key);
	}
	
	public ResourceLocation getKey()
	{
		return this.key;
	}
	
	public static void registerIcons(ThemeRegistry reg)
	{
		reg.setDefaultTexture(ICON_TICK.key, new SimpleTexture(TX_ICONS, new GuiRectangle(128, 0, 16, 16)).maintainAspect(true));
		reg.setDefaultTexture(ICON_CROSS.key, new SimpleTexture(TX_ICONS, new GuiRectangle(160, 16, 16, 16)).maintainAspect(true));
		
		reg.setDefaultTexture(ICON_POSITIVE.key, new SimpleTexture(TX_ICONS, new GuiRectangle(32, 16, 16, 16)).maintainAspect(true));
		reg.setDefaultTexture(ICON_NEGATIVE.key, new SimpleTexture(TX_ICONS, new GuiRectangle(112, 16, 16, 16)).maintainAspect(true));
		
		reg.setDefaultTexture(ICON_LEFT.key, new SimpleTexture(TX_ICONS, new GuiRectangle(208, 16, 16, 16)).maintainAspect(true));
		reg.setDefaultTexture(ICON_RIGHT.key, new SimpleTexture(TX_ICONS, new GuiRectangle(224, 16, 16, 16)).maintainAspect(true));
		reg.setDefaultTexture(ICON_UP.key, new SimpleTexture(TX_ICONS, new GuiRectangle(176, 16, 16, 16)).maintainAspect(true));
		reg.setDefaultTexture(ICON_DOWN.key, new SimpleTexture(TX_ICONS, new GuiRectangle(192, 16, 16, 16)).maintainAspect(true));
		
		reg.setDefaultTexture(ICON_PROPS.key, new SimpleTexture(TX_ICONS, new GuiRectangle(0, 0, 16, 16)).maintainAspect(true));
		reg.setDefaultTexture(ICON_GEAR.key, new SimpleTexture(TX_ICONS, new GuiRectangle(0, 16, 16, 16)).maintainAspect(true));
		reg.setDefaultTexture(ICON_TRASH.key, new SimpleTexture(TX_ICONS, new GuiRectangle(16, 0, 16, 16)).maintainAspect(true));
		reg.setDefaultTexture(ICON_SELECTION.key, new SimpleTexture(TX_ICONS, new GuiRectangle(16, 16, 16, 16)).maintainAspect(true));
		reg.setDefaultTexture(ICON_COPY.key, new SimpleTexture(TX_ICONS, new GuiRectangle(32, 0, 16, 16)).maintainAspect(true));
		reg.setDefaultTexture(ICON_GRAB.key, new SimpleTexture(TX_ICONS, new GuiRectangle(48, 0, 16, 16)).maintainAspect(true));
		reg.setDefaultTexture(ICON_NEW.key, new SimpleTexture(TX_ICONS, new GuiRectangle(48, 16, 16, 16)).maintainAspect(true));
		reg.setDefaultTexture(ICON_GRID.key, new SimpleTexture(TX_ICONS, new GuiRectangle(64, 0, 16, 16)).maintainAspect(true));
		reg.setDefaultTexture(ICON_TWO_WAY.key, new SimpleTexture(TX_ICONS, new GuiRectangle(64, 16, 16, 16)).maintainAspect(true));
		reg.setDefaultTexture(ICON_LINK.key, new SimpleTexture(TX_ICONS, new GuiRectangle(80, 0, 16, 16)).maintainAspect(true));
		reg.setDefaultTexture(ICON_SORT.key, new SimpleTexture(TX_ICONS, new GuiRectangle(80, 16, 16, 16)).maintainAspect(true));
		reg.setDefaultTexture(ICON_VIEW.key, new SimpleTexture(TX_ICONS, new GuiRectangle(96, 0, 16, 16)).maintainAspect(true));
		reg.setDefaultTexture(ICON_FUNC.key, new SimpleTexture(TX_ICONS, new GuiRectangle(96, 16, 16, 16)).maintainAspect(true));
		reg.setDefaultTexture(ICON_CURSOR.key, new SimpleTexture(TX_ICONS, new GuiRectangle(112, 0, 16, 16)).maintainAspect(true));
		reg.setDefaultTexture(ICON_REFRESH.key, new SimpleTexture(TX_ICONS, new GuiRectangle(128, 16, 16, 16)).maintainAspect(true));
		reg.setDefaultTexture(ICON_ITEM.key, new SimpleTexture(TX_ICONS, new GuiRectangle(144, 0, 16, 16)).maintainAspect(true));
		reg.setDefaultTexture(ICON_SCALE.key, new SimpleTexture(TX_ICONS, new GuiRectangle(144, 16, 16, 16)).maintainAspect(true));
		reg.setDefaultTexture(ICON_EXIT.key, new SimpleTexture(TX_ICONS, new GuiRectangle(160, 0, 16, 16)).maintainAspect(true));
		reg.setDefaultTexture(ICON_NOTICE.key, new SimpleTexture(TX_ICONS, new GuiRectangle(176, 0, 16, 16)).maintainAspect(true));
		reg.setDefaultTexture(ICON_PARTY.key, new SimpleTexture(TX_ICONS, new GuiRectangle(192, 0, 16, 16)).maintainAspect(true));
		reg.setDefaultTexture(ICON_THEME.key, new SimpleTexture(TX_ICONS, new GuiRectangle(208, 0, 16, 16)).maintainAspect(true));
		reg.setDefaultTexture(ICON_HOME.key, new SimpleTexture(TX_ICONS, new GuiRectangle(224, 0, 16, 16)).maintainAspect(true));
	}
}

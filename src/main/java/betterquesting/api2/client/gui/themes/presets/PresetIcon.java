package betterquesting.api2.client.gui.themes.presets;

import betterquesting.api2.client.gui.misc.GuiRectangle;
import betterquesting.api2.client.gui.resources.textures.IGuiTexture;
import betterquesting.api2.client.gui.resources.textures.SimpleTexture;
import betterquesting.api2.client.gui.themes.IThemeRegistry;
import betterquesting.client.themes.ThemeRegistry;
import betterquesting.core.BetterQuesting;
import net.minecraft.util.ResourceLocation;

// TODO: Reorganise these when the icon atlas is no longer bound by legacy constraints
public enum PresetIcon {
    // === SIMPLE ===
    ICON_TICK("icon_tick"),
    ICON_CROSS("icon_cross"),

    ICON_POSITIVE("icon_positive"),
    ICON_NEGATIVE("icon_negative"),

    ICON_LEFT("icon_left"),
    ICON_RIGHT("icon_right"),
    ICON_UP("icon_up"),
    ICON_DOWN("icon_down"),
    ICON_INFO("icon_info"),
    ICON_DESC("icon_desc"),
    ICON_IMAGE("icon_image"),
    ICON_BOOKS("icon_books"),

    // === TOOLS ===
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
    ICON_HOME("icon_home"),

    // === AUDIO ===
    ICON_AV_PLAY("icon_av_play"),
    ICON_AV_PAUSE("icon_av_pause"),
    ICON_AV_STOP("icon_av_stop"),
    ICON_AV_BACK("icon_av_back"),
    ICON_AV_SKIP("icon_av_skip"),
    ICON_AV_FF("icon_av_fastforward"),
    ICON_AV_REWIND("icon_av_rewind"),
    ICON_AV_SOUND("icon_av_sound"),
    ICON_AV_VOL_UP("icon_av_vol_up"),
    ICON_AV_VOL_DOWN("icon_av_vol_down"),
    ICON_AV_VOL_MUTE("icon_av_vol_mute"),

    // === FILES ===
    ICON_FILE("icon_file"),
    ICON_FOLDER_OPEN("icon_folder_open"),
    ICON_FOLDER_CLOSED("icon_folder_closed"),
    ICON_DIR_UP("icon_dir_up"),
    ICON_PG_NEXT("icon_pg_next"),
    ICON_PG_PREV("icon_pg_prev"),
    ICON_UPLOAD("icon_upload"),
    ICON_SAVE("icon_save"),

    // === PARTY ===

    // === VALUES ===

    // === OPTIONS ===

    ICON_LOCKED("icon_locked"),
    ICON_UNLOCKED("icon_unlocked"),
    ICON_VISIBILITY_HIDDEN("icon_visibility_hidden"),
    ICON_VISIBILITY_NORMAL("icon_visibility_normal"),
    ICON_VISIBILITY_IMPLICIT("icon_visibility_implicit"),

    // == MISC ===

    ICON_BOX_FIT("icon_box_fit"),
    ICON_ZOOM("icon_zoom"),
    ICON_ZOOM_IN("icon_zoom_in"),
    ICON_ZOOM_OUT("icon_zoom_out"),

    ICON_CHEST("icon_chest"),
    ICON_CHEST_ALL("icon_chest_all"),
    ICON_BOOKMARK("icon_bookmark"),
    ICON_MENU("icon_menu"),

    ICON_PATREON("icon_patreon"),
    ICON_TWITCH("icon_twitch");

    public static final ResourceLocation TX_ICONS = new ResourceLocation(BetterQuesting.MODID, "textures/gui/editor_icons.png");

    private final ResourceLocation key;

    PresetIcon(String key) {
        this.key = new ResourceLocation(BetterQuesting.MODID, key);
    }

    public IGuiTexture getTexture() {
        return ThemeRegistry.INSTANCE.getTexture(this.key);
    }

    public ResourceLocation getKey() {
        return this.key;
    }

    public static void registerIcons(IThemeRegistry reg) {
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
        reg.setDefaultTexture(ICON_INFO.key, new SimpleTexture(TX_ICONS, new GuiRectangle(64, 80, 16, 16)).maintainAspect(true));
        reg.setDefaultTexture(ICON_DESC.key, new SimpleTexture(TX_ICONS, new GuiRectangle(208, 64, 16, 16)).maintainAspect(true));
        reg.setDefaultTexture(ICON_BOOKS.key, new SimpleTexture(TX_ICONS, new GuiRectangle(224, 64, 16, 16)).maintainAspect(true));
        reg.setDefaultTexture(ICON_IMAGE.key, new SimpleTexture(TX_ICONS, new GuiRectangle(240, 64, 16, 16)).maintainAspect(true));

        // === AUDIO ===
        reg.setDefaultTexture(ICON_AV_PLAY.key, new SimpleTexture(TX_ICONS, new GuiRectangle(0, 32, 16, 16)).maintainAspect(true));
        reg.setDefaultTexture(ICON_AV_PAUSE.key, new SimpleTexture(TX_ICONS, new GuiRectangle(32, 32, 16, 16)).maintainAspect(true));
        reg.setDefaultTexture(ICON_AV_STOP.key, new SimpleTexture(TX_ICONS, new GuiRectangle(16, 32, 16, 16)).maintainAspect(true));
        reg.setDefaultTexture(ICON_AV_BACK.key, new SimpleTexture(TX_ICONS, new GuiRectangle(48, 32, 16, 16)).maintainAspect(true));
        reg.setDefaultTexture(ICON_AV_SKIP.key, new SimpleTexture(TX_ICONS, new GuiRectangle(64, 32, 16, 16)).maintainAspect(true));
        reg.setDefaultTexture(ICON_AV_FF.key, new SimpleTexture(TX_ICONS, new GuiRectangle(32, 80, 16, 16)).maintainAspect(true));
        reg.setDefaultTexture(ICON_AV_REWIND.key, new SimpleTexture(TX_ICONS, new GuiRectangle(48, 80, 16, 16)).maintainAspect(true));
        reg.setDefaultTexture(ICON_AV_VOL_UP.key, new SimpleTexture(TX_ICONS, new GuiRectangle(80, 32, 16, 16)).maintainAspect(true));
        reg.setDefaultTexture(ICON_AV_VOL_DOWN.key, new SimpleTexture(TX_ICONS, new GuiRectangle(96, 32, 16, 16)).maintainAspect(true));
        reg.setDefaultTexture(ICON_AV_VOL_MUTE.key, new SimpleTexture(TX_ICONS, new GuiRectangle(112, 32, 16, 16)).maintainAspect(true));
        reg.setDefaultTexture(ICON_AV_SOUND.key, new SimpleTexture(TX_ICONS, new GuiRectangle(128, 32, 16, 16)).maintainAspect(true));

        // === FILES ==
        reg.setDefaultTexture(ICON_FILE.key, new SimpleTexture(TX_ICONS, new GuiRectangle(0, 64, 16, 16)).maintainAspect(true));
        reg.setDefaultTexture(ICON_FOLDER_OPEN.key, new SimpleTexture(TX_ICONS, new GuiRectangle(16, 64, 16, 16)).maintainAspect(true));
        reg.setDefaultTexture(ICON_FOLDER_CLOSED.key, new SimpleTexture(TX_ICONS, new GuiRectangle(32, 64, 16, 16)).maintainAspect(true));
        reg.setDefaultTexture(ICON_DIR_UP.key, new SimpleTexture(TX_ICONS, new GuiRectangle(48, 64, 16, 16)).maintainAspect(true));
        reg.setDefaultTexture(ICON_PG_PREV.key, new SimpleTexture(TX_ICONS, new GuiRectangle(224, 48, 16, 16)).maintainAspect(true));
        reg.setDefaultTexture(ICON_PG_NEXT.key, new SimpleTexture(TX_ICONS, new GuiRectangle(240, 48, 16, 16)).maintainAspect(true));
        reg.setDefaultTexture(ICON_SAVE.key, new SimpleTexture(TX_ICONS, new GuiRectangle(0, 80, 16, 16)).maintainAspect(true));

        // === OPTIONS ===
        reg.setDefaultTexture(ICON_LOCKED.key, new SimpleTexture(TX_ICONS, new GuiRectangle(80, 48, 16, 16)).maintainAspect(true));
        reg.setDefaultTexture(ICON_UNLOCKED.key, new SimpleTexture(TX_ICONS, new GuiRectangle(96, 48, 16, 16)).maintainAspect(true));
        reg.setDefaultTexture(ICON_VISIBILITY_NORMAL.key, new SimpleTexture(TX_ICONS, new GuiRectangle(192, 80, 16, 16)).maintainAspect(true));
        reg.setDefaultTexture(ICON_VISIBILITY_IMPLICIT.key, new SimpleTexture(TX_ICONS, new GuiRectangle(208, 80, 16, 16)).maintainAspect(true));
        reg.setDefaultTexture(ICON_VISIBILITY_HIDDEN.key, new SimpleTexture(TX_ICONS, new GuiRectangle(224, 80, 16, 16)).maintainAspect(true));

        // === MISC ===
        reg.setDefaultTexture(ICON_BOX_FIT.key, new SimpleTexture(TX_ICONS, new GuiRectangle(80, 64, 16, 16)).maintainAspect(true));
        reg.setDefaultTexture(ICON_ZOOM.key, new SimpleTexture(TX_ICONS, new GuiRectangle(96, 64, 16, 16)).maintainAspect(true));
        reg.setDefaultTexture(ICON_ZOOM_IN.key, new SimpleTexture(TX_ICONS, new GuiRectangle(112, 64, 16, 16)).maintainAspect(true));
        reg.setDefaultTexture(ICON_ZOOM_OUT.key, new SimpleTexture(TX_ICONS, new GuiRectangle(128, 64, 16, 16)).maintainAspect(true));
        reg.setDefaultTexture(ICON_CHEST_ALL.key, new SimpleTexture(TX_ICONS, new GuiRectangle(144, 64, 16, 16)).maintainAspect(true));
        reg.setDefaultTexture(ICON_CHEST.key, new SimpleTexture(TX_ICONS, new GuiRectangle(160, 64, 16, 16)).maintainAspect(true));
        reg.setDefaultTexture(ICON_BOOKMARK.key, new SimpleTexture(TX_ICONS, new GuiRectangle(176, 64, 16, 16)).maintainAspect(true));
        reg.setDefaultTexture(ICON_MENU.key, new SimpleTexture(TX_ICONS, new GuiRectangle(192, 64, 16, 16)).maintainAspect(true));

        reg.setDefaultTexture(ICON_PATREON.key, new SimpleTexture(TX_ICONS, new GuiRectangle(144, 80, 16, 16)).maintainAspect(true));
        reg.setDefaultTexture(ICON_TWITCH.key, new SimpleTexture(TX_ICONS, new GuiRectangle(160, 80, 16, 16)).maintainAspect(true));
    }
}

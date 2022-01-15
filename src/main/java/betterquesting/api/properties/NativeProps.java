package betterquesting.api.properties;

import betterquesting.api.storage.BQ_Settings;
import net.minecraft.init.Items;
import net.minecraft.util.ResourceLocation;
import betterquesting.api.enums.EnumLogic;
import betterquesting.api.enums.EnumQuestVisibility;
import betterquesting.api.properties.basic.PropertyTypeBoolean;
import betterquesting.api.properties.basic.PropertyTypeEnum;
import betterquesting.api.properties.basic.PropertyTypeFloat;
import betterquesting.api.properties.basic.PropertyTypeInteger;
import betterquesting.api.properties.basic.PropertyTypeItemStack;
import betterquesting.api.properties.basic.PropertyTypeString;
import betterquesting.api.utils.BigItemStack;

// TODO: SPLIT THIS DAMN FILE UP. It's already too big and it needs to be divided up per-purpose
/**
 * List of native properties used in BetterQuesting
 */
public class NativeProps
{
	public static final IPropertyType<String> NAME =						new PropertyTypeString(new ResourceLocation("betterquesting:name"), "untitled.name");
	public static final IPropertyType<String> DESC =						new PropertyTypeString(new ResourceLocation("betterquesting:desc"), "untitled.desc");
	
	@Deprecated
	public static final IPropertyType<Boolean> MAIN =						new PropertyTypeBoolean(new ResourceLocation("betterquesting:isMain"), false);
	public static final IPropertyType<Boolean> GLOBAL =						new PropertyTypeBoolean(new ResourceLocation("betterquesting:isGlobal"), false);
	public static final IPropertyType<Boolean> GLOBAL_SHARE =				new PropertyTypeBoolean(new ResourceLocation("betterquesting:globalShare"), false);
	public static final IPropertyType<Boolean> SILENT =						new PropertyTypeBoolean(new ResourceLocation("betterquesting:isSilent"), false);
	public static final IPropertyType<Boolean> AUTO_CLAIM =					new PropertyTypeBoolean(new ResourceLocation("betterquesting:autoClaim"), false);
	public static final IPropertyType<Boolean> LOCKED_PROGRESS =			new PropertyTypeBoolean(new ResourceLocation("betterquesting:lockedProgress"), false);
	public static final IPropertyType<Boolean> SIMULTANEOUS =				new PropertyTypeBoolean(new ResourceLocation("betterquesting:simultaneous"), false);
	
	public static final IPropertyType<EnumQuestVisibility> VISIBILITY =		new PropertyTypeEnum<>(new ResourceLocation("betterquesting:visibility"), findVisibility());
	public static final IPropertyType<EnumLogic> LOGIC_TASK =				new PropertyTypeEnum<>(new ResourceLocation("betterquesting:taskLogic"), EnumLogic.AND);
	public static final IPropertyType<EnumLogic> LOGIC_QUEST =				new PropertyTypeEnum<>(new ResourceLocation("betterquesting:questLogic"), EnumLogic.AND);
	
	public static final IPropertyType<Integer> REPEAT_TIME =				new PropertyTypeInteger(new ResourceLocation("betterquesting:repeatTime"), -1);
	public static final IPropertyType<Boolean> REPEAT_REL =				    new PropertyTypeBoolean(new ResourceLocation("betterquesting:repeat_relative"), true);
	
	public static final IPropertyType<String> SOUND_UNLOCK =				new PropertyTypeString(new ResourceLocation("betterquesting:snd_unlock"), "minecraft:ui.button.click");
	public static final IPropertyType<String> SOUND_UPDATE =				new PropertyTypeString(new ResourceLocation("betterquesting:snd_update"), "minecraft:entity.player.levelup");
	public static final IPropertyType<String> SOUND_COMPLETE =				new PropertyTypeString(new ResourceLocation("betterquesting:snd_complete"), "minecraft:entity.player.levelup");
	
	public static final IPropertyType<BigItemStack> ICON =					new PropertyTypeItemStack(new ResourceLocation("betterquesting:icon"), new BigItemStack(Items.NETHER_STAR));
	//public static final IPropertyType<String> FRAME =                       new PropertyTypeString(new ResourceLocation("betterquesting:frame"), "");
	
	public static final IPropertyType<String> BG_IMAGE =					new PropertyTypeString(new ResourceLocation("betterquesting:bg_image"), "");
	public static final IPropertyType<Integer> BG_SIZE =					new PropertyTypeInteger(new ResourceLocation("betterquesting:bg_size"), 256);
	
	public static final IPropertyType<Boolean> PARTY_ENABLE =               new PropertyTypeBoolean(new ResourceLocation("betterquesting:party_enable"), true);
	
	public static final IPropertyType<Boolean> HARDCORE =					new PropertyTypeBoolean(new ResourceLocation("betterquesting:hardcore"), false);
	public static final IPropertyType<Boolean> EDIT_MODE =					new PropertyTypeBoolean(new ResourceLocation("betterquesting:editMode"), true);
	public static final IPropertyType<Integer> LIVES =						new PropertyTypeInteger(new ResourceLocation("betterquesting:lives"), 1);
	public static final IPropertyType<Integer> LIVES_DEF =					new PropertyTypeInteger(new ResourceLocation("betterquesting:livesDef"), 3);
	public static final IPropertyType<Integer> LIVES_MAX =					new PropertyTypeInteger(new ResourceLocation("betterquesting:livesMax"), 10);
	
	public static final IPropertyType<String> HOME_IMAGE =					new PropertyTypeString(new ResourceLocation("betterquesting:home_image"), "betterquesting:textures/gui/default_title.png");
	public static final IPropertyType<Float> HOME_ANC_X =					new PropertyTypeFloat(new ResourceLocation("betterquesting:home_anchor_x"), 0.5F);
	public static final IPropertyType<Float> HOME_ANC_Y =					new PropertyTypeFloat(new ResourceLocation("betterquesting:home_anchor_y"), 0F);
	public static final IPropertyType<Integer> HOME_OFF_X =					new PropertyTypeInteger(new ResourceLocation("betterquesting:home_offset_x"), -128);
	public static final IPropertyType<Integer> HOME_OFF_Y =					new PropertyTypeInteger(new ResourceLocation("betterquesting:home_offset_y"), 0);
	
	public static final IPropertyType<Integer> PACK_VER =					new PropertyTypeInteger(new ResourceLocation("betterquesting:pack_version"), 0);
	public static final IPropertyType<String> PACK_NAME =					new PropertyTypeString(new ResourceLocation("betterquesting:pack_name"), "");

	private static EnumQuestVisibility findVisibility() {
		String visibility = BQ_Settings.defaultVisibility;
		for(EnumQuestVisibility enumVisibility : EnumQuestVisibility.values()) {
			if(enumVisibility.toString().equals(visibility)) {
				return enumVisibility;
			}
		}

		return EnumQuestVisibility.NORMAL;
	}
}
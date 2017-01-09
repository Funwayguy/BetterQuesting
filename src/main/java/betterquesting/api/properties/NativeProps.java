package betterquesting.api.properties;

import net.minecraft.init.Items;
import net.minecraft.util.ResourceLocation;
import betterquesting.api.enums.EnumLogic;
import betterquesting.api.enums.EnumQuestVisibility;
import betterquesting.api.properties.basic.PropertyTypeBoolean;
import betterquesting.api.properties.basic.PropertyTypeEnum;
import betterquesting.api.properties.basic.PropertyTypeItemStack;
import betterquesting.api.properties.basic.PropertyTypeNumber;
import betterquesting.api.properties.basic.PropertyTypeString;
import betterquesting.api.utils.BigItemStack;

/**
 * List of native properties used in BetterQuesting
 */
public class NativeProps
{
	public static final IPropertyType<String> NAME =						new PropertyTypeString(new ResourceLocation("betterquesting:name"), "untitled.name");
	public static final IPropertyType<String> DESC =						new PropertyTypeString(new ResourceLocation("betterquesting:desc"), "untitled.desc");
	
	public static final IPropertyType<Boolean> MAIN =						new PropertyTypeBoolean(new ResourceLocation("betterquesting:isMain"), false);
	public static final IPropertyType<Boolean> GLOBAL =						new PropertyTypeBoolean(new ResourceLocation("betterquesting:isGlobal"), false);
	public static final IPropertyType<Boolean> GLOBAL_SHARE =				new PropertyTypeBoolean(new ResourceLocation("betterquesting:globalShare"), false);
	public static final IPropertyType<Boolean> SILENT =						new PropertyTypeBoolean(new ResourceLocation("betterquesting:isSilent"), false);
	public static final IPropertyType<Boolean> AUTO_CLAIM =					new PropertyTypeBoolean(new ResourceLocation("betterquesting:autoClaim"), false);
	public static final IPropertyType<Boolean> LOCKED_PROGRESS =			new PropertyTypeBoolean(new ResourceLocation("betterquesting:lockedProgress"), false);
	public static final IPropertyType<Boolean> SIMULTANEOUS =				new PropertyTypeBoolean(new ResourceLocation("betterquesting:simultaneous"), false);
	
	public static final IPropertyType<EnumQuestVisibility> VISIBILITY =		new PropertyTypeEnum<EnumQuestVisibility>(new ResourceLocation("betterquesting:visibility"), EnumQuestVisibility.NORMAL);
	public static final IPropertyType<EnumLogic> LOGIC_TASK =				new PropertyTypeEnum<EnumLogic>(new ResourceLocation("betterquesting:taskLogic"), EnumLogic.AND);
	public static final IPropertyType<EnumLogic> LOGIC_QUEST =				new PropertyTypeEnum<EnumLogic>(new ResourceLocation("betterquesting:questLogic"), EnumLogic.AND);
	
	public static final IPropertyType<Number> REPEAT_TIME =					new PropertyTypeNumber(new ResourceLocation("betterquesting:repeatTime"), -1);
	public static final IPropertyType<Number> PARTICIPATION =				new PropertyTypeNumber(new ResourceLocation("betterquesting:participation"), 1F);
	
	public static final IPropertyType<String> SOUND_UNLOCK =				new PropertyTypeString(new ResourceLocation("betterquesting:snd_unlock"), "random.click");
	public static final IPropertyType<String> SOUND_UPDATE =				new PropertyTypeString(new ResourceLocation("betterquesting:snd_update"), "random.levelup");
	public static final IPropertyType<String> SOUND_COMPLETE =				new PropertyTypeString(new ResourceLocation("betterquesting:snd_complete"), "random.levelup");
	
	public static final IPropertyType<BigItemStack> ICON =					new PropertyTypeItemStack(new ResourceLocation("betterquesting:icon"), new BigItemStack(Items.NETHER_STAR));
	
	public static final IPropertyType<String> BG_IMAGE =					new PropertyTypeString(new ResourceLocation("betterquesting:bg_image"), "");
	public static final IPropertyType<Number> BG_SIZE =						new PropertyTypeNumber(new ResourceLocation("betterquesting:bg_size"), 256);
	
	public static final IPropertyType<Boolean> PARTY_LOOT =					new PropertyTypeBoolean(new ResourceLocation("betterquesting:partyShareLoot"), false);
	public static final IPropertyType<Boolean> PARTY_LIVES =				new PropertyTypeBoolean(new ResourceLocation("betterquesting:partyShareLives"), false);
	
	public static final IPropertyType<Boolean> HARDCORE =					new PropertyTypeBoolean(new ResourceLocation("betterquesting:hardcore"), false);
	public static final IPropertyType<Boolean> EDIT_MODE =					new PropertyTypeBoolean(new ResourceLocation("betterquesting:editMode"), true);
	public static final IPropertyType<Number> LIVES =						new PropertyTypeNumber(new ResourceLocation("betterquesting:lives"), 1);
	
	public static final IPropertyType<String> HOME_IMAGE =					new PropertyTypeString(new ResourceLocation("betterquesting:home_image"), "betterquesting:textures/gui/default_title.png");
	public static final IPropertyType<Number> HOME_ANC_X =					new PropertyTypeNumber(new ResourceLocation("betterquesting:home_anchor_x"), 0.5F);
	public static final IPropertyType<Number> HOME_ANC_Y =					new PropertyTypeNumber(new ResourceLocation("betterquesting:home_anchor_y"), 0F);
	public static final IPropertyType<Number> HOME_OFF_X =					new PropertyTypeNumber(new ResourceLocation("betterquesting:home_offset_x"), -128);
	public static final IPropertyType<Number> HOME_OFF_Y =					new PropertyTypeNumber(new ResourceLocation("betterquesting:home_offset_y"), 0);
}
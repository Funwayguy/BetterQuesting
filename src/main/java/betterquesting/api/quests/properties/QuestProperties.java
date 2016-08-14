package betterquesting.api.quests.properties;

import net.minecraft.util.ResourceLocation;
import betterquesting.api.enums.EnumLogic;
import betterquesting.api.enums.EnumQuestVisibility;
import betterquesting.api.quests.properties.basic.QuestPropertyBoolean;
import betterquesting.api.quests.properties.basic.QuestPropertyEnum;
import betterquesting.api.quests.properties.basic.QuestPropertyNumber;

/**
 * List of standard properties quests can have
 */
public class QuestProperties
{
	public static final IQuestProperty<Boolean> MAIN =						new QuestPropertyBoolean(new ResourceLocation("betterquesting:isMain"), false);
	public static final IQuestProperty<Boolean> GLOBAL =					new QuestPropertyBoolean(new ResourceLocation("betterquesting:isGlobal"), false);
	public static final IQuestProperty<Boolean> GLOBAL_SHARE =				new QuestPropertyBoolean(new ResourceLocation("betterquesting:globalShare"), false);
	public static final IQuestProperty<Boolean> SILENT =					new QuestPropertyBoolean(new ResourceLocation("betterquesting:isSilent"), false);
	public static final IQuestProperty<Boolean> AUTO_CLAIM =				new QuestPropertyBoolean(new ResourceLocation("betterquesting:autoClaim"), false);
	public static final IQuestProperty<Boolean> LOCKED_PROGRESS =			new QuestPropertyBoolean(new ResourceLocation("betterquesting:lockedProgress"), false);
	public static final IQuestProperty<Boolean> SIMULTANEOUS =				new QuestPropertyBoolean(new ResourceLocation("betterquesting:simultaneous"), false);
	
	public static final IQuestProperty<EnumQuestVisibility> VISIBILITY =	new QuestPropertyEnum<EnumQuestVisibility>(new ResourceLocation("betterquesting:visibility"), EnumQuestVisibility.NORMAL);
	public static final IQuestProperty<EnumLogic> LOGIC_TASK =				new QuestPropertyEnum<EnumLogic>(new ResourceLocation("betterquesting:taskLogic"), EnumLogic.AND);
	public static final IQuestProperty<EnumLogic> LOGIC_QUEST =				new QuestPropertyEnum<EnumLogic>(new ResourceLocation("betterquesting:questLogic"), EnumLogic.AND);
	
	public static final IQuestProperty<Number> REPEAT_TIME =				new QuestPropertyNumber(new ResourceLocation("betterquesting:repeatTime"), -1);
	public static final IQuestProperty<Number> PARTICIPATION =				new QuestPropertyNumber(new ResourceLocation("betterquesting:participation"), 1F);
}
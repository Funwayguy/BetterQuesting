package betterquesting.api.quests.properties.basic;

import net.minecraft.util.ResourceLocation;
import betterquesting.api.quests.properties.IQuestProperty;

public abstract class QuestPropertyBase<T> implements IQuestProperty<T>
{
	private final ResourceLocation key;
	private final T def;
	
	public QuestPropertyBase(ResourceLocation key, T def)
	{
		this.key = key;
		this.def = def;
	}
	
	@Override
	public ResourceLocation getKey()
	{
		return key;
	}
	
	@Override
	public T getDefault()
	{
		return def;
	}
}

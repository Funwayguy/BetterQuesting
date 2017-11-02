package betterquesting.client.gui.editors.json;

import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import betterquesting.api.misc.ICallback;

public class TextCallbackJsonArray implements ICallback<String>
{
	private final NBTTagList json;
	private final int index;
	
	public TextCallbackJsonArray(NBTTagList json, int index)
	{
		this.json = json;
		this.index = index;
	}
	
	@Override
	public void setValue(String text)
	{
		json.set(index, new NBTTagString(text));
	}
}

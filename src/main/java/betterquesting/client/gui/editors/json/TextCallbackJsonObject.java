package betterquesting.client.gui.editors.json;

import net.minecraft.nbt.NBTTagCompound;
import betterquesting.api.misc.ICallback;

public class TextCallbackJsonObject implements ICallback<String>
{
	private final NBTTagCompound json;
	private final String key;
	
	public TextCallbackJsonObject(NBTTagCompound json, String key)
	{
		this.json = json;
		this.key = key;
	}
	
	@Override
	public void setValue(String text)
	{
		this.json.setString(key, text);
	}
}

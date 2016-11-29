package betterquesting.api.jdoc;

import net.minecraftforge.common.MinecraftForge;
import betterquesting.api.events.JsonDocEvent;

public class JsonDocBasic implements IJsonDoc
{
	private IJsonDoc parent = null;
	private String prefix = "jdoc";
	
	public JsonDocBasic(IJsonDoc parent, String prefix)
	{
		this.parent = parent;
		this.prefix = prefix;
	}
	
	@Override
	public String getUnlocalisedTitle()
	{
		return prefix + ".name";
	}
	
	@Override
	public String getUnlocalisedName(String key)
	{
		return prefix + "." + key + ".name";
	}
	
	@Override
	public String getUnlocalisedDesc(String key)
	{
		return prefix + "." + key + ".desc";
	}
	
	@Override
	public IJsonDoc getParentDoc()
	{
		return parent;
	}
	
	@Override
	public IJsonDoc getChildDoc(String child)
	{
		JsonDocEvent event = new JsonDocEvent(new JsonDocBasic(this, prefix + "." + child));
		MinecraftForge.EVENT_BUS.post(event);
		return event.getJdocResult();
	}
}

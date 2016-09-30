package betterquesting.api.events;

import cpw.mods.fml.common.eventhandler.Event;
import betterquesting.api.client.jdoc.IJsonDoc;

public class JsonDocEvent extends Event
{
	private IJsonDoc outJdoc;
	
	public JsonDocEvent(IJsonDoc jdoc)
	{
		outJdoc = jdoc;
	}
	
	public IJsonDoc getJsonDoc()
	{
		return outJdoc;
	}
	
	public void setJsonDoc(IJsonDoc jdoc)
	{
		this.outJdoc = jdoc;
	}
}

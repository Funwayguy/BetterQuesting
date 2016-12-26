package betterquesting.api.events;

import cpw.mods.fml.common.eventhandler.Event;
import betterquesting.api.jdoc.IJsonDoc;

/**
 * Can be used to override the JsonDocs in the editors with custom ones.
 */
public class JsonDocEvent extends Event
{
	private final IJsonDoc inJdoc;
	private IJsonDoc outJdoc;
	
	public JsonDocEvent(IJsonDoc jdoc)
	{
		inJdoc = jdoc;
		outJdoc = jdoc;
	}
	
	public IJsonDoc getJsonDoc()
	{
		return inJdoc;
	}
	
	public void setJdocResult(IJsonDoc jdoc)
	{
		this.outJdoc = jdoc;
	}
	
	public IJsonDoc getJdocResult()
	{
		return outJdoc == null? inJdoc : outJdoc;
	}
}

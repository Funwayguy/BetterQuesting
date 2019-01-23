package betterquesting.api.events;

import net.minecraftforge.fml.common.eventhandler.Event;
import betterquesting.api.jdoc.INbtDoc;

/**
 * Can be used to override the JsonDocs in the editors with custom ones.
 */
public class JsonDocEvent extends Event
{
	private final INbtDoc inJdoc;
	private INbtDoc outJdoc;
	
	public JsonDocEvent(INbtDoc jdoc)
	{
		inJdoc = jdoc;
		outJdoc = jdoc;
	}
	
	public INbtDoc getJsonDoc()
	{
		return inJdoc;
	}
	
	public void setJdocResult(INbtDoc jdoc)
	{
		this.outJdoc = jdoc;
	}
	
	public INbtDoc getJdocResult()
	{
		return outJdoc == null? inJdoc : outJdoc;
	}
}

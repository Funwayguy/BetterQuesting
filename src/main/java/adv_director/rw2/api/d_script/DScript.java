package adv_director.rw2.api.d_script;

import java.util.ArrayList;
import java.util.List;

public class DScript
{
	private final List<IDInstruction> body = new ArrayList<IDInstruction>();
	private Object ret;
	
	public List<IDInstruction> codeBody()
	{
		return this.body;
	}
	
	public Object execute(ScriptScope scope)
	{
		for(IDInstruction ins : body)
		{
			
		}
		return null;
	}
}

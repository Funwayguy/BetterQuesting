package adv_director.rw2.api.d_script;

public class ScriptScope
{
	public boolean hasFunction(String name)
	{
		return false;
	}
	
	public IFunction<?> getFunction(String name)
	{
		return null;
	}
	
	public boolean hasVariable(String name)
	{
		return false;
	}
	
	public Object getVariable(String name)
	{
		return null;
	}
	
	public boolean hasArray(String name)
	{
		return false;
	}
	
	public Object[] getArray(String name)
	{
		return null;
	}
}

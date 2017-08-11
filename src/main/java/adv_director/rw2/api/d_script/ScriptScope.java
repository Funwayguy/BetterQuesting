package adv_director.rw2.api.d_script;

public class ScriptScope
{
	public <T> boolean hasFunction(String name, Class<T> rType, Class<?>[] arguments)
	{
		return false;
	}
	
	public <T> IFunction<T> getFunction(String name, Class<T> type)
	{
		return null;
	}
	
	public <T> boolean hasVariable(String name, Class<T> type)
	{
		return false;
	}
	
	public <T> T getVariable(String name, Class<T> type)
	{
		return null;
	}
}

package adv_director.rw2.api.d_script;

import java.util.HashMap;

public class FunctionRegistry
{
	public static final FunctionRegistry INSTANCE = new FunctionRegistry();
	
	public final HashMap<String, IFunction<?>> funcReg = new HashMap<String, IFunction<?>>();
	
	private FunctionRegistry()
	{
	}
	
	public boolean registerFunction(String name, IFunction<?> func)
	{
		if(name == null || func == null)
		{
			throw new NullPointerException("Tried to register null function");
		} else if(funcReg.containsKey(name))
		{
			throw new IllegalArgumentException("Cannot register duplicate function name: " + name);
		} else if(funcReg.containsValue(func))
		{
			throw new IllegalArgumentException("Tried to register duplicate function: " + name);
		}
		
		funcReg.put(name, func);
		
		return true;
	}
	
	public IFunction<?> getFunction(String name)
	{
		return funcReg.get(name);
	}
}

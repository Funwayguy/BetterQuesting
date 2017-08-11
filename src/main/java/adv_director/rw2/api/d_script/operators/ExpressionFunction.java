package adv_director.rw2.api.d_script.operators;

import adv_director.rw2.api.d_script.IExpression;
import adv_director.rw2.api.d_script.ScriptScope;

public class ExpressionFunction<T> implements IExpression<T>
{
	private final String fName;
	private final Class<T> rType;
	private final IExpression<?>[] arguments;
	
	public ExpressionFunction(String name, Class<T> rType, IExpression<?>[] arguments)
	{
		this.fName = name;
		this.rType = rType;
		this.arguments = arguments;
	}
	
	@Override
	public T eval(ScriptScope scope) throws Exception
	{
		Class<?>[] argTypes = new Class<?>[arguments.length];
		
		for(int i = 0; i < argTypes.length; i++)
		{
			argTypes[i] = arguments[i].type();
		}
		
		if(!scope.hasFunction(fName, rType, argTypes))
		{
			String errArgs = "";
			
			for(int i = 0; i < argTypes.length; i++)
			{
				if(i != 0)
				{
					errArgs += ", ";
				}
				
				errArgs += argTypes[i].getSimpleName();
			}
			
			throw new Exception("Undefined function \"" + fName + "\" with return type \"" + rType.getSimpleName() + "\" and args [" + errArgs + "] in expression");
		}
		
		Object[] argValues = new Object[arguments.length];
		
		for(int i = 0; i < argValues.length; i++)
		{
			argValues[i] = arguments[i].eval(scope);
		}
		
		return scope.getFunction(fName, rType).invokeFunction(argValues);
	}
	
	@Override
	public Class<T> type()
	{
		return rType;
	}
}

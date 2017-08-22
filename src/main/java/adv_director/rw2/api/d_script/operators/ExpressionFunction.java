package adv_director.rw2.api.d_script.operators;

import adv_director.rw2.api.d_script.IExpression;
import adv_director.rw2.api.d_script.ScriptScope;

public class ExpressionFunction implements IExpression<Object>
{
	private final String fName;
	private final IExpression<?>[] arguments;
	
	public ExpressionFunction(String name, IExpression<?>[] arguments)
	{
		this.fName = name;
		this.arguments = arguments;
	}
	
	@Override
	public Object eval(ScriptScope scope) throws Exception
	{
		if(!scope.hasFunction(fName))
		{
			throw new Exception("Undefined function in expression: " + fName);
		}
		
		Object[] argValues = new Object[arguments.length];
		
		for(int i = 0; i < argValues.length; i++)
		{
			argValues[i] = arguments[i].eval(scope);
		}
		
		return scope.getFunction(fName).invokeFunction(argValues);
	}
	
	@Override
	public Class<Object> type()
	{
		return Object.class;
	}
}

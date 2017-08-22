package adv_director.rw2.api.d_script.operators;

import adv_director.rw2.api.d_script.IExpression;
import adv_director.rw2.api.d_script.ScriptScope;

public class ExpressionArray implements IExpression<Object>
{
	private final String vName;
	private final IExpression<?> index;
	
	public ExpressionArray(String name, IExpression<?> index)
	{
		this.vName = name;
		this.index = index;
		//this.vType = (Class<T[]>)Array.newInstance(type, 0).getClass(); // Not an ideal way of getting an array version but it works
	}
	
	@Override
	public Object eval(ScriptScope scope) throws Exception
	{
		if(!scope.hasArray(vName))
		{
			throw new Exception("Undefined array in expression: " + vName);
		}
		
		return scope.getArray(vName)[((Number)index.eval(scope)).intValue()];
	}
	
	@Override
	public Class<Object> type()
	{
		return Object.class;
	}
}

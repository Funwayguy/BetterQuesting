package adv_director.rw2.api.d_script.operators;

import java.lang.reflect.Array;
import adv_director.rw2.api.d_script.IExpression;
import adv_director.rw2.api.d_script.ScriptScope;

public class ExpressionArray<T> implements IExpression<T>
{
	private final String vName;
	private final Class<T[]> vType;
	private final IExpression<Number> index;
	
	@SuppressWarnings("unchecked")
	public ExpressionArray(String name, Class<T> type, IExpression<Number> index)
	{
		this.vName = name;
		this.vType = (Class<T[]>)Array.newInstance(type, 0).getClass(); // Not an ideal way of getting an array version but it works
		this.index = index;
	}
	
	@Override
	public T eval(ScriptScope scope) throws Exception
	{
		if(!scope.hasVariable(vName, vType))
		{
			throw new Exception("Undefined variable \"" + vName + "\" of type \"" + vType.getSimpleName() + "\" in expression");
		}
		
		return scope.getVariable(vName, vType)[index.eval(scope).intValue()];
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public Class<T> type()
	{
		return (Class<T>)vType.getComponentType();
	}
}

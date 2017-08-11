package adv_director.rw2.api.d_script.operators;

import adv_director.rw2.api.d_script.IExpression;
import adv_director.rw2.api.d_script.ScriptScope;

public class ExpressionVariable<T> implements IExpression<T>
{
	private final String vName;
	private final Class<T> vType;
	
	public ExpressionVariable(String name, Class<T> type)
	{
		this.vName = name;
		this.vType = type;
	}
	
	@Override
	public T eval(ScriptScope scope) throws Exception
	{
		if(!scope.hasVariable(vName, vType))
		{
			throw new Exception("Undefined variable \"" + vName + "\" of type \"" + vType.getSimpleName() + "\" in expression");
		}
		
		return scope.getVariable(vName, vType);
	}
	
	@Override
	public Class<T> type()
	{
		return vType;
	}
}

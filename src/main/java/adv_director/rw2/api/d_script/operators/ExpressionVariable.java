package adv_director.rw2.api.d_script.operators;

import adv_director.rw2.api.d_script.IExpression;
import adv_director.rw2.api.d_script.ScriptScope;

public class ExpressionVariable implements IExpression<Object>
{
	private final String vName;
	
	public ExpressionVariable(String name)
	{
		this.vName = name;
	}
	
	@Override
	public Object eval(ScriptScope scope) throws Exception
	{
		if(!scope.hasVariable(vName))
		{
			throw new Exception("Undefined variable in expression: " + vName);
		}
		
		return scope.getVariable(vName);
	}
	
	@Override
	public Class<Object> type()
	{
		return Object.class;
	}
}

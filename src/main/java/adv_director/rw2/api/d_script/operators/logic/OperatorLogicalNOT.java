package adv_director.rw2.api.d_script.operators.logic;

import adv_director.rw2.api.d_script.IExpression;
import adv_director.rw2.api.d_script.ScriptScope;

public class OperatorLogicalNOT implements IExpression<Boolean>
{
	private final IExpression<?> ex;
	
	public OperatorLogicalNOT(IExpression<?> ex)
	{
		this.ex = ex;
	}
	
	@Override
	public Boolean eval(ScriptScope scope) throws Exception
	{
		return !(Boolean)ex.eval(scope);
	}
	
	@Override
	public Class<Boolean> type()
	{
		return Boolean.class;
	}
}

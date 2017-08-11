package adv_director.rw2.api.d_script.operators.logic;

import adv_director.rw2.api.d_script.IExpression;
import adv_director.rw2.api.d_script.ScriptScope;

public class OperatorLogicalOr implements IExpression<Boolean>
{
	private final IExpression<Boolean> e1;
	private final IExpression<Boolean> e2;
	
	public OperatorLogicalOr(IExpression<Boolean> e1, IExpression<Boolean> e2)
	{
		this.e1 = e1;
		this.e2 = e2;
	}
	
	@Override
	public Boolean eval(ScriptScope scope) throws Exception
	{
		return e1.eval(scope) || e2.eval(scope);
	}

	@Override
	public Class<Boolean> type()
	{
		return Boolean.class;
	}
}

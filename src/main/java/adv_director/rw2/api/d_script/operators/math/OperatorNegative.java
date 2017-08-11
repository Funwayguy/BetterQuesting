package adv_director.rw2.api.d_script.operators.math;

import adv_director.rw2.api.d_script.IExpression;
import adv_director.rw2.api.d_script.ScriptScope;

public class OperatorNegative implements IExpression<Number>
{
	private final IExpression<Number> ex;
	
	public OperatorNegative(IExpression<Number> ex)
	{
		this.ex = ex;
	}
	
	@Override
	public Number eval(ScriptScope scope) throws Exception
	{
		return -(ex.eval(scope).doubleValue());
	}
	
	@Override
	public Class<Number> type()
	{
		return Number.class;
	}
}

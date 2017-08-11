package adv_director.rw2.api.d_script.operators.binary;

import adv_director.rw2.api.d_script.IExpression;
import adv_director.rw2.api.d_script.ScriptScope;

public class OperatorBitwiseNOT implements IExpression<Number>
{
	private final IExpression<Number> ex;
	
	public OperatorBitwiseNOT(IExpression<Number> ex)
	{
		this.ex = ex;
	}
	
	@Override
	public Number eval(ScriptScope scope) throws Exception
	{
		return ~(ex.eval(scope).longValue());
	}
	
	@Override
	public Class<Number> type()
	{
		return Number.class;
	}
}

package adv_director.rw2.api.d_script.operators.math;

import adv_director.rw2.api.d_script.IExpression;
import adv_director.rw2.api.d_script.ScriptScope;

public class OperatorSubtraction implements IExpression<Number>
{
	private final IExpression<Number> e1;
	private final IExpression<Number> e2;
	
	public OperatorSubtraction(IExpression<Number> e1, IExpression<Number> e2)
	{
		this.e1 = e1;
		this.e2 = e2;
	}
	
	@Override
	public Number eval(ScriptScope scope) throws Exception
	{
		return e1.eval(scope).doubleValue() - e2.eval(scope).doubleValue();
	}
	
	@Override
	public Class<Number> type()
	{
		return Number.class;
	}
}

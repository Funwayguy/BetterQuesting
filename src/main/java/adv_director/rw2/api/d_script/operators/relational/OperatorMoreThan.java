package adv_director.rw2.api.d_script.operators.relational;

import adv_director.rw2.api.d_script.IExpression;
import adv_director.rw2.api.d_script.ScriptScope;

public class OperatorMoreThan implements IExpression<Boolean>
{
	private final IExpression<Number> e1;
	private final IExpression<Number> e2;
	
	public OperatorMoreThan(IExpression<Number> e1, IExpression<Number> e2)
	{
		this.e1 = e1;
		this.e2 = e2;
	}
	
	@Override
	public Boolean eval(ScriptScope scope) throws Exception
	{
		return e1.eval(scope).doubleValue() > e2.eval(scope).doubleValue();
	}

	@Override
	public Class<Boolean> type()
	{
		return Boolean.class;
	}
}

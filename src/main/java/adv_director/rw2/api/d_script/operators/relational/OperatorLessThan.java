package adv_director.rw2.api.d_script.operators.relational;

import adv_director.rw2.api.d_script.IExpression;
import adv_director.rw2.api.d_script.ScriptScope;

public class OperatorLessThan implements IExpression<Boolean>
{
	private final IExpression<?> e1;
	private final IExpression<?> e2;
	
	public OperatorLessThan(IExpression<?> e1, IExpression<?> e2)
	{
		this.e1 = e1;
		this.e2 = e2;
	}
	
	@Override
	public Boolean eval(ScriptScope scope) throws Exception
	{
		Number n1 = (Number)e1.eval(scope);
		Number n2 = (Number)e2.eval(scope);
		return n1.doubleValue() < n2.doubleValue();
	}

	@Override
	public Class<Boolean> type()
	{
		return Boolean.class;
	}
}

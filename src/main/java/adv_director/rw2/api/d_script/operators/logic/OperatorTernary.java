package adv_director.rw2.api.d_script.operators.logic;

import adv_director.rw2.api.d_script.IExpression;
import adv_director.rw2.api.d_script.ScriptScope;

public class OperatorTernary<T> implements IExpression<T>
{
	private final IExpression<Boolean> e1;
	private final IExpression<T> e2;
	private final IExpression<T> e3;
	private final Class<T> type;
	
	public OperatorTernary(IExpression<Boolean> e1, IExpression<T> e2, IExpression<T> e3, Class<T> type)
	{
		this.e1 = e1;
		this.e2 = e2;
		this.e3 = e3;
		this.type = type;
	}
	
	@Override
	public T eval(ScriptScope scope) throws Exception
	{
		return e1.eval(scope) ? e2.eval(scope) : e3.eval(scope);
	}
	
	@Override
	public Class<T> type()
	{
		return type;
	}
}

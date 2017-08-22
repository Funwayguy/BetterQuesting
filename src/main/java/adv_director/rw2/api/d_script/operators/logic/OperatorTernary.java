package adv_director.rw2.api.d_script.operators.logic;

import adv_director.rw2.api.d_script.IExpression;
import adv_director.rw2.api.d_script.ScriptScope;

public class OperatorTernary implements IExpression<Object>
{
	private final IExpression<?> e1;
	private final IExpression<?> e2;
	private final IExpression<?> e3;
	
	public OperatorTernary(IExpression<?> e1, IExpression<?> e2, IExpression<?> e3)
	{
		this.e1 = e1;
		this.e2 = e2;
		this.e3 = e3;
	}
	
	@Override
	public Object eval(ScriptScope scope) throws Exception
	{
		return (Boolean)e1.eval(scope) ? e2.eval(scope) : e3.eval(scope);
	}
	
	@Override
	public Class<Object> type()
	{
		return Object.class;
	}
}

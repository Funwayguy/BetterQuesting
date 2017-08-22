package adv_director.rw2.api.d_script.operators.binary;

import adv_director.rw2.api.d_script.IExpression;
import adv_director.rw2.api.d_script.ScriptScope;

public class OperatorBitwiseOr implements IExpression<Long>
{
	private final IExpression<?> e1;
	private final IExpression<?> e2;
	
	public OperatorBitwiseOr(IExpression<?> e1, IExpression<?> e2)
	{
		this.e1 = e1;
		this.e2 = e2;
	}
	
	@Override
	public Long eval(ScriptScope scope) throws Exception
	{
		return (Long)e1.eval(scope) | (Long)e2.eval(scope);
	}
	
	@Override
	public Class<Long> type()
	{
		return Long.class;
	}
}

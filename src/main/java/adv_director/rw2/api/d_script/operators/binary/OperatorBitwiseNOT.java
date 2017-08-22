package adv_director.rw2.api.d_script.operators.binary;

import adv_director.rw2.api.d_script.IExpression;
import adv_director.rw2.api.d_script.ScriptScope;

public class OperatorBitwiseNOT implements IExpression<Long>
{
	private final IExpression<?> ex;
	
	public OperatorBitwiseNOT(IExpression<?> ex)
	{
		this.ex = ex;
	}
	
	@Override
	public Long eval(ScriptScope scope) throws Exception
	{
		return ~(Long)ex.eval(scope);
	}
	
	@Override
	public Class<Long> type()
	{
		return Long.class;
	}
}

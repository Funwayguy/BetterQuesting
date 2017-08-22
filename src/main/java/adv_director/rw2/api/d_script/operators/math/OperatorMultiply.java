package adv_director.rw2.api.d_script.operators.math;

import adv_director.rw2.api.d_script.IExpression;
import adv_director.rw2.api.d_script.ScriptScope;
import adv_director.rw2.api.d_script.operators.NumberParserUtility;

public class OperatorMultiply implements IExpression<Number>
{
	private final IExpression<?> e1;
	private final IExpression<?> e2;
	
	public OperatorMultiply(IExpression<?> e1, IExpression<?> e2)
	{
		this.e1 = e1;
		this.e2 = e2;
	}
	
	@Override
	public Number eval(ScriptScope scope) throws Exception
	{
		Number n1 = (Number)e1.eval(scope);
		Number n2 = (Number)e2.eval(scope);
		
		if(NumberParserUtility.hasDouble(n1, n2))
		{
			return n1.doubleValue() * n2.doubleValue();
		} else
		{
			return n1.longValue() * n2.longValue();
		}
	}
	
	@Override
	public Class<Number> type()
	{
		return Number.class;
	}
}

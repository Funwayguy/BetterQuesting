package adv_director.rw2.api.d_script.operators.math;

import adv_director.rw2.api.d_script.IExpression;
import adv_director.rw2.api.d_script.ScriptScope;
import adv_director.rw2.api.d_script.operators.NumberParserUtility;

// Slightly redundant operator but here to keep things consistent
public class OperatorPositive implements IExpression<Number>
{
	private final IExpression<?> ex;
	
	public OperatorPositive(IExpression<?> ex)
	{
		this.ex = ex;
	}
	
	@Override
	public Number eval(ScriptScope scope) throws Exception
	{
		Number n1 = (Number)ex.eval(scope);
		
		if(NumberParserUtility.hasDouble(n1))
		{
			return +n1.doubleValue();
		} else
		{
			return +n1.longValue();
		}
	}
	
	@Override
	public Class<Number> type()
	{
		return Number.class;
	}
}

package adv_director.rw2.api.d_script.operators.math;

import adv_director.rw2.api.d_script.IExpression;
import adv_director.rw2.api.d_script.ScriptScope;
import adv_director.rw2.api.d_script.operators.NumberParserUtility;

public class OperatorAddition implements IExpression<Object>
{
	private final IExpression<?> e1;
	private final IExpression<?> e2;
	
	public OperatorAddition(IExpression<?> e1, IExpression<?> e2)
	{
		this.e1 = e1;
		this.e2 = e2;
	}
	
	@Override
	public Object eval(ScriptScope scope) throws Exception
	{
		Object o1 = e1.eval(scope);
		Object o2 = e2.eval(scope);
		
		if(o1 instanceof String || o2 instanceof String)
		{
			return o1.toString() + o2.toString();
		} else
		{
			Number n1 = (Number)o1;
			Number n2 = (Number)o2;
			
			if(NumberParserUtility.hasDouble(n1, n2))
			{
				return n1.doubleValue() + n2.doubleValue();
			} else
			{
				return n1.longValue() + n2.longValue();
			}
		}
	}
	
	public Class<Object> type()
	{
		return Object.class;
	}
}

package adv_director.rw2.api.d_script.operators;

import adv_director.rw2.api.d_script.IExpression;
import adv_director.rw2.api.d_script.ScriptScope;

@Deprecated
public class ExpressionConcatenate implements IExpression<String>
{
	private final IExpression<?> e1;
	private final IExpression<?> e2;
	
	public ExpressionConcatenate(IExpression<?> e1, IExpression<?> e2)
	{
		this.e1 = e1;
		this.e2 = e2;
	}
	
	@Override
	public String eval(ScriptScope scope) throws Exception
	{
		return e1.eval(scope).toString() + e2.eval(scope).toString();
	}
	
	@Override
	public Class<String> type()
	{
		return String.class;
	}
}

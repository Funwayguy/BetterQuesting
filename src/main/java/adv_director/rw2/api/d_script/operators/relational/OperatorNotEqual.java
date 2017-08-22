package adv_director.rw2.api.d_script.operators.relational;

import adv_director.rw2.api.d_script.IExpression;
import adv_director.rw2.api.d_script.ScriptScope;

public class OperatorNotEqual implements IExpression<Boolean>
{
	private final IExpression<?> e1;
	private final IExpression<?> e2;
	
	public OperatorNotEqual(IExpression<?> e1, IExpression<?> e2)
	{
		this.e1 = e1;
		this.e2 = e2;
	}
	
	@Override
	public Boolean eval(ScriptScope scope) throws Exception
	{
		return e1.eval(scope) != e2.eval(scope);
	}
	
	@Override
	public Class<Boolean> type()
	{
		return Boolean.class;
	}
}

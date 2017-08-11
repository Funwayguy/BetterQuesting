package adv_director.rw2.api.d_script.operators;

import adv_director.rw2.api.d_script.IExpression;
import adv_director.rw2.api.d_script.ScriptScope;

public class ExpressionValue<T> implements IExpression<T>
{
	private final T val;
	
	public ExpressionValue(T val)
	{
		this.val = val;
	}
	
	@Override
	public T eval(ScriptScope scope)
	{
		return this.val;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public Class<T> type()
	{
		return (Class<T>)val.getClass();
	}
}

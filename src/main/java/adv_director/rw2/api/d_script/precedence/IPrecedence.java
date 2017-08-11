package adv_director.rw2.api.d_script.precedence;

import adv_director.rw2.api.d_script.ExpressionStream;
import adv_director.rw2.api.d_script.IExpression;

public interface IPrecedence
{
	public <T> IExpression<T> parse(ExpressionStream stream, Class<T> type) throws Exception;
}

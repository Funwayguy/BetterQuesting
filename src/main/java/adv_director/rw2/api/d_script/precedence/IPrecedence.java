package adv_director.rw2.api.d_script.precedence;

import adv_director.rw2.api.d_script.ExpressionStream;
import adv_director.rw2.api.d_script.IExpression;

public interface IPrecedence
{
	public IExpression<?> parse(ExpressionStream stream) throws Exception;
}

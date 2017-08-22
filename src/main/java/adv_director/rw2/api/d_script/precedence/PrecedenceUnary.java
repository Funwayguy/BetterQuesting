package adv_director.rw2.api.d_script.precedence;

import adv_director.rw2.api.d_script.ExpressionStream;
import adv_director.rw2.api.d_script.IExpression;

public class PrecedenceUnary implements IPrecedence
{
	@Override
	public IExpression<?> parse(ExpressionStream stream) throws Exception
	{
		// Merged with PrecedenceFactor
		return null;
	}
}

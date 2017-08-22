package adv_director.rw2.api.d_script.precedence;

import adv_director.rw2.api.d_script.ExpressionParser;
import adv_director.rw2.api.d_script.ExpressionStream;
import adv_director.rw2.api.d_script.IExpression;
import adv_director.rw2.api.d_script.operators.math.OperatorAddition;
import adv_director.rw2.api.d_script.operators.math.OperatorSubtraction;

public class PrecedenceAdditive implements IPrecedence
{
	@Override
	public IExpression<?> parse(ExpressionStream stream) throws Exception
	{
		IExpression<?> x = ExpressionParser.PREC_MULTI.parse(stream);
		
		for(;;)
		{
			if(stream.eat("+"))
			{
				x = new OperatorAddition(x, ExpressionParser.PREC_MULTI.parse(stream));
			} else if(stream.eat("-"))
			{
				x = new OperatorSubtraction(x, ExpressionParser.PREC_MULTI.parse(stream));
			} else
			{
				return x;
			}
		}
	}
}

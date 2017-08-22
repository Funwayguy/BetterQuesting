package adv_director.rw2.api.d_script.precedence;

import adv_director.rw2.api.d_script.ExpressionParser;
import adv_director.rw2.api.d_script.ExpressionStream;
import adv_director.rw2.api.d_script.IExpression;
import adv_director.rw2.api.d_script.operators.math.OperatorDivide;
import adv_director.rw2.api.d_script.operators.math.OperatorModulo;
import adv_director.rw2.api.d_script.operators.math.OperatorMultiply;

public class PrecedenceMultiplicative implements IPrecedence
{
	@Override
	public IExpression<?> parse(ExpressionStream stream) throws Exception
	{
		IExpression<?> x = ExpressionParser.PREC_FACTOR.parse(stream);
		
		for(;;)
		{
			if(stream.eat("*"))
			{
				x = new OperatorMultiply(x, ExpressionParser.PREC_FACTOR.parse(stream));
			} else if(stream.eat("/"))
			{
				x = new OperatorDivide(x, ExpressionParser.PREC_FACTOR.parse(stream));
			} else if(stream.eat("%"))
			{
				x = new OperatorModulo(x, ExpressionParser.PREC_FACTOR.parse(stream));
			} else
			{
				return x;
			}
		}
	}
	
}

package adv_director.rw2.api.d_script.precedence;

import adv_director.rw2.api.d_script.ExpressionParser;
import adv_director.rw2.api.d_script.ExpressionStream;
import adv_director.rw2.api.d_script.IExpression;
import adv_director.rw2.api.d_script.operators.relational.OperatorLessThan;
import adv_director.rw2.api.d_script.operators.relational.OperatorLessThanEqual;
import adv_director.rw2.api.d_script.operators.relational.OperatorMoreThan;
import adv_director.rw2.api.d_script.operators.relational.OperatorMoreThanEqual;

public class PrecedenceRelational implements IPrecedence
{
	@Override
	public IExpression<?> parse(ExpressionStream stream) throws Exception
	{
		IExpression<?> x = ExpressionParser.PREC_SHIFT.parse(stream);
		
		for(;;)
		{
			if(stream.eat(">="))
			{
				x = new OperatorMoreThanEqual(x, ExpressionParser.PREC_SHIFT.parse(stream));
			} else if(stream.eat(">"))
			{
				x = new OperatorMoreThan(x, ExpressionParser.PREC_SHIFT.parse(stream));
			} else if(stream.eat("<="))
			{
				x = new OperatorLessThanEqual(x, ExpressionParser.PREC_SHIFT.parse(stream));
			} else if(stream.eat("<"))
			{
				x = new OperatorLessThan(x, ExpressionParser.PREC_SHIFT.parse(stream));
			} else
			{
				return x;
			}
		}
	}
}

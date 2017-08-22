package adv_director.rw2.api.d_script.precedence;

import adv_director.rw2.api.d_script.ExpressionParser;
import adv_director.rw2.api.d_script.ExpressionStream;
import adv_director.rw2.api.d_script.IExpression;
import adv_director.rw2.api.d_script.operators.relational.OperatorEqual;
import adv_director.rw2.api.d_script.operators.relational.OperatorNotEqual;

public class PrecedenceEquality implements IPrecedence
{
	@Override
	public IExpression<?> parse(ExpressionStream stream) throws Exception
	{
		IExpression<?> x = ExpressionParser.PREC_RELATION.parse(stream);
		
		for(;;)
		{
			if(stream.eat("=="))
			{
				x = new OperatorEqual(x, ExpressionParser.PREC_RELATION.parse(stream));
			} else if(stream.eat("!="))
			{
				x = new OperatorNotEqual(x, ExpressionParser.PREC_RELATION.parse(stream));
			} else
			{
				return x;
			}
		}
	}
}

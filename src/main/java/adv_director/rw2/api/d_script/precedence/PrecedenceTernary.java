package adv_director.rw2.api.d_script.precedence;

import adv_director.rw2.api.d_script.ExpressionParser;
import adv_director.rw2.api.d_script.ExpressionStream;
import adv_director.rw2.api.d_script.IExpression;
import adv_director.rw2.api.d_script.operators.logic.OperatorTernary;

public class PrecedenceTernary implements IPrecedence
{
	@Override
	public IExpression<?> parse(ExpressionStream stream) throws Exception
	{
		IExpression<?> x = ExpressionParser.PREC_LOGIC.parse(stream);
		
		for(;;)
		{
			if(stream.eat("?"))
			{
				if(!Boolean.class.isAssignableFrom(x.type()))
				{
					throw new ClassCastException("Unable to cast " + x.type().getSimpleName() + " to " + Boolean.class.getSimpleName());
				}
				
				IExpression<?> e2 = ExpressionParser.PREC_FIRST.parse(stream);
				
				if(!stream.eat(":"))
				{
					throw new Exception("Incomplete ternary in expression");
				}
				
				IExpression<?> e3 = ExpressionParser.PREC_FIRST.parse(stream);
				
				x = new OperatorTernary(x, e2, e3);
			} else
			{
				return x;
			}
		}
	}
}

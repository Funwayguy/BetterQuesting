package adv_director.rw2.api.d_script.precedence;

import adv_director.rw2.api.d_script.ExpressionParser;
import adv_director.rw2.api.d_script.ExpressionStream;
import adv_director.rw2.api.d_script.IExpression;
import adv_director.rw2.api.d_script.operators.logic.OperatorTernary;

public class PrecedenceTernary implements IPrecedence
{
	@Override
	@SuppressWarnings("unchecked")
	public <T> IExpression<T> parse(ExpressionStream stream, Class<T> type) throws Exception
	{
		IExpression<?> x = ExpressionParser.PREC_LOGIC.parse(stream, Object.class);
		
		for(;;)
		{
			if(stream.eat("?"))
			{
				if(!Boolean.class.isAssignableFrom(x.type()))
				{
					throw new ClassCastException("Unable to cast " + x.type().getSimpleName() + " to " + Boolean.class.getSimpleName());
				}
				
				IExpression<?> e2 = ExpressionParser.PREC_FIRST.parse(stream, Object.class);
				
				if(!stream.eat(":"))
				{
					throw new Exception("Incomplete ternary in expression");
				}
				
				IExpression<?> e3 = ExpressionParser.PREC_FIRST.parse(stream, Object.class);
				Class<?> ternType = null;
				
				if(e2.type().isAssignableFrom(e3.type()))
				{
					ternType = e2.type();
				} else if(e3.type().isAssignableFrom(e2.type()))
				{
					ternType = e3.type();
				} else
				{
					ternType = Object.class;
				}
				
				x = new OperatorTernary(x, e2, e3, ternType); // This is probably going to blow up sooner or later
			} else if(type.isAssignableFrom(x.type()))
			{
				return (IExpression<T>)x;
			} else
			{
				throw new ClassCastException("Unable to cast " + x.type().getSimpleName() + " to " + type.getSimpleName());
			}
		}
	}
}

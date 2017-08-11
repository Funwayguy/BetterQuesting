package adv_director.rw2.api.d_script.precedence;

import adv_director.rw2.api.d_script.ExpressionParser;
import adv_director.rw2.api.d_script.ExpressionStream;
import adv_director.rw2.api.d_script.IExpression;
import adv_director.rw2.api.d_script.operators.ExpressionConcatenate;
import adv_director.rw2.api.d_script.operators.math.OperatorAddition;
import adv_director.rw2.api.d_script.operators.math.OperatorSubtraction;

public class PrecedenceAdditive implements IPrecedence
{
	@Override
	@SuppressWarnings("unchecked")
	public <T> IExpression<T> parse(ExpressionStream stream, Class<T> type) throws Exception
	{
		IExpression<?> x = ExpressionParser.PREC_MULTI.parse(stream, Object.class);
		
		for(;;)
		{
			if(stream.eat("+"))
			{
				IExpression<?> y = ExpressionParser.PREC_MULTI.parse(stream, Object.class);
				
				if(String.class.isAssignableFrom(x.type()) || String.class.isAssignableFrom(y.type()))
				{
					x = new ExpressionConcatenate(x, y);
				} else if(Number.class.isAssignableFrom(x.type()) && Number.class.isAssignableFrom(y.type()))
				{
					x = new OperatorAddition((IExpression<Number>)x, (IExpression<Number>)y);
				} else
				{
					throw new Exception("Unsupported opperand '+' on types " + x.type().getSimpleName() + " and " + y.type().getSimpleName());
				}
			} else if(stream.eat("-"))
			{
				if(Number.class.isAssignableFrom(x.type()))
				{
					x = new OperatorSubtraction((IExpression<Number>)x, ExpressionParser.PREC_MULTI.parse(stream, Number.class));
				} else
				{
					throw new Exception("Unsupported opperand '-' on type " + x.type().getSimpleName() + " and " + Number.class.getSimpleName());
				}
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

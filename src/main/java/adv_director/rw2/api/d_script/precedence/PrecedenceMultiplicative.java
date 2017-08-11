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
	@SuppressWarnings("unchecked")
	public <T> IExpression<T> parse(ExpressionStream stream, Class<T> type) throws Exception
	{
		IExpression<?> x = ExpressionParser.PREC_FACTOR.parse(stream, Object.class);
		
		for(;;)
		{
			if(stream.eat("*"))
			{
				if(Number.class.isAssignableFrom(x.type()))
				{
					x = new OperatorMultiply((IExpression<Number>)x, ExpressionParser.PREC_FACTOR.parse(stream, Number.class));
				} else
				{
					throw new Exception("Unsupported opperand '*' on type " + x.type().getSimpleName() + " and " + Number.class.getSimpleName());
				}
			} else if(stream.eat("/"))
			{
				if(Number.class.isAssignableFrom(x.type()))
				{
					x = new OperatorDivide((IExpression<Number>)x, ExpressionParser.PREC_FACTOR.parse(stream, Number.class));
				} else
				{
					throw new Exception("Unsupported opperand '/' on type " + x.type().getSimpleName() + " and " + Number.class.getSimpleName());
				}
			} else if(stream.eat("%"))
			{
				if(Number.class.isAssignableFrom(x.type()))
				{
					x = new OperatorModulo((IExpression<Number>)x, ExpressionParser.PREC_FACTOR.parse(stream, Number.class));
				} else
				{
					throw new Exception("Unsupported opperand '%' on type " + x.type().getSimpleName() + " and " + Number.class.getSimpleName());
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

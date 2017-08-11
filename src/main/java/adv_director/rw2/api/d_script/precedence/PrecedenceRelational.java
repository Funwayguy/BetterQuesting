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
	@SuppressWarnings("unchecked")
	public <T> IExpression<T> parse(ExpressionStream stream, Class<T> type) throws Exception
	{
		IExpression<?> x = ExpressionParser.PREC_SHIFT.parse(stream, Object.class);
		
		for(;;)
		{
			if(stream.eat(">="))
			{
				if(Number.class.isAssignableFrom(x.type()))
				{
					x = new OperatorMoreThanEqual((IExpression<Number>)x, ExpressionParser.PREC_SHIFT.parse(stream, Number.class));
				} else
				{
					throw new Exception("Unsupported opperand '>=' on type " + x.type().getSimpleName() + " and " + Number.class.getSimpleName());
				}
			} else if(stream.eat(">"))
			{
				if(Number.class.isAssignableFrom(x.type()))
				{
					x = new OperatorMoreThan((IExpression<Number>)x, ExpressionParser.PREC_SHIFT.parse(stream, Number.class));
				} else
				{
					throw new Exception("Unsupported opperand '>' on type " + x.type().getSimpleName() + " and " + Number.class.getSimpleName());
				}
			} else if(stream.eat("<="))
			{
				if(Number.class.isAssignableFrom(x.type()))
				{
					x = new OperatorLessThanEqual((IExpression<Number>)x, ExpressionParser.PREC_SHIFT.parse(stream, Number.class));
				} else
				{
					throw new Exception("Unsupported opperand '<=' on type " + x.type().getSimpleName() + " and " + Number.class.getSimpleName());
				}
			} else if(stream.eat("<"))
			{
				if(Number.class.isAssignableFrom(x.type()))
				{
					x = new OperatorLessThan((IExpression<Number>)x, ExpressionParser.PREC_SHIFT.parse(stream, Number.class));
				} else
				{
					throw new Exception("Unsupported opperand '<' on type " + x.type().getSimpleName() + " and " + Number.class.getSimpleName());
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

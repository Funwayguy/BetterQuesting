package adv_director.rw2.api.d_script.precedence;

import adv_director.rw2.api.d_script.ExpressionParser;
import adv_director.rw2.api.d_script.ExpressionStream;
import adv_director.rw2.api.d_script.IExpression;
import adv_director.rw2.api.d_script.operators.binary.OperatorShiftLeft;
import adv_director.rw2.api.d_script.operators.binary.OperatorShiftRight;
import adv_director.rw2.api.d_script.operators.binary.OperatorUnsignedShiftRight;

public class PrecedenceShift implements IPrecedence
{
	@Override
	@SuppressWarnings("unchecked")
	public <T> IExpression<T> parse(ExpressionStream stream, Class<T> type) throws Exception
	{
		IExpression<?> x = ExpressionParser.PREC_ADD.parse(stream, Object.class);
		
		for(;;)
		{
			if(stream.eat("<<"))
			{
				if(Number.class.isAssignableFrom(x.type()))
				{
					x = new OperatorShiftLeft((IExpression<Number>)x, ExpressionParser.PREC_ADD.parse(stream, Number.class));
				} else
				{
					throw new Exception("Unsupported opperand '<<' on type " + x.type().getSimpleName() + " and " + Number.class.getSimpleName());
				}
			} else if(stream.eat(">>>"))
			{
				if(Number.class.isAssignableFrom(x.type()))
				{
					x = new OperatorUnsignedShiftRight((IExpression<Number>)x, ExpressionParser.PREC_ADD.parse(stream, Number.class));
				} else
				{
					throw new Exception("Unsupported opperand '>>>' on type " + x.type().getSimpleName() + " and " + Number.class.getSimpleName());
				}
			} else if(stream.eat(">>"))
			{
				if(Number.class.isAssignableFrom(x.type()))
				{
					x = new OperatorShiftRight((IExpression<Number>)x, ExpressionParser.PREC_ADD.parse(stream, Number.class));
				} else
				{
					throw new Exception("Unsupported opperand '>>' on type " + x.type().getSimpleName() + " and " + Number.class.getSimpleName());
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

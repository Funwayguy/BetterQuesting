package adv_director.rw2.api.d_script.precedence;

import adv_director.rw2.api.d_script.ExpressionParser;
import adv_director.rw2.api.d_script.ExpressionStream;
import adv_director.rw2.api.d_script.IExpression;
import adv_director.rw2.api.d_script.operators.logic.OperatorLogicalAnd;
import adv_director.rw2.api.d_script.operators.logic.OperatorLogicalOr;

public class PrecedenceLogical implements IPrecedence
{
	@Override
	public <T> IExpression<T> parse(ExpressionStream stream, Class<T> type) throws Exception
	{
		return parseOr(stream, type);
	}
	
	@SuppressWarnings("unchecked")
	private <T> IExpression<T> parseOr(ExpressionStream stream, Class<T> type) throws Exception
	{
		IExpression<?> x = parseAnd(stream, Object.class);
		
		for(;;)
		{
			if(stream.eat("||"))
			{
				if(Boolean.class.isAssignableFrom(x.type()))
				{
					x = new OperatorLogicalOr((IExpression<Boolean>)x, parseAnd(stream, Boolean.class));
				} else
				{
					throw new Exception("Unsupported opperand '||' on type " + x.type().getSimpleName() + " and " + Boolean.class.getSimpleName());
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
	
	@SuppressWarnings("unchecked")
	private <T> IExpression<T> parseAnd(ExpressionStream stream, Class<T> type) throws Exception
	{
		IExpression<?> x = ExpressionParser.PREC_BITWISE.parse(stream, Object.class);
		
		for(;;)
		{
			if(stream.eat("&&"))
			{
				if(Boolean.class.isAssignableFrom(x.type()))
				{
					x = new OperatorLogicalAnd((IExpression<Boolean>)x, ExpressionParser.PREC_BITWISE.parse(stream, Boolean.class));
				} else
				{
					throw new Exception("Unsupported opperand '&&' on type " + x.type().getSimpleName() + " and " + Boolean.class.getSimpleName());
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

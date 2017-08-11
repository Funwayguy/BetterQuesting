package adv_director.rw2.api.d_script.precedence;

import adv_director.rw2.api.d_script.ExpressionParser;
import adv_director.rw2.api.d_script.ExpressionStream;
import adv_director.rw2.api.d_script.IExpression;
import adv_director.rw2.api.d_script.operators.binary.OperatorBitwiseAnd;
import adv_director.rw2.api.d_script.operators.binary.OperatorBitwiseExOr;
import adv_director.rw2.api.d_script.operators.binary.OperatorBitwiseOr;

public class PrecedenceBitwise implements IPrecedence
{
	@Override
	public <T> IExpression<T> parse(ExpressionStream stream, Class<T> type) throws Exception
	{
		return parseOr(stream, type);
	}
	
	@SuppressWarnings("unchecked")
	private <T> IExpression<T> parseOr(ExpressionStream stream, Class<T> type) throws Exception
	{
		IExpression<?> x = parseExOr(stream, Object.class);
		
		for(;;)
		{
			if(!stream.eat("||", false) && stream.eat("|"))
			{
				if(Number.class.isAssignableFrom(x.type()))
				{
					x = new OperatorBitwiseOr((IExpression<Number>)x, parseExOr(stream, Number.class));
				} else
				{
					throw new Exception("Unsupported opperand '|' on type " + x.type().getSimpleName() + " and " + Number.class.getSimpleName());
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
	private <T> IExpression<T> parseExOr(ExpressionStream stream, Class<T> type) throws Exception
	{
		IExpression<?> x = parseAnd(stream, Object.class);
		
		for(;;)
		{
			if(stream.eat("^"))
			{
				if(Number.class.isAssignableFrom(x.type()))
				{
					x = new OperatorBitwiseExOr((IExpression<Number>)x, parseAnd(stream, Number.class));
				} else
				{
					throw new Exception("Unsupported opperand '^' on type " + x.type().getSimpleName() + " and " + Number.class.getSimpleName());
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
		IExpression<?> x = ExpressionParser.PREC_EQUALITY.parse(stream, Object.class);
		
		for(;;)
		{
			if(!stream.eat("&&", false) && stream.eat("&"))
			{
				if(Number.class.isAssignableFrom(x.type()))
				{
					x = new OperatorBitwiseAnd((IExpression<Number>)x, ExpressionParser.PREC_EQUALITY.parse(stream, Number.class));
				} else
				{
					throw new Exception("Unsupported opperand '^' on type " + x.type().getSimpleName() + " and " + Number.class.getSimpleName());
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

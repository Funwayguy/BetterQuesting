package adv_director.rw2.api.d_script.precedence;

import adv_director.rw2.api.d_script.ExpressionParser;
import adv_director.rw2.api.d_script.ExpressionStream;
import adv_director.rw2.api.d_script.IExpression;
import adv_director.rw2.api.d_script.operators.logic.OperatorLogicalAnd;
import adv_director.rw2.api.d_script.operators.logic.OperatorLogicalOr;

public class PrecedenceLogical implements IPrecedence
{
	@Override
	public IExpression<?> parse(ExpressionStream stream) throws Exception
	{
		return parseOr(stream);
	}
	
	private IExpression<?> parseOr(ExpressionStream stream) throws Exception
	{
		IExpression<?> x = parseAnd(stream);
		
		for(;;)
		{
			if(stream.eat("||"))
			{
				x = new OperatorLogicalOr(x, parseAnd(stream));
			} else
			{
				return x;
			}
		}
	}
	
	private IExpression<?> parseAnd(ExpressionStream stream) throws Exception
	{
		IExpression<?> x = ExpressionParser.PREC_BITWISE.parse(stream);
		
		for(;;)
		{
			if(stream.eat("&&"))
			{
				if(Boolean.class.isAssignableFrom(x.type()))
				{
					x = new OperatorLogicalAnd(x, ExpressionParser.PREC_BITWISE.parse(stream));
				} else
				{
					throw new Exception("Unsupported opperand '&&' on type " + x.type().getSimpleName() + " and " + Boolean.class.getSimpleName());
				}
			} else
			{
				return x;
			}
		}
	}
}

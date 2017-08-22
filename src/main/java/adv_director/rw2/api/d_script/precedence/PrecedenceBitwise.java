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
	public IExpression<?> parse(ExpressionStream stream) throws Exception
	{
		return parseOr(stream);
	}
	
	private IExpression<?> parseOr(ExpressionStream stream) throws Exception
	{
		IExpression<?> x = parseExOr(stream);
		
		for(;;)
		{
			if(!stream.eat("||", false) && stream.eat("|"))
			{
				x = new OperatorBitwiseOr(x, parseExOr(stream));
			} else
			{
				return x;
			}
		}
	}
	
	private IExpression<?> parseExOr(ExpressionStream stream) throws Exception
	{
		IExpression<?> x = parseAnd(stream);
		
		for(;;)
		{
			if(stream.eat("^"))
			{
				x = new OperatorBitwiseExOr(x, parseAnd(stream));
			} else
			{
				return x;
			}
		}
	}
	
	private IExpression<?> parseAnd(ExpressionStream stream) throws Exception
	{
		IExpression<?> x = ExpressionParser.PREC_EQUALITY.parse(stream);
		
		for(;;)
		{
			if(!stream.eat("&&", false) && stream.eat("&"))
			{
				x = new OperatorBitwiseAnd(x, ExpressionParser.PREC_EQUALITY.parse(stream));
			} else
			{
				return x;
			}
		}
	}
}

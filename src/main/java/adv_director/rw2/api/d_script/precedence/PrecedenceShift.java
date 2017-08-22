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
	public IExpression<?> parse(ExpressionStream stream) throws Exception
	{
		IExpression<?> x = ExpressionParser.PREC_ADD.parse(stream);
		
		for(;;)
		{
			if(stream.eat("<<"))
			{
				x = new OperatorShiftLeft(x, ExpressionParser.PREC_ADD.parse(stream));
			} else if(stream.eat(">>>"))
			{
				x = new OperatorUnsignedShiftRight(x, ExpressionParser.PREC_ADD.parse(stream));
			} else if(stream.eat(">>"))
			{
				x = new OperatorShiftRight(x, ExpressionParser.PREC_ADD.parse(stream));
			} else
			{
				return x;
			}
		}
	}
}

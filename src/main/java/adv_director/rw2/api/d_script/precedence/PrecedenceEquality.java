package adv_director.rw2.api.d_script.precedence;

import adv_director.rw2.api.d_script.ExpressionParser;
import adv_director.rw2.api.d_script.ExpressionStream;
import adv_director.rw2.api.d_script.IExpression;
import adv_director.rw2.api.d_script.operators.relational.OperatorEqual;
import adv_director.rw2.api.d_script.operators.relational.OperatorNotEqual;

public class PrecedenceEquality implements IPrecedence
{
	@Override
	@SuppressWarnings("unchecked")
	public <T> IExpression<T> parse(ExpressionStream stream, Class<T> type) throws Exception
	{
		IExpression<?> x = ExpressionParser.PREC_RELATION.parse(stream, Object.class);
		
		for(;;)
		{
			System.out.println(stream.current());
			if(stream.eat("=="))
			{
				System.out.println("Eat meeee");
				x = new OperatorEqual(x, ExpressionParser.PREC_RELATION.parse(stream, Object.class));
			} else if(stream.eat("!="))
			{
				x = new OperatorNotEqual(x, ExpressionParser.PREC_RELATION.parse(stream, Object.class));
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

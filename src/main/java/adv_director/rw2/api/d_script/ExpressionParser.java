package adv_director.rw2.api.d_script;

import java.lang.reflect.MalformedParametersException;
import adv_director.rw2.api.d_script.precedence.IPrecedence;
import adv_director.rw2.api.d_script.precedence.PrecedenceAdditive;
import adv_director.rw2.api.d_script.precedence.PrecedenceBitwise;
import adv_director.rw2.api.d_script.precedence.PrecedenceEquality;
import adv_director.rw2.api.d_script.precedence.PrecedenceFactor;
import adv_director.rw2.api.d_script.precedence.PrecedenceLogical;
import adv_director.rw2.api.d_script.precedence.PrecedenceMultiplicative;
import adv_director.rw2.api.d_script.precedence.PrecedenceRelational;
import adv_director.rw2.api.d_script.precedence.PrecedenceShift;
import adv_director.rw2.api.d_script.precedence.PrecedenceTernary;

public class ExpressionParser
{
	public static final IPrecedence PREC_FACTOR = new PrecedenceFactor();
	public static final IPrecedence PREC_POSTFIX = null; // Do I really have to support this?
	public static final IPrecedence PREC_UNARY = null; // cheated
	public static final IPrecedence PREC_MULTI = new PrecedenceMultiplicative();
	public static final IPrecedence PREC_ADD = new PrecedenceAdditive();
	public static final IPrecedence PREC_SHIFT = new PrecedenceShift();
	public static final IPrecedence PREC_RELATION = new PrecedenceRelational();
	public static final IPrecedence PREC_EQUALITY = new PrecedenceEquality();
	public static final IPrecedence PREC_BITWISE = new PrecedenceBitwise();
	public static final IPrecedence PREC_LOGIC = new PrecedenceLogical();
	public static final IPrecedence PREC_TERNARY = new PrecedenceTernary();
	
	public static final IPrecedence PREC_FIRST = PREC_TERNARY;
	
	public static IExpression<?> parse(String s) throws Exception
	{
		ExpressionStream stream = new ExpressionStream(s);
		
		if(stream.size() <= 0 || !stream.isReady())
		{
			throw new MalformedParametersException("Unable to tokenize expression: " + s);
		}
		
		IExpression<?> expression = PREC_FIRST.parse(stream);//parseExpression(stream, type);
		
		if(stream.isReady())
		{
			throw new IllegalStateException("Unexpected token: " + stream.current());
		}
		
		return expression;
	}
}

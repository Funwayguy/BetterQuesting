package adv_director.rw2.api.d_script;

import java.lang.reflect.MalformedParametersException;
import adv_director.rw2.api.d_script.operators.ExpressionValue;
import adv_director.rw2.api.d_script.operators.math.ExpressionExponent;
import adv_director.rw2.api.d_script.operators.math.OperatorAddition;
import adv_director.rw2.api.d_script.operators.math.OperatorDivide;
import adv_director.rw2.api.d_script.operators.math.OperatorModulo;
import adv_director.rw2.api.d_script.operators.math.OperatorMultiply;
import adv_director.rw2.api.d_script.operators.math.OperatorNegative;
import adv_director.rw2.api.d_script.operators.math.OperatorSubtraction;
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
	
	public static <T> IExpression<T> parse(String s, Class<T> type) throws Exception
	{
		ExpressionStream stream = new ExpressionStream(s);
		
		if(stream.size() <= 0 || !stream.isReady())
		{
			throw new MalformedParametersException("Unable to tokenize expression: " + s);
		}
		
		IExpression<T> expression = PREC_FIRST.parse(stream, type);//parseExpression(stream, type);
		
		if(stream.isReady())
		{
			throw new IllegalStateException("Unexpected token: " + stream.current());
		}
		
		return expression;
	}
	
	// Expressions
	@SuppressWarnings("unchecked")
	private static <T> IExpression<T> parseExpression(ExpressionStream stream, Class<T> type)
	{
		IExpression<?> x = parseTerm(stream, Object.class);
		
		for(;;)
		{
			if(stream.eat("+"))
			{
				x = new OperatorAddition((IExpression<Number>)x, parseTerm(stream, Number.class));
			} else if(stream.eat("-"))
			{
				x = new OperatorSubtraction((IExpression<Number>)x, parseTerm(stream, Number.class));
			} else if(type.isAssignableFrom(x.type()))
			{
				return (IExpression<T>)x;
			} else
			{
				throw new ClassCastException("Unable to cast " + x.type().getSimpleName() + " to " + type.getSimpleName());
			}
		}
	}
	
	// Operators
	@SuppressWarnings("unchecked")
	private static <T> IExpression<T> parseTerm(ExpressionStream stream, Class<T> type)
	{
		IExpression<?> x = parseFactor(stream, Object.class);
		
		for(;;)
		{
			if(stream.eat("*"))
			{
				x = new OperatorMultiply((IExpression<Number>)x, parseFactor(stream, Number.class));
			} else if(stream.eat("/"))
			{
				x = new OperatorDivide((IExpression<Number>)x, parseFactor(stream, Number.class));
			} else if(stream.eat("%"))
			{
				x = new OperatorModulo((IExpression<Number>)x, parseFactor(stream, Number.class));
			} else if(type.isAssignableFrom(x.type()))
			{
				return (IExpression<T>)x;
			} else
			{
				throw new ClassCastException("Unable to cast " + x.type().getSimpleName() + " to " + type.getSimpleName());
			}
		}
	}
	
	// Functions, variables, raw values and higher precedent operators
	@SuppressWarnings("unchecked")
	private static <T> IExpression<T> parseFactor(ExpressionStream stream, Class<T> type)
	{
		if(stream.eat("+"))
		{
			return parseFactor(stream, type);
		}
		
		if(stream.eat("-"))
		{
			return (IExpression<T>)new OperatorNegative(parseFactor(stream, Number.class));
		}
		
		IExpression<T> x = null;
		
		if(stream.eat("("))
		{
			x = parseExpression(stream, type);
			if(!stream.eat(")"))
			{
				throw new MalformedParametersException("Unclosed bracket in expression: " + stream.rawString());
			}
		} else if(stream.isNumber())
		{
			final Number n = stream.getAsNumber();
			stream.nextToken();
			
			x = (IExpression<T>)new ExpressionValue<Number>(n);
		} else if(Character.isLetter(stream.getAsString().toCharArray()[0])) // Function or variable
		{
			String fName = stream.getAsString();
			stream.nextToken();
			
			IFunction<?> func = FunctionRegistry.INSTANCE.getFunction(fName);
			
			if(func == null)
			{
				throw new IllegalArgumentException("Unknown function: " + fName);
			} else
			{
				Class<?>[] aTypes = func.getArgumentTypes();
				Object[] args = new Object[aTypes.length];
				
				if(!stream.eat("("))
				{
					throw new MalformedParametersException("Missing function brace at token: " + stream.position());
				}
				
				for(int i = 0; i < aTypes.length; i++)
				{
					args[i] = parseExpression(stream, aTypes[i]);
					
					if(i + 1 < aTypes.length && !stream.eat(","))
					{
						throw new MalformedParametersException("Missing function comma at token: " + stream.position());
					}
				}
				
				if(!stream.eat(")"))
				{
					throw new MalformedParametersException("Missing function brace at token: " + stream.position());
				}
			}
		} else
		{
			throw new MalformedParametersException("Unexpected token: " + stream.current());
		}
		
		if(stream.eat("^"))
		{
			x = (IExpression<T>)new ExpressionExponent((IExpression<Number>)x, parseFactor(stream, Number.class));
		}
		
		if(x == null)
		{
			throw new IllegalStateException("Unexpected token: " + stream.current());
		}
		
		return x;
	}
}

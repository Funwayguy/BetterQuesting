package adv_director.rw2.api.d_script.precedence;

import java.util.ArrayList;
import java.util.List;
import adv_director.rw2.api.d_script.ExpressionParser;
import adv_director.rw2.api.d_script.ExpressionStream;
import adv_director.rw2.api.d_script.IExpression;
import adv_director.rw2.api.d_script.operators.ExpressionArray;
import adv_director.rw2.api.d_script.operators.ExpressionFunction;
import adv_director.rw2.api.d_script.operators.ExpressionValue;
import adv_director.rw2.api.d_script.operators.ExpressionVariable;
import adv_director.rw2.api.d_script.operators.binary.OperatorBitwiseNOT;
import adv_director.rw2.api.d_script.operators.logic.OperatorLogicalNOT;
import adv_director.rw2.api.d_script.operators.math.OperatorNegative;

public class PrecedenceFactor implements IPrecedence
{
	@Override
	@SuppressWarnings("unchecked")
	public <T> IExpression<T> parse(ExpressionStream stream, Class<T> type) throws Exception
	{
		// Unary prefix
		
		if(stream.eat("+"))
		{
			if(type.isAssignableFrom(Number.class))
			{
				return (IExpression<T>)parse_(stream, Number.class);
			} else
			{
				throw new ClassCastException("Unable to cast " + Number.class.getSimpleName() + " to " + type.getSimpleName());
			}
		} else if(stream.eat("-"))
		{
			if(type.isAssignableFrom(Number.class))
			{
				return (IExpression<T>)new OperatorNegative(parse_(stream, Number.class));
			} else
			{
				throw new ClassCastException("Unable to cast " + Number.class.getSimpleName() + " to " + type.getSimpleName());
			}
		} else if(stream.eat("~"))
		{
			if(type.isAssignableFrom(Number.class))
			{
				return (IExpression<T>)new OperatorBitwiseNOT(parse_(stream, Number.class));
			} else
			{
				throw new ClassCastException("Unable to cast " + Number.class.getSimpleName() + " to " + type.getSimpleName());
			}
		} else if(stream.eat("!"))
		{
			if(type.isAssignableFrom(Boolean.class))
			{
				return (IExpression<T>)new OperatorLogicalNOT(parse_(stream, Boolean.class));
			} else
			{
				throw new ClassCastException("Unable to cast " + Boolean.class.getSimpleName() + " to " + type.getSimpleName());
			}
		}
		
		return parse_(stream, type);
	}
	
	@SuppressWarnings("unchecked")
	private <T> IExpression<T> parse_(ExpressionStream stream, Class<T> type) throws Exception
	{
		if(stream.eat("(")) // Parentheses
		{
			IExpression<T> x = ExpressionParser.PREC_FIRST.parse(stream, type);
			
			if(!stream.eat(")"))
			{
				throw new Exception("Unclosed parentheses in expression");
			} else if(!type.isAssignableFrom(x.type()))
			{
				throw new ClassCastException("Unable to cast " + x.type().getSimpleName() + " to " + type.getSimpleName());
			} else
			{
				return x;
			}
		} else if(stream.isReady() && stream.current().equals('"')) // String (FML)
		{
			stream.skipWhitespace(false); // Time to break everything
			stream.eat("\"");
			boolean closed = false;
			String s = "";
			
			while(stream.isReady())
			{
				if(stream.eat("\""))
				{
					closed = true;
					break;
				} else if(stream.eat("\\"))
				{
					if(stream.eat("\""))
					{
						s += "\"";
					} else
					{
						s += "\\";
					}
					
					continue;
				} else
				{
					s += stream.getAsString();
					stream.nextToken();
				}
			}
			
			if(!closed)
			{
				throw new Exception("Unclosed quote in expression");
			} else if(type.isAssignableFrom(String.class))
			{
				System.out.println("Parsed string: '" + s + "'");
				stream.skipWhitespace(true); // Back to logic and sanity
				return (IExpression<T>)new ExpressionValue<String>(s);
			} else
			{
				throw new Exception("Unable to cast " + String.class.getSimpleName() + " to " + type.getSimpleName());
			}
		} else if(stream.isNumber())
		{
			Number n = stream.getAsNumber();
			stream.nextToken();
			
			if(!type.isAssignableFrom(Number.class))
			{
				throw new ClassCastException("Unable to cast " + Number.class.getSimpleName() + " to " + type.getSimpleName());
			} else
			{
				return (IExpression<T>)new ExpressionValue<Number>(n);
			}
		} else if(Character.isLetter(stream.getAsString().toCharArray()[0])) // Function, variable or array
		{
			String name = stream.getAsString();
			stream.nextToken();
			
			if(stream.eat("(")) // Function
			{
				List<IExpression<?>> args = new ArrayList<IExpression<?>>();
				boolean closed = false;
				
				while(stream.isReady())
				{
					if(stream.eat(")"))
					{
						closed = true;
						break;
					}
					
					args.add(ExpressionParser.PREC_FIRST.parse(stream, Object.class));
					
					if(stream.eat(","))
					{
						continue;
					} else if(stream.eat(")"))
					{
						closed = true;
						break;
					} else
					{
						throw new Exception("Unclosed round bracket on function: " + name);
					}
				}
				
				// Read function args
				
				if(!closed)
				{
					throw new Exception("Unclosed round bracket on function: " + name);
				} else
				{
					return new ExpressionFunction<T>(name, type, args.toArray(new IExpression<?>[0]));
				}
			} else if(stream.eat("[")) // Array
			{
				IExpression<Number> n = ExpressionParser.PREC_FIRST.parse(stream, Number.class);
				
				if(!stream.eat("]"))
				{
					throw new Exception("Unclosed square bracket on array: " + name);
				} else
				{
					return new ExpressionArray<T>(name, type, n);
				}
			} else // Variable
			{
				return new ExpressionVariable<T>(name, type);
			}
			
		} else
		{
			throw new Exception("Unexpected token in expression: " + stream.current());
		}
		
		//throw new Exception("How did you even get here?! Stream Ready: " + stream.isReady()); // Should be dead code
	}
}

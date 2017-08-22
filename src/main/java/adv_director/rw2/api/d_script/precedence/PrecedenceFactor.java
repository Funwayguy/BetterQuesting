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
import adv_director.rw2.api.d_script.operators.math.OperatorPositive;

public class PrecedenceFactor implements IPrecedence
{
	@Override
	public IExpression<?> parse(ExpressionStream stream) throws Exception
	{
		// Unary prefix
		
		if(stream.eat("+"))
		{
			return new OperatorPositive(parse_(stream));
		} else if(stream.eat("-"))
		{
			return new OperatorNegative(parse_(stream));
		} else if(stream.eat("~"))
		{
			return new OperatorBitwiseNOT(parse_(stream));
		} else if(stream.eat("!"))
		{
			return new OperatorLogicalNOT(parse_(stream));
		}
		
		return parse_(stream);
	}
	
	private IExpression<?> parse_(ExpressionStream stream) throws Exception
	{
		if(stream.eat("(")) // Parentheses
		{
			IExpression<?> x = ExpressionParser.PREC_FIRST.parse(stream);
			
			if(!stream.eat(")"))
			{
				throw new Exception("Unclosed parentheses in expression");
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
			} else
			{
				stream.skipWhitespace(true); // Back to logic and sanity
				return new ExpressionValue<String>(s);
			}
		} else if(stream.isNumber())
		{
			Number n = stream.getAsNumber();
			stream.nextToken();
			return new ExpressionValue<Number>(n);
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
					
					args.add(ExpressionParser.PREC_FIRST.parse(stream));
					
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
					return new ExpressionFunction(name, args.toArray(new IExpression<?>[0]));
				}
			} else if(stream.eat("[")) // Array
			{
				IExpression<?> n = ExpressionParser.PREC_FIRST.parse(stream);
				
				if(!stream.eat("]"))
				{
					throw new Exception("Unclosed square bracket on array: " + name);
				} else
				{
					return new ExpressionArray(name, n);
				}
			} else // Variable
			{
				return new ExpressionVariable(name);
			}
		} else
		{
			throw new Exception("Unexpected token in expression: " + stream.current());
		}
		
		//throw new Exception("How did you even get here?! Stream Ready: " + stream.isReady()); // Should be dead code
	}
}

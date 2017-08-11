package adv_director.rw2.api.d_script;

import java.io.IOException;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

public class ExpressionStream
{
	private final String expression;
	private final List<Object> tokens;
	private int readHead = 0;
	private boolean skipSpaces = true;
	
	public ExpressionStream(String exp) throws IOException
	{
		this.expression = exp;
		this.tokens = tokenize(exp);
	}
	
	public void reset()
	{
		readHead = 0;
	}
	
	public void skipWhitespace(boolean enable)
	{
		skipSpaces = enable;
		
		if(enable) // Just in case
		{
			while(isReady() && current().equals(' '))
			{
				readHead++;
			}
		}
	}
	
	public int size()
	{
		return tokens.size();
	}
	
	public int position()
	{
		return readHead;
	}
	
	public String rawString()
	{
		return expression;
	}
	
	public boolean isReady()
	{
		return readHead >= 0 && readHead < tokens.size();
	}
	
	public boolean hasNext()
	{
		return (readHead + 1) < tokens.size();
	}
	
	public boolean nextToken()
	{
		return nextToken(1);
	}
	
	public boolean nextToken(int move)
	{
		if(move > 0 && isReady())
		{
			readHead += move;
		}
		
		if(skipSpaces)
		{
			while(isReady() && current().equals(' '))
			{
				readHead++;
			}
		}
		
		return isReady();
	}
	
	public Object current()
	{
		if(!isReady())
		{
			return null;
		}
		
		return tokens.get(readHead);
	}
	
	public boolean isNumber()
	{
		return current() instanceof Number;
	}
	
	public Number getAsNumber()
	{
		return (Number)current();
	}
	
	public String getAsString()
	{
		return current().toString();
	}
	
	public boolean eat(String s)
	{
		return eat(s, true);
	}
	
	public boolean eat(String s, boolean doEat)
	{
		if(s == null || s.length() <= 0 || !isReady())
		{
			return false;
		}
		
		Object o = tokens.get(readHead);
		
		if(o instanceof String && s.equals((String)o))
		{
			if(doEat)
			{
				nextToken();
			}
			return true;
		} else if(o instanceof Character && s.equals(((Character)o).toString()))
		{
			if(doEat)
			{
				nextToken();
			}
			return true;
		} else if(s.length() > 1 && tokens.size() - readHead >= s.length())// Consecutive symbols
		{
			for(int i = 0; i < s.length(); i++)
			{
				o = tokens.get(readHead + i);
				char c = s.charAt(i);
				
				if(!(o instanceof Character) || !((Character)o).equals(c))
				{
					return false;
				}
			}
			
			if(doEat)
			{
				nextToken(s.length());
			}
			return true;
		}
		
		return false;
	}
	
	private List<Object> tokenize(String exp) throws IOException
	{
		StreamTokenizer tStream = new StreamTokenizer(new StringReader(exp));
		List<Object> tokBuf = new ArrayList<Object>();
		tStream.ordinaryChar(' ');
		tStream.ordinaryChar('"');
		
		while(tStream.nextToken() != StreamTokenizer.TT_EOF)
		{
			switch(tStream.ttype)
			{
				case StreamTokenizer.TT_NUMBER:
					
					if(tStream.nval == (long)tStream.nval)
					{
						tokBuf.add((long)tStream.nval);
					} else
					{
						tokBuf.add(tStream.nval);
					}
					break;
				case StreamTokenizer.TT_WORD:
					tokBuf.add(tStream.sval);
					break;
				default:
					tokBuf.add((char)tStream.ttype);
			}
		}
		
		return tokBuf;
	}
}

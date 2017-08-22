package adv_director.rw2.api.d_script.operators;

// I got lazy okay. Leave me alone
public class NumberParserUtility
{
	public static boolean hasDouble(Number... nums)
	{
		for(Number n : nums)
		{
			if(n instanceof Double || n instanceof Float)
			{
				return true;
			}
		}
		
		return false;
	}
}

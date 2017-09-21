package adv_director.rw2.api.utils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class AnnoSearch
{
	public static <T> List<Method> getAnnotatedMethods(Class<?> base, Class<? extends Annotation> anno, Class<?>[] args)
	{
		List<Method> list = new ArrayList<Method>();
		
		for(Method m : base.getDeclaredMethods())
		{
			if(m.isAnnotationPresent(anno))
			{
				
			}
		}
		
		return list;
	}
}

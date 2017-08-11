package adv_director.rw2.api.d_script;

public interface IFunction<T>
{
	public T invokeFunction(Object[] args);
	public Class<?>[] getArgumentTypes();
	public Class<T> getReturnType();
}

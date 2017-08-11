package adv_director.rw2.api.d_script;

public interface IExpression<T>
{
	public T eval(ScriptScope scope) throws Exception;
	public Class<T> type();
}

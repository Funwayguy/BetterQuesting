package adv_director.rw2.api.client.gui.controls;

public interface IValueIO<T>
{
	public T readValue();
	public void writeValue(T value);
}

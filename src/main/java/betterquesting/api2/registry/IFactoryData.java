package betterquesting.api2.registry;

public interface IFactoryData<T, E> extends IFactory<T>
{
    T loadFromData(E data);
}

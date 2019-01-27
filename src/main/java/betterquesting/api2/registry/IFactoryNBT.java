package betterquesting.api2.registry;

public interface IFactoryNBT<T, E> extends IFactory<T>
{
    T loadFromNBT(E nbt);
}

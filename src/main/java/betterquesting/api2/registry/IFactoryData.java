package betterquesting.api2.registry;

@Deprecated // This is stupid and can be done with lambdas
public interface IFactoryData<T, E> extends IFactory<T> {
    T loadFromData(E data);
}

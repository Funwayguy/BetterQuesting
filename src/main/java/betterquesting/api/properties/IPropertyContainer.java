package betterquesting.api.properties;

public interface IPropertyContainer {
    <T> T getProperty(IPropertyType<T> prop);

    <T> T getProperty(IPropertyType<T> prop, T def);

    boolean hasProperty(IPropertyType<?> prop);

    void removeProperty(IPropertyType<?> prop);

    <T> void setProperty(IPropertyType<T> prop, T value);

    void removeAllProps();
}
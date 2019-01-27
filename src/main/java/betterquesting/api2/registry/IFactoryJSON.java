package betterquesting.api2.registry;

import com.google.gson.JsonObject;

public interface IFactoryJSON<T, E extends JsonObject> extends IFactory<T>
{
    T loadFromJSON(E json);
}

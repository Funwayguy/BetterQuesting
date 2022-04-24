package betterquesting.legacy;

import com.google.gson.JsonElement;

public interface ILegacyLoader {
    void readFromJson(JsonElement json);

    void readProgressFromJson(JsonElement json);
}

package betterquesting.legacy;

import betterquesting.api.enums.EnumSaveType;
import com.google.gson.JsonObject;

public interface ILegacyLoader
{
	public void readFromJson(JsonObject json, EnumSaveType saveType);
}

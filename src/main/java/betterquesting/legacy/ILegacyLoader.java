package betterquesting.legacy;

import betterquesting.api.enums.EnumSaveType;
import com.google.gson.JsonElement;

public interface ILegacyLoader
{
	public void readFromJson(JsonElement json, EnumSaveType saveType);
}

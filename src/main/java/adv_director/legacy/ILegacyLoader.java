package adv_director.legacy;

import adv_director.api.enums.EnumSaveType;
import com.google.gson.JsonElement;

public interface ILegacyLoader
{
	public void readFromJson(JsonElement json, EnumSaveType saveType);
}

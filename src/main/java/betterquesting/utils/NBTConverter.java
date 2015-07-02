package betterquesting.utils;

import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.Set;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagByte;
import net.minecraft.nbt.NBTTagByteArray;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagDouble;
import net.minecraft.nbt.NBTTagFloat;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagLong;
import net.minecraft.nbt.NBTTagShort;
import net.minecraft.nbt.NBTTagString;
import org.apache.logging.log4j.Level;
import betterquesting.core.BetterQuesting;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import cpw.mods.fml.common.ObfuscationReflectionHelper;

public class NBTConverter
{
	/**
	 * Convert NBT tags to a JSON object
	 * @param parent
	 * @return
	 */
	private static JsonElement NBTtoJSON_Base(NBTBase tag)
	{
		if(tag == null)
		{
			return new JsonObject();
		}
		
		if(tag.getId() >= 1 && tag.getId() <= 6)
		{
			return new JsonPrimitive(getNumber(tag));
		} else if(tag instanceof NBTTagString)
		{
			return new JsonPrimitive(((NBTTagString)tag).func_150285_a_());
		} else if(tag instanceof NBTTagCompound)
		{
			return NBTtoJSON_Compound((NBTTagCompound)tag, new JsonObject());
		} else if(tag instanceof NBTTagList)
		{
			JsonArray jAry = new JsonArray();
			//jAry.add(new JsonPrimitive(((NBTTagList)tag).func_150303_d()));
			
			ArrayList<NBTBase> tagList = getTagList((NBTTagList)tag);
			
			for(int i = 0; i < tagList.size(); i++)
			{
				jAry.add(NBTtoJSON_Base(tagList.get(i)));
			}
			
			return jAry;
		} else if(tag instanceof NBTTagByteArray)
		{
			JsonArray jAry = new JsonArray();
			
			for(byte b : ((NBTTagByteArray)tag).func_150292_c())
			{
				jAry.add(new JsonPrimitive(b));
			}
			
			return jAry;
		} else if(tag instanceof NBTTagIntArray)
		{
			JsonArray jAry = new JsonArray();
			
			for(int i : ((NBTTagIntArray)tag).func_150302_c())
			{
				jAry.add(new JsonPrimitive(i));
			}
			
			return jAry;
		} else
		{
			return new JsonObject(); // No valid types found. We'll just return this to prevent a NPE
		}
	}
	
	@SuppressWarnings("unchecked")
	public static JsonObject NBTtoJSON_Compound(NBTTagCompound parent, JsonObject jObj)
	{
		if(parent == null)
		{
			return jObj;
		}
		
		for(String key : (Set<String>)parent.func_150296_c())
		{
			NBTBase tag = parent.getTag(key);
			
			if(tag == null)
			{
				continue;
			}
			
			jObj.add(key, NBTtoJSON_Base(tag));
		}
		
		return jObj;
	}
	
	/**
	 * Convert JsonObject to a NBTTagCompound
	 * @param jObj
	 * @return
	 */
	public static NBTTagCompound JSONtoNBT_Object(JsonObject jObj, NBTTagCompound tags)
	{
		if(jObj == null)
		{
			return tags;
		}
		
		for(Entry<String,JsonElement> entry : jObj.entrySet())
		{
			try
			{
				tags.setTag(entry.getKey(), JSONtoNBT_Element(entry.getValue()));
			} catch(Exception e)
			{
				continue; // Given key is not a JSON formatted NBT value
			}
		}
		
		return tags;
	}
	
	/**
	 * Used purely for array elements without tag names. Tries to interpret the tagID from the JsonElement's contents
	 * @param jObj
	 * @param type
	 * @return
	 */
	private static NBTBase JSONtoNBT_Element(JsonElement jObj)
	{
		if(jObj == null)
		{
			return new NBTTagString();
		}
		
		byte tagID = 0;
		
		if(jObj.isJsonPrimitive())
		{
			JsonPrimitive prim = jObj.getAsJsonPrimitive();
			
			if(prim.isNumber())
			{
				if(prim.getAsString().contains(".")) // Just in case we'll choose the largest possible container supporting this number type (Long or Double)
				{
					tagID = 6;
				} else
				{
					tagID = 4;
				}
			} else
			{
				tagID = 8; // Non-number primitive. Assume string
			}
		} else if(jObj.isJsonArray())
		{
			JsonArray array = jObj.getAsJsonArray();
			
			for(JsonElement entry : array)
			{
				if(entry.isJsonPrimitive() && tagID == 0) // Note: TagLists can only support Integers, Bytes and Compounds
				{
					try
					{
						array.get(0).getAsByte();
						tagID = 7;
					} catch(Exception e)
					{
						tagID = 11;
					}
				} else if(!entry.isJsonPrimitive())
				{
					tagID = 9;
					break;
				}
			}
		} else
		{
			tagID = 10;
		}
		
		try
		{
			if(tagID >= 1 && tagID <= 6)
			{
				return instanceNumber(jObj.getAsNumber(), tagID);
			} else if(tagID == 8)
			{
				return new NBTTagString(jObj.getAsString());
			} else if(tagID == 10)
			{
				return JSONtoNBT_Object(jObj.getAsJsonObject(), new NBTTagCompound());
			} else if(tagID == 7) // Byte array
			{
				JsonArray jAry = jObj.getAsJsonArray();
				
				byte[] bAry = new byte[jAry.size()];
				
				for(int i = 0; i < jAry.size(); i++)
				{
					bAry[i] = jAry.get(i).getAsByte();
				}
				
				return new NBTTagByteArray(bAry);
			} else if(tagID == 11)
			{
				JsonArray jAry = jObj.getAsJsonArray();
				
				int[] iAry = new int[jAry.size()];
				
				for(int i = 0; i < jAry.size(); i++)
				{
					iAry[i] = jAry.get(i).getAsInt();
				}
				
				return new NBTTagIntArray(iAry);
			} else if(tagID == 9)
			{
				JsonArray jAry = jObj.getAsJsonArray();
				NBTTagList tList = new NBTTagList();
				
				for(int i = 0; i < jAry.size(); i++)
				{
					JsonElement jElm = jAry.get(i);
					tList.appendTag(JSONtoNBT_Element(jElm));
				}
				
				return tList;
			}
		} catch(Exception e)
		{
			BetterQuesting.logger.log(Level.ERROR, "An error occured while parsing JsonElement to NBTBase (" + tagID + "):", e);
		}
		
		return new NBTTagString();
	}
	
	/**
	 * Pulls the raw list out of the NBTTagList
	 * @param tag
	 * @return
	 */
	public static ArrayList<NBTBase> getTagList(NBTTagList tag)
	{
		return ObfuscationReflectionHelper.getPrivateValue(NBTTagList.class, tag, new String[]{"tagList", "field_74747_a"});
	}
	
	public static Number getNumber(NBTBase tag)
	{
		if(tag instanceof NBTTagByte)
		{
			return ((NBTTagByte)tag).func_150290_f();
		} else if(tag instanceof NBTTagShort)
		{
			return ((NBTTagShort)tag).func_150289_e();
		} else if(tag instanceof NBTTagInt)
		{
			return ((NBTTagInt)tag).func_150287_d();
		} else if(tag instanceof NBTTagFloat)
		{
			return ((NBTTagFloat)tag).func_150288_h();
		} else if(tag instanceof NBTTagDouble)
		{
			return ((NBTTagDouble)tag).func_150286_g();
		} else if(tag instanceof NBTTagLong)
		{
			return ((NBTTagLong)tag).func_150291_c();
		} else
		{
			return 0;
		}
	}
	
	public static NBTBase instanceNumber(Number num, byte type)
	{
		switch (type)
        {
            case 1:
                return new NBTTagByte(num.byteValue());
            case 2:
                return new NBTTagShort(num.shortValue());
            case 3:
                return new NBTTagInt(num.shortValue());
            case 4:
                return new NBTTagLong(num.longValue());
            case 5:
                return new NBTTagFloat(num.floatValue());
            case 6:
                return new NBTTagDouble(num.doubleValue());
            default:
            	return new NBTTagByte(num.byteValue());
        }
	}
}

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
	 */
	private static JsonElement NBTtoJSON_Base(NBTBase tag, boolean format)
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
			return NBTtoJSON_Compound((NBTTagCompound)tag, new JsonObject(), format);
		} else if(tag instanceof NBTTagList)
		{
			JsonArray jAry = new JsonArray();
			
			ArrayList<NBTBase> tagList = getTagList((NBTTagList)tag);
			
			for(int i = 0; i < tagList.size(); i++)
			{
				jAry.add(NBTtoJSON_Base(tagList.get(i), format));
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
	
	public static JsonObject NBTtoJSON_Compound(NBTTagCompound parent, JsonObject jObj)
	{
		return NBTtoJSON_Compound(parent, jObj, false);
	}
	
	@SuppressWarnings("unchecked")
	public static JsonObject NBTtoJSON_Compound(NBTTagCompound parent, JsonObject jObj, boolean format)
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
			
			if(format)
			{
				jObj.add(key + ":" + tag.getId(), NBTtoJSON_Base(tag, format));
			} else
			{
				jObj.add(key, NBTtoJSON_Base(tag, format));
			}
		}
		
		return jObj;
	}
	
	public static NBTTagCompound JSONtoNBT_Object(JsonObject jObj, NBTTagCompound tags)
	{
		return JSONtoNBT_Object(jObj, tags, false);
	}
	
	/**
	 * Convert JsonObject to a NBTTagCompound
	 */
	public static NBTTagCompound JSONtoNBT_Object(JsonObject jObj, NBTTagCompound tags, boolean format)
	{
		if(jObj == null)
		{
			return tags;
		}
		
		for(Entry<String,JsonElement> entry : jObj.entrySet())
		{
			String key = entry.getKey();
			
			if(!format)
			{
				tags.setTag(key, JSONtoNBT_Element(entry.getValue(), (byte)0, format));
			} else
			{
				String[] s = key.split(":");
				byte id = 0;
				
				try
				{
					id = Byte.parseByte(s[s.length - 1]);
					key = key.substring(0, key.lastIndexOf(":" + id));
				} catch(Exception e)
				{
					if(tags.hasKey(key))
					{
						BetterQuesting.logger.log(Level.WARN, "JSON/NBT formatting conflict on key '" + key + "'. Skipping...");
						continue;
					}
				}
				
				tags.setTag(key, JSONtoNBT_Element(entry.getValue(), id, format));
			}
		}
		
		return tags;
	}
	
	/**
	 * Tries to interpret the tagID from the JsonElement's contents
	 */
	private static NBTBase JSONtoNBT_Element(JsonElement jObj, byte id, boolean format)
	{
		if(jObj == null)
		{
			return new NBTTagString();
		}
		
		byte tagID = id <= 0? fallbackTagID(jObj) : id;
		
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
				return JSONtoNBT_Object(jObj.getAsJsonObject(), new NBTTagCompound(), format);
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
					tList.appendTag(JSONtoNBT_Element(jElm, (byte)0, format));
				}
				
				return tList;
			}
		} catch(Exception e)
		{
			BetterQuesting.logger.log(Level.ERROR, "An error occured while parsing JsonElement to NBTBase (" + tagID + "):", e);
		}
		
		BetterQuesting.logger.log(Level.WARN, "Unknown NBT representation for " + jObj.toString() + " (ID: " + tagID + ")");
		return new NBTTagString();
	}
	
	/**
	 * Pulls the raw list out of the NBTTagList
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
                return new NBTTagInt(num.intValue());
            case 4:
                return new NBTTagLong(num.longValue());
            case 5:
                return new NBTTagFloat(num.floatValue());
            default:
                return new NBTTagDouble(num.doubleValue());
        }
	}
	
	private static byte fallbackTagID(JsonElement jObj)
	{
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
				if(entry.isJsonPrimitive() && tagID == 0) // Note: TagLists can only support Integers, Bytes and Compounds (Strings can be stored but require special handling)
				{
					try
					{
						for(JsonElement element : array)
						{
							// Make sure all entries can be bytes
							if(element.getAsLong() != element.getAsByte()) // In case casting works but overflows
							{
								throw new ClassCastException();
							}
						}
						tagID = 7; // Can be used as byte
					} catch(Exception e1)
					{
						try
						{
							for(JsonElement element : array)
							{
								// Make sure all entries can be integers
								if(element.getAsLong() != element.getAsInt()) // In case casting works but overflows
								{
									throw new ClassCastException();
								}
							}
							tagID = 11;
						} catch(Exception e2)
						{
							tagID = 9; // Is primitive however requires TagList interpretation
						}
					}
				} else if(!entry.isJsonPrimitive())
				{
					tagID = 9; // Non primitive, NBT compound list
					break;
				}
			}
			
			tagID = 9; // No data to judge format. Assuming tag list
		} else
		{
			tagID = 10;
		}
		
		return tagID;
	}
}

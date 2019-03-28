package betterquesting.api2.utils;

import net.minecraft.client.resources.I18n;

public class QuestTranslation
{
    public static String translate(String text, Object... args)
    {
        String out = I18n.format(text, args);
        if(out.startsWith("Format error: ")) return text; // TODO: Find a more reliable way of detecting translation failure
        return out;
    }
    
    public static String translateTrimmed(String text, Object... args)
    {
        return translate(text, args).replaceAll("\r", "");
    }
}

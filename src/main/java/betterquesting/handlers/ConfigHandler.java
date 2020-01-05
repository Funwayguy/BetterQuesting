package betterquesting.handlers;

import betterquesting.api.storage.BQ_Settings;
import betterquesting.core.BetterQuesting;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
import net.minecraftforge.common.ForgeConfigSpec.Builder;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;
import net.minecraftforge.common.ForgeConfigSpec.IntValue;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.config.ModConfig.ConfigReloading;
import net.minecraftforge.fml.config.ModConfig.Loading;
import net.minecraftforge.fml.config.ModConfig.Type;
import org.apache.commons.lang3.tuple.Pair;

@EventBusSubscriber
public class ConfigHandler
{
    public static final ForgeConfigSpec commonSpec;
    public static final ForgeConfigSpec clientSpec;
    
    public static final ConfigCommon COMMON;
    public static final ConfigClient CLIENT;
    
    static
    {
        final Pair<ConfigClient, ForgeConfigSpec> clientSpecPair = new ForgeConfigSpec.Builder().configure(ConfigClient::new);
        clientSpec = clientSpecPair.getRight();
        CLIENT = clientSpecPair.getLeft();
        
        final Pair<ConfigCommon, ForgeConfigSpec> commonSpecPair = new ForgeConfigSpec.Builder().configure(ConfigCommon::new);
        commonSpec = commonSpecPair.getRight();
        COMMON = commonSpecPair.getLeft();
    }
    
    public static class ConfigCommon implements Runnable
    {
        private ConfigCommon(Builder builder)
        {
            builder.comment("Common Config").push("common");
            
            builder.pop();
        }
    
        @Override
        public void run()
        {
        }
    }
    
    public static class ConfigClient implements Runnable
    {
        public final BooleanValue autoBookmark;
        public final BooleanValue showNotices;
        public final IntValue guiMaxWidth;
        public final IntValue guiMaxHeight;
        public final ConfigValue<String> curTheme;
        
        private ConfigClient(ForgeConfigSpec.Builder builder)
        {
            builder.comment("Client Config").push("client");
            
            this.autoBookmark = lazyBool(builder, "use_bookmark", true, "Jumps the user to the last opened quest");
            this.showNotices = lazyBool(builder, "quest_notices", true, "Enabled the popup notices when quests are completed or updated");
            this.guiMaxWidth = lazyInt(builder, "max_ui_width", -1, -1, Integer.MAX_VALUE, "Clamps the max UI width (-1 to disable)");
            this.guiMaxHeight = lazyInt(builder, "max_ui_height", -1, -1, Integer.MAX_VALUE, "Clamps the max UI height (-1 to disable)");
            this.curTheme = lazyString(builder, "theme", "betterquesting:new_test", "The current questing theme");
            
            builder.pop();
        }
    
        @Override
        public void run()
        {
            BQ_Settings.useBookmark = autoBookmark.get();
            BQ_Settings.questNotices = showNotices.get();
            BQ_Settings.guiWidth = guiMaxWidth.get();
            BQ_Settings.guiHeight = guiMaxHeight.get();
            BQ_Settings.curTheme = curTheme.get();
        }
    }
    
    @SubscribeEvent
    public static void onLoad(final Loading event)
    {
        if(!event.getConfig().getModId().equalsIgnoreCase(BetterQuesting.MODID)) return;
        
        if(event.getConfig().getType() == Type.CLIENT)
        {
            CLIENT.run();
        } else if(event.getConfig().getType() == Type.COMMON)
        {
            COMMON.run();
        }
    }
    
    @SubscribeEvent
    public static void onReload(final ConfigReloading event)
    {
        if(!event.getConfig().getModId().equalsIgnoreCase(BetterQuesting.MODID)) return;
        
        if(event.getConfig().getType() == Type.CLIENT)
        {
            CLIENT.run();
        } else if(event.getConfig().getType() == Type.COMMON)
        {
            COMMON.run();
        }
    }
    
    private static IntValue lazyInt(Builder builder, String var, int def, int min, int max, String com)
    {
        builder.comment(com);
        builder.translation(BetterQuesting.MODID + ".config." + var.replaceAll(" ", "_"));
        return builder.defineInRange(var, def, min, max);
    }
    
    private static BooleanValue lazyBool(Builder builder, String var, boolean def, String com)
    {
        builder.comment(com);
        builder.translation(BetterQuesting.MODID + ".config." + var.replaceAll(" ", "_"));
        return builder.define(var, def);
    }
    
    private static ConfigValue<String> lazyString(Builder builder, String var, String def, String com)
    {
        builder.comment(com);
        builder.translation(BetterQuesting.MODID + ".config." + var.replaceAll(" ", "_"));
        return builder.define(var, def);
    }
}

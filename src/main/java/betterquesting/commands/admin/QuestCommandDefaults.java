package betterquesting.commands.admin;

import betterquesting.api.properties.NativeProps;
import betterquesting.api.storage.BQ_Settings;
import betterquesting.api.utils.JsonHelper;
import betterquesting.api.utils.NBTConverter;
import betterquesting.core.BetterQuesting;
import betterquesting.handlers.SaveLoadHandler;
import betterquesting.legacy.ILegacyLoader;
import betterquesting.legacy.LegacyLoaderRegistry;
import betterquesting.network.handlers.NetChapterSync;
import betterquesting.network.handlers.NetQuestSync;
import betterquesting.network.handlers.NetSettingSync;
import betterquesting.questing.QuestDatabase;
import betterquesting.questing.QuestLineDatabase;
import betterquesting.storage.QuestSettings;
import com.google.gson.JsonObject;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.text.TranslationTextComponent;

import java.io.File;

public class QuestCommandDefaults
{
    private static final String permNode = "betterquesting.command.admin.default";
    
    public static ArgumentBuilder<CommandSource, ?> register()
    {
        LiteralArgumentBuilder<CommandSource> baseNode = Commands.literal("default");
        
        baseNode.then(Commands.literal("save")).executes(QuestCommandDefaults::saveDefault);
        baseNode.then(Commands.literal("load")).executes(QuestCommandDefaults::loadDefault);
        
        return baseNode;
    }
    
    private static int saveDefault(CommandContext<CommandSource> context)
    {
        File qFile = new File(BQ_Settings.defaultDir, "DefaultQuests.json");
        
        CompoundNBT base = new CompoundNBT();
        base.put("questSettings", QuestSettings.INSTANCE.writeToNBT(new CompoundNBT()));
        base.put("questDatabase", QuestDatabase.INSTANCE.writeToNBT(new ListNBT(), null));
        base.put("questLines", QuestLineDatabase.INSTANCE.writeToNBT(new ListNBT(), null));
        base.putString("format", BetterQuesting.FORMAT);
        JsonHelper.WriteToFile(qFile, NBTConverter.NBTtoJSON_Compound(base, new JsonObject(), true));
        
        context.getSource().sendFeedback(new TranslationTextComponent("betterquesting.cmd.default.save"), true);
        
        return 1;
    }
    
    private static int loadDefault(CommandContext<CommandSource> context)
    {
        File qFile = new File(BQ_Settings.defaultDir, "DefaultQuests.json");
        if(qFile.exists())
        {
            context.getSource().sendFeedback(new TranslationTextComponent("betterquesting.cmd.default.none"), true);
            return 0;
        }
        
        boolean editMode = QuestSettings.INSTANCE.getProperty(NativeProps.EDIT_MODE);
        boolean hardMode = QuestSettings.INSTANCE.getProperty(NativeProps.HARDCORE);
        
        ListNBT jsonP = QuestDatabase.INSTANCE.writeProgressToNBT(new ListNBT(), null);
        
        JsonObject j1 = JsonHelper.ReadFromFile(qFile);
        CompoundNBT nbt1 = NBTConverter.JSONtoNBT_Object(j1, new CompoundNBT(), true);
        
        ILegacyLoader loader = LegacyLoaderRegistry.getLoader(nbt1.contains("format", 8) ? nbt1.getString("format") : "0.0.0");
        
        if(loader == null)
        {
            QuestSettings.INSTANCE.readFromNBT(nbt1.getCompound("questSettings"));
            QuestDatabase.INSTANCE.readFromNBT(nbt1.getList("questDatabase", 10), false);
            QuestLineDatabase.INSTANCE.readFromNBT(nbt1.getList("questLines", 10), false);
        } else
        {
            loader.readFromJson(j1);
        }
        
        QuestDatabase.INSTANCE.readProgressFromNBT(jsonP, false);
        
        QuestSettings.INSTANCE.setProperty(NativeProps.EDIT_MODE, editMode);
        QuestSettings.INSTANCE.setProperty(NativeProps.HARDCORE, hardMode);
        
        context.getSource().sendFeedback(new TranslationTextComponent("betterquesting.cmd.default.load"), true);
        
        NetSettingSync.sendSync(null);
        NetQuestSync.quickSync(-1, true, true);
        NetChapterSync.sendSync(null, null);
        SaveLoadHandler.INSTANCE.markDirty();
        
        return 1;
    }
}

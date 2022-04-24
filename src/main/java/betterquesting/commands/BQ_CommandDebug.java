package betterquesting.commands;

import betterquesting.api.utils.JsonHelper;
import betterquesting.api2.supporter.SupporterAPI;
import betterquesting.client.themes.ResourceTheme;
import betterquesting.client.themes.ThemeRegistry;
import com.google.gson.JsonObject;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Tuple;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

public class BQ_CommandDebug extends CommandBase {
    @Override
    public String getName() {
        return "bq_debug";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "TO BE USED IN DEV ONLY";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        final String dir = "D:/Jon Stuff/Github/Repositories/BetterQuesting - 1.12/jars/test_dlc_theme";

        JsonObject manifest = new JsonObject();
        manifest.addProperty("format", 0);
        manifest.addProperty("themeID", "dlc_theme:test");
        manifest.addProperty("themeName", "DLC TEST");

        JsonObject jsonTheme = JsonHelper.ReadFromFile(new File(dir, "bq_themes.json"));
        Set<Tuple<ResourceLocation, File>> textures = new HashSet<>();
        textures.add(new Tuple<>(new ResourceLocation("dlc_theme:textures/gui/new_gui.png"), new File(dir, "new_gui.png")));
        SupporterAPI.buildCompressedFile(new File(dir, "theme.thm"), manifest, jsonTheme, textures, null, null, -1);

        ResourceTheme r = SupporterAPI.readCompressedFile(new File(dir, "theme.thm"));
        if (r != null) {
            ThemeRegistry.INSTANCE.registerTheme(r);
            System.out.println("SUCCESS");
        } else {
            System.out.println("FAILED");
        }
    }
}

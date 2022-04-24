package betterquesting.api2.supporter.theme_dlc;

import betterquesting.api.utils.JsonHelper;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

public class CatalogueEntry {
    public final String author;
    public final String name;
    public final ResourceLocation themeID;
    public final String downloadLink;

    public final List<String> reqMods = new ArrayList<>();
    public final List<String> reqThemes = new ArrayList<>();

    private String token;
    private String service;
    private int subTier;
    private String subLink;

    public CatalogueEntry(String author, String themeName, String themeID, String downloadLink, String subLink) {
        this.author = author;
        this.name = themeName;
        this.themeID = new ResourceLocation(themeID);
        this.downloadLink = downloadLink;
        this.subLink = subLink;
    }

    public CatalogueEntry setRequirement(@Nonnull String token, @Nonnull String service, int amount, @Nonnull String subLink) {
        this.token = token;
        this.service = service;
        this.subTier = amount;
        this.subLink = subLink;
        return this;
    }

    public CatalogueEntry(@Nonnull JsonObject json) {
        this.author = JsonHelper.GetString(json, "author", "Unknown");
        this.name = JsonHelper.GetString(json, "themeName", "Untitled");
        this.themeID = new ResourceLocation(JsonHelper.GetString(json, "themeID", "minecraft:untitled"));
        this.downloadLink = JsonHelper.GetString(json, "themeID", "127.0.0.1");

        reqMods.clear();
        JsonArray aryMods = JsonHelper.GetArray(json, "reqMods");
        for (JsonElement je : aryMods) {
            if (!je.isJsonPrimitive() || !je.getAsJsonPrimitive().isString()) continue;
            reqMods.add(je.getAsString());
        }

        reqMods.clear();
        JsonArray aryThms = JsonHelper.GetArray(json, "reqThemes");
        for (JsonElement je : aryThms) {
            if (!je.isJsonPrimitive() || !je.getAsJsonPrimitive().isString()) continue;
            reqThemes.add(je.getAsString());
        }
    }

    public Future<File> downloadTheme() {
        return null;
    }
}

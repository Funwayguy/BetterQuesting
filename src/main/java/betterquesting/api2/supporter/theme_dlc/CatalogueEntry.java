package betterquesting.api2.supporter.theme_dlc;

import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.concurrent.Future;

public class CatalogueEntry
{
    public final String author;
    public final String name;
    public final ResourceLocation themeID;
    public final String downloadLink;
    
    private String token;
    private String service;
    private int subTier;
    private String subLink;
    
    public CatalogueEntry(String author, String themeName, String themeID, String downloadLink, String subLink)
    {
        this.author = author;
        this.name = themeName;
        this.themeID = new ResourceLocation(themeID);
        this.downloadLink = downloadLink;
        this.subLink = subLink;
    }
    
    public CatalogueEntry setRequirement(@Nonnull String token, @Nonnull String service, int amount, @Nonnull String subLink)
    {
        this.token = token;
        this.service = service;
        this.subTier = amount;
        this.subLink = subLink;
        return this;
    }
    
    public Future<File> downloadTheme()
    {
        return null;
    }
}

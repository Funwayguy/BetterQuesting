package betterquesting.misc;

import betterquesting.core.BetterQuesting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.resources.IResourcePack;
import net.minecraft.client.resources.ResourcePackFileNotFoundException;
import net.minecraft.client.resources.data.IMetadataSection;
import net.minecraft.client.resources.data.MetadataSerializer;
import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.Level;

import javax.annotation.Nonnull;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class QuestResourcesFolder implements IResourcePack {
  private static final ResourceLocation UNKNOWN_PACK_TEXTURE = new ResourceLocation("textures/misc/unknown_pack.png");

  private static final File rootFolder = new File("config/betterquesting/resources/");
  private BufferedImage bufferedImage = null;

  @Nonnull
  @Override
  public InputStream getInputStream(@Nonnull ResourceLocation location) throws IOException {
    if (!resourceExists(location)) {
      throw new ResourcePackFileNotFoundException(rootFolder, location.toString());
    }

    // TODO: Figure out if we can fix UTF8 encoding from here
    return Files.newInputStream(new File(rootFolder.getPath() + "/" + location.getNamespace(), location.getPath()).toPath());
  }

  @Override
  public boolean resourceExists(@Nonnull ResourceLocation location) {
    File res = new File(rootFolder.getPath() + "/" + location.getNamespace(), location.getPath());
    return res.exists();
  }

  @Nonnull
  @Override
  public Set<String> getResourceDomains() {
    if (!rootFolder.exists() && !rootFolder.mkdirs()) {
      return Collections.emptySet();
    }

    String[] content = rootFolder.list();
    if (content == null || content.length == 0) { return Collections.emptySet(); }

    HashSet<String> folders = new HashSet<>();
    for (String s : content) {
      File f = new File(rootFolder, s);

      if (f.exists() && f.isDirectory()) {
        if (!f.getName().equals(f.getName().toLowerCase())) {
          logNameNotLowercase(f.getName(), f.toString());
        } else {
          folders.add(f.getName());
        }
      }
    }

    return folders;
  }

  @Override
  public <T extends IMetadataSection> T getPackMetadata(@Nonnull MetadataSerializer meta, @Nonnull String s) {
    return null;
  }

  @Nonnull
  @Override
  public BufferedImage getPackImage() {
    if (bufferedImage != null) { return bufferedImage; }

    try {
      bufferedImage = TextureUtil.readBufferedImage(
          Minecraft.getMinecraft().getResourceManager().getResource(UNKNOWN_PACK_TEXTURE).getInputStream());
    } catch (IOException ioexception) {
      throw new Error("Couldn't bind resource pack icon", ioexception);
    }

    return bufferedImage;
  }

  @Nonnull
  @Override
  public String getPackName() {
    return BetterQuesting.NAME + "_folders";
  }

  private void logNameNotLowercase(String name, String file) {
    BetterQuesting.logger.log(Level.WARN, "ResourcePack: ignored non-lowercase namespace: {} in {}",
                              new Object[] { name, file });
  }
}

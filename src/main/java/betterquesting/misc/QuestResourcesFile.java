package betterquesting.misc;

import betterquesting.core.BetterQuesting;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
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
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class QuestResourcesFile implements IResourcePack, Closeable {

  private static final ResourceLocation UNKNOWN_PACK_TEXTURE = new ResourceLocation("textures/misc/unknown_pack.png");

  private static final File rootFolder = new File("config/betterquesting/resources/");
  private static final Splitter entryNameSplitter = Splitter.on('/').omitEmptyStrings().limit(3);
  private List<ZipFile> zipList = null;
  private BufferedImage bufferedImage = null;

  @Nonnull
  @Override
  public InputStream getInputStream(@Nonnull ResourceLocation loc) throws IOException {
    String locName = locationToName(loc);

    for (ZipFile zipfile : getZipFiles()) {
      ZipEntry zipentry = zipfile.getEntry(locName);

      if (zipentry != null) {
        return zipfile.getInputStream(zipentry);
      }
    }

    throw new ResourcePackFileNotFoundException(rootFolder, locName);
  }

  @Override
  public boolean resourceExists(@Nonnull ResourceLocation loc) {
    String locName = locationToName(loc);

    try {
      for (ZipFile zipfile : getZipFiles()) {
        ZipEntry zipentry = zipfile.getEntry(locName);

        if (zipentry != null) {
          return true;
        }
      }
    } catch (Exception ignored) { }

    return false;
  }

  @Override
  @Nonnull
  public Set<String> getResourceDomains() {
    HashSet<String> hashset = Sets.newHashSet();

    try {
      for (ZipFile f : getZipFiles()) {
        hashset.addAll(GetZipDomains(f));
      }
    } catch (Exception ignored) { }

    return hashset;
  }

  private Set<String> GetZipDomains(ZipFile zipfile) {
    Enumeration<? extends ZipEntry> enumeration = zipfile.entries();
    HashSet<String> hashset = Sets.newHashSet();

    while (enumeration.hasMoreElements()) {
      ZipEntry zipentry = enumeration.nextElement();
      String s = zipentry.getName();

      if (s.startsWith("assets/")) {
        ArrayList<String> arraylist = Lists.newArrayList(entryNameSplitter.split(s));

        if (arraylist.size() > 1) {
          String s1 = arraylist.get(1);

          if (!s1.equals(s1.toLowerCase())) {
            logNameNotLowercase(s1, zipfile.getName());
          } else {
            hashset.add(s1);
          }
        }
      }
    }

    return hashset;
  }

  @Override
  public <T extends IMetadataSection> T getPackMetadata(@Nonnull MetadataSerializer meta, @Nonnull String s) {
    return null;
  }

  @Nonnull
  @Override
  public BufferedImage getPackImage() {
    if (bufferedImage != null) {
      return bufferedImage;
    }

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
    return BetterQuesting.NAME + "_files";
  }

  private List<ZipFile> getZipFiles() {
    if (zipList != null) {
      return zipList;
    }

    if (!rootFolder.exists() && !rootFolder.mkdirs()) {
      return Collections.emptyList();
    }

    File[] files = rootFolder.listFiles();
    if (files == null || files.length == 0) {
      return Collections.emptyList();
    }

    zipList = new ArrayList<>();

    for (File f : files) {
      if (f.exists() && f.isFile()) {
        try {
          zipList.add(new ZipFile(f));
        } catch (Exception ignored) { }
      }
    }

    return zipList;
  }

  private static String locationToName(ResourceLocation loc) {
    return String.format("%s/%s/%s", "assets", loc.getNamespace(), loc.getPath());
  }

  private void logNameNotLowercase(String name, String file) {
    BetterQuesting.logger.log(Level.WARN, "ResourcePack: ignored non-lowercase namespace: {} in {}",
                              new Object[] { name, file });
  }

  @Override
  protected void finalize() throws Throwable {
    close();
    super.finalize();
  }

  @Override
  public void close() throws IOException {
    if (zipList != null) {
      for (ZipFile zip : zipList) {
        if (zip != null) {
          zip.close();
        }
      }

      zipList = null;
    }
  }
}

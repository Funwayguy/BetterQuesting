package betterquesting.api2.supporter;

import betterquesting.api.api.QuestingAPI;
import betterquesting.api.utils.JsonHelper;
import betterquesting.client.themes.ResourceTheme;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class SupporterAPI
{
    private static Gson GSON = new GsonBuilder().create(); // No pretty print
    private static final String charSet = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuv0123456789";
    private static final Random rand = new Random();
    
    @Nullable
    public static JsonObject readManifest(File loc)
    {
        try(DataInputStream dis = new DataInputStream(new GZIPInputStream(new FileInputStream(loc))))
        {
            return GSON.fromJson(new String(Base64.getDecoder().decode(dis.readUTF())), JsonObject.class);
        } catch(Exception ignored){ return null; }
    }
    
    public static ResourceTheme readCompressedFile(File loc)
    {
        // Yes this obscurity not security. No I don't really care for your opinion. Leave me alone to tinker in peace...
        // Reversing this isn't necessarily hard but it takes more effort than just making a new theme or donating a dollar
        
        // Manifest is in charge of constructing this class and checking if the supporter goals were met before install.
        // By this point the file should have been approved so if this fails something is wrong or tampered with.
        
        try(DataInputStream dis = new DataInputStream(new GZIPInputStream(new FileInputStream(loc))))
        {
            JsonObject manifest = GSON.fromJson(new String(Base64.getDecoder().decode(dis.readUTF()), StandardCharsets.UTF_8), JsonObject.class);
            
            int format = JsonHelper.GetNumber(manifest, "format", 0).intValue();
            ResourceLocation parID = manifest.has("parentID") ? new ResourceLocation(JsonHelper.GetString(manifest, "parentID", "minecraft:null")) : null;
            ResourceLocation thmID = new ResourceLocation(JsonHelper.GetString(manifest, "themeID", "betterquesting:untitled"));
            String thmName = JsonHelper.GetString(manifest, "themeName", "Untitled Theme");
            
            ResourceTheme theme = new ResourceTheme(parID, thmID, thmName);
            
            // Subject to change depending on format
            byte[] fileKey = readFileKey(dis, format);
            
            // Read out first but not applied until the remaining textures have been loaded so fallbacks kick in for missing things
            JsonObject themeJson = GSON.fromJson(decode(dis.readUTF(), fileKey), JsonObject.class);
            
            int numTex = dis.readInt();
            for(int n = 0; n < numTex; n++)
            {
                ResourceLocation resID = new ResourceLocation(decode(dis.readUTF(), fileKey));
                int w = dis.readInt();
                int h = dis.readInt();
                int[] rgb = new int[w * h];
                for(int i = 0; i < rgb.length; i++)
                {
                    int flip = fileKey[(i * 4) % fileKey.length];
                    flip |= fileKey[(i * 4 + 1) % fileKey.length] << 8;
                    flip |= fileKey[(i * 4 + 2) % fileKey.length] << 16;
                    flip |= fileKey[(i * 4 + 3) % fileKey.length] << 24;
                    rgb[i] = dis.readInt() ^ flip;
                }
                final RgbTexture texture = new RgbTexture(w, h, rgb);
                Minecraft.getMinecraft().addScheduledTask(() -> Minecraft.getMinecraft().getTextureManager().loadTexture(resID, texture));
            }
            
            // TODO: Support other resource types?
            
            theme.loadFromJson(themeJson);
            return theme;
        } catch(Exception ignored){} // I'm not telling you why. You should know if you got this far
        return null;
    }
    
    public static void buildCompressedFile(File fileOut, JsonObject jsonDetails, JsonObject jsonTheme, Collection<Tuple<ResourceLocation, File>> textures, String token, String service, int tier)
    {
        if(fileOut.exists())
        {
            try
            {
                fileOut.delete();
            } catch(Exception ignored){}
        }
        
        try(DataOutputStream dos = new DataOutputStream(new GZIPOutputStream(new FileOutputStream(fileOut))))
        {
            int format = JsonHelper.GetNumber(jsonDetails, "format", 0).intValue();
            
            dos.writeUTF(Base64.getEncoder().encodeToString(GSON.toJson(jsonDetails).getBytes(StandardCharsets.UTF_8)));
            
            final byte[] key = format == 0 ? makeFormat_0(dos, rand.nextLong()) : makeFormat_1(dos, token, 3 + rand.nextInt(6), service, tier);
            
            dos.writeUTF(encode(GSON.toJson(jsonTheme), key));
            
            dos.writeInt(textures.size()); // Texture count
            
            for(Tuple<ResourceLocation,File> t : textures)
            {
                dos.writeUTF(encode(t.getFirst().toString(), key));
                BufferedImage bufImg = ImageIO.read(t.getSecond());
                int[] rgb = bufImg.getRGB(0, 0, bufImg.getWidth(), bufImg.getHeight(), null, 0, bufImg.getWidth());
                dos.writeInt(bufImg.getWidth());
                dos.writeInt(bufImg.getHeight());
                for(int i = 0; i < rgb.length; i++)
                {
                    int flip = key[(i * 4) % key.length];
                    flip |= key[(i * 4 + 1) % key.length] << 8;
                    flip |= key[(i * 4 + 2) % key.length] << 16;
                    flip |= key[(i * 4 + 3) % key.length] << 24;
                    dos.writeInt(rgb[i] ^ flip);
                }
            }
        } catch(Exception e)
        {
            e.printStackTrace();
        }
    }
    
    private static String encode(String s, byte[] key)
    {
        return Base64.getEncoder().encodeToString(flipBytes(s.getBytes(StandardCharsets.UTF_8), key));
    }
    
    private static String decode(String s, byte[] key)
    {
        return new String(flipBytes(Base64.getDecoder().decode(s), key), StandardCharsets.UTF_8);
    }
    
    private static byte[] flipBytes(@Nonnull byte[] input, @Nonnull byte[] key)
    {
        byte[] output = new byte[input.length];
        for(int i = 0; i < input.length; i++) output[i] = (byte)(input[i] ^ key[i % key.length]);
        return output;
    }
    
    private static byte[] makeFormat_0(@Nonnull DataOutputStream dos, long seed) throws IOException
    {
        byte[] b = new byte[16];
        new Random(seed).nextBytes(b);
        dos.writeLong(seed);
        return b;
    }
    
    private static byte[] makeFormat_1(@Nonnull DataOutputStream dos, @Nonnull String token, int salts, String service, int threshold) throws IOException
    {
        List<Tuple<Boolean,String>> list = new ArrayList<>();
        list.add(new Tuple<>(true, token));
        for(int i = 0; i < salts; i++)
        {
            StringBuilder sb = new StringBuilder();
            for(int j = rand.nextInt(9) + 16; j >= 0; j--) sb.append(charSet.charAt(rand.nextInt(charSet.length())));
            list.add(new Tuple<>(false,sb.toString()));
        }
        Collections.shuffle(list,rand);
        return makeFormat_1(dos, list, service, threshold);
    }
    
    private static byte[] makeFormat_1(@Nonnull DataOutputStream dos, @Nonnull Collection<Tuple<Boolean,String>> tokens, String service, int threshold) throws IOException
    {
        dos.writeInt(tokens.size());
        int s = 0;
        Set<byte[]> l = new HashSet<>();
        for(Tuple<Boolean,String> t : tokens)
        {
            byte[] b = t.getFirst() ? t.getSecond().getBytes(StandardCharsets.UTF_8) : new byte[16];
            if(!t.getFirst()) new Random(t.getSecond().hashCode()).nextBytes(b);
            l.add(b);
            if(s < b.length) s = b.length;
            dos.writeUTF(Base64.getEncoder().encodeToString(t.getSecond().getBytes(StandardCharsets.UTF_8)));
        }
        
        dos.writeUTF(Base64.getEncoder().encodeToString(service.getBytes(StandardCharsets.UTF_8)));
        dos.writeInt(threshold);
        
        byte[] k = new byte[s];
        for(int i = 0; i < s; i++) for(byte[] e : l) k[i] ^= e[i % e.length];
        return k;
    }
    
    @SideOnly(Side.CLIENT)
    private static byte[] readFileKey(@Nonnull DataInputStream dis, int format)
    {
        switch(format)
        {
            case -1: return new byte[] {127};
            case 0: return readFormat_0(dis);
            case 1: return readFormat_1(dis);
            default: return new byte[]{ 127};
        }
    }
    
    private static byte[] readFormat_0(@Nonnull DataInputStream dis)
    {
        try
        {
            byte[] b = new byte[16];
            long seed = dis.readLong();
            new Random(seed).nextBytes(b);
            return b;
        } catch(Exception ignored) { return new byte[]{127}; }
    }
    
    @SideOnly(Side.CLIENT)
    private static byte[] readFormat_1(@Nonnull DataInputStream dis)
    {
        try
        {
            String[] tokens = new String[dis.readInt()];
            for(int n = 0; n < tokens.length; n++) tokens[n] = new String(Base64.getDecoder().decode(dis.readUTF()));
            String service = new String(Base64.getDecoder().decode(dis.readUTF()));
            int threshold = dis.readInt();
            
            UUID playerID = QuestingAPI.getQuestingUUID(Minecraft.getMinecraft().player);
            SupporterEntry entry = SupporterDB.INSTANCE.getValue(playerID);
            
            int m = 0;
            Set<byte[]> encoded = new HashSet<>();
            for(String k : tokens)
            {
                boolean c = entry != null && entry.getServices(k).entrySet().stream().anyMatch((v) -> v.getKey().equals(service) && v.getValue() >= threshold);
                byte[] b = c ? k.getBytes(StandardCharsets.UTF_8) : new byte[16];
                if(c)  new Random(k.hashCode()).nextBytes(b);
                encoded.add(b);
                if(b.length > m) m = b.length;
            }
            
            byte[] merged = new byte[m];
            for(int i = 0; i < m; i++) for(byte[] e : encoded) merged[i] ^= e[i % e.length];
            return merged;
        } catch(Exception ignored) { return new byte[]{127}; }
    }
}

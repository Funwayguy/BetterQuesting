package betterquesting.abs.misc;

import net.minecraft.util.ResourceLocation;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import java.util.regex.Pattern;

// Internal version of ResourceLocation
public class ResourceID implements Comparable<ResourceID>
{
    private static Pattern VALID_NAMESPACE_CHARS = Pattern.compile("[a-z0-9_.\\-]]");
    private static Pattern VALID_PATH_CHARS = Pattern.compile("[a-z0-9_.\\-/]]");
    private static char SPLIT_CHAR = ':';
    
    private final String namespace;
    private final String path;
    
    public ResourceID(@Nonnull String resource)
    {
        this(splitResourceName(resource));
    }
    
    public ResourceID(@Nonnull String d, @Nonnull String p)
    {
        this(new String[]{d, p});
    }
    
    private ResourceID(@Nonnull String[] parts)
    {
        // LOWER CASE ENFORCED!
        this.namespace = StringUtils.isEmpty(parts[0]) ? "minecraft" : parts[0].toLowerCase();
        this.path = parts[1].toLowerCase();
        
        if(!VALID_NAMESPACE_CHARS.matcher(namespace).matches()) throw new IllegalArgumentException("Resource ID path " + namespace + " contains non [a-z0-9_.-] characters");
        if(!VALID_PATH_CHARS.matcher(path).matches()) throw new IllegalArgumentException("Resource ID path " + path + " contains non [a-z0-9_.-/] characters");
    }
    
    private static String[] splitResourceName(String resourceName)
    {
      String[] split = new String[]{"minecraft", resourceName};
      int i = resourceName.indexOf(SPLIT_CHAR);
      if (i >= 0) // There's two halves, a namespace and path
      {
         split[1] = resourceName.substring(i + 1); // Namespace
         if (i >= 1) split[0] = resourceName.substring(0, i); // Domain if not empty
      }
      return split;
    }
    
    public ResourceLocation toNative()
    {
        return new ResourceLocation(this.namespace, this.path);
    }
    
    @Override
    public String toString()
    {
        return namespace + ":" + path;
    }
    
    @Override
    public boolean equals(Object o)
    {
        if(o == null) return false;
        if(o == this) return true;
        if(o instanceof ResourceID || o instanceof ResourceLocation)
        {
            return o.toString().equalsIgnoreCase(this.toString());
        }
        return false;
    }
    
    @Override
    public int hashCode()
    {
      return 31 * this.namespace.hashCode() + this.path.hashCode();
    }
    
    @Override
    public int compareTo(@Nonnull ResourceID res)
    {
        // NOTE: Unlike vanilla we sort by path first
        return this.toString().compareToIgnoreCase(res.toString());
    }
}

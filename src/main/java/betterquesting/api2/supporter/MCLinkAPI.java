package betterquesting.api2.supporter;

import betterquesting.api.utils.JsonHelper;
import betterquesting.api2.supporter.mc_link.McLinkEndpoint;
import betterquesting.core.BetterQuesting;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.UUID;

public class MCLinkAPI
{
    private static final String MCL_TOKEN = "Yo1nkbXn7uVptLoL3GpkAaT7HsU8QFGJ";
    
	private static String userAgent = null;
	
	@Nonnull
	public JsonObject getApiStatus()
    {
        return new JsonObject();
    }
    
    public void updateSupporterInfo(@Nonnull Collection<UUID> playerIDs, Collection<String> tokens)
    {
        JsonObject jsonBase = new JsonObject();
        
        JsonObject jsonTokens = new JsonObject();
        
        JsonObject jsonServices = new JsonObject();
        JsonArray teirArgs = new JsonArray();
        teirArgs.add(0); // Just report any tier for now
        jsonServices.add("Patreon", teirArgs);
        jsonServices.add("Twitch", new JsonArray());
        
        tokens.forEach((key) -> jsonTokens.add(key, jsonServices));
        jsonBase.add("tokens", jsonTokens);
        
        JsonArray jsonIds = new JsonArray();
        playerIDs.forEach((id) -> jsonIds.add(id.toString()));
        jsonBase.add("uuids", jsonIds);
        
        try
        {
            JsonElement response = sendJsonPost(McLinkEndpoint.API_AUTH.URL, jsonBase);
        } catch(Exception e)
        {
            BetterQuesting.logger.error("Unable to lookup supporter info", e);
        }
    }
	
    private static JsonElement sendJsonPost(String endpoint, JsonElement json) throws IOException
    {
        if(userAgent == null) setupMetadata();
        
        URL url = new URL(endpoint);
        String redirect = url.toString();
        HttpURLConnection con;
        
        do
        {
            url = new URL(url, redirect);
            con = (HttpURLConnection)url.openConnection();
            con.setRequestMethod("POST");
            con.setInstanceFollowRedirects(true);
            con.setAllowUserInteraction(false);
            con.setConnectTimeout(30000);
            con.setReadTimeout(30000);
            con.setDoOutput(true);
            
            con.setRequestProperty("User-Agent", userAgent);
            con.setRequestProperty("Accept-Charset", StandardCharsets.UTF_8.name());
            con.setRequestProperty("Accept", "application/json");
            con.setRequestProperty("Content-Type", "application/json; charset=" + StandardCharsets.UTF_8.name());
            
            try(OutputStreamWriter osw = new OutputStreamWriter(con.getOutputStream(), StandardCharsets.UTF_8))
            {
                JsonHelper.GSON.toJson(json, osw);
                osw.flush();
                redirect = con.getHeaderField("Location");
            }
        } while(redirect != null && con.getResponseCode() / 100 == 3); // Continue following redirects
        
        //TODO: Flag the JSON response as an error when necessary so it can be handled as such
        InputStream is = con.getErrorStream();
        if(is == null) is = con.getInputStream();
        
        try(InputStreamReader isr = new InputStreamReader(is, StandardCharsets.UTF_8))
        {
            JsonElement jsonOut = JsonHelper.GSON.fromJson(isr, JsonElement.class);
            return jsonOut != null ? jsonOut : new JsonObject();
        }
    }
    
    // This is setup to match what MC Link expects the user agent metadata to contain.
    private static void setupMetadata()
    {
        //noinspection ConstantConditions
        String modVersion = BetterQuesting.VERSION.equalsIgnoreCase("@VERSION@") ? "DEV" : BetterQuesting.VERSION;
        String mcVersion = "1.12.2";
        String branding = "Forge";
        
        StringBuilder sb = new StringBuilder(BetterQuesting.NAME);
        sb.append('/').append(modVersion.replaceAll("[;()\n\r]", ""));
        sb.append(" (APIv").append(1).append("; ");
        sb.append("MCv").append(mcVersion.replaceAll("[;()\n\r]", "")).append("; ");
        sb.append(branding.replaceAll("[;()\n\r]", "")).append("; ");
        String os = System.getProperty("os.name") + ' ' + System.getProperty("os.arch") + ' ' + System.getProperty("os.version");
        sb.append(os.replaceAll("[;()\n\r]", ""));
        userAgent = sb.append(')').toString();
    }
}

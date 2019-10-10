package net.justugh.xt.util;

import com.google.common.collect.Maps;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.UUID;

public class UUIDUtil {

    private static HashMap<String, UUID> uuidCache = Maps.newHashMap();

    /**
     * Read UUID data.
     * 
     * @param data The data to read
     * @param result The StringBuilder to append the data onto
     */
    private static void readData(String data, StringBuilder result) {
        int character = 7;

        if (data.length() < 7) {
            result.append("null");
            return;
        }

        while (character < 200) {
            if (!String.valueOf(data.charAt(character)).equalsIgnoreCase("\"")) {
                result.append(data.charAt(character));
            } else {
                break;
            }

            character++;
        }
    }

    /**
     * Call the a URL to retrieve JSON data.
     * 
     * @param URL The URL to be scanned
     * @return The data retrieved
     */
    private static String callURL(String URL) {
        StringBuilder jsonData = new StringBuilder();

        try {
            URL url = new URL(URL);
            URLConnection urlConnection = url.openConnection();

            if (urlConnection != null) {
                urlConnection.setReadTimeout(60 * 1000);
            }

            if (urlConnection != null && urlConnection.getInputStream() != null) {
                InputStreamReader inputStreamReader = new InputStreamReader(urlConnection.getInputStream(), Charset.defaultCharset());
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                int nextCharacter;

                while ((nextCharacter = bufferedReader.read()) != -1) {
                    jsonData.append((char) nextCharacter);
                }

                bufferedReader.close();
                inputStreamReader.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return jsonData.toString();
    }

    /**
     * Get a player's UUID from Mojang, so we know it's up to date.
     * 
     * @param playerName The player whose UUID is being retrieved
     * @return The UUID retrieved
     */
    public static UUID getUUID(String playerName) {
        if(getUUIDCache().containsKey(playerName)) {
            return getUUIDCache().get(playerName);
        }
        
        String output = callURL("https://api.mojang.com/users/profiles/minecraft/" + playerName);

        StringBuilder result = new StringBuilder();

        readData(output, result);

        String newResult = result.toString();

        if (newResult.equals("null")) {
            return UUID.fromString("00000000-0000-0000-0000-000000000000");
        }

        StringBuilder uuid = new StringBuilder();

        for (int i = 0; i <= 31; i++) {
            uuid.append(newResult.charAt(i));
            if (i == 7 || i == 11 || i == 15 || i == 19) {
                uuid.append("-");
            }
        }

        getUUIDCache().put(playerName, UUID.fromString(uuid.toString()));
        return UUID.fromString(uuid.toString());
    }

    public static HashMap<String, UUID> getUUIDCache() {
        return uuidCache;
    }
}

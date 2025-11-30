package placefinder.frameworks_drivers.api;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * Utility class for making HTTP requests.
 */
public class HttpUtil {

    /**
     * Performs an HTTP GET request to the specified URL.
     *
     * @param urlString The URL to request
     * @return The response body as a String
     * @throws Exception if the request fails
     */
    public static String get(String urlString) throws Exception {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(10000);

        int responseCode = conn.getResponseCode();
        if (responseCode != 200) {
            throw new Exception("HTTP error code: " + responseCode);
        }

        StringBuilder response = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
        }

        conn.disconnect();
        return response.toString();
    }
}
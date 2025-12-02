package placefinder.frameworks_drivers.api;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;

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

        int status = conn.getResponseCode();

        InputStream is = (status >= 200 && status < 300)
                ? conn.getInputStream()
                : conn.getErrorStream();   // <– read error body if not 2xx

        if (is == null) {
            throw new IOException("No response from server, HTTP status " + status);
        }

        StringBuilder response = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
        } finally {
            conn.disconnect();
        }

        if (status >= 200 && status < 300) {
            return response.toString();
        } else {
            throw new IOException("HTTP " + status + ": " + response);
        }
    }


    /**
     * Performs an HTTP POST request with the JSON input body to the specified URL.
     * This calls the overloaded method with no extra headers.
     *
     * @param urlString The URL to request
     * @param body      The JSON input body
     * @return The response body as a String
     * @throws IOException if the request fails
     */
    public static String post(String urlString, String body) throws IOException {
        return post(urlString, body, null);
    }

    /**
     * Performs an HTTP POST request with the JSON input body to the specified URL and
     * allows callers to pass additional headers.  When {@code extraHeaders} is null or empty,
     * the default headers of Accept and Content‑Type will be used.  Any headers
     * specified in {@code extraHeaders} override the default ones or add new entries.
     *
     * @param urlString    The URL to request
     * @param body         The JSON input body
     * @param extraHeaders Optional map of header names to values to include in the request
     * @return The response body as a String
     * @throws IOException if the request fails
     */
    public static String post(String urlString, String body,
                              Map<String, String> extraHeaders) throws IOException {
        HttpURLConnection conn = null;
        try {
            URL url = new URL(urlString);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setConnectTimeout(15000);
            conn.setReadTimeout(15000);
            conn.setDoOutput(true);

            // Set baseline headers; values can be overridden by extraHeaders
            conn.setRequestProperty("User-Agent", "PlaceFinder/1.0");
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");

            // Apply any user‑provided headers, overriding defaults if necessary
            if (extraHeaders != null) {
                for (Map.Entry<String, String> entry : extraHeaders.entrySet()) {
                    conn.setRequestProperty(entry.getKey(), entry.getValue());
                }
            }

            // Write request body
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = body.getBytes(StandardCharsets.UTF_8);
                os.write(input);
                os.flush();
            }

            int status = conn.getResponseCode();
            InputStream is = (status >= 200 && status < 300)
                    ? conn.getInputStream()
                    : conn.getErrorStream();

            if (is == null) {
                throw new IOException("No response from server, HTTP status " + status);
            }

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(is, StandardCharsets.UTF_8))) {

                StringBuilder sb = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }

                if (status >= 200 && status < 300) {
                    return sb.toString();
                } else {
                    throw new IOException("HTTP " + status + ": " + sb);
                }
            }

        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }
}
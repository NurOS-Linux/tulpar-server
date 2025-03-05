package org.meigo.tulpar.server.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.vdurmont.semver4j.Semver;
import org.meigo.tulpar.server.Main;
import org.meigo.tulpar.server.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * =============================================================================
 * UpdateManager Class
 * =============================================================================
 * This class is responsible for checking and downloading updates for TulparServer.
 *
 * It performs the following tasks:
 * - Retrieves version tags from the GitHub repository's tags endpoint.
 * - Sanitizes version strings to comply with Semantic Versioning (Semver) rules.
 *   (For example, a tag like "v2.0-beta.2" is converted to "2.0.0-beta.2".)
 * - Compares the latest version from GitHub with the current version (using Semver4j).
 * - If a newer version is available, it constructs the download URL and downloads the update
 *   using the DownloadManager.
 *
 * Details:
 * - GitHub tags are fetched via the GitHub API at:
 *   https://api.github.com/repos/NurOS-Linux/tulpar-server/tags
 * - The download URL is built based on the latest tag. For example, if the tag is "v2.0-beta.2",
 *   the file will be downloaded from:
 *   https://github.com/NurOS-Linux/tulpar-server/releases/download/v2.0-beta.2/tulparserver2.0-beta.2.zip
 *
 * Usage:
 *   UpdateManager.checkForUpdates();
 *
 * =============================================================================
 */
public class UpdateManager {

    // GitHub API endpoint to retrieve repository tags.
    private static final String TAGS_API_URL = "https://api.github.com/repos/NurOS-Linux/tulpar-server/tags";

    /**
     * Checks for updates by fetching the latest tag from GitHub and comparing it
     * with the current version defined in Main.VERSION. If a newer version is available,
     * the update file is downloaded.
     */
    public static void checkForUpdates() {
        try {
            // Fetch the latest version tag from GitHub.
            String latestTag = fetchLatestTag();
            if (latestTag == null) {
                Logger.info("No valid tags found on GitHub. Update check aborted.");
                return;
            }
            Logger.info("Latest version tag from GitHub: " + latestTag);

            // Sanitize version strings to ensure they comply with semver (e.g., "v2.0-beta.2" -> "2.0.0-beta.2").
            String sanitizedLatestTag = sanitizeVersion(latestTag);
            String sanitizedCurrentVersion = sanitizeVersion(Main.VERSION);

            // Parse version strings using Semver4j in LOOSE mode.
            Semver currentVersion = new Semver(sanitizedCurrentVersion, Semver.SemverType.LOOSE);
            Semver latestVersion = new Semver(sanitizedLatestTag, Semver.SemverType.LOOSE);

            // Compare versions.
            if (latestVersion.isGreaterThan(currentVersion)) {
                Logger.info("Update available: " + latestVersion + " (current: " + currentVersion + ")");
                // Construct the download URL.
                // For example, if latestTag is "v2.0-beta.2", remove the 'v' and form:
                // https://github.com/NurOS-Linux/tulpar-server/releases/download/v2.0-beta.2/tulparserver2.0-beta.2.zip
                String fileNameVersion = sanitizedLatestTag.replace("-", ""); // Remove dashes if needed for filename.
                String downloadUrl = "https://github.com/NurOS-Linux/tulpar-server/releases/download/"
                        + latestTag + "/tulparserver" + fileNameVersion + ".zip";

                Logger.info("Downloading update from: " + downloadUrl);

                // Define the destination path for the update file.
                String destinationPath = "tulparserver_update.zip";

                // Download the update using the DownloadManager.
                DownloadManager.downloadFile(downloadUrl, destinationPath);
                Logger.info("Update downloaded successfully to: " + destinationPath);
            } else {
                Logger.info("No update available. Current version (" + currentVersion + ") is up-to-date.");
            }
        } catch (Exception e) {
            Logger.error("Failed to check for updates: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Fetches the latest version tag from GitHub using the tags API.
     *
     * @return The latest version tag as a String (e.g., "v2.0-beta.2"),
     *         or null if no valid tags are found.
     * @throws IOException If an I/O error occurs during the HTTP request.
     */
    private static String fetchLatestTag() throws IOException {
        URL url = new URL(TAGS_API_URL);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        // Set a User-Agent to ensure GitHub API accepts the request.
        connection.setRequestProperty("User-Agent", "TulparServer UpdateManager");
        connection.connect();

        int responseCode = connection.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
            Logger.error("GitHub API returned non-OK response code: " + responseCode);
            return null;
        }

        // Read the JSON response.
        InputStream inputStream = connection.getInputStream();
        StringBuilder responseBuilder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                responseBuilder.append(line);
            }
        }
        connection.disconnect();

        // Parse the JSON response.
        JsonElement jsonElement = JsonParser.parseString(responseBuilder.toString());
        if (!jsonElement.isJsonArray()) {
            Logger.error("Unexpected JSON format from GitHub tags API.");
            return null;
        }
        JsonArray tagsArray = jsonElement.getAsJsonArray();
        if (tagsArray.size() == 0) {
            return null;
        }

        // Iterate through the tags to determine the latest version.
        String latestTag = null;
        Semver latestSemver = null;
        for (JsonElement element : tagsArray) {
            if (element.isJsonObject()) {
                JsonObject tagObject = element.getAsJsonObject();
                if (tagObject.has("name")) {
                    String tagName = tagObject.get("name").getAsString();
                    try {
                        // Sanitize the tag before parsing.
                        String sanitizedTag = sanitizeVersion(tagName);
                        Semver semver = new Semver(sanitizedTag, Semver.SemverType.LOOSE);
                        if (latestSemver == null || semver.isGreaterThan(latestSemver)) {
                            latestSemver = semver;
                            latestTag = tagName;
                        }
                    } catch (Exception e) {
                        // Skip tags that do not conform to semantic versioning.
                        Logger.warn("Skipping tag with invalid semver format: " + tagName);
                    }
                }
            }
        }
        return latestTag;
    }

    /**
     * Sanitizes a version string to ensure it complies with Semantic Versioning (Semver).
     * <p>
     * The method performs the following:
     * - Removes a leading "v" or "V" if present.
     * - Ensures that the core version (before any prerelease identifier) has major, minor, and patch numbers.
     *   If the patch version is missing, ".0" is appended.
     * </p>
     *
     * @param version The version string to sanitize.
     * @return A sanitized version string (e.g., "v2.0-beta.2" becomes "2.0.0-beta.2").
     */
    private static String sanitizeVersion(String version) {
        if (version == null || version.isEmpty()) {
            return version;
        }
        version = version.trim();
        // Remove leading "v" or "V"
        if (version.startsWith("v") || version.startsWith("V")) {
            version = version.substring(1);
        }
        // Separate the core version and the prerelease/build identifiers if present.
        String coreVersion;
        String prerelease = "";
        int dashIndex = version.indexOf("-");
        if (dashIndex >= 0) {
            coreVersion = version.substring(0, dashIndex);
            prerelease = version.substring(dashIndex); // retains the dash
        } else {
            coreVersion = version;
        }
        // Count the number of dots in the core version.
        int dotCount = coreVersion.length() - coreVersion.replace(".", "").length();
        if (dotCount < 2) {
            // Append missing patch version.
            coreVersion = coreVersion + ".0";
        }
        return coreVersion + prerelease;
    }
}

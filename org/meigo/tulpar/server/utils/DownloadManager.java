package org.meigo.tulpar.server.utils;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;

/**
 * =============================================================================
 * DownloadManager Class @ DEV BY meigo inc. \ discord: @glebbb tg: @numarktop1gg
 * =============================================================================
 * This utility class provides methods for downloading files from the internet
 * with a real-time progress indicator printed to the console.
 *
 * Key functionalities include:
 * - Synchronous file download from a specified URL to a designated local path.
 * - Asynchronous file download via a separate thread.
 * - A visual progress bar that updates in-place using carriage returns.
 * - Displaying additional information such as download speed and amount
 *   downloaded (in megabytes) compared to the total file size.
 * - Cross-platform support (Windows, Linux, macOS).
 *
 * Usage examples:
 *   // Synchronous download:
 *   DownloadManager.downloadFile("http://example.com/file.zip", "/path/to/file.zip");
 *
 *   // Asynchronous download:
 *   DownloadManager.downloadFileAsync("http://example.com/file.zip", "/path/to/file.zip");
 *
 * =============================================================================
 */
public class DownloadManager {

    /**
     * Downloads a file from the given URL and saves it to the specified destination path.
     * This method prints a progress bar to the console that updates in real-time,
     * including the download speed and the amount downloaded (in megabytes).
     *
     * @param fileURL         The URL of the file to download.
     * @param destinationPath The local file path where the downloaded file will be saved.
     * @throws IOException If an I/O error occurs during the download process.
     */
    public static void downloadFile(String fileURL, String destinationPath) throws IOException {
        URL url = new URL(fileURL);
        HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
        int responseCode = httpConn.getResponseCode();

        // Verify that the HTTP response code is 200 (OK)
        if (responseCode != HttpURLConnection.HTTP_OK) {
            throw new IOException("Server returned non-OK response code: " + responseCode);
        }

        // Get the content length (if available) for progress calculation.
        long contentLength = httpConn.getContentLengthLong();

        // Open input stream to read data from the URL.
        try (InputStream inputStream = new BufferedInputStream(httpConn.getInputStream());
             FileOutputStream outputStream = new FileOutputStream(destinationPath)) {

            byte[] buffer = new byte[4096];
            long totalBytesRead = 0;
            int bytesRead;
            // Record the start time for calculating download speed.
            long startTime = System.currentTimeMillis();

            while ((bytesRead = inputStream.read(buffer, 0, buffer.length)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
                totalBytesRead += bytesRead;

                // Update the progress bar if content length is known.
                if (contentLength > 0) {
                    printProgress(totalBytesRead, contentLength, startTime);
                } else {
                    System.out.print("\rDownloaded " + totalBytesRead + " bytes");
                    System.out.flush();
                }
            }
            // Ensure the progress bar completes on a new line.
            System.out.println();
        } finally {
            httpConn.disconnect();
        }
    }

    /**
     * Initiates an asynchronous download of a file from the given URL to the specified destination.
     * The download process is executed on a separate thread.
     *
     * @param fileURL         The URL of the file to download.
     * @param destinationPath The local file path where the file will be saved.
     */
    public static void downloadFileAsync(String fileURL, String destinationPath) {
        new Thread(() -> {
            try {
                downloadFile(fileURL, destinationPath);
            } catch (IOException e) {
                System.err.println("Error during file download: " + e.getMessage());
                e.printStackTrace();
            }
        }).start();
    }

    /**
     * Prints a progress bar to the console, updating in-place using a carriage return.
     * The progress bar includes a visual indicator, percentage completed, download speed,
     * and the amount downloaded (in megabytes) out of the total file size.
     *
     * @param bytesRead  The total number of bytes downloaded so far.
     * @param totalBytes The total number of bytes to be downloaded.
     * @param startTime  The start time of the download (in milliseconds) for speed calculation.
     */
    private static void printProgress(long bytesRead, long totalBytes, long startTime) {
        final int barWidth = 50; // Total width of the progress bar in characters
        double progress = (double) bytesRead / totalBytes;
        int filledLength = (int) (barWidth * progress);

        // Build progress bar string.
        StringBuilder bar = new StringBuilder("[");
        for (int i = 0; i < filledLength; i++) {
            bar.append("=");
        }
        for (int i = filledLength; i < barWidth; i++) {
            bar.append(" ");
        }
        bar.append("]");

        int percent = (int) (progress * 100);

        // Calculate elapsed time and download speed.
        double elapsedTimeSec = (System.currentTimeMillis() - startTime) / 1000.0;
        double speedMBs = (elapsedTimeSec > 0) ? (bytesRead / (1024.0 * 1024.0)) / elapsedTimeSec : 0;
        double downloadedMB = bytesRead / (1024.0 * 1024.0);
        double totalMB = totalBytes / (1024.0 * 1024.0);

        // Format numbers to two decimal places.
        DecimalFormat df = new DecimalFormat("0.00");

        String progressMessage = String.format("\r%s %d%% | Speed: %s MB/s | %sMB / %sMB",
                bar, percent, df.format(speedMBs), df.format(downloadedMB), df.format(totalMB));

        System.out.print(progressMessage);
        System.out.flush();
    }
}

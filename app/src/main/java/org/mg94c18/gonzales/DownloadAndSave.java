package org.mg94c18.gonzales;

import android.os.Build;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.Log;
import android.util.Pair;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.List;
import java.util.Scanner;
import java.util.zip.GZIPOutputStream;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import static org.mg94c18.gonzales.Logger.LOG_V;
import static org.mg94c18.gonzales.Logger.TAG;

public class DownloadAndSave {
    private static final String TMP_SUFFIX = ".tmp";

    static String downloadAndSave(String link, File imageFile, int attempts) {
        for (int i = 0; i < attempts; i++) {
            Pair<String, Boolean> result = downloadAndSaveNoRetry(link, imageFile);
            if (result.first != null || result.second) {
                return result.first;
            }
        }
        return null;
    }

    private static SSLContext tlsEnablingSslContext = null;
    private static SSLSocketFactory tlsEnablingFactory = null;
    private static synchronized SSLSocketFactory getTlsEnablingFactory(String version) throws NoSuchAlgorithmException, KeyManagementException {
        if (tlsEnablingFactory == null) {
            tlsEnablingSslContext = SSLContext.getInstance(version);
            tlsEnablingSslContext.init(null, null, new SecureRandom());
            tlsEnablingFactory = new MySocketFactory(tlsEnablingSslContext, version);
        }
        return tlsEnablingFactory;
    }

    private static class MySocketFactory extends SSLSocketFactory {
        private String[] protocols = new String[1];

        public MySocketFactory(SSLContext context, String protocol) {
            this.factory = context.getSocketFactory();
            this.protocols[0] = protocol;
        }

        private SSLSocketFactory factory;

        @Override
        public String[] getDefaultCipherSuites() {
            return factory.getDefaultCipherSuites();
        }

        @Override
        public String[] getSupportedCipherSuites() {
            return factory.getSupportedCipherSuites();
        }

        @Override
        public Socket createSocket(Socket socket, String host, int port, boolean autoClose) throws IOException {
            return enableProtocolOnSocket(factory.createSocket(socket, host, port, autoClose));
        }

        @Override
        public Socket createSocket(String host, int port) throws IOException, UnknownHostException {
            return enableProtocolOnSocket(factory.createSocket(host, port));
        }

        @Override
        public Socket createSocket(String host, int port, InetAddress localHost, int localPort) throws IOException, UnknownHostException {
            return enableProtocolOnSocket(factory.createSocket(host, port, localHost, localPort));
        }

        @Override
        public Socket createSocket(InetAddress host, int port) throws IOException {
            return enableProtocolOnSocket(factory.createSocket(host, port));
        }

        @Override
        public Socket createSocket(InetAddress host, int port, InetAddress localHost, int localPort) throws IOException {
            return enableProtocolOnSocket(factory.createSocket(host, port, localHost, localPort));
        }

        private Socket enableProtocolOnSocket(Socket s) {
            if (s instanceof SSLSocket) {
                ((SSLSocket) s).setEnabledProtocols(protocols);
            }
            return s;
        }
    }

    private static void tryEnablingTls(HttpsURLConnection connection, String version) {
        try {
            connection.setSSLSocketFactory(getTlsEnablingFactory(version));
            Log.i(TAG, "tryEnablingTls(" + version + ") worked");
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            Log.wtf(TAG, "tryEnablingTls(" + version + ") failed", e);
        }
    }

    private static HttpURLConnection openConnection(String link) throws IOException {
        // Server enforces TLS1.2, which is available in 16+ (https://developer.android.com/reference/javax/net/ssl/SSLContext)
        URLConnection connection = new URL(link).openConnection();

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
            tryEnablingTls((HttpsURLConnection) connection, "TLSv1.2");
        }
        return (HttpURLConnection) connection;
    }

    private static @NonNull Pair<String, Boolean> downloadAndSaveNoRetry(String link, File imageFile) {
        HttpURLConnection connection = null;
        InputStream inputStream = null;
        FileOutputStream fileOutputStream = null;
        File tempFile = new File(imageFile.getAbsolutePath() + TMP_SUFFIX);
        try {
            if (BuildConfig.DEBUG) { LOG_V(">> downloadAndSave(" + imageFile.getName() + ")"); }
            connection = openConnection(link);
            connection.connect();
            inputStream = connection.getInputStream();
            String bitmap = GoogleBitmapHelper.decodeSampledBitmapFromStream(
                    inputStream,
                    tempFile);
            if (bitmap == null) {
                return Pair.create(null, Boolean.FALSE);
            }
            if (!tempFile.renameTo(imageFile)) {
                Log.wtf(TAG, "Can't rename " + tempFile + " to " + imageFile);
                return Pair.create(null, Boolean.FALSE);
            }
            return Pair.create(imageFile.getAbsolutePath(), Boolean.FALSE);
        } catch (IOException e) {
            final Boolean interrupted;
            if (e instanceof java.io.InterruptedIOException) {
                interrupted = Boolean.TRUE;
                // No logs since this is considered normal
            } else {
                interrupted = Boolean.FALSE;
                Log.wtf(TAG, "Can't downloadAndSave(" + imageFile + ")", e);
            }
            return Pair.create(null, interrupted);
        } finally {
            if (tempFile.exists()) {
                MainActivity.deleteFile(tempFile);
            }
            IOUtils.closeQuietly(inputStream);
            IOUtils.closeQuietly(fileOutputStream);
            if (connection != null) connection.disconnect();
            if (BuildConfig.DEBUG) { LOG_V("<< downloadAndSave(" + imageFile.getName() + ")"); }
        }
    }

    // HttpURLConnection follows redirects...  Except when it doesn't.
    // So sometimes it gets into this state of "downloading" but in fact the inputStream receives raw HTTP headers
    public static @Nullable Pair<HttpURLConnection, InputStream> readUrlWithRedirect(String url) {
        HttpURLConnection connection = null;
        InputStream inputStream = null;
        try {
            connection = openConnection(url);
            connection.connect(); // when changing this, change the below connection.connect() too
            inputStream = new BufferedInputStream(connection.getInputStream());
            byte[] httpHeader = "HTTP/".getBytes("UTF-8");
            byte[] readHeader = new byte[httpHeader.length];
            inputStream.mark(readHeader.length * 2); // *2 not really needed, but being super-clean means asking for trouble
            int bytesRead = inputStream.read(readHeader);
            inputStream.reset();
            if (bytesRead == readHeader.length && IOUtils.equals(readHeader, httpHeader)) {
                Scanner scanner = null;
                try {
                    scanner = new Scanner(inputStream);
                    String line;
                    String locationHeader = "Location: ";
                    while (scanner.hasNextLine()) {
                        line = scanner.nextLine();
                        if (line.isEmpty()) {
                            Log.wtf(TAG, "Didn't find Location header, reached the end");
                            // Either not a 301/302, or something else unexpected.  In both cases, break instead of returning null.
                            // Who knows, perhaps consuming all the headers and the empty line will make it so that the rest is body and works OK.
                            break;
                        }

                        if (BuildConfig.DEBUG) { LOG_V("Found header '" + line + "'"); }

                        if (line.startsWith(locationHeader)) {
                            url = line.substring(locationHeader.length());
                            if (BuildConfig.DEBUG) { LOG_V("Found Location: " + url); }
                            connection.disconnect();
                            IOUtils.closeQuietly(inputStream);
                            connection = openConnection(url);
                            connection.connect();
                            inputStream = new BufferedInputStream(connection.getInputStream());
                            break;
                        }
                    }
                } finally {
                    if (scanner != null) scanner.close();
                }
            } else {
                if (BuildConfig.DEBUG) { LOG_V("Read " + bytesRead + " bytes, not matching the header"); }
            }
            return Pair.create(connection, inputStream);
        } catch (IOException e) {
            Log.wtf(TAG, "Can't readUrlWithRedirect(" + url + ")", e);
            if (connection != null) connection.disconnect();
            IOUtils.closeQuietly(inputStream);
            return null;
        }
    }

    public static boolean saveUrlToFile(String url, File file) {
        HttpURLConnection connection = null;
        InputStream inputStream = null;
        FileOutputStream fileOutputStream = null;
        try {
            if (BuildConfig.DEBUG) { LOG_V(">> saveUrlToFile(" + url + "," + file.getName() + ")"); }
            Pair<HttpURLConnection, InputStream> readInfo = readUrlWithRedirect(url);
            if (readInfo == null) {
                // WTF already logged
                return false;
            }
            connection = readInfo.first;
            inputStream = readInfo.second;
            fileOutputStream = new FileOutputStream(file);
            IOUtils.copy(inputStream, fileOutputStream);
            fileOutputStream.close();
            return true;
        } catch (IOException e) {
            Log.e(TAG, "Can't saveUrlToFile", e);
            if (file.exists()) {
                MainActivity.deleteFile(file);
            }
            return false;
        } finally {
            IOUtils.closeQuietly(inputStream);
            IOUtils.closeQuietly(fileOutputStream);
            if (connection != null) connection.disconnect();
            if (BuildConfig.DEBUG) { LOG_V("<< saveUrlToFile(" + file.getName() + ")"); }
        }
    }

    public static boolean createEmptyFile(@NonNull File parentDir, @NonNull String subDirName, @NonNull String fileName) {
        FileOutputStream fileOutputStream = null;
        File file = null;
        try {
            File subDir = new File(parentDir, subDirName);
            if (!subDir.exists()) {
                if (!subDir.mkdir()) {
                    Log.wtf(TAG, "Can't create subdir " + subDirName + " at " + parentDir.getAbsolutePath());
                    return false;
                }
            }
            file = new File(subDir, fileName);
            if (BuildConfig.DEBUG) { LOG_V(">> createEmptyFile(" + file.getName() + ")"); }
            fileOutputStream = new FileOutputStream(file);
            fileOutputStream.close();
            return true;
        } catch (IOException e) {
            Log.e(TAG, "Can't createEmptyFile", e);
            if (file.exists()) {
                MainActivity.deleteFile(file);
            }
            return false;
        } finally {
            IOUtils.closeQuietly(fileOutputStream);
            if (BuildConfig.DEBUG) { LOG_V("<< createEmptyFile(" + fileName + ")"); }
        }
    }

    @NonNull static String fileNameFromNumber(String number) {
        return number + ".mp3";
    }

    public static boolean saveUrlDiffToFile(String url, List<String> currentContent, File destination) {
        List<String> remoteContent = AssetLoader.loadFromUrl(url);
        if (remoteContent == null) {
            if (BuildConfig.DEBUG) { LOG_V("Can't loadFromUrl: " + url); }
            return false;
        }

        OutputStream outputStream = null;
        GZIPOutputStream gzipOutputStream = null;
        OutputStreamWriter writer = null;
        try {
            outputStream = new FileOutputStream(destination);
            gzipOutputStream = new GZIPOutputStream(outputStream);
            writer = new OutputStreamWriter(gzipOutputStream);

            int contentSize = Math.max(remoteContent.size(), currentContent.size());
            for (int i = 0; i < contentSize; i++) {
                if (i >= remoteContent.size()) {
                    writer.write(currentContent.get(i));
                } else if (i >= currentContent.size()) {
                    writer.write(remoteContent.get(i));
                } else {
                    if (remoteContent.get(i).isEmpty()) {
                        writer.write(currentContent.get(i));
                    } else if (!remoteContent.get(i).equals(currentContent.get(i))) {
                        writer.write(remoteContent.get(i));
                    }
                }
                writer.write('\n');
            }

            writer.close();
            gzipOutputStream.close();
            outputStream.close();
            return true;
        } catch (IOException e) {
            Log.wtf(TAG, "Can't read asset", e);
            MainActivity.deleteFile(destination);
        } finally {
            IOUtils.closeQuietly(writer);
            IOUtils.closeQuietly(gzipOutputStream);
            IOUtils.closeQuietly(outputStream);
        }
        return false;
    }
}

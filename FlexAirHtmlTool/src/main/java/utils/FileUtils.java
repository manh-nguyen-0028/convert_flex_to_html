package utils;

import constants.Constants;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FileUtils {

    /**
     * Get all files in folder path
     *
     * @param folder folder path
     * @return List files in folder
     */
    public static Map<String, List<String>> getAllFiles(String folder) {
        Map<String, List<String>> pkList = new HashMap<>();
        try {
            readDirectory(folder, "", pkList);
        } catch (Exception e) {
            throw e;
        }
        return pkList;
    }

    /**
     * Check existed folder, create new folder if not exist.
     *
     * @param folder folder path
     */
    public static void createIfNotExistFolder(String folder) {
        try {
            File directory = new File(folder);
            if (!directory.exists()) {
                Files.createDirectories(Paths.get(folder));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Read all files in a directory and sub directory
     */
    private static void readDirectory(String currentFolder, String parentFolder, Map<String, List<String>> pkList) {
        File directory = new File(currentFolder);
        String pkStr = CommonUtils.isNullOrEmpty(parentFolder) ? directory.getName() : parentFolder + File.separator + directory.getName();
        List<String> filePaths = new ArrayList<>();
        File[] files = directory.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                readDirectory(file.getAbsolutePath(), pkStr, pkList);
            } else {
                filePaths.add(file.getAbsolutePath());
            }
        }
        pkList.put(pkStr, filePaths);
    }

    /**
     * Read all bytes in an InputStream data
     *
     * @throws IOException
     */
    public static byte[] readAllBytes(InputStream inputStream) throws IOException {
        final int bufLen = 4 * 0x400; // 4KB
        byte[] buf = new byte[bufLen];
        int readLen;
        IOException exception = null;

        try {
            try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                while ((readLen = inputStream.read(buf, 0, bufLen)) != -1)
                    outputStream.write(buf, 0, readLen);

                return outputStream.toByteArray();
            }
        } catch (IOException e) {
            exception = e;
            throw e;
        } finally {
            if (exception == null) inputStream.close();
            else try {
                inputStream.close();
            } catch (IOException e) {
                exception.addSuppressed(e);
            }
        }
    }

    public static List<String> readResourcesTxt(String fileName) {
        List<String> result = new ArrayList<>();
        try {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            InputStream inputStream = classLoader.getResourceAsStream("text/" + fileName);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                result.add(line);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return result;
    }

    public static String getFileNameMxml(String filePath) {
        return getFileName(filePath, Constants.MXML_EXT);
    }

    public static String getFileName(String filePath, String suffixFile) {
        Path inputFilePath = Paths.get(filePath);
        return inputFilePath.getFileName().toString().split(suffixFile)[0];
    }
}

package utils;

import as.enums.ASKeyword;
import constants.Constants;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FileUtils {

    /**
     * Get all mxml files in folder path
     *
     * @param folder folder path
     * @param type   file types
     * @return List files in folder
     */
    public static Map<String, List<String>> getAllFilesByType(String folder, String type) {
        Map<String, List<String>> pkList = new HashMap<>();
        try {
            readDirectory(folder, "", pkList, type);
        } catch (Exception e) {
            throw e;
        }
        return pkList;
    }

    /**
     * Get all files in folder path
     *
     * @param folder folder path
     * @return List files in folder
     */
    public static Map<String, List<String>> getAllFiles(String folder) {
        Map<String, List<String>> pkList = new HashMap<>();
        try {
            readDirectory(folder, "", pkList, null);
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
    private static void readDirectory(String currentFolder, String parentFolder, Map<String, List<String>> pkList, String fileType) {
        File directory = new File(currentFolder);
        String pkStr = CommonUtils.isNullOrEmpty(parentFolder) ? directory.getName() : parentFolder + File.separator + directory.getName();
        List<String> filePaths = new ArrayList<>();
        File[] files = directory.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                readDirectory(file.getAbsolutePath(), pkStr, pkList, fileType);
            } else {
                if (!CommonUtils.isNullOrEmpty(fileType) && file.getAbsolutePath().endsWith(fileType)) {
                    filePaths.add(file.getAbsolutePath());
                }
            }
        }
        pkList.put(pkStr, filePaths);
    }

    /**
     * Write string to java file with UTF-8 encoding
     */
    public static void writeFileUTF8(String strFilePath, String text) throws IOException {
        try {
//TODO Overflow exception
//               File file = new File(srcPath + File.separator + "MG1001001_01_000_AS.js");
//                //Instantiating the FileOutputStream class
//                FileOutputStream fileOut = new FileOutputStream(file);
//                //Instantiating the DataOutputStream class
//                DataOutputStream outputStream = new DataOutputStream(fileOut);
//                //Writing UTF data to the output stream
//                outputStream.writeUTF(compiledSource);
            //Creating a BufferedWriter object
            BufferedWriter writer = Files.newBufferedWriter(Paths.get(strFilePath), StandardCharsets.UTF_8);
            //Appending the UTF-8 String to the file
            writer.write(text);
            //Flushing data to the file
            writer.flush();
            writer.close();
        } catch (Exception ex) {
            throw ex;
        }
    }

    /**
     * Read file with UTF-8 encoding
     * @param path file path
     * @return file data in string
     * @throws IOException
     */
    public static String readFileUTF8(Path path) throws IOException {
        try {
            return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw e;
        }
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

    public static String getASFilePath(String mxmlFile, boolean isSuffix) {
        Path path = Paths.get(mxmlFile);
        String mxmlFileName = FileUtils.getFileName(mxmlFile, Constants.MXML_EXT);
        if (isSuffix) {
            String asFileNameAS = mxmlFileName + ASKeyword.ASSUFFIX + Constants.AS_EXT;
            Path asPathAS = Paths.get(path.getParent().toString(), asFileNameAS);
            return asPathAS.toFile().getAbsolutePath();
        } else {
            String asFileNameNonAS = mxmlFileName + Constants.AS_EXT;
            Path asPathNonAS = Paths.get(path.getParent().toString(), asFileNameNonAS);
            return asPathNonAS.toFile().getAbsolutePath();
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

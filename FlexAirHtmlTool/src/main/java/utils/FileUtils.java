package utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FileUtils {

    public static Map<String, List<String>> getAllFiles(String folder) {
        Map<String, List<String>> pkList = new HashMap<>();
        try {
            readDirectory(folder, "", pkList);
        }catch (Exception e) {
            throw e;
        }
        return pkList;
    }
    public static void createIfNotExistFolder(String folder) {
        try {
            File directory = new File(folder);
            if (!directory.exists()) {
                Files.createDirectories(Paths.get(folder));
            }
        }catch (Exception e) {
          e.printStackTrace();
        }
    }
    private static void readDirectory(String currentFolder, String parentFolder, Map<String, List<String>> pkList) {
        File directory = new File(currentFolder);
        String pkStr = StringUtils.isNullOrEmpty(parentFolder) ? directory.getName() : parentFolder + File.separator + directory.getName();
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

}

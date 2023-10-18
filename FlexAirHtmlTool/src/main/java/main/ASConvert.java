package main;

import as.enums.ASKeyword;
import as.parser.ASClass;
import as.parser.ASParser;
import as.parser.ParamOption;
import as.parser.ParserOptions;
import as.types.ASMember;
import constants.ReservedWords;
import utils.Log;
import utils.StringUtils;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ASConvert {
    public ASConvert() {
    }
    public void convert(String inputPath, String outputPath) {
//TODO
        // Prepare parameter
        ParamOption option = new ParamOption();
        option.setSrcPaths(new ArrayList(){{add(inputPath);}});
        option.setEntry("");
        option.setEntryMode("");
        option.setIgnoreFlash(false);
        option.setRawPackages(null);
        option.setSafeRequire(false);
        option.setSilent(false);
        option.setVerbose(true);
        // Compile data
        Map<String, String> result =  compile(option);
        for (String key:result.keySet()) {
            // Save file
            String compiledSource = result.get(key);
            if (!StringUtils.isNullOrEmpty(compiledSource)) {
                writeFileUTF8(inputPath + File.separator + key + ".java", compiledSource);
            }
        }
    }

    /**
     * Compile(Read, Parse, Convert) file
     */
    public Map<String, String> compile(ParamOption options) {
        Map<String, String> packages = new HashMap<>();  //Will contain the final map of package names to source text
        Map<String, String> result = new HashMap<>();
        String tmp;
        options = options != null ? options : new ParamOption();
        List<String> srcPaths = options.getSrcPaths();
        List<String> rawPackages = options.getRawPackages() == null ? new ArrayList<>() : options.getRawPackages();
        ParserOptions parserOptions = new ParserOptions();
        parserOptions.setIgnoreFlash(options.isIgnoreFlash());
        parserOptions.setSafeRequire(options.isSafeRequire());
        // Enable/disable debug mode
        Log.DEBUG_MODE = options.isVerbose();
        Log.SILENT = options.isSilent();
        //Temp classes for holding raw class info
        ASClass rawClass;
        ASParser rawParser;
        // List of class in package
        Map<String, ASClass> classes = new HashMap<>();
        String buffer = "";

        // Read the contents of each file in the input folder and save to list of path package.
        Map<String, Map<String, ASParser>> pkgLists = new HashMap<>();
        for (String path : srcPaths) {
            pkgLists.put(path, buildPackageList(path));
        }

        //First, parse through the file-based classes and get the basic information
        for (String path : pkgLists.keySet()) {
            for (String pkg: pkgLists.get(path).keySet()) {
                Log.log("Analyzing class path: " + pkgLists.get(path).get(pkg).getClassPath());
                classes.put(pkgLists.get(path).get(pkg).getClassPath(), pkgLists.get(path).get(pkg).parse(parserOptions));
                Log.debug(classes.get(pkgLists.get(path).get(pkg).getClassPath()));
            }
        }
        // Read class template
        String classTemplate = readClassTemplate();
        System.out.println(classTemplate);

        // Create package
        for (String key: classes.keySet() ) {
            ASClass cls = classes.get(key);
            cls.process();
            classTemplate = classTemplate.replace("{{package}}", cls.getPackageName());
            classTemplate = classTemplate.replace("{{imports}}", String.join("", cls.getImports()));

            // Generate Controller Class
            String strClassName = cls.getClassName() + ReservedWords.CONTROLLER;
            classTemplate = classTemplate.replace("{{classname}}", strClassName);
            // Generate Created date
            classTemplate = classTemplate.replace("{{created_dt}}", StringUtils.getDateYYYYMMDD());

            // Generate controller source
            String compileSource = cls.generateString();
            classTemplate = classTemplate.replace("{{classsource}}", compileSource);
            // Set result
            result.put(key,classTemplate);

        }

        return result;
    }

    /**
     * Read all file in directory input
     */
    private void readDirectory(String location, String pkgBuffer, Map<String, ASParser> pkList) {
        File directory = new File(location);
        File[] files = directory.listFiles();
        for (File file : files) {
            String pkg = pkgBuffer;
            if (file.isDirectory()) {
                if (!pkg.equals("")) {
                    pkg += ".";
                }
                readDirectory(location + File.separator + file.getName(), pkg + file.getName(), pkList);
            } else if (file.isFile() && file.getName().endsWith(".as")) {
                readFileContent(file, pkList, pkgBuffer);
            }
        }
    }

    /**
     * Read file content with UTF-8 encoding
     */
    public void readFileContent(File file, Map<String, ASParser>  pkList, String pkg) {
        if (!pkg.equals("")) {
            pkg += ".";
        }
        pkg += file.getName().substring(0, file.getName().length() - 3);
        String content = readFileUTF8(file.toPath());
        pkList.put(pkg, new ASParser(content, pkg));
        Log.debug("Loaded file: " + file.getPath() + " (package: " + pkg + ")");
    }

    /**
     * Build package list
     */
    public Map<String, ASParser>  buildPackageList(String location) {
        Map<String, ASParser>  pkList = new HashMap<>();
        location = location.replace("/", File.separator);
        try {
            File directory = new File(location);
            if (directory.exists() && directory.isDirectory()) {
                // Read all folder file
                readDirectory(location, "", pkList);
                // Return package list
                return pkList;
            }  else if (directory.isFile() && directory.getName().endsWith(".as")) {
                // Read file only
                readFileContent(directory, pkList, "");
                // Return package list
                return pkList;
            }
            return null;
        }catch (Exception e) {
            throw e;
        }
    }

    /**
     * Write string to java file with UTF-8 encoding
     */
    public void writeFileUTF8(String strFilePath, String text) {
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
            writer.append(text);
            //Flushing data to the file
            writer.flush();
        }catch (Exception ex) {
            Log.warn(ex.getMessage());
        }
    }
    private String readClassTemplate() {

        try {
            URL templateUrl = getClass().getClassLoader().getResource("templates/classtemplate");
            return readFileUTF8(Paths.get(templateUrl.toURI()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private String readFileUTF8(Path path) {
        try {
            return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}

package main;

import as.enums.ASParseState;
import as.parser.ASClass;
import as.parser.ASParser;
import as.parser.ParamOption;
import as.parser.ParserOptions;
import constants.Constants;
import constants.ReservedWords;
import utils.Log;
import utils.StringUtils;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class ASConvert {
    private String asFileName;
    private String javaFileName;
    public void convertScriptInline(String scriptInline, Map<String, List<String>> xmlObjectInline, String saveFile, String srcPath) {
        Log.log("START: Convert Script inline-------------------------------");
        // Prepare parameter
        ParserOptions parserOptions = new ParserOptions();
        parserOptions.setIgnoreFlash(false);
        parserOptions.setSafeRequire(false);
        Path path = Paths.get(srcPath);
        asFileName = path.getFileName().toString().split(Constants.MXML_EXT)[0];
        // Read the contents of each file in the input folder and save to list of path package.
        ASParser parser = new ASParser(scriptInline, asFileName);

        //First, parse through the file-based classes and get the basic information
        Log.log("Analyzing class path: " + parser.getClassPath());
        Stack<String> stack = new Stack<>();
        stack.push(ASParseState.CLASS);
        ASClass rawClass = parser.parse(parserOptions, stack);
        // Create package
        rawClass.process();
        // Read class template
        String classTemplate = readClassTemplate("templates/classmodel");
        //classTemplate = classTemplate.replace("{{package}}", rawClass.getPackageName());
        classTemplate = classTemplate.replace("{{imports}}", String.join("", rawClass.getImports()));

        // Generate Controller Class
        javaFileName = rawClass.getClassName();
        classTemplate = classTemplate.replace("{{classname}}", javaFileName);
        // Generate Created date
        classTemplate = classTemplate.replace("{{created_dt}}", StringUtils.getDateYYYYMMDD());

        // Generate controller source
        String compileSource = rawClass.generateModelString();
        classTemplate = classTemplate.replace("{{classSource}}", compileSource);

        String outputFile = saveFile + File.separator + javaFileName + Constants.JAVA_EXT;
        Log.log("Save output file: " + outputFile);
        if (!StringUtils.isNullOrEmpty(classTemplate)) {
            writeFileUTF8(outputFile, classTemplate);
        }
        Log.log("END: Convert Script inline-------------------------------");
    }
    public void convert(String inputPath, String outputPath) {
        Log.log("START: Convert Actionscript-------------------------------");
        // Prepare parameter
        ParamOption option = new ParamOption();
        option.setSrcPaths(inputPath);
        option.setEntry("");
        option.setEntryMode("");
        option.setIgnoreFlash(false);
        option.setRawPackages(null);
        option.setSafeRequire(false);
        option.setSilent(false);
        option.setVerbose(true);
        // Compile data
        String compiledSource =  compile(option);
        String outputFile = outputPath + File.separator + javaFileName + Constants.JAVA_EXT;
        Log.log("Save output file: " + outputFile);
        if (!StringUtils.isNullOrEmpty(compiledSource)) {
            writeFileUTF8(outputFile, compiledSource);
        }
        Log.log("END: Convert Actionscript-------------------------------");
    }

    /**
     * Compile(Read, Parse, Convert) file
     */
    public String compile(ParamOption options) {
        Map<String, String> packages = new HashMap<>();  //Will contain the final map of package names to source text
        Map<String, String> result = new HashMap<>();
        String tmp;
        options = options != null ? options : new ParamOption();
        String srcPaths = options.getSrcPaths();
        List<String> rawPackages = options.getRawPackages() == null ? new ArrayList<>() : options.getRawPackages();
        ParserOptions parserOptions = new ParserOptions();
        parserOptions.setIgnoreFlash(options.isIgnoreFlash());
        parserOptions.setSafeRequire(options.isSafeRequire());
        // Enable/disable debug mode
        Log.DEBUG_MODE = options.isVerbose();
        Log.SILENT = options.isSilent();
        //Temp classes for holding raw class info
        ASClass rawClass;
        // Read the contents of each file in the input folder and save to list of path package.
        ASParser parser = readFileContent(srcPaths);

        //First, parse through the file-based classes and get the basic information
        Log.log("Analyzing class path: " + parser.getClassPath());
        Stack<String> stack = new Stack<>();
        stack.push(ASParseState.START);
        rawClass = parser.parse(parserOptions, stack);
        Log.debug(parser.getClassPath());


        // Create package
        rawClass.process();
        // Read class template
        String classTemplate = readClassTemplate("templates/classcontroller");
        classTemplate = classTemplate.replace("{{package}}", rawClass.getPackageName());
        classTemplate = classTemplate.replace("{{imports}}", String.join("", rawClass.getImports()));

        // Generate Controller Class
        javaFileName = rawClass.getClassName() + ReservedWords.CONTROLLER;
        classTemplate = classTemplate.replace("{{classname}}", javaFileName);
        // Generate Created date
        classTemplate = classTemplate.replace("{{created_dt}}", StringUtils.getDateYYYYMMDD());

        // Generate controller source
        String compileSource = rawClass.generateString();
        classTemplate = classTemplate.replace("{{classsource}}", compileSource);
        // Set result
        return classTemplate;
    }

    /**
     * Read file content with UTF-8 encoding
     */
    public ASParser readFileContent(String filePath) {
        Path path = Paths.get(filePath);
        asFileName = path.getFileName().toString().split(Constants.AS_EXT)[0];
        String content = readFileUTF8(path);
        Log.debug("Loaded file: " + filePath + " (package: " + asFileName + ")");
        return new ASParser(content, asFileName);
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
    private String readClassTemplate(String resourceName) {

        try {
            URL templateUrl = getClass().getClassLoader().getResource(resourceName);
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

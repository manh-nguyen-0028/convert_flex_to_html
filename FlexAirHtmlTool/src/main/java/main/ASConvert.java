package main;

import as.enums.ASParseState;
import as.parser.ASClass;
import as.parser.ASParser;
import constants.Constants;
import constants.ReservedWords;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import utils.CommonUtils;
import utils.FileUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public class ASConvert {
    private static final Logger logger = LogManager.getLogger(ASConvert.class);
    private String asFileName;
    private String javaFileName;
    private String packageName;
    private String className;

    public void convertScriptInline(String scriptInline, Map<String, List<String>> xmlObjectInline, String saveFile
            , String srcPath, Map<String, String> pkgFileMapping) {
        try {
            logger.info("START: Convert Script inline-------------------------------");
            // Prepare parameter
            Path path = Paths.get(srcPath);
            asFileName = path.getFileName().toString().split(Constants.MXML_EXT)[0];
            // Read the contents of each file in the input folder and save to list of path package.
            ASParser parser = new ASParser(scriptInline, asFileName);

            //First, parse through the file-based classes and get the basic information
            logger.info("Analyzing class path: " + parser.getClassPath());
            Stack<String> stack = new Stack<>();
            stack.push(ASParseState.CLASS);
            ASClass rawClass = parser.parse(stack);
            // Create package
            rawClass.modelProcess();
            // Read class template
            String classTemplate = readClassTemplate("templates/classmodel");
            String packageName;
            if (pkgFileMapping.size() > 0) {
                packageName = pkgFileMapping.get(asFileName);
                if (!CommonUtils.isNullOrEmpty(packageName)) {
                    classTemplate = classTemplate.replace("{{package}}", packageName);
                }
            }
            if (rawClass.getImports().size() > 0) {
                classTemplate = classTemplate.replace("{{imports}}", String.join(CommonUtils.EMPTY, rawClass.getImports()) + ";");
            } else {
                classTemplate = classTemplate.replace("{{imports}}", CommonUtils.EMPTY);
            }

            // Generate Controller Class
            javaFileName = rawClass.getClassName();
            classTemplate = classTemplate.replace("{{classname}}", javaFileName);
            // Generate Created date
            classTemplate = classTemplate.replace("{{created_dt}}", CommonUtils.getDateYYYYMMDD());

            // Generate controller source
            String compileSource = rawClass.generateModelString(xmlObjectInline);
            classTemplate = classTemplate.replace("{{classSource}}", compileSource);

            String outputFile = saveFile + File.separator + javaFileName + Constants.JAVA_EXT;
            logger.info("Save output file: " + outputFile);
            if (!CommonUtils.isNullOrEmpty(classTemplate)) {
                writeFileUTF8(outputFile, classTemplate);
            }
            logger.info("END: Convert Script inline-------------------------------");
        } catch (Exception e) {
            logger.error(e);
        }
    }

    public void convert(String inputPath, String outputPath) {
        try {
            logger.info("START: Convert Actionscript-------------------------------");
            // Prepare parameter
            // Compile data
            String compiledSource = compile(inputPath);
            String outputFile = outputPath + File.separator + javaFileName + Constants.JAVA_EXT;
            logger.info("Save output file: " + outputFile);
            if (!CommonUtils.isNullOrEmpty(compiledSource)) {
                writeFileUTF8(outputFile, compiledSource);
                logger.info("Done.");
            }
            logger.info("END: Convert Actionscript-------------------------------");
        } catch (Exception e) {
            logger.error(e);
        }
    }

    /**
     * Compile(Read, Parse, Convert) file
     */
    private String compile(String inputPath) {
        Stack<String> stack = new Stack<>();
        //Temp classes for holding raw class info
        ASClass rawClass;
        //First, parse through the file-based classes and get the basic information
        // Read the contents of each file in the input folder and save to list of path package.
        ASParser parser = readFileContent(inputPath);

        // Now parse through any raw string classes
        logger.info("Analyzing class path: " + parser.getClassPath());
        stack.push(ASParseState.START);
        rawClass = parser.parse(stack);

        // Create package
        rawClass.process();
        // Read class template from resource files
        logger.info("Read template controller class: templates/classcontroller");
        String classTemplate = readClassTemplate("templates/classcontroller");
        logger.info("Generating package name:" + rawClass.getPackageName());
        classTemplate = classTemplate.replace("{{package}}", rawClass.getPackageName());
        logger.info("Generating imported packages: " + rawClass.getImports().size() + " packages");
        classTemplate = classTemplate.replace("{{imports}}", String.join("", rawClass.getImports()));

        // Generate Controller Class
        javaFileName = rawClass.getClassName() + ReservedWords.CONTROLLER;
        logger.info("Generating controller class:" + javaFileName);
        classTemplate = classTemplate.replace("{{classname}}", javaFileName);
        // Generate Created date
        classTemplate = classTemplate.replace("{{created_dt}}", CommonUtils.getDateYYYYMMDD());

        // Generate controller source
        String compileSource = rawClass.generateString();
        classTemplate = classTemplate.replace("{{classsource}}", compileSource);
        //Set package and class name for model class
        setPackageName(rawClass.getPackageName());
        setClassName(rawClass.getClassName());
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
        logger.debug("Loaded file: " + filePath + " (package: " + asFileName + ")");
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
            writer.write(text);
            //Flushing data to the file
            writer.flush();
            writer.close();
            logger.debug("Save output file: Success");
        } catch (Exception ex) {
            logger.error(ex);
        }
    }

    private String readClassTemplate(String resourceName) {
        try {
            ClassLoader classloader = Thread.currentThread().getContextClassLoader();
            return new String(FileUtils.readAllBytes(classloader.getResourceAsStream(resourceName)), StandardCharsets.UTF_8);
        } catch (Exception ex) {
            logger.error(ex);
        }
        return null;
    }

    private String readFileUTF8(Path path) {
        try {
            return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
        } catch (Exception e) {
            logger.error(e);
        }
        return null;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }
}

package main;

import as.enums.ASParseState;
import as.parser.ASClass;
import as.parser.ASParser;
import as.types.ASFunction;
import as.types.ASMember;
import constants.Constants;
import constants.ReservedWords;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import utils.CommonUtils;
import utils.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Stack;
import java.util.stream.Collectors;

public class ASConvert {
    private static final Logger logger = LogManager.getLogger(ASConvert.class);
    private String asFileName;
    private String javaFileName;
    private String packageName;
    private String className;
    private String scriptInline;
    List<ASMember> modelMembers;
    private String inputPath;
    private String outputPath;
    //Temp classes for holding raw class info
    private ASClass rawInlineClass;
    private ASClass rawClass;
    private Map<String, List<String>> xmlObjectInline;

    private String classModelTemplate;
    private String classRecordTemplate;
    /**
     * Default constructor
     */
    public ASConvert() {
    }

    public ASConvert(String inputPath, String outputPath, String scriptInline
            , List<ASMember> modelMembers, Map<String, List<String>> xmlObjectInline) {
        this.scriptInline = scriptInline;
        this.modelMembers = modelMembers;
        this.inputPath = inputPath;
        this.outputPath = outputPath;
        this.xmlObjectInline = xmlObjectInline;
    }

    public ASConvert(String inputPath, String outputPath, String scriptInline, List<ASMember> modelMembers) {
        this.scriptInline = scriptInline;
        this.modelMembers = modelMembers;
        this.inputPath = inputPath;
        this.outputPath = outputPath;
    }
    /**
     * Compile file actionscript inline
     * Convert actionscript to java syntax
     */
    public String compileScriptInline() {
        try {
            logger.info("START: compile actionscript inline-------------------------------");

            // Create package
            rawInlineClass.modelProcess();
            // Read class template
            classModelTemplate = readClassTemplate("templates/classmodel");
            if (!CommonUtils.isNullOrEmpty(packageName)) {
                classModelTemplate = classModelTemplate.replace("{{package}}", packageName);
            }
            if (rawInlineClass.getImports().size() > 0) {
                classModelTemplate = classModelTemplate.replace("{{imports}}", String.join(CommonUtils.EMPTY, rawInlineClass.getImports()) + ";");
            } else {
                classModelTemplate = classModelTemplate.replace("{{imports}}", CommonUtils.EMPTY);
            }

            // Generate Controller Class
            javaFileName = rawInlineClass.getClassName();
            classModelTemplate = classModelTemplate.replace("{{classname}}", javaFileName);
            // Generate Created date
            classModelTemplate = classModelTemplate.replace("{{created_dt}}", CommonUtils.getDateYYYYMMDD());

            // Generate model source
            String compileSource = rawInlineClass.generateModelString(modelMembers);
            classModelTemplate = classModelTemplate.replace("{{classSource}}", compileSource);
            if (xmlObjectInline != null && xmlObjectInline.size() > 0) {
                classRecordTemplate = readClassTemplate("templates/classRecord");
                if (!CommonUtils.isNullOrEmpty(packageName)) {
                    classRecordTemplate = classRecordTemplate.replace("{{package}}", packageName);
                }
                classRecordTemplate = classRecordTemplate.replace("{{classname}}", javaFileName.replace(ReservedWords.MODEL, ReservedWords.RECORD));
                classRecordTemplate = classRecordTemplate.replace("{{created_dt}}", CommonUtils.getDateYYYYMMDD());
                compileSource = rawInlineClass.generateRecordString(xmlObjectInline);
                // Generate Created date
                classRecordTemplate = classRecordTemplate.replace("{{classSource}}", compileSource);
            }

            logger.info("END: compile actionscript inline-------------------------------");
        } catch (Exception ex) {
            logger.error("[Error-compileScriptInline]: " + inputPath, ex);
            return null;
        }
        return classModelTemplate;
    }

    /**
     * Convert Actionscript and inline script to java file
     * Reading input file, parsing data, convert syntax, write file
     */
    public void convert() {
        try {
            String outputFile;
            String compiledSource;
            logger.info("START: parser Actionscript & Actionscript Inline-------------------------------");
            parser();
            logger.info("END: parser Actionscript & Actionscript Inline-------------------------------");
            if (rawClass != null) {
                logger.info("START: Convert Actionscript & Actionscript Inline-------------------------------");
                //Convert Actionscript file
                compiledSource = compileActionScript();
                if (!CommonUtils.isNullOrEmpty(compiledSource)) {
                    outputFile = outputPath + File.separator + javaFileName + Constants.JAVA_EXT;
                    logger.info("Save output controller file: " + outputFile);
                   FileUtils.writeFileUTF8(outputFile, compiledSource);
                    logger.info("Done.");
                }
            }
            if (rawInlineClass != null) {
                //Convert Actionscript inline to Model, Record java file
                compileScriptInline();
                if (!CommonUtils.isNullOrEmpty(classModelTemplate)) {
                    //Write Model file
                    outputFile = outputPath + File.separator + javaFileName + Constants.JAVA_EXT;
                    logger.info("Save output model file: " + outputFile);
                    FileUtils.writeFileUTF8(outputFile, classModelTemplate);
                    logger.info("Model file Done.");
                    classModelTemplate = null;
                }
                if (!CommonUtils.isNullOrEmpty(classRecordTemplate)) {
                    //Write Record file
                    outputFile = outputPath + File.separator + javaFileName.replace(ReservedWords.MODEL, ReservedWords.RECORD) + Constants.JAVA_EXT;
                    logger.info("Save output record file: " + outputFile);
                    FileUtils.writeFileUTF8(outputFile, classRecordTemplate);
                    logger.info("Record file Done.");
                    classRecordTemplate = null;
                    rawInlineClass = null;
                }
                logger.info("END: Convert Actionscript & Actionscript Inline-------------------------------");
            }
        } catch (Exception ex) {
            logger.error("[Error-convert]: " + inputPath, ex);
        }
    }

    private void parser() {
        Stack<String> stack = new Stack<>();
        //First, parse through the file-based classes and get the basic information
        // Read the contents of each file in the input folder and save to list of path package.
        ASParser parser = readFileContent(inputPath);
        if (parser != null) {
            // Now parse through any raw string classes
            logger.info("Analyzing class path for Actionscript: " + parser.getClassPath());
            stack.push(ASParseState.START);
            rawClass = parser.parse(stack);
            className = rawClass.getClassName();
            packageName = rawClass.getPackageName();
        }
        if (!CommonUtils.isNullOrEmpty(scriptInline)) {
            if (CommonUtils.isNullOrEmpty(className)) {
                className = FileUtils.getFileName(inputPath, Constants.MXML_EXT);
            }
            //Parsing Actionscript inline
            // Read the contents of each file in the input folder and save to list of path package.
            ASParser parserInline = new ASParser(scriptInline, className);
            //First, parse through the file-based classes and get the basic information
            logger.info("Analyzing class path for Actionscript inline: " + parserInline.getClassPath());
            stack = new Stack<>();
            stack.push(ASParseState.CLASS);
            rawInlineClass = parserInline.parse(stack);
        }
    }
    /**
     * Compile file actionscript
     * Merge method from inline class, convert actionscript to java syntax
     */
    private String compileActionScript() {
        if (rawInlineClass != null) {
            // Add functions from MXML
            List<ASMember> inlineFuncs = rawInlineClass.getMembers().stream()
                    .filter(f -> f instanceof ASFunction).collect(Collectors.toList());
            if (inlineFuncs != null && inlineFuncs.size() > 0) {
                for (ASMember member : inlineFuncs) {
                    // Find member inline class in actionscript member
                    Optional<ASMember> asMember = rawClass.getMembers().stream()
                            .filter(f -> (f instanceof ASFunction) && f.getName().equals(member.getName()) && f.getSubType() == null).findFirst();
                    if (!asMember.isPresent()) {
                        String commentStr = member.getComment().replaceAll("^;\\s*\\t*\\n", "");
                        commentStr = commentStr.replaceAll("\n\t", "\n");
                        member.setComment(commentStr);
                        String funcValue = member.getValue();
                        funcValue = funcValue.replaceAll("\n\t", "\n");
                        member.setValue(funcValue);
                        rawClass.getMembers().add(member);
                    }
                }
            }
        }
        // Create package
        rawClass.process();
        // Read class template from resource files
        logger.info("Read template controller class: templates/classcontroller");
        String classTemplate = readClassTemplate("templates/classcontroller");
        logger.info("Generating package name:" + packageName);
        classTemplate = classTemplate.replace("{{package}}", packageName);
        logger.info("Generating imported packages: " + rawClass.getImports().size() + " packages");
        classTemplate = classTemplate.replace("{{imports}}", String.join("", rawClass.getImports()));

        // Generate Controller Class
        javaFileName = className + ReservedWords.CONTROLLER;
        logger.info("Generating controller class:" + javaFileName);
        classTemplate = classTemplate.replace("{{classname}}", javaFileName);
        // Generate Created date
        classTemplate = classTemplate.replace("{{created_dt}}", CommonUtils.getDateYYYYMMDD());

        // Generate controller source
        String compileSource = rawClass.generateString();
        classTemplate = classTemplate.replace("{{classsource}}", compileSource);

        // Set result
        return classTemplate;
    }

    /**
     * Read file content with UTF-8 encoding
     */
    public ASParser readFileContent(String filePath){
        if (CommonUtils.isNullOrEmpty(filePath)) return null;
        Path asFilePath;
        if (filePath.endsWith(Constants.AS_EXT)) {
            asFilePath = Paths.get(filePath);
        } else {
            // Find AS equivalent file with MXML
            asFilePath = getASFilePath(filePath);
            if (asFilePath == null) return null;
        }
        String content = null;
        try {
            content = FileUtils.readFileUTF8(asFilePath);
        } catch (IOException ex) {
            logger.error("[Error-readFileContent]: " + asFilePath, ex);
            return null;
        }
        if (CommonUtils.isNullOrEmpty(content)) {
            logger.error("Parser data is empty! Please check input file and run again.");
            return null;
        }
        asFileName = FileUtils.getFileName(filePath, Constants.MXML_EXT);
        logger.debug("Loaded file: " + filePath + " (package: " + asFileName + ")");
        return new ASParser(content, asFileName);
    }

    /**
     * Get Actionscript file corresponding to MXML file
     * AS file maybe ending with '_AS' string
     * @param mxmlFile MXML file path
     * @return AS file path
     */
    private Path getASFilePath(String mxmlFile) {
        try {
            // Find AS equivalent file with MXML
            Path asPathAS = Paths.get(FileUtils.getASFilePath(mxmlFile, true));
            Path asPathNonAS = Paths.get(FileUtils.getASFilePath(mxmlFile, false));
            // Check exist file
            if (asPathAS.toFile().exists()) {
                // File name ending with _AS suffix
                return asPathAS;
            } else if (asPathNonAS.toFile().exists()) {
                // File name ending with non _AS suffix
                return asPathNonAS;
            } else {
                return null;
            }
        } catch (Exception ex){
            logger.error("[Error-getASFilePath]: "+ mxmlFile, ex);
            return null;
        }
    }

    /**
     * Read resource data
     * @param resourceName resource name
     * @return file data in string
     */
    private String readClassTemplate(String resourceName) {
        try {
            ClassLoader classloader = Thread.currentThread().getContextClassLoader();
            return new String(FileUtils.readAllBytes(classloader.getResourceAsStream(resourceName)), StandardCharsets.UTF_8);
        } catch (Exception ex) {
            logger.error("[Error-readClassTemplate]: "+resourceName, ex);
        }
        return null;
    }
}

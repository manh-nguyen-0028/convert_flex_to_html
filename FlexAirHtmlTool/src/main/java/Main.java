import constants.Constants;
import main.ASConvert;
import main.MxmlConvert;
import mxml.config.XhtmlConfig;
import org.apache.commons.cli.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import utils.FileUtils;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class Main {
    private static final Logger logger = LogManager.getLogger(ASConvert.class);

    public static void main(String[] args) {
        ASConvert asConvert;
        MxmlConvert mxmlConvert;
        Options options = new Options();
        boolean hasMxmlFiles = true;
        // Input source code folder option param
        Option input = new Option("i", "input", true, "Input folder source code to convert(contains flex air files)");
        input.setRequired(true);
        options.addOption(input);
        // Output folder option param
        Option output = new Option("o", "output", true, "Output folder to save converted files(java and xhtml files)");
        output.setRequired(true);
        options.addOption(output);

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd = null;// not a good practice, it serves it purpose
        try {
            cmd = parser.parse(options, args);
        } catch (org.apache.commons.cli.ParseException ex) {
            ex.printStackTrace();
            formatter.printHelp("Parameters", options);
            logger.error("[Error]:", ex);
            System.exit(1);
        }
        // Get params argument
        String outputPath = cmd.getOptionValue("output");
        String inputPath = cmd.getOptionValue("input");
        // Get all files from input folder
        try {
            Map<String, List<String>> mxmlFiles = FileUtils.getAllFilesByType(inputPath, Constants.MXML_EXT);
            // parse all files
            for (String pk : mxmlFiles.keySet()) {
                List<String> filePaths = mxmlFiles.get(pk);
                if (filePaths.size() == 0) {
                    // No file to convert
                    logger.warn("There are no mxml files in the input folder to perform data conversion processing.");
                    break;
                }
                String savePath = outputPath + File.separator + pk;
                // Create if output folder is not exist
                FileUtils.createIfNotExistFolder(savePath);
                // Convert mxml files
                List<XhtmlConfig> xhtmlConfigs = filePaths.stream()
                        .map(item -> new XhtmlConfig(item, FileUtils.getFileNameMxml(item)))
                        .collect(Collectors.toList());
                Map<String, XhtmlConfig> xhtmlConfigMap = xhtmlConfigs.stream()
                        .collect(Collectors.toMap(item -> item.getFileName(), item -> item));
                // Convert mxml files
                for (String file : filePaths) {
                    // Mxml convert
                    mxmlConvert = new MxmlConvert();
                    mxmlConvert.convert(file, savePath, xhtmlConfigMap);
                    // Action script convert
                    asConvert = new ASConvert(file, savePath, mxmlConvert.getScriptInline()
                            , mxmlConvert.getModelMembers(), mxmlConvert.getXmlObjectInline());
                    asConvert.convert();
                }
            }
            // Find as file independence mxml file
            Map<String, List<String>> asFiles = FileUtils.getAllFilesByType(inputPath, Constants.AS_EXT);
            for (String pk : asFiles.keySet()) {
                List<String> asFilePaths = asFiles.get(pk);
                List<String> mxmlFilePaths = mxmlFiles.get(pk);
                String savePath = outputPath + File.separator + pk;
                for (String asFile : asFilePaths) {
                    Optional<String> matchedPath = mxmlFilePaths
                            .stream()
                            .filter(f -> FileUtils.getASFilePath(f, true).equals(asFile)
                                    || FileUtils.getASFilePath(f, false).equals(asFile))
                            .findFirst();
                    if (!matchedPath.isPresent()) {
                        // Action script convert
                        asConvert = new ASConvert(asFile, savePath, null, null);
                        asConvert.convert();
                    }
                }
            }
        } catch (Exception ex) {
            logger.error("[Error]:", ex);
        }
    }
}

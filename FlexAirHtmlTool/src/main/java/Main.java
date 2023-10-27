import constants.Constants;
import main.ASConvert;
import main.MxmlConvert;
import org.apache.commons.cli.*;
import utils.FileUtils;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Main {
    public static void main(String[] args) {
        ASConvert asConvert;
        MxmlConvert mxmlConvert;
        Options options = new Options();
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
        } catch (org.apache.commons.cli.ParseException e) {
            e.printStackTrace();
            formatter.printHelp("Parameters", options);
            System.exit(1);
        }
        // Get params argument
        String outputPath = cmd.getOptionValue("output");
        String inputPath = cmd.getOptionValue("input");
        // Get all files from input folder
        Map<String, List<String>> files = FileUtils.getAllFiles(inputPath);
        Map<String, String> pkgFileMapping = new HashMap<>();
        // parse all files
        for (String pk : files.keySet()) {
            List<String> filePath = files.get(pk);
            String savePath = outputPath + File.separator + pk;
            // Create if output folder is not exist
            FileUtils.createIfNotExistFolder(savePath);
            // mxml files
            List<String> mxmlFiles = filePath.stream().filter(f -> f.endsWith(Constants.MXML_EXT)).collect(Collectors.toList());
            // Convert action script files
            List<String> asFiles = filePath.stream().filter(f -> f.endsWith(Constants.AS_EXT)).collect(Collectors.toList());
            for (String file : asFiles) {
                asConvert = new ASConvert();
                // Action script convert
                asConvert.convert(file, savePath);
                pkgFileMapping.put(asConvert.getClassName(), asConvert.getPackageName());
            }
            // Convert mxml files
            for (String file : mxmlFiles) {
                // Mxml convert
                mxmlConvert = new MxmlConvert();
                mxmlConvert.convert(file, savePath);
                // Script inline convert
                ASConvert asConvertInline = new ASConvert();
                asConvertInline.convertScriptInline(mxmlConvert.getScriptInline(), mxmlConvert.getXmlObjectInline(), savePath, file, pkgFileMapping);
            }
        }
    }
}

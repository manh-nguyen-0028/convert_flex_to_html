import constants.Constants;
import main.ASConvert;
import main.MxmlConvert;
import utils.FileUtils;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.cli.*;

public class Main {
    public static void main(String[] args) {
        ASConvert asConvert;
        MxmlConvert mxmlConvert;
        Options options = new Options();

        Option input = new Option("i", "input", true, "input folder");
        input.setRequired(true);
        options.addOption(input);

        Option output = new Option("o", "output", true, "output folder");
        output.setRequired(true);
        options.addOption(output);

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd = null;// not a good practice, it serves it purpose
        try {
            cmd = parser.parse(options, args);
        } catch (org.apache.commons.cli.ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("utility-name", options);

            System.exit(1);
        }
        // Get params argument
        String outputPath = cmd.getOptionValue("output");
        String inputPath = cmd.getOptionValue("input");
        // Get all files from input folder
        Map<String, List<String>> files = FileUtils.getAllFiles(inputPath);
        Map<String, String> pkgFileMapping = new HashMap<>();
        // parse all files
        for (String pk: files.keySet()) {
            List<String> filePath = files.get(pk);
            String savePath = outputPath + File.separator + pk;
            // Create if output folder is not exist
            FileUtils.createIfNotExistFolder(savePath);
            // mxml files
            List<String> mxmlFiles = filePath.stream().filter(f -> f.endsWith(Constants.MXML_EXT)).collect(Collectors.toList());
            // as files
            List<String> asFiles = filePath.stream().filter(f -> f.endsWith(Constants.AS_EXT)).collect(Collectors.toList());
            for (String file: asFiles) {
                asConvert = new ASConvert();
                // Action script convert
                asConvert.convert(file, savePath);
                pkgFileMapping.put(asConvert.getClassName(), asConvert.getPackageName());
            }
            //TODO parse mxml files
            for (String file: mxmlFiles) {
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

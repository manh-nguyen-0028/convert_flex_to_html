import main.ASConvert;
import main.MxmlConvert;

import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {
    public static void main(String[] args) {
        String resourcePath = Main.class.getClassLoader().getResource("").getPath();
        ASConvert asConvert = new ASConvert();
        // Use resource path if you want
        // String inputPath = resourcePath + "\\input";

        String outputPath = "D:\\output";
        String inputPath = "D:\\input";

        Path inputFilePath = Paths.get(inputPath);

        System.out.println(inputPath);

        MxmlConvert.convert(inputPath, outputPath);
        // Call convert as file
//        asConvert.convert(inputPath, outputPath);
    }
}

package main;

import org.jsoup.Jsoup;
import org.w3c.dom.Document;
import service.ConvertMxmlService;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class MxmlConvert {
    public static void convert(String inputPath, String outputPath) {
//        String fileInputPath = inputPath + File.separator + "MG3001001_01_000.mxml";
        String fileInputPath = inputPath + File.separator + "MG1001001_00_000.mxml";
//        String fileInputPath = inputPath + File.separator + "MG1001001_01_000.mxml";

        try {
            Path inputFilePath = Paths.get(fileInputPath);
            String xmlFileName = inputFilePath.getFileName().toString().split(".mxml")[0];

            try (InputStream is = Files.newInputStream(inputFilePath);
                 BufferedWriter htmlWriter = Files.newBufferedWriter(Paths.get(outputPath, xmlFileName + ".html"));
                 BufferedWriter cssWriter = Files.newBufferedWriter(Paths.get(outputPath, "css", xmlFileName + ".css"))) {

                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                Document doc = dbf.newDocumentBuilder().parse(is);

                System.out.println("Root Element :" + doc.getDocumentElement().getNodeName());
                System.out.println("------");

                StringBuilder html = new StringBuilder();
                html.append(ConvertMxmlService.initHtml("MXML convert to HTML", xmlFileName));

                // style
                StringBuilder cssInFile = new StringBuilder();
                if (doc.hasChildNodes()) {
                    ConvertMxmlService.printNote(doc.getChildNodes(), html, null, cssInFile, xmlFileName);
                }

                html.append("</body></html>");

                org.jsoup.nodes.Document jsoupDoc = Jsoup.parse(html.toString());
                htmlWriter.write(jsoupDoc.toString());
                System.out.println("Html file created successfully.");

                cssWriter.write(cssInFile.toString());
                System.out.println("CSS file created successfully.");

            } catch (FileNotFoundException e) {
                System.err.println("File not found: " + inputFilePath);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

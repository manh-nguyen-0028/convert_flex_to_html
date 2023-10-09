import org.jsoup.Jsoup;
import org.w3c.dom.*;
import org.xml.sax.SAXException;
import service.ConvertService;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;

public class Main {
    //    private static final String FILENAME = "D:\\\\Project\\\\Nissho\\\\AdobeFlexAir\\\\506519.Roseindia-Flex-Examples\\\\Other Flex examples\\\\Scarab.mxml";
//    private static final String FILENAME = "D:\\Project\\Nissho\\AdobeFlexAir\\Source\\02.FromCustomer\\MG1001001_ログイン画面\\CLIENT\\" + "MG1001001_01_000.mxml";
    private static final String FILENAME = "D:\\Project\\Nissho\\AdobeFlexAir\\Source\\04.SourceCode\\branches\\st\\CLIENT\\ACC\\src\\jp\\co\\nissho_ele\\acc\\mg\\portal\\" + "MG1002001_01_000.mxml";

    private static final String outputPath = "D:\\Project\\Nissho\\AdobeFlexAir\\output";

    public static String xmlFileName;

    public static void main(String[] args) {

        // Instantiate the Factory
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

        File initialFile = new File(FILENAME);

        xmlFileName = initialFile.getName().split(".mxml")[0];

        try (InputStream is = new FileInputStream(initialFile);) {

            // parse XML file
            DocumentBuilder db = dbf.newDocumentBuilder();
            // read from a project's resources folder
            Document doc = db.parse(is);

            System.out.println("Root Element :" + doc.getDocumentElement().getNodeName());
            System.out.println("------");

            StringBuilder html = new StringBuilder();

            html.append(ConvertService.initHtml("MXML convert to HTML", xmlFileName));

            // style
            StringBuilder cssInFile = new StringBuilder();
            if (doc.hasChildNodes()) {
                new ConvertService().printNote(doc.getChildNodes(), html, null, cssInFile);
            }

            html.append("</body></html>");

            org.jsoup.nodes.Document jsoupDoc = Jsoup.parse(html.toString());
            String htmlFilePath = outputPath + "\\" + xmlFileName + ".html";
            String cssFilePath = outputPath + "\\css\\" + xmlFileName + ".css";

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(htmlFilePath))) {
                // Ghi nội dung vào file
                writer.write(jsoupDoc.toString());
                System.out.println("File created and content written successfully.");
            } catch (IOException e) {
                // Xử lý nếu có lỗi
                System.err.println("Error writing to the file: " + e.getMessage());
            }

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(cssFilePath))) {
                // Ghi nội dung vào file
                writer.write(cssInFile.toString());
                System.out.println("File created and content written successfully.");
            } catch (IOException e) {
                // Xử lý nếu có lỗi
                System.err.println("Error writing to the file: " + e.getMessage());
            }

//            System.out.println(jsoupDoc.toString());
        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        }

    }
}

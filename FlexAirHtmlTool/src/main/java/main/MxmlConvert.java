package main;

import dto.mxml.mapping.AttributeMap;
import dto.mxml.mapping.NodeMap;
import dto.mxml.modify.ElementReplace;
import dto.mxml.modify.RadioGroupReplace;
import org.jsoup.Jsoup;
import org.jsoup.parser.Parser;
import org.w3c.dom.Document;
import service.ConvertMxmlService;
import service.XmlService;
import utils.Log;
import utils.StringUtils;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

public class MxmlConvert {
    private MxmlConvert() {

    }

    public static void convert(String inputPath, String outputPath) {
//        String fileInputPath = inputPath + File.separator + "MG3001001_01_000.mxml";
//        String fileInputPath = inputPath + File.separator + "MG1001001_00_000.mxml";
//        String fileInputPath = inputPath + File.separator + "MG1001001_01_000.mxml";
        String fileInputPath = inputPath + File.separator + "MG5002001_01_000.mxml";

        try {
            Path inputFilePath = Paths.get(fileInputPath);
            String xmlFileName = inputFilePath.getFileName().toString().split(".mxml")[0];

            ElementReplace elementReplace = new ElementReplace();
            elementReplace.setFormName(xmlFileName.replaceAll("_", ""));

            // 1. Read file html base
            StringBuilder baseHtml = createHtmlBase();

            // 2. Read file xml then append to htmlNeedAddText
            try (InputStream is = Files.newInputStream(inputFilePath);
                 BufferedWriter htmlWriter = Files.newBufferedWriter(Paths.get(outputPath, xmlFileName + ".xhtml"));
                 BufferedWriter cssWriter = Files.newBufferedWriter(Paths.get(outputPath, "css", xmlFileName + ".css"))) {

                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
                Document doc = factory.newDocumentBuilder().parse(is);

                Log.log("Root Element :" + doc.getDocumentElement().getNodeName());
                Log.log("------");

                StringBuilder html = new StringBuilder();

                // style
                StringBuilder cssInFile = new StringBuilder();

                Map<String, NodeMap> hmNodeMap = XmlService.getNodeMap();

                Map<String, AttributeMap> hmAttributeMap = XmlService.getAttributeMap();

                // Get mapping config
                if (doc.hasChildNodes()) {
                    new ConvertMxmlService(hmNodeMap, hmAttributeMap, elementReplace).printNote(true, doc.getChildNodes(), baseHtml, html);
                }

                StringUtils.replaceInStringBuilder(baseHtml, "{form_content}", html.toString());
                StringUtils.replaceInStringBuilder(baseHtml, "{css_file_name}", xmlFileName);

                handleElementReplace(baseHtml, elementReplace);

                org.jsoup.nodes.Document jsoupDoc = Jsoup.parse(baseHtml.toString(), "UTF-8", Parser.xmlParser());
                htmlWriter.write(jsoupDoc.toString());
                Log.log("Html file created successfully.");

                cssWriter.write(cssInFile.toString());
                Log.log("CSS file created successfully.");

            } catch (FileNotFoundException e) {
                Log.log("File not found: " + inputFilePath);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static StringBuilder createHtmlBase() {
        StringBuilder baseHtml = new StringBuilder();
        try {
            ClassLoader classLoader = MxmlConvert.class.getClassLoader();
            String baseHtmlFileName = "templates/base_html.txt";
            InputStream inputStream = classLoader.getResourceAsStream(baseHtmlFileName);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                baseHtml.append(line);
                baseHtml.append("\n");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return baseHtml;
    }

    private static void handleElementReplace(StringBuilder baseHtml, ElementReplace elementReplace) {
        StringUtils.replaceInStringBuilder(baseHtml, "{title_form_content}", "<ui:define name=\"title\">" + elementReplace.getTitle() + "</ui:define>");
        StringUtils.replaceInStringBuilder(baseHtml, "{css_include_content}", "<h:outputStylesheet library=\"acc_css\" name=\"" + elementReplace.getFormName() + ".css\" />");
        StringUtils.replaceInStringBuilder(baseHtml, "{js_include_content}", "<h:outputScript library=\"acc_js\" name=\"" + elementReplace.getFormName() + ".js\" />");
        StringUtils.replaceInStringBuilder(baseHtml, "{form_id_content}", elementReplace.getFormName() + "Form");

        // Handle radio group
        for (RadioGroupReplace item : elementReplace.getRadioGroupReplaces()) {
            String groupId = item.getGroupId();
            for (String radioItem : item.getSelectItemList()) {
                StringUtils.replaceInStringBuilder(baseHtml, "id=\"" + groupId + "\">", "id=\"" + groupId + "\">" + radioItem);
            }
        }
    }
}

package main;

import constants.Constants;
import mxml.dto.mapping.ComponentMap;
import mxml.dto.mapping.PropertyMap;
import mxml.dto.modify.ElementReplace;
import mxml.dto.modify.RadioGroupReplace;
import mxml.dto.parser.HtmlElementParser;
import mxml.service.ConvertMxmlService;
import mxml.service.JsoupService;
import mxml.service.XmlService;
import org.apache.commons.collections4.CollectionUtils;
import org.jsoup.Jsoup;
import org.jsoup.parser.Parser;
import org.w3c.dom.CharacterData;
import org.w3c.dom.*;
import utils.FileUtils;
import utils.Log;
import utils.StringUtils;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MxmlConvert {

    // Script Inline source code
    private String scriptInline;
    // xml properties list
    private Map<String, List<String>> xmlObjectInline;

    public void convert(String inputPath, String outputPath) {

        String cssPath = Paths.get(outputPath, "css").toString();
        //Create if folder not exist
        FileUtils.createIfNotExistFolder(cssPath);
        Path inputFilePath = Paths.get(inputPath);
        String xmlFileName = inputFilePath.getFileName().toString().split(Constants.MXML_EXT)[0];

        ElementReplace elementReplace = new ElementReplace();
        elementReplace.setFormName(xmlFileName.replace("_", ""));

        // 1. Read file html base
        StringBuilder baseHtml = createHtmlBase();

        // 2. Read file xml then append to htmlNeedAddText
        try (InputStream is = Files.newInputStream(inputFilePath);
             BufferedWriter htmlWriter = Files.newBufferedWriter(Paths.get(outputPath, xmlFileName + Constants.XHTML_EXT));
             BufferedWriter cssWriter = Files.newBufferedWriter(Paths.get(cssPath, xmlFileName + Constants.CSS_EXT))) {

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            Document doc = factory.newDocumentBuilder().parse(is);

            Log.log("Root Element :" + doc.getDocumentElement().getNodeName());
            Log.log("------");

            StringBuilder html = new StringBuilder();

            // style
            StringBuilder cssInFile = new StringBuilder();

            // Find script tag and get (CDATA) data
            NodeList scriptNodes = doc.getElementsByTagName(Constants.SCRIPT_TAG);
            if (scriptNodes != null && scriptNodes.getLength() > 0) {
                setScriptInline(getCharacterDataFromElement((Element) scriptNodes.item(0)));
            }
            // Find xml Object properties
            NodeList xmlNodes = doc.getElementsByTagName(Constants.XML_TAG);
            if (xmlNodes != null && xmlNodes.getLength() > 0) {
                setXmlObjectInline(getXmlObjectProperties(xmlNodes));
            }

            Map<String, ComponentMap> hmNodeMap = XmlService.getNodeMap();

            Map<String, PropertyMap> hmAttributeMap = XmlService.getAttributeMap();

            HtmlElementParser elementRootParser = new HtmlElementParser("First Node", null, null, null, true);

            // Get mapping config
            if (doc.hasChildNodes()) {
                new ConvertMxmlService(hmNodeMap, hmAttributeMap, xmlFileName, elementReplace)
                        .handleNodeXml(elementRootParser, true, doc.getChildNodes(), baseHtml, html);
            }

            StringBuilder htmlContent = new StringBuilder();
            List<HtmlElementParser> elementList = elementRootParser.getChildList();
            handleElementNode(htmlContent, elementList);
            StringUtils.replaceInStringBuilder(baseHtml, "{form_content}", htmlContent.toString());
            StringUtils.replaceInStringBuilder(baseHtml, "{css_file_name}", xmlFileName);

            handleElementReplace(baseHtml, elementReplace);

            org.jsoup.nodes.Document jsoupDoc = Jsoup.parse(baseHtml.toString(), "UTF-8", Parser.xmlParser());
            JsoupService.handleTagJsoup(jsoupDoc, elementReplace);

            htmlWriter.write(jsoupDoc.toString());
            Log.log("Html file created successfully.");

            cssWriter.write(cssInFile.toString());
            Log.log("CSS file created successfully.");
        } catch (FileNotFoundException e) {
            Log.log("File not found: " + inputFilePath);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void handleElementNode(StringBuilder html, List<HtmlElementParser> elementParserList) {
        for (HtmlElementParser elementParser : elementParserList) {
            printElement(html, elementParser);
            Log.debug(html);
        }

    }

    private void printElement(StringBuilder html, HtmlElementParser elementParser) {
        Log.debug("parent name: " + elementParser.getParentNodeName());
        Log.debug("name: " + elementParser.getNodeName());
        boolean isGenerateHtml = elementParser.isGenerateHtml();

        if (isGenerateHtml) {
            html.append(elementParser.getStartTag());

            StringBuilder cssElement = ConvertMxmlService.createCssElement(elementParser.getCssParsers());
            StringBuilder attributeElement = ConvertMxmlService.createSyntaxAttributeHtml(elementParser);

            html.append(attributeElement);

            if (org.apache.commons.lang3.StringUtils.isNotEmpty(cssElement)) {
                StringBuilder cssElementInline = ConvertMxmlService.createSyntaxCssInline(cssElement);
                html.append(cssElementInline);
            }

            html.append(elementParser.getEndStartTag());

            if (CollectionUtils.isNotEmpty(elementParser.getChildList())) {
                for (HtmlElementParser item : elementParser.getChildList()) {
                    printElement(html, item);
                }
            }
            html.append(elementParser.getEndTag());
        } else {
            if (CollectionUtils.isNotEmpty(elementParser.getChildList())) {
                for (HtmlElementParser item : elementParser.getChildList()) {
                    printElement(html, item);
                }
            }
        }

        Log.debug("cssElement: " + ConvertMxmlService.createCssElement(elementParser.getCssParsers()));
        Log.debug("attributeElement: " + ConvertMxmlService.createSyntaxAttributeHtml(elementParser));
    }

    private static StringBuilder createHtmlBase() {
        StringBuilder baseHtml = new StringBuilder();
        try {
            ClassLoader classLoader = MxmlConvert.class.getClassLoader();
            String baseHtmlFileName = "templates/base_html.html";
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
        if (CollectionUtils.isNotEmpty(elementReplace.getCssCompositionFirstList())) {
            StringBuilder styleFirstComposition = ConvertMxmlService.createSyntaxCssInline(elementReplace.getCssCompositionFirstList());
            StringUtils.replaceInStringBuilder(baseHtml, "{style_composition_first}", styleFirstComposition.toString());
        }
        // Handle radio group
        for (RadioGroupReplace item : elementReplace.getRadioGroupReplaces()) {
            String groupId = item.getGroupId();
            for (String radioItem : item.getSelectItemList()) {
                StringUtils.replaceInStringBuilder(baseHtml, "id=\"" + groupId + "\">", "id=\"" + groupId + "\">" + radioItem);
            }
        }
    }

    /**
     * Get all properties from xml Object
     */
    private Map<String, List<String>> getXmlObjectProperties(NodeList xmlNodes) {
        Map<String, List<String>> xmlObjects = new HashMap<>();
        List<String> properties = null;
        if (xmlNodes != null && xmlNodes.getLength() > 0) {
            for (int i = 0; i < xmlNodes.getLength(); i++) {
                Element element = (Element) xmlNodes.item(i);
                if (element instanceof Text) continue;
                // Id tag
                String id = element.getAttributes().getNamedItem("id").getNodeValue();
                if (element.hasChildNodes()) {
                    // Properties list
                    NodeList nodeClassList = element.getChildNodes();
                    if (nodeClassList.getLength() > 0) {
                        for (int j = 0; j < nodeClassList.getLength(); j++) {
                            Node nodeClass = nodeClassList.item(j);
                            if (nodeClass != null && nodeClass.hasChildNodes()) {
                                properties = new ArrayList<>();
                                NodeList xmlClass = nodeClass.getChildNodes();
                                for (int k = 0; k < xmlClass.getLength(); k++) {
                                    if (xmlClass.item(k) instanceof Text) continue;
                                    properties.add(xmlClass.item(k).getNodeName());
                                }
                            }
                        }
                    }

                }
                //Add xml class to list
                xmlObjects.put(id, properties);
            }
        }
        return xmlObjects;
    }

    /**
     * Get character data (CDATA) from xml document
     */
    private String getCharacterDataFromElement(Element e) {
//        Node child = e.getFirstChild();
//        if (child instanceof CharacterData) {
//            CharacterData cd = (CharacterData) child;
//            return cd.getData();
//        }
//        return "";
        NodeList list = e.getChildNodes();
        String data;

        for (int index = 0; index < list.getLength(); index++) {
            if (list.item(index) instanceof CharacterData) {
                CharacterData child = (CharacterData) list.item(index);
                data = child.getData();

                if (data != null && data.trim().length() > 0)
                    return child.getData();
            }
        }
        return "";
    }

    public String getScriptInline() {
        return scriptInline;
    }

    public void setScriptInline(String scriptInline) {
        this.scriptInline = scriptInline;
    }

    public Map<String, List<String>> getXmlObjectInline() {
        return xmlObjectInline;
    }

    public void setXmlObjectInline(Map<String, List<String>> xmlObjectInline) {
        this.xmlObjectInline = xmlObjectInline;
    }
}

package main;

import constants.Constants;
import mxml.config.XhtmlConfig;
import mxml.dto.modify.AjaxEvent;
import mxml.dto.modify.ElementReplace;
import mxml.dto.parser.HtmlElementParser;
import mxml.enums.TemplatePlaceholder;
import mxml.service.ConvertMxmlService;
import mxml.service.CssService;
import mxml.service.JsoupService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.CharacterData;
import org.w3c.dom.*;
import utils.CommonUtils;
import utils.FileUtils;

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
    private static final Logger logger = LogManager.getLogger(MxmlConvert.class);
    // Script Inline source code
    private String scriptInline;
    // xml properties list
    private Map<String, List<String>> xmlObjectInline;

    /**
     * Convert syntax mxml to syntax xhtml
     *
     * @param inputPath
     * @param outputPath
     */
    public void convert(String inputPath, String outputPath, Map<String, XhtmlConfig> xhtmlConfigMap) {

        String cssPath = Paths.get(outputPath, "css").toString();
        //Create if folder not exist
        FileUtils.createIfNotExistFolder(cssPath);
        Path inputFilePath = Paths.get(inputPath);
        String xmlFileName = FileUtils.getFileNameMxml(inputPath);

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

            logger.info("Root Element :" + doc.getDocumentElement().getNodeName());
            logger.info("------");

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

            HtmlElementParser elementRootParser = new HtmlElementParser(Constants.MXML_ROOT_NODE, null, null, null, null, true, false);

            // Get mapping config
            if (doc.hasChildNodes()) {
                new ConvertMxmlService(xmlFileName, elementReplace)
                        .parser(elementRootParser, doc.getChildNodes(), baseHtml, html, xhtmlConfigMap);
            }

            writeHtml(htmlWriter, xmlFileName, baseHtml, elementRootParser, elementReplace, xhtmlConfigMap);

            writeCss(cssWriter, cssInFile);

        } catch (FileNotFoundException e) {
            logger.info("File not found: " + inputFilePath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void writeHtml(BufferedWriter htmlWriter, String xmlFileName, StringBuilder baseHtml, HtmlElementParser elementRootParser, ElementReplace elementReplace, Map<String, XhtmlConfig> xhtmlConfigMap) throws IOException {
        StringBuilder htmlContent = new StringBuilder();
        List<HtmlElementParser> elementList = elementRootParser.getChildList();

        processGenerateHtmlContent(htmlContent, elementList);

        handleElementReplace(baseHtml, htmlContent, elementReplace, xmlFileName, xhtmlConfigMap);

        String jsoupDocument = JsoupService.createJsoupDocument(baseHtml, elementReplace);

        baseHtml = new StringBuilder(jsoupDocument);

        xmlnsReplace(baseHtml);

        CommonUtils.replaceInStringBuilder(baseHtml, TemplatePlaceholder.FORM_CONTENT.getPlaceholder(), htmlContent.toString());

        htmlWriter.write(baseHtml.toString());

        logger.info("Html file created successfully.");
    }

    private void writeCss(BufferedWriter cssWriter, StringBuilder cssInFile) throws IOException {
        cssWriter.write(cssInFile.toString());

        logger.info("CSS file created successfully.");
    }

    private void processGenerateHtmlContent(StringBuilder html, List<HtmlElementParser> elementParserList) {
        elementParserList.stream().forEach(item -> generateHtmlContent(html, item));
    }

    private void generateHtmlContent(StringBuilder html, HtmlElementParser elementParser) {
        logger.debug("parent name: " + elementParser.getParentNodeName());
        logger.debug("name: " + elementParser.getNodeName());
        boolean isGenerateHtml = elementParser.isGenerateHtml();

        if (isGenerateHtml) {
            html.append(elementParser.getStartTag());

            StringBuilder cssElement = CssService.createCssElement(elementParser.getCssParsers());
            StringBuilder attributeElement = ConvertMxmlService.createSyntaxAttributeHtml(elementParser);

            html.append(attributeElement);

            if (StringUtils.isNotEmpty(cssElement)) {
                StringBuilder cssElementInline = CssService.createSyntaxCssInline(cssElement);
                html.append(cssElementInline);
            }

            html.append(elementParser.getEndStartTag());

            List<HtmlElementParser> childList = elementParser.getChildList();
            if (CollectionUtils.isNotEmpty(childList)) {
                childList.forEach(item -> generateHtmlContent(html, item));
            }

            html.append(elementParser.getEndTag());
        } else {
            List<HtmlElementParser> childList = elementParser.getChildList();
            if (CollectionUtils.isNotEmpty(childList)) {
                childList.forEach(item -> generateHtmlContent(html, item));
            }
        }

        logger.debug("cssElement: " + CssService.createCssElement(elementParser.getCssParsers()));
        logger.debug("attributeElement: " + ConvertMxmlService.createSyntaxAttributeHtml(elementParser));
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

    private static void handleElementReplace(StringBuilder baseHtml, StringBuilder htmlContent, ElementReplace elementReplace, String xmlFileName, Map<String, XhtmlConfig> xhtmlConfigMap) {
        XhtmlConfig xhtmlConfig = xhtmlConfigMap.get(xmlFileName);
        String formGenerateSyntaxStart = String.format("<h:form id=\"%sForm\">", elementReplace.getFormName());
        String formGenerateSyntaxEnd = "</h:form>";

        boolean generateForm = xhtmlConfig.isGenerateForm();
        String formContent = generateForm ? formGenerateSyntaxStart + htmlContent.toString() + formGenerateSyntaxEnd : htmlContent.toString();

        CommonUtils.replaceInStringBuilder(baseHtml, TemplatePlaceholder.FORM_CONTENT.getPlaceholder(), formContent);
        CommonUtils.replaceInStringBuilder(baseHtml, TemplatePlaceholder.CSS_FILE_NAME.getPlaceholder(), xmlFileName);

        String title = elementReplace.getTitle();
        String titleSyntax = StringUtils.isNotEmpty(title) ? String.format("<ui:define name=\"title\">%s</ui:define>", title) : StringUtils.EMPTY;
        CommonUtils.replaceInStringBuilder(baseHtml, TemplatePlaceholder.TITLE_FORM_CONTENT.getPlaceholder(), titleSyntax);

        if (CollectionUtils.isNotEmpty(elementReplace.getCssCompositionFirstList())) {
            StringBuilder styleFirstComposition = CssService.createSyntaxCssInline(elementReplace.getCssCompositionFirstList());
            CommonUtils.replaceInStringBuilder(baseHtml, TemplatePlaceholder.STYLE_COMPOSITION_FIRST.getPlaceholder(), styleFirstComposition.toString());
        }

        // Handle radio group
        elementReplace.getRadioGroupReplaces().forEach(item -> {
            String groupId = item.getGroupId();
            for (int i = 0; i < item.getSelectItemList().size(); i++) {
                if (i == 0 && item.getAjaxEvent() != null) {
                    AjaxEvent ajaxEvent = item.getAjaxEvent();
                    String ajaxSyntax = String.format("<p:ajax event=\"%s\" listener=\"%s\"/>", ajaxEvent.getEvent(), ajaxEvent.getListener());
                    CommonUtils.replaceInStringBuilder(baseHtml, "id=\"" + groupId + "\">", "id=\"" + groupId + "\">" + item.getSelectItemList().get(i) + ajaxSyntax);
                } else {
                    CommonUtils.replaceInStringBuilder(baseHtml, "id=\"" + groupId + "\">", "id=\"" + groupId + "\">" + item.getSelectItemList().get(i));
                }
            }
        });
    }

    private static void xmlnsReplace(StringBuilder baseHtml) {
        CommonUtils.replaceInStringBuilder(baseHtml, "{xmlns_lib_define}=\"\"", "\n    template=\"/jp/co/nissho_ele/acc/common/commonLayout.xhtml\"\n" +
                "    xmlns=\"http://www.w3.org/1999/xhtml\"\n" +
                "    xmlns:f=\"http://xmlns.jcp.org/jsf/core\"\n" +
                "    xmlns:h=\"http://xmlns.jcp.org/jsf/html\"\n" +
                "    xmlns:ui=\"http://xmlns.jcp.org/jsf/facelets\"\n" +
                "    xmlns:pe=\"http://primefaces.org/ui/extensions\"\n" +
                "    xmlns:p=\"http://primefaces.org/ui\"\n" +
                "    xmlns:c=\"http://xmlns.jcp.org/jsp/jstl/core\"\n" +
                "    xmlns:acc=\"http://xmlns.jcp.org/jsf/composite/acc_component\"");
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

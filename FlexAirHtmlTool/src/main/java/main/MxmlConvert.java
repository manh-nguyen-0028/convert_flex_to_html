package main;

import as.types.ASMember;
import constants.Constants;
import constants.ReservedWords;
import constants.Templates;
import mxml.config.MappingHandler;
import mxml.config.XhtmlConfig;
import mxml.dto.modify.AjaxEvent;
import mxml.dto.modify.ElementReplace;
import mxml.dto.parser.HtmlElementParser;
import mxml.enums.MXMLPattern;
import mxml.enums.TemplatePlaceholder;
import mxml.service.ConvertMxmlService;
import mxml.service.CssService;
import mxml.service.JsoupService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
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
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MxmlConvert {
    private static final Logger logger = LogManager.getLogger(MxmlConvert.class);
    // Script Inline source code
    private String scriptInline;
    // Model properties list
    private List<ASMember> modelMembers;
    // xml properties list
    private Map<String, List<String>> xmlObjectInline;
    /**
     * Convert syntax mxml to syntax xhtml
     *
     * @param inputPath
     * @param outputPath
     */
    public void convert(String inputPath, String outputPath, Map<String, XhtmlConfig> xhtmlConfigMap) {
        // all mapping
        MappingHandler dataHandler = MappingHandler.getInstance();
        Map<String, Object> retrievedMap = dataHandler.getDataMap();

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

            logger.info("Root Element : {}", doc.getDocumentElement().getNodeName());
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
            HtmlElementParser elementRootParser = new HtmlElementParser(Constants.MXML_ROOT_NODE);

            // Get mapping config
            if (doc.hasChildNodes()) {
                new ConvertMxmlService(xmlFileName, elementReplace, retrievedMap, xhtmlConfigMap)
                        .parser(elementRootParser, doc.getChildNodes(), baseHtml, html);
            }

            writeHtml(htmlWriter, xmlFileName, baseHtml, elementRootParser, elementReplace, xhtmlConfigMap);

            //Find Model member
            getModelMembers(elementRootParser);

            writeCss(cssWriter, cssInFile);

        } catch (FileNotFoundException e) {
            logger.info("File not found: {}", inputFilePath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getModelMembers(HtmlElementParser currentElement) {
        if (ArrayUtils.contains(Templates.ASInputControls, currentElement.getNodeName())) {
            if (modelMembers == null) {
                modelMembers = new ArrayList<>();
            }
            ASMember member = new ASMember(currentElement.getId(), ReservedWords.STRING);
            modelMembers.add(member);
        }
        List<HtmlElementParser> childList = currentElement.getChildList();
        if (CollectionUtils.isNotEmpty(childList)) {
            childList.stream().forEach(itemFor -> {
                getModelMembers(itemFor);
            });
        }
    }

    private void writeHtml(BufferedWriter htmlWriter, String xmlFileName, StringBuilder baseHtml, HtmlElementParser elementRootParser, ElementReplace elementReplace, Map<String, XhtmlConfig> xhtmlConfigMap) throws IOException {
        StringBuilder htmlContent = new StringBuilder();
        List<HtmlElementParser> elementList = elementRootParser.getChildList();

        processGenerateHtmlContent(htmlContent, elementList);

        baseHtml = handleElementReplace(baseHtml, htmlContent, elementReplace, xmlFileName, xhtmlConfigMap);

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
        logger.debug("parent name: {}", elementParser.getParentNodeName());
        logger.debug("name: {}", elementParser.getNodeName());
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

        logger.debug("cssElement: {}", CssService.createCssElement(elementParser.getCssParsers()));
        logger.debug("attributeElement: {}", ConvertMxmlService.createSyntaxAttributeHtml(elementParser));
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

    private static StringBuilder handleElementReplace(StringBuilder baseHtml, StringBuilder htmlContent, ElementReplace elementReplace, String xmlFileName, Map<String, XhtmlConfig> xhtmlConfigMap) {
        XhtmlConfig xhtmlConfig = xhtmlConfigMap.get(xmlFileName);
        String formGenerateSyntaxStart = String.format("<h:form id=\"%sForm\">", elementReplace.getFormName());
        String formGenerateSyntaxEnd = "</h:form>";

        boolean generateForm = xhtmlConfig.isGenerateForm();
        String formContent;
        String startFirstUiDetail = StringUtils.EMPTY;
        String endFirstUiDetail = StringUtils.EMPTY;
        String classFirstContent = StringUtils.EMPTY;
        if (generateForm) {
            formContent = formGenerateSyntaxStart + htmlContent.toString() + formGenerateSyntaxEnd;
            classFirstContent = "class=\"p-grid p-nogutter p-align-center p-justify-center\"";
            startFirstUiDetail = "<ui:define name=\"content\">";
            endFirstUiDetail = "</ui:define>";
        } else {
            formContent = htmlContent.toString();
        }
        CommonUtils.replaceInStringBuilder(baseHtml, TemplatePlaceholder.FORM_CONTENT.getPlaceholder(), formContent);
        CommonUtils.replaceInStringBuilder(baseHtml, TemplatePlaceholder.CLASS_FIRST_CONTENT.getPlaceholder(), classFirstContent);
        CommonUtils.replaceInStringBuilder(baseHtml, TemplatePlaceholder.START_FIRST_UI_DETAIL.getPlaceholder(), startFirstUiDetail);
        CommonUtils.replaceInStringBuilder(baseHtml, TemplatePlaceholder.END_FIRST_UI_DETAIL.getPlaceholder(), endFirstUiDetail);
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
            List<String> selectItemList = item.getSelectItemList();
            Collections.reverse(selectItemList);
            for (int i = 0; i < selectItemList.size(); i++) {
                String targetSyntax = String.format("id=\"%s\">", groupId);
                if (i == 0 && item.getAjaxEvent() != null) {
                    AjaxEvent ajaxEvent = item.getAjaxEvent();
                    String ajaxSyntax = String.format("<p:ajax event=\"%s\" listener=\"%s\"/>", ajaxEvent.getEvent(), ajaxEvent.getListener());
                    String replaceSyntax = targetSyntax + selectItemList.get(i) + ajaxSyntax;
                    CommonUtils.replaceInStringBuilder(baseHtml, targetSyntax, replaceSyntax);
                } else {
                    CommonUtils.replaceInStringBuilder(baseHtml, targetSyntax, targetSyntax + selectItemList.get(i));
                }
            }


            /*String targetSyntax = String.format("id=\"%s\">", groupId);
            if (i == 0 && item.getAjaxEvent() != null) {
                AjaxEvent ajaxEvent = item.getAjaxEvent();
                String ajaxSyntax = String.format("<p:ajax event=\"%s\" listener=\"%s\"/>", ajaxEvent.getEvent(), ajaxEvent.getListener());
                String replaceSyntax = targetSyntax + item.getSelectItemList().get(i) + ajaxSyntax;
                CommonUtils.replaceInStringBuilder(baseHtml, targetSyntax, replaceSyntax);
            } else {
                CommonUtils.replaceInStringBuilder(baseHtml, targetSyntax, targetSyntax + item.getSelectItemList().get(i));
            }*/
        });

        // handle tool tip
        String modifiedHtml = handleReplaceToolTip(baseHtml);
        return new StringBuilder(modifiedHtml);
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

    public static String handleReplaceToolTip(StringBuilder baseHtml) {
        // Regex pattern to match the specified format
        String pattern = MXMLPattern.TOOL_TIP;

        // Create a pattern object
        Pattern regex = Pattern.compile(pattern);

        // Create a matcher object
        Matcher matcher = regex.matcher(baseHtml);

        // Create a StringBuilder for the modified result
        StringBuilder modifiedHtml = new StringBuilder();

        // Variable to track the end of the last match
        int lastMatchEnd = 0;

        // Find and replace the pattern in the baseHtml
        while (matcher.find()) {
            // Extract values from the matched groups
            String id = matcher.group(2);
            String value = matcher.group(4);
            String showDataTips = matcher.group(6);
            String style = matcher.group(8);
            String endOfPattern = matcher.group(9);

            // Append the content between the last match and the current match to the modified result
            modifiedHtml.append(baseHtml.substring(lastMatchEnd, matcher.start()));

            String syntaxFormat = "<h:outputText " + (StringUtils.isNotEmpty(id) ? Constants.XHTML_ID + "=" + Constants.SYNTAX_DOUBLE_QUOTATION + id + Constants.SYNTAX_DOUBLE_QUOTATION : "")
                    + (StringUtils.isNotEmpty(value) ? Constants.SYNTAX_SPACE + Constants.XHTML_VALUE + "=" + Constants.SYNTAX_DOUBLE_QUOTATION + value + Constants.SYNTAX_DOUBLE_QUOTATION : "")
                    + (StringUtils.isNotEmpty(style) ? Constants.SYNTAX_SPACE + Constants.ATTRIBUTE_STYLE + "=" + Constants.SYNTAX_DOUBLE_QUOTATION + style + Constants.SYNTAX_DOUBLE_QUOTATION : "");

            modifiedHtml.append(syntaxFormat);

            // Close the opening tag
            modifiedHtml.append("></h:outputText>");

            // Add <p:tooltip> if showDataTips was "true"
            if (showDataTips != null && "true".equals(showDataTips)) {
                modifiedHtml.append(String.format("\n<p:tooltip for=\"%s\" value=\"%s\" />", id, value));
            }

            modifiedHtml.append(endOfPattern);

            // Update the last match end position
            lastMatchEnd = matcher.end();
        }

        // Append the remaining content after the last match to the modified result
        modifiedHtml.append(baseHtml.substring(lastMatchEnd));

        return modifiedHtml.toString();
    }

    public String getScriptInline() {
        return scriptInline;
    }

    public void setScriptInline(String scriptInline) {
        this.scriptInline = scriptInline;
    }

    public List<ASMember> getModelMembers() {
        return modelMembers;
    }

    public void setModelMembers(List<ASMember> modelMembers) {
        this.modelMembers = modelMembers;
    }

    public Map<String, List<String>> getXmlObjectInline() {
        return xmlObjectInline;
    }

    public void setXmlObjectInline(Map<String, List<String>> xmlObjectInline) {
        this.xmlObjectInline = xmlObjectInline;
    }
}

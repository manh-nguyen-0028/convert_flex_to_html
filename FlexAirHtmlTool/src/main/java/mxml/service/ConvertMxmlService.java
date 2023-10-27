package mxml.service;

import constants.Constants;
import mxml.dto.mapping.ComponentMap;
import mxml.dto.mapping.TransformerSpecialElement;
import mxml.dto.modify.CheckBoxReplace;
import mxml.dto.modify.ElementReplace;
import mxml.dto.modify.RadioGroupReplace;
import mxml.dto.parser.HtmlElementParser;
import mxml.dto.parser.PropertyParser;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import utils.FileUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ConvertMxmlService {
    private static final Logger logger = LogManager.getLogger(ConvertMxmlService.class);

    private final String xmlFileName;

    private final ElementReplace elementReplace;

    public ConvertMxmlService(String xmlFileName, ElementReplace elementReplace) {
        this.xmlFileName = xmlFileName;
        this.elementReplace = elementReplace;
    }

    /**
     * Element parser
     *
     * @param elementParser
     * @param isFirstCanvas
     * @param nodeList
     * @param baseHtml
     * @param html
     */
    public void parser(HtmlElementParser elementParser, boolean isFirstCanvas, NodeList nodeList, StringBuilder baseHtml, StringBuilder html) {
        Map<String, ComponentMap> hmComponentMap = MappingService.getComponentMap();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node nodeItem = nodeList.item(i);
            String nodeName = nodeItem.getNodeName();
            logger.info("Node Name =" + nodeName + " [OPEN]");
            if (nodeItem.getNodeType() == Node.ELEMENT_NODE) {
                ComponentMap componentMap;
                String regex = getRegexScreenInclude();
                Matcher matcher = getMatchScreenInclude(regex, nodeName);
                if (matcher.matches()) {
                    String fileIncludeName = nodeName.replaceAll(regex, "$2" + ".xhtml");
                    componentMap = new ComponentMap(nodeName, "<ui:include src=\"" + fileIncludeName + "\"", ">", "</ui:include>");
                } else {
                    componentMap = hmComponentMap.get(nodeName);
                }
                if (componentMap != null) {
                    componentParse(elementParser, componentMap, nodeItem, nodeName, baseHtml, isFirstCanvas, html);
                }
            } else if (nodeItem.getNodeType() == Node.COMMENT_NODE && !"First Node".equals(elementParser.getParentNodeName())) {
                HtmlElementParser currentElement = new HtmlElementParser(nodeName, "<!-- ", nodeItem.getNodeValue(), "-->", true);
                elementParser.getChildList().add(currentElement);
            }
        }
    }

    /**
     * component parse
     *
     * @param parentElement
     * @param componentMap
     * @param nodeItem
     * @param currentNodeName
     * @param baseHtml
     * @param isFirstCanvas
     * @param html
     */
    private void componentParse(HtmlElementParser parentElement, ComponentMap componentMap, Node nodeItem, String currentNodeName, StringBuilder baseHtml, boolean isFirstCanvas, StringBuilder html) {
        String parentNodeName = parentElement.getNodeName();
        HtmlElementParser currentElement = new HtmlElementParser(currentNodeName, componentMap.getHtmlTagStart(), componentMap.getHtmlEndStartTag(), componentMap.getHtmlTagEnd(), componentMap.getIsGenerateHtml());
        currentElement.setParentNodeName(parentNodeName);
        // Convert attribute
        new AttributeService(this.xmlFileName).attributeParse(nodeItem, currentElement);
        // change component by property
        changeComponentByProperty(currentElement);
        // handle special component
        handleSpecialComponent(currentElement);
        // append property for child
        appendPropertyFromParent(currentElement, parentElement);

        if (nodeItem.hasChildNodes()) {
            // Recursion if have child nodes
            addPropertyForChild(currentElement);
            parser(currentElement, isFirstCanvas, nodeItem.getChildNodes(), baseHtml, html);
        }
        parentElement.getChildList().add(currentElement);

        logger.info("Node Name =" + currentNodeName + " [CLOSE]");
    }

    /**
     * Append property for child component from parent component
     *
     * @param currentElement
     * @param parentElement
     */
    private void appendPropertyFromParent(HtmlElementParser currentElement, HtmlElementParser parentElement) {
        if (CollectionUtils.isNotEmpty(parentElement.getPropertyForChild())) {
            currentElement.getPropertyParsers().addAll(parentElement.getPropertyForChild());
        }
    }

    /**
     * Add property from parent component to child component
     *
     * @param elementParser
     */
    private void addPropertyForChild(HtmlElementParser elementParser) {
        Map<String, PropertyParser> hmAttribute = elementParser.getMapPropertyParser();
        if (Constants.MXML_MX_DATA_GRID_COLUMN.equals(elementParser.getNodeName())
                || Constants.MXML_MX_HEADER_RENDERER.equals(elementParser.getNodeName())
                || Constants.MXML_MX_COMPONENT.equals(elementParser.getNodeName())) {
            PropertyParser attributeForChild = hmAttribute.get("value");
            elementParser.getPropertyForChild().add(attributeForChild);
        }
    }

    /**
     * Handle hard component, special component
     *
     * @param currentElement
     */
    private void handleSpecialComponent(HtmlElementParser currentElement) {
        // Handle first canvas element
        handleFirstElement(currentElement);

        // Handle special element
        StringBuilder attributeElement = createSyntaxAttributeHtml(currentElement);
        StringBuilder cssElement = CssService.createCssElement(currentElement.getCssParsers());
        StringBuilder elementParser = createHtmlElementParser(currentElement, attributeElement, cssElement);
        elementParser.append(currentElement.getEndTag());
        String nodeName = currentElement.getNodeName();
        if (Constants.MXML_CONTROLS_ACC_RADIO_BUTTON.equals(nodeName)) {
            Optional<PropertyParser> groupNameOptional = currentElement.getPropertyParsers().stream().filter(itemFilter -> "groupName".equals(itemFilter.getKey())).findFirst();
            if (groupNameOptional.isPresent()) {
                String groupId = groupNameOptional.get().getValue();
                Optional<RadioGroupReplace> radioGroupReplaceOptional = elementReplace.getRadioGroupReplaces()
                        .stream()
                        .filter(itemFilter -> groupId.equals(itemFilter.getGroupId())).findFirst();
                if (radioGroupReplaceOptional.isPresent()) {
                    RadioGroupReplace radioGroupReplace = radioGroupReplaceOptional.get();
                    List<String> selectItemList = radioGroupReplace.getSelectItemList();
                    selectItemList.add(elementParser.toString());
                    radioGroupReplace.setSelectItemList(selectItemList);
                } else {
                    RadioGroupReplace radioGroupReplace = new RadioGroupReplace(groupNameOptional.get().getValue());
                    radioGroupReplace.getSelectItemList().add(elementParser.toString());
                    elementReplace.getRadioGroupReplaces().add(radioGroupReplace);
                }
            }
        }

        // Handle element have ajax tag
        handleElementHaveAjax(currentElement);
    }

    /**
     * Change component by property
     * Ex: component Controls:ACCTextInput + property maxByte = component inputTextMaxByte
     *
     * @param htmlElementParser
     */
    private static void changeComponentByProperty(HtmlElementParser htmlElementParser) {
        List<TransformerSpecialElement> specialElementList = MappingService.getListComponentByProperty();
        List<PropertyParser> attributeList = htmlElementParser.getPropertyParsers();
        List<TransformerSpecialElement> specialElements = specialElementList.stream()
                .filter(itemFilter -> htmlElementParser.getNodeName().equals(itemFilter.getElementFrom()))
                .collect(Collectors.toList());
        for (TransformerSpecialElement specialElement : specialElements) {
            Optional<PropertyParser> parserOptional = attributeList.stream()
                    .filter(itemFilter -> specialElement.getAttribute().equals(itemFilter.getKey()))
                    .findFirst();
            if (parserOptional.isPresent()) {
                String value = specialElement.getValue();
                if (StringUtils.isEmpty(value) || (StringUtils.isNotEmpty(value) && parserOptional.get().getValue().equals(value))) {
                    htmlElementParser.setStartTag(specialElement.getHtmlTagStart());
                    htmlElementParser.setEndTag(specialElement.getHtmlTagEnd());
                }
            }
        }
    }

    /**
     * @param elementParser
     * @return syntax attribute in html
     */
    public static StringBuilder createSyntaxAttributeHtml(HtmlElementParser elementParser) {
        StringBuilder attributeBuilder = new StringBuilder();
        // add attribute
        for (PropertyParser attribute : elementParser.getPropertyParsers()) {
            if (attribute.isUse()) {
                attributeBuilder
                        .append(Constants.SYNTAX_SPACE)
                        .append(attribute.getKey())
                        .append(Constants.SYNTAX_EQUAL)
                        .append(Constants.SYNTAX_DOUBLE_QUOTATION)
                        .append(attribute.getValue())
                        .append(Constants.SYNTAX_DOUBLE_QUOTATION);
            }
        }

        return attributeBuilder;
    }

    private StringBuilder createHtmlElementParser(HtmlElementParser elementParser, StringBuilder attributeElement, StringBuilder cssElement) {
        StringBuilder elementBuilder = new StringBuilder(elementParser.getStartTag());
        elementBuilder.append(Constants.SYNTAX_SPACE).append(attributeElement);
        if (StringUtils.isNotEmpty(cssElement)) {
            elementBuilder.append(Constants.SYNTAX_SPACE).append(Constants.ATTRIBUTE_STYLE).append(Constants.SYNTAX_EQUAL).append(Constants.SYNTAX_DOUBLE_QUOTATION)
                    .append(cssElement).append(Constants.SYNTAX_DOUBLE_QUOTATION);
        }
        elementBuilder.append(elementParser.getEndStartTag());
        return elementBuilder;
    }

    /**
     * Handle first element of mxml file
     * Example:<Containers:ACCCanvas
     *
     * @param currentElement
     */
    private void handleFirstElement(HtmlElementParser currentElement) {
        String parentNodeName = currentElement.getParentNodeName();
        String currentNodeName = currentElement.getNodeName();
        if ("First Node".equals(parentNodeName) && (currentNodeName.equals(Constants.MXML_CONTAINERS_ACC_CANVAS) || currentNodeName.equals(Constants.MXML_CONTAINERS_ACC_TITLE_WINDOW))) {
            currentElement.setGenerateHtml(false);
            Optional<PropertyParser> parserOptional = currentElement.getPropertyParsers().stream().filter(item -> "title".equals(item.getKey())).findFirst();
            if (parserOptional.isPresent()) {
                this.elementReplace.setTitle(parserOptional.get().getValue());
            }
            this.elementReplace.setCssCompositionFirstList(currentElement.getCssParsers());
            currentElement.setStartTag("<ui:composition");
            currentElement.setEndTag("</ui:composition");
        }
    }

    /**
     * @param elementParser Handle tag jsf may be have ajax tag
     *                      Example: checkbox, radio,...
     */
    private void handleElementHaveAjax(HtmlElementParser elementParser) {
        PropertyParser propertyParser = elementParser.getMapPropertyParser().get("change");
        if (propertyParser != null) {
            if (Constants.MXML_CONTROLS_ACC_CHECK_BOX.equals(elementParser.getNodeName())) {
                CheckBoxReplace checkBoxReplace = new CheckBoxReplace();
                checkBoxReplace.setId(elementParser.getId());
                checkBoxReplace.setEvent(propertyParser.getKey());
                this.elementReplace.getCheckBoxReplaces().add(checkBoxReplace);
            }
        }
    }

    /**
     * get match screen include with format: start with screen-include-mapping + ':' + filename
     * file name not include suffix _AS
     *
     * @param nodeName
     * @return matcher
     */
    private Matcher getMatchScreenInclude(String regex, String nodeName) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(nodeName);
        return matcher;
    }

    private String getRegexScreenInclude() {
        List<String> screenMappings = FileUtils.readResourcesTxt("screen-include-mapping.txt");
        return "^(" + String.join("|", screenMappings) + "):(.*?)(?<!_AS)$";
    }
}

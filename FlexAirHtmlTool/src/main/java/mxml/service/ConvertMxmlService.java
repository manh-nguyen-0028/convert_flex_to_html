package mxml.service;

import constants.Constants;
import mxml.config.XhtmlConfig;
import mxml.dto.mapping.ComponentMap;
import mxml.dto.mapping.ComponentPropertyMap;
import mxml.dto.mapping.TransformerSpecialElement;
import mxml.dto.modify.AjaxEvent;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
     * @param nodeList
     * @param baseHtml
     * @param html
     */
    public void parser(HtmlElementParser elementParser, NodeList nodeList, StringBuilder baseHtml, StringBuilder html, Map<String, XhtmlConfig> xhtmlConfigMap) {
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
                    String fileIncludeName = nodeName.replaceAll(regex, "$2");
                    XhtmlConfig xhtmlConfig = xhtmlConfigMap.get(fileIncludeName);
                    if (xhtmlConfig != null) {
                        xhtmlConfig.setGenerateForm(false);
                    }
                    String fileIncludeNameXhtml = fileIncludeName + ".xhtml";
                    componentMap = new ComponentMap(nodeName, "ui:include", "<ui:include src=\"" + fileIncludeNameXhtml + "\"", ">", "</ui:include>");
                } else {
                    componentMap = hmComponentMap.get(nodeName);
                }
                if (componentMap != null) {
                    componentParse(elementParser, componentMap, nodeItem, nodeName, baseHtml, html, xhtmlConfigMap);
                }
            } else if (nodeItem.getNodeType() == Node.COMMENT_NODE && !Constants.MXML_ROOT_NODE.equals(elementParser.getParentNodeName())) {
                HtmlElementParser currentElement = new HtmlElementParser(nodeName, "", "<!-- ", nodeItem.getNodeValue(), "-->", true, false);
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
     * @param html
     */
    private void componentParse(HtmlElementParser parentElement, ComponentMap componentMap, Node nodeItem, String currentNodeName, StringBuilder baseHtml, StringBuilder html, Map<String, XhtmlConfig> xhtmlConfigMap) {
        String parentNodeName = parentElement.getNodeName();
        HtmlElementParser currentElement = new HtmlElementParser(currentNodeName, componentMap.getXhtmlTag(),
                componentMap.getXhtmlTagStart(), componentMap.getXhtmlEndStartTag(), componentMap.getXhtmlTagEnd(),
                componentMap.getIsGenerateXHTML(), componentMap.isUseAjaxTag());
        currentElement.setParentNodeName(parentNodeName);
        // Convert attribute
        new AttributeService(this.xmlFileName).attributeParse(nodeItem, currentElement);
        // change component by property
        changeComponentByProperty(currentElement);

        // handle special component
        handleSpecialComponent(currentElement);

        // change property by component
        changePropertyByComponent(currentElement);

        // append property for child
        addParentProperty(currentElement, parentElement);

        if (nodeItem.hasChildNodes()) {
            // Recursion if have child nodes
            addChildProperty(currentElement);
            parser(currentElement, nodeItem.getChildNodes(), baseHtml, html, xhtmlConfigMap);
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
    private void addParentProperty(HtmlElementParser currentElement, HtmlElementParser parentElement) {
        if (CollectionUtils.isNotEmpty(parentElement.getPropertyForChild())) {
            currentElement.getPropertyParsers().addAll(parentElement.getPropertyForChild());
        }
    }

    /**
     * Add property from parent component to child component
     *
     * @param elementParser
     */
    private void addChildProperty(HtmlElementParser elementParser) {
        Map<String, PropertyParser> hmAttribute = elementParser.getMapPropertyParser();
        if (Constants.MXML_MX_DATA_GRID_COLUMN.equals(elementParser.getNodeName())
                || Constants.MXML_MX_HEADER_RENDERER.equals(elementParser.getNodeName())
                || Constants.MXML_MX_COMPONENT.equals(elementParser.getNodeName())) {

            if (hmAttribute.containsKey("value")) {
                PropertyParser attributeForChild = hmAttribute.get("value");

                if (Constants.MXML_MX_COMPONENT.equals(elementParser.getNodeName())) {
                    attributeForChild.setValue("#{record." + attributeForChild.getValue() + "}");
                }

                // remove value in current element after append for child
                elementParser.getPropertyParsers().removeIf(attribute -> attribute == attributeForChild);

                elementParser.getPropertyForChild().add(attributeForChild);
            }
        }
    }

    /**
     * Handle hard component, special component
     *
     * @param currentElement
     */
    private void handleSpecialComponent(HtmlElementParser currentElement) {
        handleFirstElement(currentElement);

        StringBuilder attributeElement = createSyntaxAttributeHtml(currentElement);
        StringBuilder cssElement = CssService.createCssElement(currentElement.getCssParsers());
        StringBuilder elementParser = createHtmlElementParser(currentElement, attributeElement, cssElement);
        elementParser.append(currentElement.getEndTag());

        handleRadioButton(currentElement, elementParser);
        handleRadioButtonGroup(currentElement);
        handleParentNode(currentElement);

        handleElementHaveAjax(currentElement);
    }

    private void handleRadioButton(HtmlElementParser currentElement, StringBuilder elementParser) {
        if (Constants.MXML_CONTROLS_ACC_RADIO_BUTTON.equals(currentElement.getNodeName())) {
            Optional<PropertyParser> groupNameOptional = currentElement.getPropertyParsers().stream().filter(itemFilter -> "groupName".equals(itemFilter.getKey())).findFirst();
            if (groupNameOptional.isPresent()) {
                String groupId = groupNameOptional.get().getValue();
                Optional<RadioGroupReplace> radioGroupReplaceOptional = getRadioGroupReplace(groupId);
                if (radioGroupReplaceOptional.isPresent()) {
                    radioGroupReplaceOptional.get().getSelectItemList().add(elementParser.toString());
                } else {
                    RadioGroupReplace radioGroupReplace = new RadioGroupReplace(groupId);
                    radioGroupReplace.getSelectItemList().add(elementParser.toString());
                    elementReplace.getRadioGroupReplaces().add(radioGroupReplace);
                }
            }
        }
    }

    private void handleRadioButtonGroup(HtmlElementParser currentElement) {
        if (Constants.MXML_CONTROLS_ACC_RADIO_BUTTON_GROUP.equals(currentElement.getNodeName())) {
            Map<String, PropertyParser> propertyParserMap = currentElement.getMapPropertyParser();
            PropertyParser groupIdProperty = propertyParserMap.get("id");
            if (groupIdProperty != null) {
                RadioGroupReplace radioGroupReplace = new RadioGroupReplace(groupIdProperty.getValue());
                PropertyParser ajaxEventProperty = propertyParserMap.get("change");
                if (ajaxEventProperty != null) {
                    AjaxEvent ajaxEvent = new AjaxEvent(ajaxEventProperty.getKey(), ajaxEventProperty.getValue());
                    radioGroupReplace.setAjaxEvent(ajaxEvent);
                }
                elementReplace.getRadioGroupReplaces().add(radioGroupReplace);
            }
        }
    }

    private void handleParentNode(HtmlElementParser currentElement) {
        String parentNodeName = currentElement.getParentNodeName();
        Map<String, String> nodeToXhtmlMap = getNodeToXhtmlMap();

        if (nodeToXhtmlMap.containsKey(parentNodeName)) {
            String xhtmlTag = nodeToXhtmlMap.get(parentNodeName);
            currentElement.setXhtmlTag(xhtmlTag);
            currentElement.setStartTag("<" + xhtmlTag);
            currentElement.setEndTag("</" + xhtmlTag + ">");
        }
    }

    private Optional<RadioGroupReplace> getRadioGroupReplace(String groupId) {
        return elementReplace.getRadioGroupReplaces()
                .stream()
                .filter(itemFilter -> groupId.equals(itemFilter.getGroupId()))
                .findFirst();
    }

    private Map<String, String> getNodeToXhtmlMap() {
        Map<String, String> nodeToXhtmlMap = new HashMap<>();
        nodeToXhtmlMap.put(Constants.MXML_CONTAINERS_ACC_TAB_NAVIGATOR, Constants.XHTML_P_TAB);
        nodeToXhtmlMap.put(Constants.MXML_MX_COMPONENT, Constants.XHTML_H_OUTPUT_TEXT);
        // Add other mappings as needed
        return nodeToXhtmlMap;
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

        specialElementList.stream()
                .filter(itemFilter -> htmlElementParser.getNodeName().equals(itemFilter.getElementFrom()))
                .filter(specialElement ->
                        attributeList.stream()
                                .anyMatch(parser -> specialElement.getAttribute().equals(parser.getKey())
                                        && (StringUtils.isEmpty(specialElement.getValue())
                                        || parser.getValue().equals(specialElement.getValue()))
                                )
                )
                .findFirst()
                .ifPresent(specialElement -> {
                    htmlElementParser.setXhtmlTag(specialElement.getElementTo());
                    htmlElementParser.setStartTag(specialElement.getHtmlTagStart());
                    htmlElementParser.setEndTag(specialElement.getHtmlTagEnd());
                    htmlElementParser.setUseAjaxTag(specialElement.isUseAjaxTag());
                });
    }

    private static void changePropertyByComponent(HtmlElementParser htmlElementParser) {
        List<ComponentPropertyMap> propertyByComponentList = MappingService.getPropertyByComponent();
        List<PropertyParser> propertyParserList = htmlElementParser.getPropertyParsers();

        if (CollectionUtils.isNotEmpty(propertyParserList)) {
            String xhtmlTag = htmlElementParser.getXhtmlTag();

            propertyParserList.stream().forEach(propertyParser -> {
                Optional<ComponentPropertyMap> componentPropertyMapOptional = propertyByComponentList.stream()
                        .filter(itemFilter -> itemFilter.getComponentName().equals(xhtmlTag) && itemFilter.getPropertyFrom().equals(propertyParser.getKey()))
                        .findFirst();

                if (componentPropertyMapOptional.isPresent()) {
                    ComponentPropertyMap propertyMap = componentPropertyMapOptional.get();
                    propertyParser.setKey(propertyMap.getPropertyTo());
                }
            });
        }
    }

    /**
     * @param elementParser
     * @return syntax attribute in html
     */
    public static StringBuilder createSyntaxAttributeHtml(HtmlElementParser elementParser) {
        StringBuilder attributeBuilder = new StringBuilder();
        // add attribute
        elementParser.getPropertyParsers().stream().forEach(attribute -> {
            if (attribute.isUse()) {
                attributeBuilder
                        .append(Constants.SYNTAX_SPACE)
                        .append(attribute.getKey())
                        .append(Constants.SYNTAX_EQUAL)
                        .append(Constants.SYNTAX_DOUBLE_QUOTATION)
                        .append(attribute.getValue())
                        .append(Constants.SYNTAX_DOUBLE_QUOTATION);
            }
        });
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
        if (Constants.MXML_ROOT_NODE.equals(parentNodeName) && (currentNodeName.equals(Constants.MXML_CONTAINERS_ACC_CANVAS) || currentNodeName.equals(Constants.MXML_CONTAINERS_ACC_TITLE_WINDOW))) {
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
        List<PropertyParser> propertyParserList = elementParser.getPropertyJsParser();
        if (elementParser.isUseAjaxTag() && CollectionUtils.isNotEmpty(propertyParserList)) {
            PropertyParser propertyParser = propertyParserList.get(0);
            CheckBoxReplace checkBoxReplace = new CheckBoxReplace(elementParser.getId(), propertyParser.getKey());
            this.elementReplace.getCheckBoxReplaces().add(checkBoxReplace);
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

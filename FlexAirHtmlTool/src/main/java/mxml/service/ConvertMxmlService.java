package mxml.service;

import constants.Constants;
import mxml.config.XhtmlConfig;
import mxml.dto.mapping.ComponentMap;
import mxml.dto.mapping.ComponentPropertyMap;
import mxml.dto.mapping.TransformerSpecialElement;
import mxml.dto.modify.AjaxEvent;
import mxml.dto.modify.ElementReplace;
import mxml.dto.modify.RadioGroupReplace;
import mxml.dto.parser.CssParser;
import mxml.dto.parser.HtmlElementParser;
import mxml.dto.parser.PropertyParser;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import utils.CommonUtils;
import utils.FileUtils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConvertMxmlService {
    private static final Logger logger = LogManager.getLogger(ConvertMxmlService.class);

    private final String xmlFileName;

    private final ElementReplace elementReplace;

    private final Map<String, XhtmlConfig> xhtmlConfigMap;

    private final Map<String, Object> retrievedMap;

    public ConvertMxmlService(String xmlFileName, ElementReplace elementReplace, Map<String, Object> retrievedMap, Map<String, XhtmlConfig> xhtmlConfigMap) {
        this.xmlFileName = xmlFileName;
        this.retrievedMap = retrievedMap;
        this.elementReplace = elementReplace;
        this.xhtmlConfigMap = xhtmlConfigMap;
    }

    /**
     * Element parser
     *
     * @param elementParser
     * @param nodeList
     * @param baseHtml
     * @param html
     */
    public void parser(HtmlElementParser elementParser, NodeList nodeList, StringBuilder baseHtml, StringBuilder html) throws Exception {
        Map<String, ComponentMap> hmComponentMap = (Map<String, ComponentMap>) retrievedMap.get("componentMap");
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node nodeItem = nodeList.item(i);
            String nodeName = nodeItem.getNodeName();
            logger.info("Node Name ={} [OPEN]", nodeName);
            if (nodeItem.getNodeType() == Node.ELEMENT_NODE) {
                handleElementNode(nodeItem, nodeName, elementParser, baseHtml, html, hmComponentMap);
            } else if (nodeItem.getNodeType() == Node.COMMENT_NODE && !Constants.MXML_ROOT_NODE.equals(elementParser.getParentNodeName())) {
                handleCommentNode(nodeItem, nodeName, elementParser);
            }
        }
    }

    /**
     * Handle element node
     *
     * @param nodeItem
     * @param nodeName
     * @param elementParser
     * @param baseHtml
     * @param html
     * @param hmComponentMap
     * @throws Exception
     */
    private void handleElementNode(Node nodeItem, String nodeName, HtmlElementParser elementParser, StringBuilder baseHtml, StringBuilder html, Map<String, ComponentMap> hmComponentMap) throws Exception {
        ComponentMap componentMap;
        String regex = getRegexScreenInclude();
        Matcher matcher = getMatchScreenInclude(regex, nodeName);

        if (matcher.matches()) {
            String fileIncludeName = nodeName.replaceAll(regex, "$2");
            XhtmlConfig xhtmlConfig = xhtmlConfigMap.get(fileIncludeName);

            if (xhtmlConfig != null) {
                xhtmlConfig.setGenerateForm(false);
                Map<String, CssParser> cssParserMap = elementParser.getMapCssParser();
                String cssWith = cssParserMap.get(Constants.CSS_WIDTH) != null ? cssParserMap.get(Constants.CSS_WIDTH).getValue() : StringUtils.EMPTY;
                String cssHeight = cssParserMap.get(Constants.CSS_HEIGHT) != null ? cssParserMap.get(Constants.CSS_HEIGHT).getValue() : StringUtils.EMPTY;
                xhtmlConfig.setCssHeight(cssHeight);
                xhtmlConfig.setCssWith(cssWith);
            }

            String fileIncludeNameXhtml = fileIncludeName + Constants.XHTML_EXT;
            componentMap = new ComponentMap(nodeName, Constants.XHTML_UI_INCLUDE, String.format("<ui:include src=\"%s\"", fileIncludeNameXhtml), ">", "</ui:include>");
        } else {
            componentMap = hmComponentMap.get(nodeName);
        }

        if (componentMap != null) {
            componentParse(elementParser, componentMap, nodeItem, nodeName, baseHtml, html);
        }
    }

    /**
     * handle comment element node
     *
     * @param nodeItem
     * @param nodeName
     * @param elementParser
     */
    private void handleCommentNode(Node nodeItem, String nodeName, HtmlElementParser elementParser) {
        HtmlElementParser element = new HtmlElementParser(nodeName, "", "<!-- ", nodeItem.getNodeValue(), "-->", true, false);
        elementParser.getChildList().add(element);
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
    private void componentParse(HtmlElementParser parentElement, ComponentMap componentMap, Node nodeItem, String currentNodeName, StringBuilder baseHtml, StringBuilder html) throws Exception {
        String parentNodeName = parentElement.getNodeName();
        HtmlElementParser element = new HtmlElementParser(currentNodeName, componentMap.getXhtmlTag(),
                componentMap.getXhtmlTagStart(), componentMap.getXhtmlEndStartTag(), componentMap.getXhtmlTagEnd(),
                componentMap.getIsGenerateXHTML(), componentMap.isUseAjaxTag());
        element.setParentNodeName(parentNodeName);
        element.setParentNode(parentElement);
        // Convert attribute
        new AttributeService(this.xmlFileName, this.retrievedMap).attributeParse(nodeItem, element);
        // change component by property
        changeComponentByProperty(element);

        // change property by component
        changePropertyByComponent(element);

        // handle special component
        handleSpecialComponent(element);

        // handle special property
        handleSpecialProperty(element);

        // append property for child
        addParentProperty(element, parentElement);

        handleElementHaveAjax(element);

        if (nodeItem.hasChildNodes()) {
            // Recursion if have child nodes
            addChildProperty(element);
            parser(element, nodeItem.getChildNodes(), baseHtml, html);
        }

        parentElement.getChildList().add(element);

        logger.info("Node Name ={} [CLOSE]", currentNodeName);
    }

    private void handleSpecialProperty(HtmlElementParser element) {
        PropertyParser enabled = element.getMapPropertyParser().get(Constants.XHTML_ENABLED);
        if (enabled != null) {
            enabled.setKey(Constants.XHTML_DISABLED);
            enabled.setValue(Constants.STRING_FALSE.equals(enabled.getValue()) ? Constants.STRING_TRUE : Constants.STRING_FALSE);
        }

        PropertyParser scrollable = element.getMapPropertyParser().get("scrollable");
        if (scrollable != null) {
            scrollable.setValue(Constants.STRING_ON.equals(scrollable.getValue()) ? Constants.STRING_TRUE : Constants.STRING_FALSE);
        }

        PropertyParser allowMultipleSelection = element.getMapPropertyParser().get("allowMultipleSelection");
        if (allowMultipleSelection != null) {
            allowMultipleSelection.setKey("selectionMode");
            allowMultipleSelection.setValue(Constants.STRING_TRUE.equals(allowMultipleSelection.getValue()) ? "multiple" : Constants.STRING_FALSE);
        }

    }

    /**
     * Append property for child component from parent component
     *
     * @param element
     * @param parentElement
     */
    private void addParentProperty(HtmlElementParser element, HtmlElementParser parentElement) {
        List<PropertyParser> propertyForChild = parentElement.getPropertyForChild();
        if (CollectionUtils.isNotEmpty(propertyForChild)) {
            element.getPropertyParsers().addAll(parentElement.getPropertyForChild());
        }
    }

    /**
     * Add property from parent component to child component
     *
     * @param elementParser
     */
    private void addChildProperty(HtmlElementParser elementParser) throws Exception {
        List<String> controlDataGrids = Arrays.asList(Constants.MXML_MX_DATA_GRID_COLUMN, Constants.MXML_MX_HEADER_RENDERER, Constants.MXML_MX_COMPONENT);

        Map<String, PropertyParser> hmAttribute = elementParser.getMapPropertyParser();

        // handle data grid control
        if (controlDataGrids.contains(elementParser.getNodeName()) && hmAttribute.containsKey(Constants.XHTML_VALUE)) {
            PropertyParser valueProperty = hmAttribute.get(Constants.XHTML_VALUE);
            PropertyParser idProperty = hmAttribute.get(Constants.XHTML_ID);
            PropertyParser labelFunctionProperty = hmAttribute.get(Constants.XHTML_CONVERTER);
            PropertyParser showDataTipProperty = hmAttribute.get(Constants.XHTML_SHOW_DATA_TIPS);
            PropertyParser idPropertyForChild = new PropertyParser();

            // If showDataTipProperty = true => tags have tooltip => Function Begin
            // Generate If column have id => <h:outputText + id + sub fix Id + <p:tooltip for="updTimesId" (id of output text)
            // Else => <h:outputText + id (id = dataField) + <p:tooltip for="updTimes" (id of output text)
            // => Function End

            // Else If showDataTipProperty = false
            // Only genarete value output text Example <h:outputText value = "#{record.bunno}"

            if (idProperty == null) {
                idProperty = new PropertyParser(Constants.XHTML_ID, valueProperty.getValue(), valueProperty.getType());
            }

            BeanUtils.copyProperties(idPropertyForChild, idProperty);

            List<PropertyParser> propertyForChild = new ArrayList<>();
            if (Constants.XHTML_P_COLUMN.equals(elementParser.getXhtmlTag())) {
                String valueSyntax = String.format("#{record.%s}", valueProperty.getValue());
                valueProperty.setValue(valueSyntax);
                if (idProperty != null && showDataTipProperty != null) {
                    String idForChild = idProperty.getValue() + "Id";
                    idPropertyForChild.setValue(idForChild);
                } else if (idProperty != null) {
                    idPropertyForChild = null;
                }
            }

            if (idPropertyForChild != null && StringUtils.isNotEmpty(idPropertyForChild.getValue())) {
                propertyForChild.add(idPropertyForChild);
            }

            propertyForChild.add(valueProperty);

            if (showDataTipProperty != null) {
                propertyForChild.add(showDataTipProperty);
            }

            if (labelFunctionProperty != null) {
                propertyForChild.add(labelFunctionProperty);
            }

            // remove value in current element after append for child
            elementParser.getPropertyParsers().removeIf(attribute -> attribute == valueProperty);
            elementParser.getPropertyParsers().removeIf(attribute -> attribute == showDataTipProperty);
            elementParser.getPropertyParsers().removeIf(attribute -> attribute == labelFunctionProperty);
            PropertyParser finalIdProperty = idProperty;
            elementParser.getPropertyParsers().removeIf(attribute -> attribute == finalIdProperty);
            elementParser.getPropertyForChild().addAll(propertyForChild);
        }
    }

    /**
     * Handle hard component, special component
     *
     * @param element
     */
    private void handleSpecialComponent(HtmlElementParser element) {
        handleFirstElement(element);

        StringBuilder attributeElement = createSyntaxAttributeHtml(element);
        StringBuilder cssElement = CssService.createCssElement(element.getCssParsers());
        StringBuilder elementParser = createHtmlElementParser(element, attributeElement, cssElement);
        elementParser.append(element.getEndTag());

        handleRadioButton(element, elementParser);
        handleRadioButtonGroup(element);
        handleSpecialParentNode(element);
        handleInputText(element);
//        handleAccButton(element);
    }

 /*   private void handleAccButton(HtmlElementParser element) {
        if (Constants.MXML_CONTAINERS_ACC_BUTTON.equals(element.getNodeName())) {
            PropertyParser enabled = element.getMapPropertyParser().get(Constants.XHTML_ENABLED);
            if (enabled != null) {
                enabled.setKey(Constants.XHTML_DISABLED);
                enabled.setValue(Constants.STRING_FALSE.equals(enabled.getValue()) ? Constants.STRING_TRUE : Constants.STRING_FALSE);
            }
        }
    }*/

    private void handleInputText(HtmlElementParser element) {
        List<String> inputTexts = Arrays.asList(Constants.MXML_CONTAINERS_ACC_TEXT_INPUT,
                Constants.MXML_CONTAINERS_ACC_TEXT_INPUT_MASK_NUMBER,
                Constants.MXML_CONTAINERS_ACC_TEXT_INPUT_MASK_ZIP,
                Constants.MXML_CONTAINERS_ACC_DATE_FIELD,
                Constants.MXML_CONTAINERS_ACC_TEXT_INPUT_MASK_DATE,
                Constants.MXML_CONTROLS_ACC_CHECK_BOX,
                Constants.MXML_CONTAINERS_ACC_TEXT_INPUT_MASK_NUMBER_ONLY);

        if (inputTexts.contains(element.getNodeName()) && StringUtils.isNotEmpty(element.getId())) {
            String id = element.getId();
            String syntaxValue = String.format("#{%s.view.%s}", CommonUtils.getFormController(xmlFileName), id);
            PropertyParser value = new PropertyParser(Constants.XHTML_VALUE, syntaxValue, Constants.XHTML_ATTRIBUTE_PROPERTY);
            element.getPropertyParsers().add(value);
        }
    }

    /**
     * Handle radio button
     *
     * @param element
     * @param elementParser
     */
    private void handleRadioButton(HtmlElementParser element, StringBuilder elementParser) {
        if (Constants.MXML_CONTROLS_ACC_RADIO_BUTTON.equals(element.getNodeName())) {
            PropertyParser groupName = element.getMapPropertyParser().get(Constants.XHTML_ATTRIBUTE_GROUP_NAME);
            List<PropertyParser> ajaxEventProperty = element.getPropertyJsParser();
            if (groupName != null) {
                String groupNameValue = groupName.getValue();
                Optional<RadioGroupReplace> radioGroupReplaceOptional = getRadioGroupReplace(groupNameValue);
                if (radioGroupReplaceOptional.isPresent()) {
                    RadioGroupReplace radioGroupReplace = radioGroupReplaceOptional.get();
                    radioGroupReplace.getSelectItemList().add(elementParser.toString());
                    if (CollectionUtils.isNotEmpty(ajaxEventProperty)) {
                        PropertyParser ajaxItem = ajaxEventProperty.get(0);
                        AjaxEvent ajaxEvent = new AjaxEvent(ajaxItem.getKey(), ajaxItem.getValue());
                        radioGroupReplace.setAjaxEvent(ajaxEvent);
                    }
                } else {
                    RadioGroupReplace radioGroupReplace = new RadioGroupReplace(groupNameValue);
                    radioGroupReplace.getSelectItemList().add(elementParser.toString());
                    if (CollectionUtils.isNotEmpty(ajaxEventProperty)) {
                        PropertyParser ajaxItem = ajaxEventProperty.get(0);
                        AjaxEvent ajaxEvent = new AjaxEvent(ajaxItem.getKey(), ajaxItem.getValue());
                        radioGroupReplace.setAjaxEvent(ajaxEvent);
                    }
                    elementReplace.getRadioGroupReplaces().add(radioGroupReplace);
                }

                // final, remove property group name
                element.getPropertyParsers().removeIf(property -> property == groupName);
            }
        }
    }

    /**
     * Handle radio button group
     *
     * @param element
     */
    private void handleRadioButtonGroup(HtmlElementParser element) {
        if (Constants.MXML_CONTROLS_ACC_RADIO_BUTTON_GROUP.equals(element.getNodeName())) {
            Map<String, PropertyParser> propertyParserMap = element.getMapPropertyParser();
            PropertyParser groupIdProperty = propertyParserMap.get(Constants.XHTML_ID);
            if (groupIdProperty != null) {
                RadioGroupReplace radioGroupReplace = new RadioGroupReplace(groupIdProperty.getValue());
                PropertyParser ajaxEventProperty = propertyParserMap.get(Constants.JS_EVENT_CHANGE);
                if (ajaxEventProperty != null) {
                    AjaxEvent ajaxEvent = new AjaxEvent(ajaxEventProperty.getKey(), ajaxEventProperty.getValue());
                    radioGroupReplace.setAjaxEvent(ajaxEvent);
                }
                elementReplace.getRadioGroupReplaces().add(radioGroupReplace);
            }
        }
    }

    /**
     * handle special parent node
     *
     * @param element
     */
    private void handleSpecialParentNode(HtmlElementParser element) {
        String parentNodeName = element.getParentNodeName();
        Map<String, String> specialComponentMap = createSpecialComponentMap();

        if (specialComponentMap.containsKey(parentNodeName)) {
            String xhtmlTag = specialComponentMap.get(parentNodeName);
            String currentXHtmlTag = element.getXhtmlTag();
            String syntaxInclude = StringUtils.EMPTY;
            if (Constants.XHTML_UI_INCLUDE.equals(currentXHtmlTag)) {
                syntaxInclude = element.getStartTag() + element.getEndStartTag() + element.getEndTag();
            }
            element.setXhtmlTag(xhtmlTag);
            element.setStartTag("<" + xhtmlTag);
            element.setEndTag(syntaxInclude + "</" + xhtmlTag + ">");
        }
    }

    /**
     * Get radio group replace
     *
     * @param groupId
     * @return
     */
    private Optional<RadioGroupReplace> getRadioGroupReplace(String groupId) {
        return elementReplace.getRadioGroupReplaces()
                .stream()
                .filter(itemFilter -> groupId.equals(itemFilter.getGroupId()))
                .findFirst();
    }

    /**
     * Create special component map
     * Ex: mxml tag Containers:ACCTabNavigator will change to xhtml p:tab
     *
     * @return map special component
     */
    private Map<String, String> createSpecialComponentMap() {
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
    private void changeComponentByProperty(HtmlElementParser htmlElementParser) {
        List<TransformerSpecialElement> specialElementList = (List<TransformerSpecialElement>) retrievedMap.get("componentByPropertyList");
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

    /**
     * This function will change property of htmlElementParser by component
     * Example: component p:commandButton + property onclick => change property onclick = actionListener
     *
     * @param htmlElementParser
     */
    private void changePropertyByComponent(HtmlElementParser htmlElementParser) {
        List<ComponentPropertyMap> propertyByComponentList = (List<ComponentPropertyMap>) retrievedMap.get("propertyByComponentList");
        List<PropertyParser> propertyParserList = htmlElementParser.getPropertyParsers();

        if (CollectionUtils.isNotEmpty(propertyParserList)) {
            String xhtmlTag = htmlElementParser.getXhtmlTag();

            propertyParserList.stream()
                    .forEach(propertyParser -> propertyByComponentList.stream()
                            .filter(itemFilter -> itemFilter.getComponentName().equals(xhtmlTag) && itemFilter.getPropertyFrom().equals(propertyParser.getKey()))
                            .findFirst()
                            .ifPresent(item -> propertyParser.setKey(item.getPropertyTo())));
        }
    }

    /**
     * @param elementParser
     * @return syntax attribute in html
     */
    public static StringBuilder createSyntaxAttributeHtml(HtmlElementParser elementParser) {
        StringBuilder attributeBuilder = new StringBuilder();
        // add attribute
        elementParser.getPropertyParsers().stream()
                .forEach(attribute -> {
                    if (attribute.isGenerateHtml()) {
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

    /**
     * @param elementParser
     * @param attributeElement
     * @param cssElement
     * @return syntax html element
     */
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
     * First element is element have parent node = MXML_ROOT_NODE
     * Example:<Containers:ACCCanvas
     *
     * @param element
     */
    private void handleFirstElement(HtmlElementParser element) {
        String parentNodeName = element.getParentNodeName();
        String currentNodeName = element.getNodeName();
        List<String> mxmlFirstTags = Arrays.asList(Constants.MXML_CONTAINERS_ACC_CANVAS, Constants.MXML_CONTAINERS_ACC_TITLE_WINDOW);
        if (Constants.MXML_ROOT_NODE.equals(parentNodeName) && mxmlFirstTags.contains(currentNodeName)) {
            element.setGenerateHtml(false);
            element.getPropertyParsers().stream()
                    .filter(item -> "title".equals(item.getKey()))
                    .findFirst()
                    .ifPresent(property -> this.elementReplace.setTitle(property.getValue()));
            this.elementReplace.setCssCompositionFirstList(element.getCssParsers());
            element.setStartTag("<ui:composition");
            element.setEndTag("</ui:composition");
        }
    }

    /**
     * Jsf handler tag can have ajax tag
     * Identify possible ajax tags via config attribute with isUseAjaxTag = true
     * Example: checkbox, radio,...
     *
     * @param elementParser
     */
    private void handleElementHaveAjax(HtmlElementParser elementParser) {
        List<PropertyParser> propertyJsParser = elementParser.getPropertyJsParser();
        if (elementParser.isUseAjaxTag() && CollectionUtils.isNotEmpty(propertyJsParser)) {
            PropertyParser propertyParser = propertyJsParser.get(0);
            AjaxEvent ajaxEvent = new AjaxEvent(elementParser.getId(), elementParser.getNodeName(), propertyParser.getKey(), propertyParser.getValue());
            this.elementReplace.getAjaxEventReplaces().add(ajaxEvent);
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
        return pattern.matcher(nodeName);
    }

    /**
     * @return regex screen had include
     */
    private String getRegexScreenInclude() {
        List<String> screenMappings = FileUtils.readResourcesTxt("screen-include-mapping.txt");
        return "^(" + String.join("|", screenMappings) + "):(.*?)(?<!_AS)$";
    }
}

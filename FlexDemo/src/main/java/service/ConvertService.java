package service;

import dto.AttributeTag;
import dto.HtmlAttributeTag;
import dto.MxmlTag;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.IOException;
import java.util.*;

public class ConvertService {
    public static HashMap<String, AttributeTag> hmAttributeTag;

    public static HashMap<String, MxmlTag> hmXmlTag;

    private static String ATT_CSS = "css";
    private static String ATT_JS = "js";
    private static String ATT_TEXT = "text";
    private static String ATT_OTHER = "other";
    private static String ATT_ATTRIBUTE = "attribute";

    public static String initHtml(String title, String cssName) {
        String htmlStart = "<html><head>" + "<meta charset=\"UTF-8\">" +
                "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">" +
                "<title>{p_title}</title>" +
                "<link rel=\"stylesheet\" href=\"css\\common.css\">" +
                "<link rel=\"stylesheet\" href=\"css\\control.css\">" +
                "<link rel=\"stylesheet\" href=\"css\\{p_css}.css\">" +
                "</head><body id=\"" + cssName + "_body\" >" ;
        htmlStart = htmlStart.replace("{p_css}", cssName).replace("{p_title}", title);

//        cssName = "kkk";
        return htmlStart;
    }

    public void printNote(NodeList nodeList, StringBuilder html, String ownerNode, StringBuilder cssFileAdd) throws IOException {
        hmAttributeTag = XmlService.getAttributeConfig();

        hmXmlTag = XmlService.getMxmlConfig();

        HtmlAttributeTag htmlAttTag = new HtmlAttributeTag();
        List<Double> fillAlphas = new ArrayList<>();
        List<String> fillColors = new ArrayList<>();

        boolean firstCanvas = true;
        for (int count = 0; count < nodeList.getLength(); count++) {

            Node tempNode = nodeList.item(count);
            if (tempNode.getNodeType() == Node.ELEMENT_NODE) {
                MxmlTag mxmlTagConfig = hmXmlTag.get(tempNode.getNodeName());
                if (mxmlTagConfig != null) {
                    String htmlTagStart = mxmlTagConfig.getHtmlTagStart();
                    String htmlTagEnd = mxmlTagConfig.getHtmlTagEnd();
                    String className = mxmlTagConfig.getClassName();


                    if (tempNode.hasAttributes()) {
                        // get attributes names and values
                        NamedNodeMap nodeAttMap = tempNode.getAttributes();
                        for (int i = 0; i < nodeAttMap.getLength(); i++) {
                            Node nodeAtt = nodeAttMap.item(i);
                            String nodeName = nodeAtt.getNodeName();
                            String nodeValue = nodeAtt.getNodeValue();
                            AttributeTag attributeTag = hmAttributeTag.get(nodeName);
                            if (attributeTag != null) {
                                handleAttributeTag(htmlAttTag, attributeTag, nodeAtt, tempNode, fillAlphas, fillColors);
                            }
                            System.out.println("attr name : " + nodeName);
                            System.out.println("attr value : " + nodeValue);
                        }

                        if (StringUtils.isEmpty(htmlAttTag.getId())) {
                            htmlAttTag.setId("id_" + utils.StringUtils.generateRandomText(3));
                        }
                    }

                    if (fillColors.size() > 0) {
                        StringBuilder styleValue = new StringBuilder("linear-gradient(");
                        for (int i = 0; i < fillColors.size(); i++) {
                            double alpha = fillAlphas.get(i);
                            int percentageValue = (int) alpha * 100;
                            styleValue.append(fillColors.get(i)).append(" ").append(percentageValue).append("%, ");
                        }
                        styleValue.substring(0,styleValue.length() - 2);
                        styleValue.append(")");
                        htmlAttTag.getStyles().put("background",styleValue.toString());
                    }
                    /*try {
                        if (mxmlTagConfig != null && mxmlTagConfig.getHadAttribute() && mxmlTagConfig != null && attributeText.length() > 0) {
                            htmlNeedAdd = htmlNeedAdd + attributeText;
//                            html.append(attributeText + "");
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }*/
                    // style to file
//                    StringBuilder cssFileAdd = new StringBuilder();
                    StringBuilder cssInHtmlAdd = new StringBuilder();
                    StringBuilder htmlTextAdd = new StringBuilder();
                    StringBuilder cssElement = new StringBuilder();

                    HashMap<String, String> cssMap = htmlAttTag.getStyles();
                    // Sử dụng Iterator để duyệt qua các phần tử
                    Iterator<Map.Entry<String, String>> iterator = cssMap.entrySet().iterator();
                    while (iterator.hasNext()) {
                        Map.Entry<String, String> entry = iterator.next();
                        String key = entry.getKey();
                        String value = entry.getValue();
                        System.out.println("Key: " + key + ", Value: " + value);
                        cssElement.append(key).append(": ").append(value).append(";");
                    }

                    if ("Containers:ACCHBox".equals(ownerNode) || "Containers:ACCVBox".equals(ownerNode)) {
                        String strNeedReplace = "position: absolute;";
                        int startIndex = cssElement.indexOf(strNeedReplace);
                        int lengthOfStr = strNeedReplace.length();
                        if (startIndex >= 0) {
                            cssElement = cssElement.replace(startIndex, startIndex + lengthOfStr, "");
                        }
                    }

                    StringBuilder cssNeedAddFile = new StringBuilder();
                    if (!CollectionUtils.sizeIsEmpty(htmlAttTag.getStyles())) {
                        if (StringUtils.isNotEmpty(htmlAttTag.getId())) {
                            cssNeedAddFile.append("#").append(htmlAttTag.getId()).append(" {").append(cssElement).append("} \n");
                            if ("Containers:ACCCanvas".equals(tempNode.getNodeName()) && firstCanvas) {
                                cssNeedAddFile.append("body {").append(cssElement).append("} \n");
                                className = className + " acc-title-window";
                                firstCanvas = false;
                            }
                        } else {
                            cssInHtmlAdd = cssInHtmlAdd.append("style ='").append(cssElement).append("'");
                        }
                    }

                    htmlTagStart = htmlTagStart.replace("{1}", className);
                    String htmlNeedAdd = htmlTagStart;

                    if (mxmlTagConfig.getStyleInFile()) {
                        // remove position: absolute;
                        cssFileAdd.append(cssNeedAddFile);
                    } else {
                        htmlNeedAdd = htmlNeedAdd.replace("{style}", "style ='" + cssElement + "'");
//                        cssFileAdd = new StringBuilder();
                    }

                    if(StringUtils.isNotEmpty(htmlAttTag.getId())) {
                        htmlNeedAdd = htmlNeedAdd + "id='" + htmlAttTag.getId() +"'";
                    }
                    html.append(htmlNeedAdd);
                    mxmlTagConfig.setHtmlTagStart3(htmlAttTag.getText());
                    html.append(mxmlTagConfig.getHtmlTagStart2());
                    if (StringUtils.isNotEmpty(mxmlTagConfig.getHtmlTagStart3())) {
                        html.append(mxmlTagConfig.getHtmlTagStart3());
                    }
                    htmlAttTag = new HtmlAttributeTag();
                    fillAlphas = new ArrayList<>();
                    fillColors = new ArrayList<>();
                    if (tempNode.hasChildNodes()) {
                        // loop again if has child nodes
                        printNote(tempNode.getChildNodes(), html, tempNode.getNodeName(), cssFileAdd);
                    }

                    html.append(mxmlTagConfig.getHtmlTagEnd());
                    System.out.println("Node Name =" + tempNode.getNodeName() + " [CLOSE]");

                }
            }
        }
    }

    public void handleAttributeTag(HtmlAttributeTag htmlAttTag, AttributeTag attributeTag, Node nodeAtt, Node tempNode, List<Double> fillAlphas, List<String> fillColors) {
        String nodeValue = nodeAtt.getNodeValue();

        String startTag = attributeTag.getStartTag();
        String attType = attributeTag.getType();
        if (ATT_CSS.equals(attType)) {
            htmlAttTag = convertToCss(htmlAttTag, attributeTag, nodeValue);
        } else if (ATT_TEXT.equals(attType)) {
            if ("Controls:ACCRadioButton".equals(tempNode.getNodeName()) || "Controls:ACCCheckBox".equals(tempNode.getNodeName())) {
                htmlAttTag.setText("<label for=\"rdoOneReam1\">" + nodeValue + "</label>");
            } else {
                htmlAttTag.setText(nodeValue);
            }
        } else if ("id".equals(attType)) {
            htmlAttTag.setId(nodeValue);
        } else if (ATT_OTHER.equals(attType)) {
            if ("fillAlphas".equals(startTag)) {
                String[] aAlpha = nodeValue.split(",");
                for (String s : aAlpha) {
                    double alpha = Double.parseDouble(s.replace("[", "").replace("]", ""));
                    fillAlphas.add(alpha);
                }
            } else if ("fillColors".equals(startTag)) {
                String[] aColor = nodeValue.split(",");
                for (String s : aColor) {
                    fillColors.add(s.replace("[", "").replace("]", ""));
                }
            }
        } else if (ATT_ATTRIBUTE.equals(attType)) {
            htmlAttTag = convertToAtt(htmlAttTag, attributeTag, nodeValue);
        } else if (ATT_JS.equals(attType)) {
            htmlAttTag = convertToJs(htmlAttTag, attributeTag, nodeValue);
        } else {

        }
    }

    private HtmlAttributeTag convertToCss(HtmlAttributeTag htmlAttTag, AttributeTag attributeTag, String nodeValue) {
        String startTag = attributeTag.getStartTag();
        String cssAdd = startTag + ":" + nodeValue + attributeTag.getEndTag() + ";";
        if ("opacity".equals(startTag)) {
            if (Double.parseDouble(nodeValue) > 0) {
                htmlAttTag.getStyles().put(startTag, nodeValue + attributeTag.getEndTag());
            }
        } else {
            htmlAttTag.getStyles().put(startTag, nodeValue + attributeTag.getEndTag());
        }

        return htmlAttTag;
    }

    private HtmlAttributeTag convertToAtt(HtmlAttributeTag htmlAttTag, AttributeTag attributeTag, String nodeValue) {
        // handle when att = img
        String startTag = attributeTag.getStartTag();
        boolean compareTrueWithValue = attributeTag.isValueCompareTrue();
        if ("src".equals(startTag)) {
            nodeValue = nodeValue.replace("@Embed('", "").replace("')", "");
        }
        if (compareTrueWithValue) {
            if ("true".equals(nodeValue)) {
                htmlAttTag.getAttributes().put(startTag, "true");
//                attributeText = attributeText + startTag + " ";
            }
        } else {
//            attributeText = attributeText + startTag + "=\"" + nodeValue + "\"";
            htmlAttTag.getAttributes().put(startTag, nodeValue);
        }

        return htmlAttTag;
    }

    private static HtmlAttributeTag convertToJs(HtmlAttributeTag htmlAttTag, AttributeTag attributeTag, String nodeValue) {
        String startTag = attributeTag.getStartTag();
        htmlAttTag.getAttributes().put(startTag, nodeValue);
//        attributeText = attributeText + startTag + "=\"" + nodeValue + "\"";
        return htmlAttTag;
    }
}

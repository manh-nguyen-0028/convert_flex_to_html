package service;

import dto.AttributeTag;
import dto.MxmlTag;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import utils.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
//                "<link rel=\"stylesheet\" href=\"css\\control.css\">" +
                "<link rel=\"stylesheet\" href=\"css\\{p_css}.css\">" +
                "</head><body id=\"" + cssName + "_body\">";
        htmlStart = htmlStart.replace("{p_css}", cssName).replace("{p_title}", title);
        return htmlStart;
    }

    public static void printNote(NodeList nodeList, StringBuilder html, String ownerNode, StringBuilder styleHere) throws IOException {
        hmAttributeTag = XmlService.getAttributeConfig();

        hmXmlTag = XmlService.getMxmlConfig();

        String attributeText = "";
        String styleValue = "";
        String sText = "";
        List<Double> fillAlphas = new ArrayList<>();
        List<String> fillColors = new ArrayList<>();

        String id = "";
        for (int count = 0; count < nodeList.getLength(); count++) {

            Node tempNode = nodeList.item(count);

            // make sure it's element node.
            boolean firstCanvas = true;
            if (tempNode.getNodeType() == Node.ELEMENT_NODE) {
                MxmlTag mxmlTagConfig = hmXmlTag.get(tempNode.getNodeName());
                if (mxmlTagConfig != null) {
                    String htmlTagStart = mxmlTagConfig.getHtmlTagStart();
                    String htmlTagEnd = mxmlTagConfig.getHtmlTagEnd();
                    String className = mxmlTagConfig.getClassName();
                    htmlTagStart = htmlTagStart.replace("{1}", className);
                    String htmlNeedAdd = htmlTagStart;

                    if (tempNode.hasAttributes()) {
                        // get attributes names and values
                        NamedNodeMap nodeAttMap = tempNode.getAttributes();
                        for (int i = 0; i < nodeAttMap.getLength(); i++) {
                            Node nodeAtt = nodeAttMap.item(i);
                            String nodeName = nodeAtt.getNodeName();
                            String nodeValue = nodeAtt.getNodeValue();
                            AttributeTag attributeTag = hmAttributeTag.get(nodeName);
                            if (attributeTag != null) {
                                String startTag = attributeTag.getStartTag();
                                String attType = attributeTag.getType();
                                if (ATT_CSS.equals(attType)) {
                                    styleValue = convertToCss(attributeTag, nodeValue, styleValue);
                                } else if (ATT_TEXT.equals(attType)) {
                                    if ("Controls:ACCRadioButton".equals(tempNode.getNodeName()) || "Controls:ACCCheckBox".equals(tempNode.getNodeName())) {
                                        sText = sText + "<label for=\"rdoOneReam1\">" + nodeValue + "</label>";
                                    } else {
                                        sText = sText + nodeValue;
                                    }
                                } else if ("id".equals(attType)) {
                                    id = nodeValue;
                                    attributeText = attributeText + startTag + "='" + id + "'";
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
                                    attributeText = convertToAtt(attributeTag, nodeValue, attributeText);
                                } else if (ATT_JS.equals(attType)) {
                                    attributeText = convertToJs(attributeTag, nodeValue, attributeText);
                                } else {

                                }
                            }
                            System.out.println("attr name : " + nodeName);
                            System.out.println("attr value : " + nodeValue);
                        }

                        if (id.isEmpty()) {
                            id = "id_" + StringUtils.generateRandomText(3);
                            attributeText = attributeText + "id='" + id + "'";
                        }
                    }

//                    background: linear-gradient(#0000FF 0%, #00FF00 100%);
                    if (fillColors.size() > 0) {
                        styleValue = styleValue + "background: linear-gradient(";
                        for (int i = 0; i < fillColors.size(); i++) {
                            double alpha = fillAlphas.get(i);
                            int percentageValue = (int) alpha * 100;
                            styleValue = styleValue + fillColors.get(i) + " " + percentageValue + "%, ";
                        }
                        styleValue = styleValue.substring(0, styleValue.length() - 2) + ");";
                    }
                    try {
                        if (mxmlTagConfig != null && mxmlTagConfig.getHadAttribute() && mxmlTagConfig != null && attributeText.length() > 0) {
                            htmlNeedAdd = htmlNeedAdd + attributeText;
//                            html.append(attributeText + "");
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }

                    String styleHereNeedAdd = "";
                    // style to file
                    if (!styleValue.isEmpty()) {
                        if (id != null && id != "") {
                            styleHereNeedAdd = styleHereNeedAdd + "#" + id + " {" + styleValue + "} \n";
//                            styleHere.append("#" + id + " {").append(styleValue + "} \n");
                            if("Containers:ACCCanvas".equals(tempNode.getNodeName()) && firstCanvas) {
//                                styleHere.append("body {").append(styleValue + "} \n");
                                styleHereNeedAdd = styleHereNeedAdd + "body {"+styleValue + "} \n";
                                firstCanvas = false;
                            }
                            System.out.println(styleHereNeedAdd);
                        } else {
                            htmlNeedAdd = htmlNeedAdd + "style ='" + styleValue + "'";
//                            html.append("style ='" + styleValue + "'");
                        }
                    }

                    if ("Containers:ACCHBox".equals(ownerNode) || "Containers:ACCVBox".equals(ownerNode)) {
                        styleHereNeedAdd = styleHereNeedAdd.replace("position: absolute;","");
                        styleValue = styleValue.replace("position: absolute;","");
                    }

                    if (mxmlTagConfig.getStyleInFile()) {
                        // remove position: absolute;
                        styleHere.append(styleHereNeedAdd);
//                        htmlNeedAdd = htmlNeedAdd.replace("{style}", "style ='" + styleValue + "'");
                    } else {
                        htmlNeedAdd = htmlNeedAdd.replace("{style}", "style ='" + styleValue + "'");
                    }

                    html.append(htmlNeedAdd);
                    mxmlTagConfig.setHtmlTagStart3(sText);
                    html.append(mxmlTagConfig.getHtmlTagStart2());
                    if (mxmlTagConfig.getHtmlTagStart3().length() > 0) {
                        html.append(mxmlTagConfig.getHtmlTagStart3());
                    }
                    attributeText = "";
                    styleValue = "";
                    sText = "";
                    id = "";
                    fillAlphas = new ArrayList<>();
                    fillColors = new ArrayList<>();
                    if (tempNode.hasChildNodes()) {
                        // loop again if has child nodes
                        printNote(tempNode.getChildNodes(), html, tempNode.getNodeName(), styleHere);
                    }

                    html.append(mxmlTagConfig.getHtmlTagEnd());
                    System.out.println("Node Name =" + tempNode.getNodeName() + " [CLOSE]");

                }
            }
        }
    }

    private static String convertToCss(AttributeTag attributeTag, String nodeValue, String styleValue) {
        String startTag = attributeTag.getStartTag();
        String cssAdd = startTag + ":" + nodeValue + attributeTag.getEndTag() + ";";
        if ("opacity".equals(startTag)) {
            if (Double.parseDouble(nodeValue) > 0) {
                styleValue = styleValue + cssAdd;
            }
        } else {
            styleValue = styleValue + cssAdd;
        }
        return styleValue;
    }

    private static String convertToAtt(AttributeTag attributeTag, String nodeValue, String attributeText) {
        // handle when att = img
        String startTag = attributeTag.getStartTag();
        boolean compareTrueWithValue = attributeTag.isValueCompareTrue();
        if ("src".equals(startTag)) {
            nodeValue = nodeValue.replace("@Embed('", "").replace("')", "");
        }
        if (compareTrueWithValue) {
            if ("true".equals(nodeValue)) {
                attributeText = attributeText + startTag + " ";
            }
        } else {
            attributeText = attributeText + startTag + "=\"" + nodeValue + "\"";
        }
        return attributeText;
    }

    private static String convertToJs(AttributeTag attributeTag, String nodeValue, String attributeText) {
        String startTag = attributeTag.getStartTag();
        attributeText = attributeText + startTag + "=\"" + nodeValue + "\"";
        return attributeText;
    }
}

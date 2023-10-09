package com.company;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;

public class Main {

    /*public static void main(String[] args) {
        try {
//            File mxmlFile = new File("D:\\Project\\Nissho\\AdobeFlexAir\\506519.Roseindia-Flex-Examples\\Other Flex examples\\src\\HelloWorld.mxml"); // Thay đổi đường dẫn đến tệp MXML của bạn
            File mxmlFile = new File("D:\\Project\\Nissho\\AdobeFlexAir\\506519.Roseindia-Flex-Examples\\Other Flex examples\\Morpheus.mxml"); // Thay đổi đường dẫn đến tệp MXML của bạn

            // Khởi tạo DocumentBuilderFactory
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

            // Bật hỗ trợ UTF-8
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);

            // Tạo DocumentBuilder từ factory
            DocumentBuilder builder = factory.newDocumentBuilder();

            // Đọc tệp MXML thành đối tượng Document
            Document document = builder.parse(mxmlFile);

            // Bắt đầu xử lý cây DOM, bắt đầu từ nút gốc
            processNode(document.getDocumentElement(), 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }*/

    public static void main(String[] args) {
        try {
            File mxmlFile = new File("D:\\\\Project\\\\Nissho\\\\AdobeFlexAir\\\\506519.Roseindia-Flex-Examples\\\\Other Flex examples\\\\Scarab.mxml"); // Thay đổi đường dẫn đến tệp MXML của bạn

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(mxmlFile);

            // Tạo một đối tượng StringBuilder để xây dựng mã HTML
            StringBuilder html = new StringBuilder();
            StringBuilder cssSyntax = new StringBuilder();

            // Lấy danh sách các phần tử trong tệp MXML
            NodeList elements = document.getElementsByTagName("*");

            // Duyệt qua danh sách các phần tử và in ra giá trị của thuộc tính 'id'
            for (int i = 0; i < elements.getLength(); i++) {
                Element element = (Element) elements.item(i);
                if ("mx:Application".equals(element.getTagName())) {
                    html.append("<html>");
                } else if("mx:Panel".equals(element.getTagName())) {
                    String title = element.getAttribute("title");
                    html.append("<head> \n");
                    html.append("<title>").append(title).append("</title> \n");
                    html.append("</head> \n");
                    html.append("<body> \n");
                    html.append(" <div class=\"panel\"> \n");
                    /*if (!id.isEmpty()) {
                        System.out.println("Element ID: " + id);
                    }*/
                } else if("mx:ViewStack".equals(element.getTagName())) {
                    html.append(" <div class=\"view-stack\"> \n");
                    /*if (!id.isEmpty()) {
                        System.out.println("Element ID: " + id);
                    }*/
                } else if("mx:Canvas".equals(element.getTagName())) {
                    html.append(" <div class=\"canvas\"> \n");
                    /*if (!id.isEmpty()) {
                        System.out.println("Element ID: " + id);
                    }*/
                } else if("mx:Canvas".equals(element.getTagName())) {
                    html.append(" <div class=\"canvas\"> \n");
                    /*if (!id.isEmpty()) {
                        System.out.println("Element ID: " + id);
                    }*/
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Hàm đệ quy để duyệt cây DOM và in ra các thẻ
    private static void processNode(Node node, int indent) {
        if (node.getNodeType() == Node.ELEMENT_NODE) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < indent; i++) {
                sb.append("  ");
            }
            sb.append("<").append(node.getNodeName()).append(">");
            System.out.println(sb.toString());

            NodeList childNodes = node.getChildNodes();
            for (int i = 0; i < childNodes.getLength(); i++) {
                processNode(childNodes.item(i), indent + 1);
            }

            sb = new StringBuilder();
            for (int i = 0; i < indent; i++) {
                sb.append("  ");
            }
            sb.append("</").append(node.getNodeName()).append(">");
            System.out.println(sb.toString());
        }
    }
}

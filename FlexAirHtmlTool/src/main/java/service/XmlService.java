package service;

import dto.mxml.mapping.ComponentMap;
import dto.mxml.mapping.PropertyMap;
import dto.mxml.mapping.TransformerSpecialElement;
import utils.JsonUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class XmlService {
    private XmlService() {
    }

    public static Map<String, ComponentMap> getNodeMap() {
        Map<String, ComponentMap> hmXmlTag = new HashMap<>();
        String filePath = "json/component-mapping-config.json";
        List<ComponentMap> mxmlTags = new JsonUtils<ComponentMap>().readJsonFile(filePath, ComponentMap.class);

        for (ComponentMap mxmlTag : mxmlTags) {
            hmXmlTag.put(mxmlTag.getName(), mxmlTag);
        }

        return hmXmlTag;
    }

    public static Map<String, PropertyMap> getAttributeMap() {
        Map<String, PropertyMap> hmAttributeTag = new HashMap<>();
        String filePath = "json/properties-mapping-config.json";
        List<PropertyMap> attributeTags = new JsonUtils<PropertyMap>().readJsonFile(filePath, PropertyMap.class);
        for (PropertyMap attributeTag : attributeTags) {
            hmAttributeTag.put(attributeTag.getName(), attributeTag);
        }
        return hmAttributeTag;
    }

    public static List<TransformerSpecialElement> getTransformerSpecialElement() {
        String filePath = "json/transformer-special-element-attribute.json";
        List<TransformerSpecialElement> specialElementList = new JsonUtils<TransformerSpecialElement>().readJsonFile(filePath, TransformerSpecialElement.class);
        return specialElementList;
    }
}

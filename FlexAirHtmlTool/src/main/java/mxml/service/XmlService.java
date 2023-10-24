package mxml.service;

import mxml.dto.mapping.ComponentMap;
import mxml.dto.mapping.PropertyMap;
import mxml.dto.mapping.TransformerSpecialElement;
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
        return new JsonUtils<TransformerSpecialElement>().readJsonFile(filePath, TransformerSpecialElement.class);
    }
}

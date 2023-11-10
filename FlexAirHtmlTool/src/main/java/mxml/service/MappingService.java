package mxml.service;

import mxml.dto.mapping.ComponentMap;
import mxml.dto.mapping.ComponentPropertyMap;
import mxml.dto.mapping.PropertyMap;
import mxml.dto.mapping.TransformerSpecialElement;
import utils.JsonUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MappingService {
    private MappingService() {
    }

    public static Map<String, ComponentMap> getComponentMap() {
        Map<String, ComponentMap> hmXmlTag = new HashMap<>();
        String filePath = "json/component-mapping.json";
        List<ComponentMap> mxmlTags = new JsonUtils<ComponentMap>().readJsonFile(filePath, ComponentMap.class);

        for (ComponentMap mxmlTag : mxmlTags) {
            hmXmlTag.put(mxmlTag.getMxmlTag(), mxmlTag);
        }

        return hmXmlTag;
    }

    public static Map<String, PropertyMap> getAttributeMap() {
        Map<String, PropertyMap> hmAttributeTag = new HashMap<>();
        String filePath = "json/properties-mapping.json";
        List<PropertyMap> attributeTags = new JsonUtils<PropertyMap>().readJsonFile(filePath, PropertyMap.class);
        for (PropertyMap attributeTag : attributeTags) {
            hmAttributeTag.put(attributeTag.getMxmlProperties(), attributeTag);
        }
        return hmAttributeTag;
    }

    public static List<TransformerSpecialElement> getListComponentByProperty() {
        String filePath = "json/component-by-property-mapping.json";
        return new JsonUtils<TransformerSpecialElement>().readJsonFile(filePath, TransformerSpecialElement.class);
    }

    public static List<ComponentPropertyMap> getPropertyByComponent() {
        String filePath = "json/property-by-component-mapping.json";
        return new JsonUtils<ComponentPropertyMap>().readJsonFile(filePath, ComponentPropertyMap.class);
    }
}

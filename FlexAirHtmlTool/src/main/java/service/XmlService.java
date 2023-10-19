package service;

import dto.mxml.mapping.ComponentMap;
import dto.mxml.mapping.MappingElementModify;
import dto.mxml.mapping.PropertyMap;
import dto.mxml.mapping.TabConfig;
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

    public static Map<String, MappingElementModify> getMappingElementModify() {
        Map<String, MappingElementModify> hmElementModify = new HashMap<>();
        String filePath = "json/mapping-element-modify.json";
        List<MappingElementModify> elementModifies = new JsonUtils<MappingElementModify>().readJsonFile(filePath, MappingElementModify.class);

        for (MappingElementModify elementModify : elementModifies) {
            hmElementModify.put(elementModify.getParentXmlName(), elementModify);
        }

        return hmElementModify;
    }

    public static TabConfig getTabConfigByFileName(String sFileName, String idTab) {
        String filePath = "json/tab-config.json";
        List<TabConfig> tabConfigs = new JsonUtils<TabConfig>().readJsonFile(filePath, TabConfig.class);

        TabConfig tabConfig = tabConfigs.stream().filter(itemFilter -> itemFilter.getFileName().equals(sFileName) && itemFilter.getId().equals(idTab)).findFirst().get();

        return tabConfig;
    }
}

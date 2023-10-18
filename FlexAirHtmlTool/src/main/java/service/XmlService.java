package service;

import dto.mxml.mapping.AttributeMap;
import dto.mxml.mapping.MappingElementModify;
import dto.mxml.mapping.NodeMap;
import dto.mxml.mapping.TabConfig;
import utils.JsonUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class XmlService {
    private XmlService() {
    }

    public static Map<String, NodeMap> getNodeMap() {
        Map<String, NodeMap> hmXmlTag = new HashMap<>();
        String filePath = "json/node-map.json";
        List<NodeMap> mxmlTags = new JsonUtils<NodeMap>().readJsonFile(filePath, NodeMap.class);

        for (NodeMap mxmlTag : mxmlTags) {
            hmXmlTag.put(mxmlTag.getName(), mxmlTag);
        }

        return hmXmlTag;
    }

    public static Map<String, AttributeMap> getAttributeMap() {
        Map<String, AttributeMap> hmAttributeTag = new HashMap<>();
        String filePath = "json/attribute-config.json";
        List<AttributeMap> attributeTags = new JsonUtils<AttributeMap>().readJsonFile(filePath, AttributeMap.class);
        for (AttributeMap attributeTag : attributeTags) {
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

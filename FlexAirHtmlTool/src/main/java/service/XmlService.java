package service;

import dto.mxml.mapping.*;
import utils.JsonUtils;

import java.util.HashMap;
import java.util.List;

public class XmlService {
    public static HashMap<String, MappingAttribute> getMappingAttribute() {
        HashMap<String, MappingAttribute> hmXmlTag = new HashMap<>();
        String filePath = "json/config.json";
        List<MappingAttribute> mxmlTags = new JsonUtils().readJsonFile(filePath, MappingAttribute.class);

        for (MappingAttribute mxmlTag : mxmlTags) {
            hmXmlTag.put(mxmlTag.getName(), mxmlTag);
        }

        return hmXmlTag;
    }

    public static HashMap<String, MappingTag> getMappingTag() {
        HashMap<String, MappingTag> hmAttributeTag = new HashMap<>();
        String filePath = "json/attribute-config.json";
        List<MappingTag> attributeTags = new JsonUtils().readJsonFile(filePath, MappingTag.class);
        for (MappingTag attributeTag : attributeTags) {
            hmAttributeTag.put(attributeTag.getName(), attributeTag);
        }
        return hmAttributeTag;
    }

    public static HashMap<String, MappingElementModify> getMappingElementModify() {
        HashMap<String, MappingElementModify> hmElementModify = new HashMap<>();
        String filePath = "json/mapping-element-modify.json";
        List<MappingElementModify> elementModifies = new JsonUtils().readJsonFile(filePath, MappingElementModify.class);

        for (MappingElementModify elementModify : elementModifies) {
            hmElementModify.put(elementModify.getParentXmlName(), elementModify);
        }

        return hmElementModify;
    }

    public static TabConfig getTabConfigByFileName(String sFileName, String idTab) {
        String filePath = "json/tab-config.json";
        List<TabConfig> tabConfigs = new JsonUtils().readJsonFile(filePath, TabConfig.class);

        TabConfig tabConfig = tabConfigs.stream().filter(itemFilter -> itemFilter.getFileName().equals(sFileName) && itemFilter.getId().equals(idTab)).findFirst().get();

        return tabConfig;
    }
}

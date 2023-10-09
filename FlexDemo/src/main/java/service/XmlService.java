package service;

import dto.AttributeTag;
import dto.MxmlTag;
import utils.JsonUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

public class XmlService {
    public static HashMap<String, MxmlTag> getMxmlConfig() throws IOException {
        HashMap<String, MxmlTag> hmXmlTag = new HashMap<>();
        String filePath = "D:\\Project\\Nissho\\AdobeFlexDemo\\FlexDemo\\src\\main\\java\\config.json";

        List<MxmlTag> mxmlTags = new JsonUtils().readJsonFile(filePath, MxmlTag.class);

        for (MxmlTag mxmlTag : mxmlTags) {
            hmXmlTag.put(mxmlTag.getName(), mxmlTag);
        }

        return hmXmlTag;
    }

    public static HashMap<String, AttributeTag> getAttributeConfig() throws IOException {
        HashMap<String, AttributeTag> hmAttributeTag = new HashMap<>();
        String filePath = "D:\\Project\\Nissho\\AdobeFlexDemo\\FlexDemo\\src\\main\\java\\attribute-config.json";

        List<AttributeTag> attributeTags = new JsonUtils().readJsonFile(filePath, AttributeTag.class);

        for (AttributeTag attributeTag : attributeTags) {
            hmAttributeTag.put(attributeTag.getName(), attributeTag);
        }

        return hmAttributeTag;
    }
}

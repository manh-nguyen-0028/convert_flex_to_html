package mxml.config;

import mxml.dto.mapping.ComponentMap;
import mxml.dto.mapping.ComponentPropertyMap;
import mxml.dto.mapping.PropertyMap;
import mxml.dto.mapping.TransformerSpecialElement;
import mxml.service.MappingService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MappingHandler {
    private static MappingHandler instance;
    private Map<String, Object> dataMap;

    private MappingHandler() {
        this.dataMap = initializeData();
    }

    public static synchronized MappingHandler getInstance() {
        if (instance == null) {
            instance = new MappingHandler();
        }
        return instance;
    }

    private Map<String, Object> initializeData() {
        Map<String, Object> dataMapping = new HashMap<>();

        Map<String, ComponentMap> hmComponentMap = MappingService.getComponentMap();
        Map<String, PropertyMap> hmAttributeMap = MappingService.getAttributeMap();
        List<TransformerSpecialElement> componentByPropertyList = MappingService.getListComponentByProperty();
        List<ComponentPropertyMap> propertyByComponentList = MappingService.getPropertyByComponent();

        dataMapping.put("componentMap", hmComponentMap);
        dataMapping.put("attributeMap", hmAttributeMap);
        dataMapping.put("componentByPropertyList", componentByPropertyList);
        dataMapping.put("propertyByComponentList", propertyByComponentList);

        return dataMapping;
    }

    public Map<String, Object> getDataMap() {
        return dataMap;
    }
}

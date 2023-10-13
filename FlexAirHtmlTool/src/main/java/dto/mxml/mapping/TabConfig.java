package dto.mxml.mapping;

import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;
import java.util.List;

@Getter
@Setter
public class TabConfig {
    private String fileName;
    private String id;
    private String tabNames;
    private List<String> tabNameList;

    public List<String> getTabNameList() {
        return Arrays.asList(tabNames.split(";"));
    }
}

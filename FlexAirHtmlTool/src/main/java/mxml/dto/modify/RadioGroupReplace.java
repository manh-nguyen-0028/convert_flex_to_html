package mxml.dto.modify;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class RadioGroupReplace {
    public RadioGroupReplace(String groupId) {
        this.groupId = groupId;
    }

    private String groupId;
    private AjaxEvent ajaxEvent;
    List<String> selectItemList = new ArrayList<>();
}

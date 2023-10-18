package dto.mxml.modify;

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
    List<String> selectItemList = new ArrayList<>();
}

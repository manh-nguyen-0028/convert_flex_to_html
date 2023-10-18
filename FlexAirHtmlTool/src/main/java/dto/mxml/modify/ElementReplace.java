package dto.mxml.modify;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class ElementReplace {
    private String cssFirstCanvas;
    private String title;
    private String formName;
    private List<RadioGroupReplace> radioGroupReplaces = new ArrayList<>();
}

package dto.mxml.modify;

import dto.mxml.parser.CssParser;
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
    private List<CssParser> cssCompositionFirstList;
    private List<RadioGroupReplace> radioGroupReplaces = new ArrayList<>();
}

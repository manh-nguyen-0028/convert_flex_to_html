package mxml.dto.modify;

import lombok.Getter;
import lombok.Setter;
import mxml.dto.parser.CssParser;

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
    private List<CheckBoxReplace> checkBoxReplaces = new ArrayList<>();
}

package mxml.dto.modify;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CheckBoxReplace {
    private String id;
    private String event;
    private String listener;

    public CheckBoxReplace() {
    }

    public CheckBoxReplace(String id, String event) {
        this.id = id;
        this.event = event;
    }

    public CheckBoxReplace(String id, String event, String listener) {
        this.id = id;
        this.event = event;
        this.listener = listener;
    }
}

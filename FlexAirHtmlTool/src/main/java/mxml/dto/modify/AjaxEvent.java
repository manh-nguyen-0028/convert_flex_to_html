package mxml.dto.modify;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class AjaxEvent {
    private String id;
    private String nodeName;
    private String event;
    private String listener;

    public AjaxEvent(String event, String listener) {
        this.event = event;
        this.listener = listener;
    }

    public AjaxEvent(String id, String event, String listener) {
        this.id = id;
        this.event = event;
        this.listener = listener;
    }

    public AjaxEvent(String id, String nodeName, String event, String listener) {
        this.id = id;
        this.nodeName = nodeName;
        this.event = event;
        this.listener = listener;
    }
}

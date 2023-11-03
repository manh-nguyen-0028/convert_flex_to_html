package mxml.dto.modify;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class AjaxEvent {
    private String event;
    private String listener;

    public AjaxEvent(String event, String listener) {
        this.event = event;
        this.listener = listener;
    }
}

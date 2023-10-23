package service;

import dto.mxml.modify.CheckBoxReplace;
import dto.mxml.modify.ElementReplace;
import org.apache.commons.collections4.CollectionUtils;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.List;

public class JsoupService {
    public static void handleTagJsoup(Document jsoupDoc, ElementReplace elementReplace) {
        // Handle tag p:selectBooleanCheckbox
        handleTagJsoup(jsoupDoc, elementReplace);
    }

    private void handleCheckBoxHaveAjax(Document jsoupDoc, ElementReplace elementReplace) {
        List<CheckBoxReplace> checkBoxReplaceList = elementReplace.getCheckBoxReplaces();
        if (CollectionUtils.isNotEmpty(checkBoxReplaceList)) {
            for (CheckBoxReplace item : checkBoxReplaceList) {
                String id = item.getId();
                for (Element checkbox : jsoupDoc.select("[id=" + id + "]")) {
                    Attributes attributes = checkbox.attributes();
                    String valueEvent = attributes.get(item.getEvent());
                    checkbox.removeAttr(item.getEvent());
                    org.jsoup.nodes.Element ajaxTag = new org.jsoup.nodes.Element("p:ajax");
                    ajaxTag.attr("event", item.getEvent());
                    ajaxTag.attr("listener", valueEvent);
                    checkbox.appendChild(ajaxTag);
                }
            }
        }
    }
}

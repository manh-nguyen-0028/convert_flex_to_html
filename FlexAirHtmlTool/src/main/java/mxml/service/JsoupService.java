package mxml.service;

import mxml.dto.modify.CheckBoxReplace;
import mxml.dto.modify.ElementReplace;
import org.apache.commons.collections4.CollectionUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;

import java.util.List;

public class JsoupService {

    public static String createJsoupDocument(StringBuilder baseHtml, ElementReplace elementReplace) {
        Document jsoupDoc = Jsoup.parse(baseHtml.toString(), "UTF-8", Parser.xmlParser());
        JsoupService.handleTagJsoup(jsoupDoc, elementReplace);
        return jsoupDoc.toString();
    }

    public static void handleTagJsoup(Document jsoupDoc, ElementReplace elementReplace) {
        // Handle tag p:selectBooleanCheckbox
        handleCheckBoxHaveAjax(jsoupDoc, elementReplace);
    }

    private static void handleCheckBoxHaveAjax(Document jsoupDoc, ElementReplace elementReplace) {
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

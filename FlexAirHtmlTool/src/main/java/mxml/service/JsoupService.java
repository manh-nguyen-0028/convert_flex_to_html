package mxml.service;

import constants.Constants;
import mxml.dto.modify.AjaxEvent;
import mxml.dto.modify.ElementReplace;
import org.apache.commons.collections4.CollectionUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;

import java.util.List;

public class JsoupService {

    private JsoupService() {
    }

    public static String createJsoupDocument(StringBuilder baseHtml, ElementReplace elementReplace) {
        Document jsoupDoc = Jsoup.parse(baseHtml.toString(), "UTF-8", Parser.xmlParser());
        JsoupService.handleTagJsoup(jsoupDoc, elementReplace);
        return jsoupDoc.toString();
    }

    public static void handleTagJsoup(Document jsoupDoc, ElementReplace elementReplace) {
        // Handle tag p:ajax
        handleAjaxElement(jsoupDoc, elementReplace);
    }

    private static void handleAjaxElement(Document jsoupDoc, ElementReplace elementReplace) {
        List<AjaxEvent> ajaxEventReplaces = elementReplace.getAjaxEventReplaces();
        if (CollectionUtils.isNotEmpty(ajaxEventReplaces)) {
            for (AjaxEvent item : ajaxEventReplaces) {
                String id = item.getId();
                for (Element element : jsoupDoc.select(String.format("[id=%s]", id))) {
                    Attributes attributes = element.attributes();
                    String valueEvent = attributes.get(item.getEvent());
                    element.removeAttr(item.getEvent());
                    if (!Constants.MXML_CONTROLS_ACC_RADIO_BUTTON.equals(item.getNodeName())) {
                        Element ajaxTag = new Element(Constants.XHTML_P_AJAX);
                        ajaxTag.attr(Constants.XHTML_AJAX_EVENT, item.getEvent());
                        ajaxTag.attr(Constants.XHTML_AJAX_LISTENER, valueEvent);
                        element.appendChild(ajaxTag);
                    }
                }
            }
        }
    }
}

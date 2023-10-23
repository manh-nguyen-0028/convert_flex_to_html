import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class RegexExample {
    public static void main(String[] args) {
        String html = "<p:selectBooleanCheckbox change=\"#\\{MG3001001_01_000Controller.ckNyukinYakusokuJyokenChangeHandler};\" id=\"ckNyukinYakusokuJyoken\" itemLabel=\"入金約束を条件に含む\" tabIndex=\"3\" style=\"height:20;width:163;position: absolute; left:181;top:54;\"></p:selectBooleanCheckbox>";

        Document doc = Jsoup.parse(html, "text/xml");

        // Lấy tất cả các thẻ <p:selectBooleanCheckbox>
        for (Element checkbox : doc.select("[id=ckNyukinYakusokuJyoken]")) {
            // Xóa thuộc tính change
            checkbox.removeAttr("change");

            // Thêm thẻ <p:ajax> vào thẻ <p:selectBooleanCheckbox>
            Element ajaxTag = new Element("p:ajax");
            ajaxTag.attr("event", "change");
            ajaxTag.attr("listener", "#{MG3001001_01_000Controller.ckNyukinYakusokuJyokenChangeHandler}");

            checkbox.appendChild(ajaxTag);
        }

        // In HTML sau khi thay đổi
        System.out.println(doc.html());
    }
}
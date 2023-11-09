import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexExample {
    public static void main(String[] args) {
        StringBuilder baseHtml = new StringBuilder();
        baseHtml.append("<div>");
        baseHtml.append("<h:outputText id=\"ttNameId\" value=\"#{record.ttName}\" showDataTips=\"true\" style=\"text-align:center;\"></h:outputText>");
        baseHtml.append("<p>This is some additional content.</p>");
        baseHtml.append("</div>");

        // Regex pattern to match the specified format
        String pattern = "<h:outputText(?:\\s+(id=\"(\\w+)\")?|\\s+(value=\"(#[^\"]+)\")?|\\s+(showDataTips=\"(true|false)\")?|\\s+(style=\"([^\"]*)\")?)*></h:outputText>";

        // Create a pattern object
        Pattern regex = Pattern.compile(pattern);

        // Create a matcher object
        Matcher matcher = regex.matcher(baseHtml);

        // Create a StringBuilder for the modified result
        StringBuilder modifiedHtml = new StringBuilder();

        // Variable to track the end of the last match
        int lastMatchEnd = 0;

        // Find and replace the pattern in the baseHtml
        while (matcher.find()) {
            // Extract values from the matched groups
            String id = matcher.group(2);
            String value = matcher.group(4);
            String showDataTips = matcher.group(6);

            // Append the content between the last match and the current match to the modified result
            modifiedHtml.append(baseHtml.substring(lastMatchEnd, matcher.start()));

            // Append the modified result to the StringBuilder
            modifiedHtml.append(String.format("<h:outputText id=\"%s\" value=\"%s\"", id, value));

            // Close the opening tag
            modifiedHtml.append("></h:outputText>");

            // Add <p:tooltip> if showDataTips was "true"
            if (showDataTips != null && "true".equals(showDataTips)) {
                modifiedHtml.append(String.format("\n<p:tooltip for=\"%s\" value=\"%s\" />", id, value));
            }

            // Update the last match end position
            lastMatchEnd = matcher.end();
        }

        // Append the remaining content after the last match to the modified result
        modifiedHtml.append(baseHtml.substring(lastMatchEnd));

        baseHtml = modifiedHtml;
        System.out.println(baseHtml.toString());
    }
}
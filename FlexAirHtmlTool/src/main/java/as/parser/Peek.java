package as.parser;

public class Peek {
    private String token;
    private String extracted;
    private int startIndex;
    private int endIndex;

    public Peek(String token, String extracted, int startIndex, int endIndex) {
        this.token = token;
        this.extracted = extracted;
        this.startIndex = startIndex;
        this.endIndex = endIndex;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getExtracted() {
        return extracted;
    }

    public void setExtracted(String extracted) {
        this.extracted = extracted;
    }

    public int getStartIndex() {
        return startIndex;
    }

    public void setStartIndex(int startIndex) {
        this.startIndex = startIndex;
    }

    public int getEndIndex() {
        return endIndex;
    }

    public void setEndIndex(int endIndex) {
        this.endIndex = endIndex;
    }
}

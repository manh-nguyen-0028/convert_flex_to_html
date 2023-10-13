package as.parser;

/**
 * Wrapper class for each token
 */
public class ASToken {
    private String token;
    private int index;
    private String extra;

    public ASToken(String token, int index, String extra) {
        this.token = token;
        this.index = index;
        this.extra = extra;
    }
    //----------------------------------------------
    // getterとsetter生成
    //----------------------------------------------
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String getExtra() {
        return extra;
    }

    public void setExtra(String extra) {
        this.extra = extra;
    }
}

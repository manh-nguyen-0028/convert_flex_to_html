package as.parser;

/**
 * Wrapper class for each token
 */
public class ASToken {
    private String token;
    private int index;
    private String extra;
    private boolean isBindable = false;

    public ASToken(String token, int index, String extra, boolean isBindable) {
        this.token = token;
        this.index = index;
        this.extra = extra;
        this.isBindable = isBindable;
    }
    //----------------------------------------------
    // getterとsetter生成
    //----------------------------------------------

    public boolean isBindable() {
        return isBindable;
    }

    public void setBindable(boolean bindable) {
        isBindable = bindable;
    }

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

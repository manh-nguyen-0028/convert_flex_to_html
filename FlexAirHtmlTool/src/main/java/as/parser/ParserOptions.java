package as.parser;

public class ParserOptions {
    private boolean safeRequire = false;
    private boolean ignoreFlash = false;

    public boolean isSafeRequire() {
        return safeRequire;
    }

    public void setSafeRequire(boolean safeRequire) {
        this.safeRequire = safeRequire;
    }

    public boolean isIgnoreFlash() {
        return ignoreFlash;
    }

    public void setIgnoreFlash(boolean ignoreFlash) {
        this.ignoreFlash = ignoreFlash;
    }
}

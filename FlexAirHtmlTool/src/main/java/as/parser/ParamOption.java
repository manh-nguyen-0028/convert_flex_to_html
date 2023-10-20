package as.parser;

import java.util.ArrayList;
import java.util.List;

public class ParamOption {
    private String entry;
    private String entryMode;
    private boolean ignoreFlash;
    private boolean safeRequire;
    private boolean silent;
    private String srcPaths;
    private List<String> rawPackages;
    private boolean verbose;

    public ParamOption() {
        this.rawPackages = new ArrayList<>();
    }

    public boolean isVerbose() {
        return verbose;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    public List<String> getRawPackages() {
        return rawPackages;
    }

    public void setRawPackages(List<String> rawPackages) {
        this.rawPackages = rawPackages;
    }

    public String getEntry() {
        return entry;
    }

    public void setEntry(String entry) {
        this.entry = entry;
    }

    public String getEntryMode() {
        return entryMode;
    }

    public void setEntryMode(String entryMode) {
        this.entryMode = entryMode;
    }

    public boolean isIgnoreFlash() {
        return ignoreFlash;
    }

    public void setIgnoreFlash(boolean ignoreFlash) {
        this.ignoreFlash = ignoreFlash;
    }

    public boolean isSafeRequire() {
        return safeRequire;
    }

    public void setSafeRequire(boolean safeRequire) {
        this.safeRequire = safeRequire;
    }

    public boolean isSilent() {
        return silent;
    }

    public void setSilent(boolean silent) {
        this.silent = silent;
    }

    public String getSrcPaths() {
        return srcPaths;
    }

    public void setSrcPaths(String srcPaths) {
        this.srcPaths = srcPaths;
    }
}

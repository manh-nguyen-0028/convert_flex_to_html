package as.types;

public class ASArgument extends ASVariable {
    private boolean isRestParam = false;
    public ASArgument() {}

    public boolean isRestParam() {
        return isRestParam;
    }

    public void setRestParam(boolean restParam) {
        isRestParam = restParam;
    }
}
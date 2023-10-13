package as.types;

import constants.Constants;
import utils.StringUtils;
import java.util.ArrayList;
import java.util.List;
/**
 * Wrapper class for functions
 */
public class ASFunction extends ASMember{
    private List<ASVariable> argList;

    public ASFunction() {
        argList = new ArrayList<>();
    }

    /**
     * Check exist current variable in argument list
     * @return
     */
    public boolean hasArgument() {
        for(ASMember m : argList) {
            if(m.getName().equals(getName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get local variable for function
     * Find all variable with format: 'var variable_name:types' and convert to ASVariable
     * @return Variable list
     */
    public List<ASVariable> buildLocalVariableStack() {
        int i;
        String text = getValue() != null ? getValue() : "";
        String[] matches = StringUtils.matches(Constants.VARIABLE_PATTERN, text);
        List<ASVariable> locals = new ArrayList<>();
        if(argList != null) {
            locals.addAll(argList);
        }
        for(i = 0; i < matches.length; i++) {
            ASVariable tmpVar = new ASVariable();
            tmpVar.setName(matches[i].replaceAll(Constants.VARIABLE_PATTERN, "$3"));
            tmpVar.setType(matches[i].replaceAll(Constants.VARIABLE_PATTERN, "$4"));
            locals.add(tmpVar);
        }

        return locals;
    }

    public List<ASVariable> getArgList() {
        return argList;
    }

    public void setArgList(List<ASVariable> argList) {
        this.argList = argList;
    }
}

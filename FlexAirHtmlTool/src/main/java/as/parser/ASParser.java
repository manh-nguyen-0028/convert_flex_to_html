package as.parser;

import as.enums.ASParseState;
import as.enums.ASPattern;
import as.types.ASMember;
import constants.Constants;
import utils.StringUtils;

import java.util.Stack;

public class ASParser {
    public static String PREVIOUS_BLOCK;
    private Stack<String> stack;
    private String src;
    private String classPath;
    private ParserOptions parserOptions;

    public ASParser(String src, String classPath) {
        this.stack = new Stack<>();
        this.src = src;
        this.classPath = classPath;
    }

    /**
     * Parse content
     */
    public ASClass parse(ParserOptions options) {
        parserOptions = options == null ? new ParserOptions() : options;

        ASClass classDefinition = new ASClass(parserOptions);
        stack = new Stack<>();
        stack.push(ASParseState.START);

        if (classDefinition.getClassName() == null) {
            throw new Error("Error, no class provided for package: " + classPath);
        }
        return classDefinition;
    }
    private void parseHelper(ASClass cls, String src) {
        ASToken currToken = null;
        ASToken tmpToken = null;
        String tmpStr = null;
        String[] tmpArr = null;
        ASMember currMember = null;
        int index;
        for (index = 0; index < src.length(); index++) {
            if (getState() == ASParseState.START) {
                //String together letters only until we reach a non-letter
                currToken = this.nextWord(src, index, ASPattern.IDENTIFIER[0], ASPattern.IDENTIFIER[1]);
                index = currToken.getIndex() - 1; //Update to the new position
                if (currToken.getToken().equals(Constants.PACKAGE)) {
                    stack.push(ASParseState.PACKAGE_NAME);
                }
            }
        }

    }
    public static ASToken nextWord(String src, int index, String characters, String pattern) {
        return  null;
    }
    /**
     * Get state on the top of the stack
     */
    public String getState() {
        // return (this.stack.size() > 0) ? this.stack.get(this.stack.size() - 1) : null;
        return (this.stack.size() > 0) ? this.stack.peek() : null;
    }
    //----------------------------------------------
    // getterとsetter生成
    //----------------------------------------------
    public ParserOptions getParserOptions() {
        return parserOptions;
    }

    public void setParserOptions(ParserOptions parserOptions) {
        this.parserOptions = parserOptions;
    }

    public Stack<String> getStack() {
        return stack;
    }

    public void setStack(Stack<String> stack) {
        this.stack = stack;
    }

    public String getSrc() {
        return src;
    }

    public void setSrc(String src) {
        this.src = src;
    }

    public String getClassPath() {
        return classPath;
    }

    public void setClassPath(String classPath) {
        this.classPath = classPath;
    }
}

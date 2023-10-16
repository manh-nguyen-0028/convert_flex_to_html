package as.parser;

import as.enums.ASKeyword;
import as.enums.ASParseState;
import as.enums.ASPattern;
import as.types.ASMember;
import constants.Constants;
import utils.Log;
import utils.StringUtils;

import java.util.Stack;
import java.util.regex.Pattern;

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
        parseHelper(classDefinition, getSrc());
//        if (classDefinition.getClassName() == null) {
//            throw new Error("Error, no class provided for package: " + classPath);
//        }
        return classDefinition;
    }
    private void parseHelper(ASClass cls, String src) {
        ASToken currToken = null;
        ASToken tmpToken = null;
        ASToken prevTokenImport = null;
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
            } else if (getState() == ASParseState.PACKAGE_NAME) {
                currToken = ASParser.nextWord(src, index, ASPattern.OBJECT[0], ASPattern.OBJECT[1]); //Package name
                tmpToken = ASParser.nextWord(src, index, ASPattern.CURLY_BRACE[0], ASPattern.CURLY_BRACE[1]); //Upcoming curly brace
                index = currToken.getIndex() - 1;
                if (currToken.getToken() == null || tmpToken.getToken() == null) {
                    throw new Error("Error parsing package name.");
                } else {
                    if (tmpToken.getIndex() < currToken.getIndex()) {
                        cls.setPackageName(""); //Curly brace came before next token
                        index = tmpToken.getIndex();
                    } else {
                        cls.setPackageName(currToken.getToken()); //Just grab the package name
                    }
                    Log.debug("Found package: " + cls.getPackageName());
                    cls.getImportWildcards().add(fixClassPath(cls.getPackageName() + ".*")); //Add wild card for its own folder
                    stack.add(ASParseState.PACKAGE);
                    Log.debug("Attempting to parse package...");
                }
            } else if (getState() == ASParseState.PACKAGE) {
                currToken = ASParser.nextWord(src, index, ASPattern.IDENTIFIER[0], ASPattern.IDENTIFIER[1]);
                index = currToken.getIndex() - 1;
                if (currToken.getToken().equals(ASKeyword.CLASS) || currToken.getToken().equals(ASKeyword.INTERFACE)) {
                    if (currToken.getToken().equals(ASKeyword.INTERFACE))
                        cls.setInterface(true);
                    stack.add(ASParseState.CLASS_NAME);
                    Log.debug("Found class keyword...");
                } else if (currToken.getToken().equals(ASKeyword.IMPORT)) {
                    prevTokenImport = currToken;
                    stack.add(ASParseState.IMPORT_PACKAGE);
                    Log.debug("Found import keyword...");
                } else if (currToken.getToken().equals(ASKeyword.REQUIRE)) {
                    stack.add(ASParseState.REQUIRE_MODULE);
                    Log.debug("Found require keyword...");
                }
            } else if (getState() == ASParseState.IMPORT_PACKAGE) {
                //The current token is a class import
                currToken = ASParser.nextWord(src, index, ASPattern.IMPORT[0], ASPattern.IMPORT[1]);
                index = currToken.getIndex() - 1;
                if (currToken.getToken() == null) {
                    throw new Error("Error parsing import.");
                } else {
                    Log.debug("Parsed import name: " + currToken.getToken());
                    if (currToken.getToken().indexOf("*") >= 0) {
                        cls.getImportWildcards().add(currToken.getToken()); //To be resolved later
                    } else {
                        // cls.getImports().add(currToken.getToken()); //No need to resolve
                        // Remove CURLY_BRACE
                        String prevExtra = prevTokenImport.getExtra();
                        int braceIndex = prevExtra.indexOf(Constants.CURLY_BRACE);
                        if (braceIndex > 0)
                        {
                            prevExtra = prevExtra.substring(braceIndex + 1, prevExtra.length());
                        }
                        String importToken = prevExtra + prevTokenImport.getToken() + currToken.getExtra() + currToken.getToken();
                        importToken = importToken.replaceAll("\\t", "");
                        cls.getImports().add(importToken); //No need to resolve
                    }
                    stack.add(ASParseState.PACKAGE);
                }
            }
        }
    }

    /**
     * Get a token word from source content, start at index with pattern and characters word detect.
     */
    public static ASToken nextWord(String src, int index, String characters, String pattern) {
        // Default word for not input
        characters = characters != null ? characters : ASPattern.IDENTIFIER[0];
        pattern = pattern != null ? pattern : ASPattern.IDENTIFIER[1];
        String tokenBuffer = null;
        String extraBuffer = ""; // Contains characters that were missed
        boolean escapeToggle = false;
        String innerState = null;
        for (; index < src.length(); index++) {
            char c = src.charAt(index);
            String tmpStrComment = "";
            if (src.length() > index + 2) {
                tmpStrComment = src.substring(index, index + 2);
            }
            if (String.valueOf(c).matches(characters)) {
                tokenBuffer = !StringUtils.isNullOrEmpty(tokenBuffer) ? tokenBuffer + c : String.valueOf(c); // Create new token buffer if
                // needed, otherwise append
            }else if (innerState == null && ASParser.checkForCommentOpen(tmpStrComment) != null
                    && tokenBuffer == null) {
                tokenBuffer = null;
                Log.debug("Entering comment...");
                innerState = ASParser.checkForCommentOpen(tmpStrComment);
                extraBuffer += tmpStrComment;
                index += 2; // Skip next index
                // Loop until we break out of comment
                for (; index < src.length(); index++) {
                    tmpStrComment = "";
                    if (src.length() > index + 2) {
                        tmpStrComment = src.substring(index, index + 2);
                    }
                    if (ASParser.checkForCommentClose(innerState, tmpStrComment)) {
                        if (innerState.equals(ASParseState.COMMENT_MULTILINE)) { // Use equals instead of ==
                            extraBuffer += tmpStrComment;
                            index++; // Skip next token
                        } else {
                            extraBuffer += src.charAt(index);
                        }
                        innerState = null; // Return to previous state
                        Log.debug("Exiting comment...");
                        break;
                    } else {
                        extraBuffer += src.charAt(index);
                    }
                }
            } else if (innerState == null && ASParser.checkForStringOpen(String.valueOf(src.charAt(index))) != null
                    && tokenBuffer == null) {
                tokenBuffer = null;
                Log.debug("Entering string...");
                innerState = ASParser.checkForStringOpen(String.valueOf(src.charAt(index)));
                extraBuffer += src.substring(index, index + 1);
                index++; // Skip to next index
                // Loop until we break out of string
                for (; index < src.length(); index++) {
                    extraBuffer += src.charAt(index);
                    if (!escapeToggle && src.charAt(index) == '\\') {
                        escapeToggle = true;
                        continue;
                    }
                    if (ASParser.checkForStringClose(innerState, String.valueOf(src.charAt(index)))) {
                        innerState = null; // Return to previous state
                        Log.debug("Exiting string...");
                        break;
                    }
                    escapeToggle = false;
                }
            } else if (!StringUtils.isNullOrEmpty(tokenBuffer) && StringUtils.isMatched(pattern, tokenBuffer)) {
                return new ASToken(tokenBuffer, index, extraBuffer); // [Token, Index]
            } else {
                if (tokenBuffer != null) {
                    extraBuffer += tokenBuffer + c;
                } else {
                    extraBuffer += c;
                }
                tokenBuffer = null;
            }
        }
        return new ASToken(tokenBuffer, index, extraBuffer); // Return an ASToken object instead of an array of mixed
        // types
    }

    private String fixClassPath(String clsPath) {
        // Class paths at the root level might accidentally be prepended with a "."
        return clsPath.replaceFirst("^\\.", "");
    }
    /**
     * Get state on the top of the stack
     */
    public String getState() {
        // return (this.stack.size() > 0) ? this.stack.get(this.stack.size() - 1) : null;
        return (this.stack.size() > 0) ? this.stack.peek() : null;
    }

    /**
     * Check string is comment line(//) or multiline(/*) open
     */
    public static String checkForCommentOpen(String str) {
        return (str == "//") ? ASParseState.COMMENT_INLINE : (str == "/*") ? ASParseState.COMMENT_MULTILINE : null;
    }

    /**
     * Check string is comment line(\n | \r | blank(0)) or multiline( * /) close
     */
    public static boolean checkForCommentClose(String state, String str) {
        return (state == ASParseState.COMMENT_INLINE
                && (str.charAt(0) == '\n' || str.charAt(0) == '\r' || str.charAt(0) == 0)) ? true
                : (state == ASParseState.COMMENT_MULTILINE && str == "*/") ? true : false;
    }

    /**
     *
     * Check for open string (")
     */
    public static String checkForStringOpen(String str) {
        return (str == "\"") ? ASParseState.STRING_DOUBLE_QUOTE
                : (str == "'") ? ASParseState.STRING_SINGLE_QUOTE : null;
    }

    /**
     *
     * Check for close string(")
     */
    public static boolean checkForStringClose(String state, String str) {
        return (state == ASParseState.STRING_DOUBLE_QUOTE && str == "\"") ? true
                : (state == ASParseState.STRING_SINGLE_QUOTE && str == "'") ? true : false;
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

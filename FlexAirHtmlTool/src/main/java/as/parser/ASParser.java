package as.parser;

import as.enums.*;
import as.types.ASArgument;
import as.types.ASFunction;
import as.types.ASMember;
import as.types.ASVariable;
import constants.Constants;
import constants.ReservedWords;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import utils.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.regex.Pattern;

public class ASParser {
    private static final Logger logger = LogManager.getLogger(ASParser.class);
    private String PREVIOUS_BLOCK;
    private Stack<String> stack;
    private String src;
    private String classPath;
    private ParserOptions parserOptions;

    public ASParser() {
    }

    public ASParser(String src, String classPath) {
        this.stack = new Stack<>();
        this.src = src;
        this.classPath = classPath;
    }

    /**
     * Parse content
     */
    public ASClass parse(ParserOptions options, Stack<String> stack) {
        logger.info("Starting parser class....................");
        parserOptions = options == null ? new ParserOptions() : options;
        ASClass classDefinition = new ASClass(parserOptions);
        this.stack = stack;
        //
        parseHelper(classDefinition, getSrc());
//        if (classDefinition.getClassName() == null) {
//            throw new Error("Error, no class provided for package: " + classPath);
//        }
        logger.info("End parser class.................");
        return classDefinition;
    }

    private void parseHelper(ASClass cls, String src) {
        ASToken currToken;
        ASToken tmpToken;
        ASToken prevTokenImport = null;
        String tmpStr;
        String[] tmpArr;
        ASMember currMember = null;
        int index;
        // Replace [Bindable] annotation
        src = src.replaceAll("\\[Bindable\\]", "");
        for (index = 0; index < src.length(); index++) {
            if (ASParseState.START.equals(getState())) {
                //String together letters only until we reach a non-letter
                currToken = this.nextWord(src, index, ASPattern.IDENTIFIER[0], ASPattern.IDENTIFIER[1]);
                index = currToken.getIndex() - 1; //Update to the new position
                if (currToken.getToken().equals(ReservedWords.PACKAGE)) {
                    stack.push(ASParseState.PACKAGE_NAME);
                }
            } else if (ASParseState.PACKAGE_NAME.equals(getState())) {
                logger.info("Parsing package start.................");
                currToken = nextWord(src, index, ASPattern.OBJECT[0], ASPattern.OBJECT[1]); //Package name
                tmpToken = nextWord(src, index, ASPattern.CURLY_BRACE[0], ASPattern.CURLY_BRACE[1]); //Upcoming curly brace
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
                    logger.debug("Found package: " + cls.getPackageName());
                    cls.getImportWildcards().add(fixClassPath(cls.getPackageName() + ".*")); //Add wild card for its own folder
                    stack.add(ASParseState.PACKAGE);
                    logger.info("Parsing package end.................");
                    logger.debug("Attempting to parse package...");
                }
            } else if (ASParseState.PACKAGE.equals(getState())) {
                currToken = nextWord(src, index, ASPattern.IDENTIFIER[0], ASPattern.IDENTIFIER[1]);
                index = currToken.getIndex() - 1;
                if (currToken.getToken().equals(ASKeyword.CLASS) || currToken.getToken().equals(ASKeyword.INTERFACE)) {
                    if (currToken.getToken().equals(ASKeyword.INTERFACE))
                        cls.setInterface(true);
                    stack.add(ASParseState.CLASS_NAME);
                    logger.debug("Found class keyword...");
                    logger.info("Parsing import package end.................");
                } else if (currToken.getToken().equals(ASKeyword.IMPORT)) {
                    prevTokenImport = currToken;
                    stack.add(ASParseState.IMPORT_PACKAGE);
                    logger.debug("Found import keyword...");
                    logger.info("Parsing import package start.................");
                }
            } else if (ASParseState.IMPORT_PACKAGE.equals(getState())) {
                //The current token is a class import
                currToken = nextWord(src, index, ASPattern.IMPORT[0], ASPattern.IMPORT[1]);
                index = currToken.getIndex() - 1;
                if (currToken.getToken() == null) {
                    throw new Error("Error parsing import.");
                } else {
                    logger.debug("Parsed import name: " + currToken.getToken());
                    if (currToken.getToken().contains("*")) {
                        cls.getImportWildcards().add(currToken.getToken()); //To be resolved later
                    } else {
                        // Remove CURLY_BRACE
                        String prevExtra = removeCurlyBrace(prevTokenImport.getExtra());
                        // Import token contains special characters as \n \r \t to format data
                        String importToken = prevExtra + prevTokenImport.getToken() + currToken.getExtra() + currToken.getToken();
                        importToken = importToken.replaceAll("\\t", "");
                        cls.getImports().add(importToken); //No need to resolve
                    }
                    stack.add(ASParseState.PACKAGE);
                }
            } else if (ASParseState.CLASS_NAME.equals(getState())) {
                currToken = nextWord(src, index, ASPattern.IDENTIFIER[0], ASPattern.IDENTIFIER[1]);
                tmpToken = nextWord(src, index, ASPattern.CURLY_BRACE[0], ASPattern.CURLY_BRACE[1]);
                index = currToken.getIndex();
                if (currToken.getToken() == null || tmpToken.getToken() == null) {
                    throw new Error("Error parsing class name.");
                } else if (tmpToken.getIndex() < currToken.getIndex()) {
                    throw new Error("Error, no class name found before curly brace.");
                } else {
                    //Set the class name and replace suffix '_AS'
                    cls.setClassName(removeSuffixAS(currToken.getToken()));
                    //Store origin class name
                    cls.setOrgClassName(currToken.getToken());

                    // Update fully qualified class path if needed
                    classPath = classPath != null ? classPath : fixClassPath(cls.getPackageName() + "." + cls.getClassName()); //Remove extra "." for top level packages
                    cls.getClassMap().put(cls.getClassName(), cls); //Register self into the import map (used for static detection)
                    //Now we will check for parent class and any interfaces
                    currToken = nextWord(src, index, ASPattern.IDENTIFIER[0], ASPattern.IDENTIFIER[1]);
                    if ((currToken.getToken().equals(ASKeyword.EXTENDS) || currToken.getToken().equals(ASKeyword.IMPLEMENTS))
                            && currToken.getIndex() < tmpToken.getIndex()) {
                        index = currToken.getIndex();
                        currToken = nextWord(src, index, ASPattern.IDENTIFIER[0], ASPattern.IDENTIFIER[1]);
                        index = currToken.getIndex();
                        // TODO: Extends ACCControllerBase
                        currToken.setToken(ReservedWords.ACC_CONTROLLER_BASE);
                        //The token following 'extends' must be the parent class
                        cls.setParent(currToken.getToken());
                        //Prep the next token
                        currToken = nextWord(src, index, ASPattern.IDENTIFIER[0], ASPattern.IDENTIFIER[1]);
                        logger.debug("Found parent: " + cls.getParent());
                    }
                    logger.debug("Parsed class name: " + cls.getClassName());
                    //Now parsing inside of the class
                    stack.add(ASParseState.CLASS);
                    logger.debug("Attempting to parse class...");
                    logger.info("Parsing class start.................");
                    //Extract out the next method block
                    PREVIOUS_BLOCK = cls.getClassName() + ":Class";
                    tmpStr = extractBlock(src, index, null, null)[0];
                    index += tmpStr.length() - 1;

                    //Recursively call parseHelper again under this new state (Once returned, package will be exited)
                    parseHelper(cls, tmpStr);
                }
            } else if (ASParseState.CLASS.equals(getState())) {
                currMember = currMember != null ? currMember : new ASMember(); //Declare a new member to work with if it doesn't exist yet
                currToken = nextWord(src, index, ASPattern.IDENTIFIER[0], ASPattern.IDENTIFIER[1]);
                index = currToken.getIndex() - 1;
                if (currToken.getToken() == null) continue;
                //------------------------------------------------------
                //For generate model class
                if (currToken.getToken().equals(ASKeyword.IMPORT)) {
                    if (StringUtils.isNullOrEmpty(cls.getClassName())) {
                        cls.setClassName(getClassPath() + ReservedWords.MODEL);
                        cls.setOrgClassName(getClassPath());
                    }
                    prevTokenImport = currToken;
                    logger.debug("Found import keyword...");
                    //The current token is a class import
                    currToken = nextWord(src, index + 1, ASPattern.IMPORT[0], ASPattern.IMPORT[1]);
                    index = currToken.getIndex() - 1;
                    if (currToken.getToken() == null) {
                        throw new Error("Error parsing import.");
                    } else {
                        logger.debug("Parsed import name: " + currToken.getToken());
                        if (currToken.getToken().contains("*")) {
                            cls.getImportWildcards().add(currToken.getToken()); //To be resolved later
                        } else {
                            // cls.getImports().add(currToken.getToken()); //No need to resolve
                            // Remove CURLY_BRACE
                            String prevExtra = removeCurlyBrace(prevTokenImport.getExtra());
                            // Import token contains special characters as \n \r \t to format data
                            String importToken = prevExtra + prevTokenImport.getToken() + currToken.getExtra() + currToken.getToken();
                            importToken = importToken.replaceAll("\\t", "");
                            cls.getImports().add(importToken); //No need to resolve
                        }
                    }
                }
                //------------------------------------------------------
                else {
                    if (currToken.getToken() != null
                            && (currToken.getToken().equals(ASEncapsulation.PUBLIC)
                            || currToken.getToken().equals(ASEncapsulation.PRIVATE)
                            || currToken.getToken().equals(ASEncapsulation.PROTECTED))) {
                        currMember.setEncapsulation(currToken.getToken());
                        currMember.setComment(removeCurlyBrace(currToken.getExtra()));
                        logger.debug("->Member encapsulation set to " + currMember.getEncapsulation());
                    } else if (currToken.getToken() != null && currToken.getToken().equals(ASKeyword.STATIC)) {
                        currMember.setStatic(true);
                        logger.debug("-->Static flag set");
                    } else if (currToken.getToken() != null && (currToken.getToken().equals(ASMemberType.VAR)
                            || currToken.getToken().equals(ASMemberType.CONST))) {
                        currMember.setConst(currToken.getToken().equals(ASMemberType.CONST));
                        currMember = currMember.createVariable(); //Transform the member into a variable
                        stack.add(ASParseState.MEMBER_VARIABLE);
                        logger.info("Parsing class member variable start.................");
                        logger.debug("--->Member type \"variable\" set.");
                    } else if (currToken.getToken() != null && currToken.getToken().equals(ASMemberType.FUNCTION)) {
                        currToken = nextWord(src, index + 1, ASPattern.IDENTIFIER[0], ASPattern.IDENTIFIER[1]);
                        //Check for getter/setter
                        if (currToken.getToken() != null
                                && (currToken.getToken().equals(ASKeyword.GET) || currToken.getToken().equals(ASKeyword.SET))
                                && src.charAt(index + 1 + currToken.getToken().length() + 1) != '(') {
                            logger.debug("--->Member sub-type " + currToken.getToken() + " set.");
                            currMember.setSubType(currToken.getToken());
                            index = currToken.getIndex() - 1;
                        }
                        currMember = currMember.createFunction(); //Transform the member into a function
                        stack.add(ASParseState.MEMBER_FUNCTION);
                        logger.info("Parsing class member variable end.................");
                        logger.info("Parsing class member function start.................");
                        logger.debug("---->Member type function set.");
                    }
                }
            } else if (ASParseState.MEMBER_VARIABLE.equals(getState())) {
                currToken = nextWord(src, index, ASPattern.IDENTIFIER[0], ASPattern.IDENTIFIER[1]);
                currMember.setName(currToken.getToken()); //Set the member name
                logger.debug("---->Variable name declared: " + currToken.getToken());
                index = currToken.getIndex();
                if (src.charAt(index) == Constants.COLON_CHAR) {
                    currToken = nextWord(src, index, ASPattern.VARIABLE_TYPE[0], ASPattern.VARIABLE_TYPE[1]);
                    index = currToken.getIndex() - 1;
                    String type = currToken.getToken();
                    if (type.equals(cls.getClassName())) {
                        // Model type
                        type += ReservedWords.MODEL;
                    }
                    currMember.setType(type);//Set the value type name

                    logger.debug("---->Variable type for " + currMember.getName() + " declared as: " + currToken.getToken());
                }
                // Check arguments assign
                if (argumentCheck(src, index)) {
                    currToken = nextWord(src, index, ASPattern.ASSIGN_START[0], ASPattern.ASSIGN_START[1]);
                    if (currToken.getToken().equals(Constants.EQUAL_OPERATOR)) {
                        //Use all characters after self symbol to set value
                        index = currToken.getIndex();
                        tmpArr = extractUpTo(src, index, Constants.STATEMENT_END_PATTERN);
                        //Store value
                        currMember.setValue(tmpArr[0].trim());
                        index = Integer.parseInt(tmpArr[1]) - 1;
                        // Check initialize model variable
                        if (StringUtils.isMatched(Constants.MODEL_VARIABLE_PATTERN, currMember.getValue())) {
                            currMember.setValue(" new " + currMember.getType() + "()");
                        }
                        cls.getMembersWithAssignments().add(currMember);
                        logger.debug("---->Variable with assignment value: " + tmpArr[0]);
                    }
                }
                //Store and delete current member and exit
                cls.getMembers().add(currMember);
                cls.registerField(currMember.getName(), currMember);
                currMember = null;
                stack.pop();
            } else if (ASParseState.MEMBER_FUNCTION.equals(getState())) {
                //Parse the arguments
                currToken = nextWord(src, index, ASPattern.IDENTIFIER[0], ASPattern.IDENTIFIER[1]);
                index = currToken.getIndex();
                currMember.setName(currToken.getToken()); //Set the member name
                logger.debug("****>Function name declared: " + currToken.getToken());

                PREVIOUS_BLOCK = currMember.getName() + ":Function";
                tmpArr = extractBlock(src, index, "(", ")");
                index = Integer.parseInt(tmpArr[1]) - 1; //Ending index of parsed block
                tmpStr = tmpArr[0].trim(); //Parsed block
                if (tmpStr.length() - 1 < 1) {
                    tmpStr = "";
                } else {
                    tmpStr = tmpStr.substring(1, tmpStr.length() - 1); //Remove outer parentheses
                }
                ASFunction tmpFunction = null;
                tmpArr = null; //Trash this
                tmpArr = tmpStr.split(Constants.COMMA_STRING); //Split args by commas
                //Don't bother if there are no arguments
                if (tmpArr.length > 0 && !StringUtils.isNullOrEmpty(tmpArr[0])) {
                    //Truncate spaces and assign values to arguments as needed
                    for (String s : tmpArr) {
                        tmpStr = s;
                        //Grab the function name
                        tmpToken = nextWord(tmpStr, 0, ASPattern.VARIABLE[0], ASPattern.VARIABLE[1]); //Parse out the function name
                        if (currMember instanceof ASFunction) {
                            tmpFunction = (ASFunction) currMember;
                        }
                        tmpFunction.getArgList().add(new ASArgument());
                        if (tmpStr.indexOf("...") == 0) {
                            //This is a ...rest argument, stop here
                            tmpFunction.getArgList().get(tmpFunction.getArgList().size() - 1).setName(tmpStr.substring(3));
                            ((ASArgument) tmpFunction.getArgList().get(tmpFunction.getArgList().size() - 1)).setRestParam(true);
                            logger.debug("----->Parsed a ...rest param: " + tmpFunction.getArgList().get(tmpFunction.getArgList().size() - 1).getName());
                            break;
                        } else {
                            tmpFunction.getArgList().get(tmpFunction.getArgList().size() - 1).setName(tmpToken.getToken()); //Set the argument name
                            logger.debug("----->Function argument found: " + tmpToken.getToken());
                            //If a colon was next, we'll assume it was typed and grab it
                            if (tmpToken.getIndex() < tmpStr.length() && tmpStr.charAt(tmpToken.getIndex()) == ':') {
                                tmpToken = nextWord(tmpStr, tmpToken.getIndex(), ASPattern.VARIABLE_TYPE[0], ASPattern.VARIABLE_TYPE[1]); //Parse out the argument type
                                tmpFunction.getArgList().get(tmpFunction.getArgList().size() - 1).setType(tmpToken.getToken()); //Set the argument type
                                logger.debug("----->Function argument typed to: " + tmpToken.getToken());
                            }
                            tmpToken = nextWord(tmpStr, tmpToken.getIndex(), ASPattern.ASSIGN_START[0], ASPattern.ASSIGN_START[1]);
                            if (tmpToken.getToken() != null && tmpToken.getToken().equals("=")) {
                                //Use all characters after self symbol to set value
                                tmpToken = nextWord(tmpStr, tmpToken.getIndex(), ASPattern.ASSIGN_UPTO[0], ASPattern.ASSIGN_UPTO[1]);
                                if (tmpToken == null) {
                                    throw new Error("Error during variable assignment in arg" + tmpFunction.getArgList().get(tmpFunction.getArgList().size() - 1).getName());
                                }
                                //Store value
                                tmpFunction.getArgList().get(tmpFunction.getArgList().size() - 1).setValue(tmpToken.getToken().trim());
                                logger.debug("----->Function argument defaulted to: " + tmpToken.getToken().trim());
                            }
                        }
                    }
                }
                logger.debug("------>Completed paring args: " + ((ASFunction) currMember).getArgList().toString());
                //Type the function if needed
                if (src.charAt(index + 1) == Constants.COLON_CHAR) {
                    tmpToken = nextWord(src, index + 1, ASPattern.VARIABLE_TYPE[0], ASPattern.VARIABLE_TYPE[1]); //Parse out the function type if needed
                    index = tmpToken.getIndex();
                    currMember.setType(tmpToken.getToken());
                    logger.debug("------>Typed the function to: " + currMember.getType());
                }
                if (cls.isInterface()) {
                    //Store and delete current member and exit
                    currMember.setValue("{}");
                    if (currMember.getSubType().equals(ASKeyword.GET)) {
                        if ((currMember.isStatic())) {
                            cls.getStaticGetters().add(currMember);
                        } else {
                            cls.getGetters().add(currMember);
                        }
                    } else if (currMember.getSubType().equals(ASKeyword.SET)) {
                        if ((currMember.isStatic())) {
                            cls.getStaticSetters().add(currMember);
                        } else {
                            cls.getSetters().add(currMember);
                        }
                    } else if (currMember.isStatic()) {
                        cls.getStaticMembers().add(currMember);
                    } else {
                        cls.getMembers().add(currMember);
                    }
                    cls.registerField(currMember.getName(), currMember);
                    //Done parsing function
                    currMember = null;
                    stack.pop();
                } else {
                    //Save the function body
                    PREVIOUS_BLOCK = currMember.getName() + ":Function";
                    tmpArr = extractBlock(src, index, null, null);
                    index = Integer.parseInt(tmpArr[1]);
                    currMember.setValue(tmpArr[0]);

                    //Store and delete current member and exit
                    if (currMember.getSubType() != null && currMember.getSubType().equals(ASKeyword.GET)) {
                        if ((currMember.isStatic())) {
                            cls.getStaticGetters().add(currMember);
                        } else {
                            cls.getGetters().add(currMember);
                        }
                    } else if (currMember.getSubType() != null && currMember.getSubType().equals(ASKeyword.SET)) {
                        if ((currMember.isStatic())) {
                            cls.getStaticSetters().add(currMember);
                        } else {
                            cls.getSetters().add(currMember);
                        }
                    } else if (currMember.isStatic()) {
                        cls.getStaticMembers().add(currMember);
                    } else {
                        cls.getMembers().add(currMember);
                    }
                    cls.registerField(currMember.getName(), currMember);

                    currMember = null;
                    stack.pop();
                }
            }
        }
        logger.info("Parsing class end.................");
    }


    /**
     * Get a token word from source content, start at index with pattern and characters word detect.
     */
    private ASToken nextWord(String src, int index, String characters, String pattern) {
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
            if (src.length() > index + 2) { // && (c == '*' || c == '/')
                tmpStrComment = src.substring(index, index + 2);
            }
            if (String.valueOf(c).matches(characters) || ((tokenBuffer + c).matches(characters))) {
                tokenBuffer = !StringUtils.isNullOrEmpty(tokenBuffer) ? tokenBuffer + c : String.valueOf(c); // Create new token buffer if
                // needed, otherwise append
            } else if (innerState == null && ASParser.checkForCommentOpen(tmpStrComment) != null
                    && tokenBuffer == null) {
                logger.debug("Entering comment...");
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
                        logger.debug("Exiting comment...");
                        break;
                    } else {
                        extraBuffer += src.charAt(index);
                    }
                }
            } else if (innerState == null && ASParser.checkForStringOpen(String.valueOf(src.charAt(index))) != null
                    && tokenBuffer == null) {
                logger.debug("Entering string...");
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
                        logger.debug("Exiting string...");
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

    /**
     * Extract block code start with open and close brace
     */
    private String[] extractBlock(String text, int start, String opening, String closing) {
        opening = StringUtils.getDefaultValueString(opening, "{");
        closing = StringUtils.getDefaultValueString(closing, "}");
        String buffer = "";
        int i = start;
        int count = 0;
        boolean started = false;
        String insideString = null;
        String insideComment = null;
        boolean escapingChar = false;
        String indentTab = "";
        while (!(count == 0 && started) && i < text.length()) {
            if (insideComment != null) {
                // Inside of a comment, wait until we get out
                if (insideComment.equals("//") && (text.charAt(i) == '\n' || text.charAt(i) == '\r')) {
                    insideComment = null; // End inline comment
                    logger.debug("Exited comment");
                } else if (insideComment.equals("/*") && text.charAt(i) == '*' && i + 1 < text.length()
                        && text.charAt(i + 1) == '/') {
                    insideComment = null; // End multiline comment
                    logger.debug("Exited comment");
                }
            } else if (insideString != null) {
                // Inside of a string, wait until we get out
                if (!escapingChar && text.charAt(i) == '\\') {
                    escapingChar = true; // Start escape sequence
                } else if (!escapingChar && String.valueOf(text.charAt(i)).equals(insideString)) {
                    insideString = null; // Found closing quote
                } else {
                    escapingChar = false; // Forget escape sequence
                }
            } else if (text.charAt(i) == opening.charAt(0)) {
                started = true;
                //First opening char, add tab indent
                if (count == 0) {
                    buffer += indentTab.replaceAll("^\\)", "");
                }
                count++; // Found opening
            } else if (text.charAt(i) == closing.charAt(0)) {
                count--; // Found closing
            } else if ((text.charAt(i) == '\"' || text.charAt(i) == '\'')) {
                insideString = String.valueOf(text.charAt(i)); // Now inside of a string
            } else if (text.charAt(i) == '/' && i + 1 < text.length() && text.charAt(i + 1) == '/') {
                logger.debug("Entering comment... " + "(//)");
                insideComment = "//";
            } else if (text.charAt(i) == '/' && i + 1 < text.length() && text.charAt(i + 1) == '*') {
                logger.debug("Entering comment..." + "(/*)");
                insideComment = "/*";
            }
            if (started) {
                buffer += text.charAt(i);
            }
            if (count == 0) indentTab += text.charAt(i);
            i++;
        }
        if (!started) {
            throw new Error("Error, no starting '" + opening + "' found for method body while parsing "
                    + PREVIOUS_BLOCK);
        } else if (count > 0) {
            throw new Error("Error, no closing '" + closing + "' found for method body while parsing "
                    + PREVIOUS_BLOCK);
        } else if (count < 0) {
            throw new Error("Error, malformed enclosing '" + opening + closing + " body while parsing "
                    + PREVIOUS_BLOCK);
        }
        return new String[]{buffer, String.valueOf(i)};
    }

    /**
     * Extract text from start index to target string found.
     */
    private String[] extractUpTo(String text, int start, String target) {
        String buffer = "";
        int i = start;
        String insideString = null;
        String insideComment = null;
        boolean escapingChar = false;
        Pattern pattern = Pattern.compile(target);
        while (i < text.length()) {
            if (insideComment != null) {
                // Inside of a comment, wait until we get out
                if (insideComment.equals("//")
                        && (text.charAt(i) == '\n'
                        || text.charAt(i) == '\r')) {
                    insideComment = null; // End inline comment
                    logger.debug("Exited comment");
                } else if (insideComment.equals("/*") && text.charAt(i) == '*' && i + 1 < text.length()
                        && text.charAt(i + 1) == '/') {
                    insideComment = null; // End multiline comment
                    logger.debug("Exited comment");
                }
            } else if (insideString != null) {
                // Inside of a string, wait until we get out
                if (!escapingChar && text.charAt(i) == '\\') {
                    escapingChar = true; // Start escape sequence
                } else if (!escapingChar && String.valueOf(text.charAt(i)).equals(insideString)) {
                    insideString = null; // Found closing quote
                } else {
                    escapingChar = false; // Forget escape sequence
                }
            } else if ((text.charAt(i) == '\"' || text.charAt(i) == '\'')) {
                insideString = String.valueOf(text.charAt(i)); // Now inside of a string
            } else if (text.charAt(i) == '/' && i + 1 < text.length() && text.charAt(i + 1) == '/') {
                logger.debug("Entering comment... " + "(//)");
                insideComment = "//";
            } else if (text.charAt(i) == '/' && i + 1 < text.length() && text.charAt(i + 1) == '*') {
                logger.debug("Entering comment..." + "(/*)");
                insideComment = "/*";
            } else if (pattern.matcher(String.valueOf(text.charAt(i))).matches()) {
                break; // Done
            }
            buffer += text.charAt(i);
            i++;
        }
        return new String[]{buffer, String.valueOf(i)};
    }

    public String[] parseFunc(ASClass cls, String fnText, List<ASVariable> stack) {
        int index;
        String result = "";
        ASMember tmpMember;
        ASClass tmpClass;
        ASMember tmpField;
        ASToken prevToken;
        ASToken currToken = null;
        String[] tmpParse;
        boolean tmpStatic;
        Peek tmpPeek;
        String objBuffer; //Tracks the current object that is being "pathed" (e.g. "object.field1" or "object.field1[index + 1]", etc)
        boolean justCreatedVar = false; //Keeps track if we just started a var statement (to help test if we're setting a type))

        for (index = 0; index < fnText.length(); index++) {
            objBuffer = "";
            prevToken = currToken;
            currToken = nextWord(fnText, index, ASPattern.VARIABLE[0], ASPattern.VARIABLE[1]);
            result += currToken.getExtra(); //<-Puts all other non-identifier characters into the buffer first
            tmpMember = ASParser.checkStack(stack, currToken.getToken()); //<-Check the stack for a member with this identifier already
            index = currToken.getIndex();
            if (!StringUtils.isNullOrEmpty(currToken.getToken())) {
                if (currToken.getToken().equals("this")) {
                    //No need to perform any extra checks on the subsequent token
                    tmpStatic = false;
                    tmpClass = cls;
                    objBuffer += currToken.getToken();
                    result += currToken.getToken();
                } else {
                    if (cls.getClassMap().get(currToken.getToken()) != null
                            && cls.getParentDefinition() != cls.getClassMap().get(currToken.getToken())
                            && !(justCreatedVar && currToken.getExtra().matches(":\\s*"))) {
                        // If this is a token that matches a class from a potential import statement, store it in the filtered classMap
                        cls.getClassMapFiltered().put(currToken.getToken(), cls.getClassMap().get(currToken.getToken()));
                    }
                    tmpStatic = (cls.getOrgClassName().equals(currToken.getToken())
                            || cls.retrieveField(currToken.getToken(), true) != null);

                    //Find field in class, then make sure we didn't already have a local member defined with this name, and skip next block if static since the definition is the class itself
                    //Note: tmpMember needs to be checked, if something is in there it means we have a variable with the same name in local scope
                    if (cls.retrieveField(currToken.getToken(), tmpStatic) != null
                            && !cls.getClassName().equals(currToken.getToken())
                            && tmpMember == null && !(prevToken != null
                            && prevToken.getToken().equals(ReservedWords.VAR))) {
                        tmpMember = cls.retrieveField(currToken.getToken(), tmpStatic); //<-Reconciles the type of the current variable
                        {
                            if (tmpStatic) {
                                objBuffer += (cls.getClassName().equals(currToken.getToken())) ? currToken.getToken() : cls.getClassName() + "." + currToken.getToken();
                                result += (cls.getClassName().equals(currToken.getToken())) ? currToken.getToken() : cls.getClassName() + "." + currToken.getToken();
                            } else {
                                objBuffer += currToken.getToken();
                                result += currToken.getToken();
                            }
                        }
                    } else {
                        //TODO
                        // Check Object type is MODEL class following patter: MGNNNNNNN_NN_NNN then convert it to
                        // MGNNNNNNN_NN_NNN + MODEL class
                        //Likely a local variable, argument, or static reference
                        objBuffer += currToken.getToken();
                        // Check model variable
                        if (prevToken != null
                                && ReservedWords.AS.equals(prevToken.getToken())
                                && currToken.getToken().equals(cls.getClassName())) {
                            result = result.substring(0, result.indexOf(Constants.EQUAL_OPERATOR) + 1 + 1); //Extra space
                            result += "new " + currToken.getToken() + ReservedWords.MODEL + "()";
                            index = currToken.getIndex();
                        } else if (prevToken != null
                                && ReservedWords.NEW.equals(prevToken.getToken())
                                && fnText.charAt(index) == Constants.SEMICOLON_CHAR) {
                            result += currToken.getToken() + "()";
                        } else if (StringUtils.isMatched(Constants.CLASS_NAME_PATTERN, currToken.getToken())
                                && currToken.getExtra().trim().contains(Constants.EQUAL_OPERATOR))
                        {
                            result += ReservedWords.NEW + " " + currToken.getToken();
                        }
                        else {
                            result += currToken.getToken();
                        }
                    }
                    if (tmpStatic) {
                        //Just use the class itself, we will reference fields from it. If parser injected the static prefix manually, we'll try to determome the type of var instead
                        tmpClass = (cls.getClassName().equals(currToken.getToken())) ? cls : (tmpMember != null) ? cls.getClassMap().get(tmpMember.getType()) : null;
                    } else {
                        //Use the member's type to determine the class it's mapped to
                        tmpClass = (tmpMember != null && tmpMember.getType() != null && !tmpMember.getType().equals("*")) ? cls.getClassMap().get(tmpMember.getType()) : null;
                        //If no mapping was found, this may be a static reference
                        if (tmpClass == null && cls.getClassMap().get(currToken.getToken()) != null) {
                            tmpClass = cls.getClassMap().get(currToken.getToken());
                            tmpStatic = true;
                        }
                    }
                    //If tmpClass is null, it's possible we were trying to retrieve a Vector type. Let's fix this:
                    if (tmpClass == null && tmpMember != null && tmpMember.getType() != null
                            && !tmpMember.getType().replace("/Vector\\.<(.*?)>/g", "$1").equals(tmpMember.getType())) {
                        //Extract Vector type if necessary by testing regex
                        tmpClass = cls.getClassMap().get(tmpMember.getType().replace("/Vector\\.<(.*?)>/g", "$1"));
                    }
                }
                //We have parsed the current token, and the index sits at the next level down in the object
                for (; index < fnText.length(); index++) {
                    //Loop until we stop parsing a variable declaration
                    if (fnText.charAt(index) == '.') {
                        boolean parsingVector = (prevToken != null && prevToken.getToken().equals("new") && currToken.getToken().equals("Vector"));
                        prevToken = currToken;
                        if (parsingVector) {
                            //We need to allow asterisk
                            currToken = nextWord(fnText, index, ASPattern.VARIABLE_TYPE[0], ASPattern.VARIABLE_TYPE[1]);
                        } else {
                            currToken = nextWord(fnText, index, ASPattern.VARIABLE[0], ASPattern.VARIABLE[1]);
                        }
                        //Check Datetime type
                        if (isDateTimeType(cls, objBuffer)) {
                            // Get '=' token
                            ASToken tmpToken = nextWord(fnText, index, ASPattern.VARIABLE[0], ASPattern.VARIABLE[1]);
                            if (tmpToken.getToken().equals(ASKeyword.FORMATSTRING_METHOD)) {
                                index = tmpToken.getIndex();
                                // Get value token
                                currToken = nextWord(fnText, index, ASPattern.VARIABLE[0], ASPattern.VARIABLE[1]);
                                index = currToken.getIndex();
                                // Convert datetime
                                if (fnText.charAt(index) == '"') {
                                    result += " = new SimpleDateFormat(\"" + currToken.getToken() + "\")";
                                    index++;
                                } else {
                                    result += " = new SimpleDateFormat(" + currToken.getExtra() + ")";
                                }
                                index--;
                                break;
                            } else {
                                result += currToken.getExtra();
                                result += currToken.getToken();
                                index = currToken.getIndex();
                            }
                        } else {
                            result += currToken.getExtra(); //<-Puts all other non-identifier characters into the buffer first
                            index = currToken.getIndex();
                            if (tmpClass != null) {
                                //This means we are coming from a typed variable
                                tmpField = tmpClass.retrieveField(currToken.getToken(), tmpStatic);
                                if (tmpField != null) {
                                    logger.debug("parsing: " + tmpField.getName() + ":" + tmpField.getType());
                                    //We found a field that matched this value within the class
                                    if (tmpField instanceof ASFunction) {
                                        if (tmpField.getSubType() != null && (tmpField.getSubType().equals("get") || tmpField.getSubType().equals("set"))) {
                                            tmpPeek = lookAhead(fnText, index);
                                            if (tmpPeek != null) {
                                                //Handle differently if we are assigning a setter
                                                objBuffer += ".get_" + currToken.getToken() + "()";
                                                result += "set_" + currToken.getToken() + "(";
                                                index = tmpPeek.getEndIndex();
                                                if (tmpPeek.getToken().equals("++")) {
                                                    result += objBuffer + " + 1";
                                                } else if (tmpPeek.getToken().equals("--")) {
                                                    result += objBuffer + " - 1";
                                                } else {
                                                    tmpParse = parseFunc(cls, tmpPeek.getExtracted(), stack); //Recurse into the assignment to parse vars
                                                    if (tmpPeek.getToken().equals("=")) {
                                                        result += tmpParse[0].trim();
                                                    } else {
                                                        result += objBuffer + " " + tmpPeek.getToken().charAt(0) + " (" + tmpParse[0] + ")";
                                                    }
                                                }
                                                result += ")";
                                            } else {
                                                objBuffer += ".get_" + currToken.getToken() + "()";
                                                result += "get_" + currToken.getToken() + "()";
                                            }
                                            //console.log("set get flag: " + currToken.getToken());
                                        } else {
                                            objBuffer += "." + currToken.getToken();
                                            result += currToken.getToken();
                                        }
                                    } else {
                                        objBuffer += "." + currToken.getToken();
                                        result += currToken.getToken();
                                    }
                                } else {
                                    objBuffer += "." + currToken.getToken();
                                    result += currToken.getToken();
                                    logger.debug("appened typed: " + currToken.getToken());
                                }
                                //Update the type if this is not a static prop
                                if (tmpClass != null && tmpField != null && tmpField.getType() != null && !tmpField.getType().equals("*")) {
                                    //Extract Vector type if necessary by testing regex
                                    tmpClass = (!tmpField.getType().replaceFirst("Vector\\.<(.*?)>", "$1").equals(tmpField.getType()))
                                            ? tmpClass.getClassMap().get(tmpField.getType().replaceFirst("Vector\\.<(.*?)>", "$1"))
                                            : tmpClass.getClassMap().get(tmpField.getType());
                                } else {
                                    tmpClass = null;
                                }
                            } else {
                                logger.debug("appened untyped: " + currToken.getToken());
                                objBuffer += "." + currToken.getToken();
                                result += currToken.getToken();
                            }
                        }
                    } else if (fnText.charAt(index) == '[') {
                        //We now have to recursively parse the inside of this open bracket
                        tmpParse = extractBlock(fnText, index, "[", "]");
                        index = Integer.parseInt(tmpParse[1]);
                        tmpParse = parseFunc(cls, tmpParse[0], stack); //Recurse into the portion that was extracted
                        logger.debug("recursed into: " + tmpParse[0]);
                        objBuffer += tmpParse[0]; //Append this text to the object buffer string so we can remember the variable we have accessed
                        result += tmpParse[0];
                    }
                    tmpStatic = false; //Static can no longer be possible after the second field
                    if (!String.valueOf(fnText.charAt(index)).matches("[.\\[]")) {
                        objBuffer = ""; //Clear out the current object buffer
                        index--;
                        break;
                    }
                    index--;
                }
            } else {
                index = currToken.getIndex() - 1;
            }
        }
        //Now cleanup variable types
        // Convert catch statement
        result = result.replaceAll(Constants.CATCH_PATTERN, "$2Exception $3$6");
        // Convert log error method
        result = result.replaceAll(Constants.TRACE_ERROR_PATTERN, ReservedWords.WRITEERRORLOG + "$3$5$6");
        result = result.replaceAll(Constants.ACC_LOG_ERROR_PATTERN, ReservedWords.WRITEERRORLOG + "$3");
        // Convert log info method
        result = result.replaceAll(Constants.TRACE_INFO_PATTERN, ReservedWords.WRITEINFOLOG + "$3$4");
        // Convert 「.text」不要
        result = result.replaceAll(Constants.TEXT_PROP_PATTERN, "$3$4");
        result = result.replaceAll(Constants.TEXT_PROP_END_PATTERN, "");
        // Narrow function
        result = result.replaceAll(Constants.NARROW_FUNCTION_PATTERN, "$3 ->");
        // Parse function
        result = result.replaceAll(Constants.PARSE_FNC_PATTERN, "Integer.parseInt$3");
        // Parse int function
        result = result.replaceAll(Constants.PARSE_INT_PATTERN, "Integer.parseInt$3$4$5");
        // Initialize for statement
        result = result.replaceAll(Constants.INIT_FOR_PATTERN, "$2$3 = $3$4");
        // convert variable
        result = result.replaceAll(Constants.FUNC_VARIABLE_PATTERN, "$6$3");
        return new String[]{result, String.valueOf(index)};
    }

    private Peek lookAhead(String str, int index) {
        //Look ahead in the function for assignments
        int startIndex = -1;
        int endIndex = -1;
        int semicolonIndex = -1;
        String token = "";
        String extracted = "";
        //Not a setter if there is a dot operator immediately after
        if (str.charAt(index) == Constants.DOT_CHAR) {
            return new Peek(null, "", startIndex, endIndex);
        }
        for (; index < str.length(); index++) {
            if (String.valueOf(str.charAt(index)).matches("[+-\\/=*]")) {
                //Append to the assignment instruction
                token += str.charAt(index);
                startIndex = index;
            } else if (startIndex < 0 && String.valueOf(str.charAt(index)).matches("[\\t\\s]")) { //Skip these characters
                continue;
            } else {
                break; //Exits when token has already been started and no more regexes pass
            }
        }
        //Only allow these patterns
        if (!(token.equals("=") || token.equals("++") || token.equals("--") || token.equals("+=") || token.equals("-=") || token.equals("*=") || token.equals("/="))) {
            token = null;
        }

        if (token != null) {
            //Pick whatever is closer, new line or semicolon
            endIndex = str.indexOf('\n', startIndex);
            if (endIndex < 0) {
                endIndex = str.length() - 1;
            }
            //Windows fix
            if (str.charAt(endIndex - 1) == '\r') {
                endIndex--;
            }
            //We want to place closing parens before semicolon if it exists
            semicolonIndex = str.indexOf(";", startIndex);
            if (semicolonIndex < endIndex) {
                endIndex = semicolonIndex;
            }
            extracted = str.substring(startIndex + token.length(), endIndex);
        }

        return new Peek(token, extracted, startIndex, endIndex);
    }

    /**
     * Check datetime type
     */
    private static boolean isDateTimeType(ASClass cls, String variableName) {
        ASMember m = cls.retrieveField(variableName, false);
        return m != null ? m.getType().equals(ASKeyword.DATEFORMATTER) : false;
    }

    private static String getListArgument(List<ASArgument> tmpArgs) {
        List<String> arr = new ArrayList<>();
        for (int i = 0; i < tmpArgs.size(); i++) {
            if (tmpArgs.get(i).equals("...rest")) {
                break;
            }
            arr.add(tmpArgs.get(i).getName());
        }
        String str = String.join(", ", arr);
        return str;
    }

    /**
     * Parser Arguments
     */
    private ArrayList<ASArgument> parseArguments(String str) {
        ArrayList<ASArgument> args = new ArrayList<ASArgument>();
        ASToken tmpToken;
        String[] tmpArr = extractBlock(str, 0, "(", ")");
        String[] tmpExtractArr = null;
        // Ending index of parsed block
        String tmpStr = tmpArr[0].trim(); // Parsed block
        if (tmpStr.length() - 2 < 1) {
            tmpStr = "";
        } else {
            tmpStr = tmpStr.substring(1, tmpStr.length() - 2); // Remove outer parentheses
        }
        tmpArr = null; // Trash this
        tmpArr = tmpStr.split(Constants.COMMA_STRING); // Split args by commas
        // Don't bother if there are no arguments
        if (tmpArr.length > 0 && !StringUtils.isNullOrEmpty(tmpArr[0])) {
            // Truncate spaces and assign values to arguments as needed
            for (int i = 0; i < tmpArr.length; i++) {
                tmpStr = tmpArr[i].trim();
                args.add(new ASArgument());
                if (tmpStr.indexOf(Constants.ELLIPSIS_POINTS_STRING) == 0) {
                    // This is a ...rest argument, stop here
                    args.get(args.size() - 1).setName(tmpStr.substring(3));
                    args.get(args.size() - 1).setRestParam(true);
                    logger.debug("----->Parsed a ...rest param: " + args.get(args.size() - 1).getName());
                    break;
                } else {
                    // Grab the function name
                    tmpToken = nextWord(tmpStr, 0, ASPattern.VARIABLE[0], ASPattern.VARIABLE[1]); // Parse
                    // out the
                    // function
                    // name
                    args.get(args.size() - 1).setName(tmpToken.getToken()); // Set the argument name
                    logger.debug("----->Sub-Function argument found: " + tmpToken.getToken());
                    // If a colon was next, we'll assume it was typed and grab it
                    if (tmpToken.getIndex() < tmpStr.length() && tmpStr.charAt(tmpToken.getIndex()) == Constants.COLON_CHAR) {
                        tmpToken = nextWord(tmpStr, tmpToken.getIndex(), ASPattern.VARIABLE_TYPE[0],
                                ASPattern.VARIABLE_TYPE[1]); // Parse out the argument type
                        args.get(args.size() - 1).setType(tmpToken.getToken()); // Set the argument type
                        logger.debug("----->Sub-Function argument typed to: " + tmpToken.getToken());
                    }
                    tmpToken = nextWord(tmpStr, tmpToken.getIndex(), ASPattern.ASSIGN_START[0],
                            ASPattern.ASSIGN_START[1]);
                    if (Constants.EQUAL_OPERATOR.equals(tmpToken.getToken())) {
                        // Use all characters after self symbol to set value
                        tmpExtractArr = extractUpTo(tmpStr, tmpToken.getIndex(), Constants.STATEMENT_END_PATTERN);
                        // Store value
                        args.get(args.size() - 1).setValue(tmpExtractArr[0].trim());
                        // Store value
                        logger.debug("----->Sub-Function argument defaulted to: " + tmpExtractArr[0].trim());
                    }
                }
            }
        }
        return args;
    }

    /**
     * Remove (.) char
     */
    private String fixClassPath(String clsPath) {
        // Class paths at the root level might accidentally be prepended with a "."
        return clsPath.replaceFirst("^\\.", "");
    }

    /**
     * Get stack by stack name
     */
    public static ASMember checkStack(List<ASVariable> stack, String name) {
        if (name == null) {
            return null;
        }
        for (int i = stack.size() - 1; i >= 0; i--) {
            if (stack.get(i).getName().equals(name)) {
                return stack.get(i);
            }
        }
        return null;
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
        if ("//".equals(str)) {
            return ASParseState.COMMENT_INLINE;
        }
        if ("/*".equals(str)) {
            return ASParseState.COMMENT_MULTILINE;
        }
        return null;
        // return ("//".equals(str)) ? ASParseState.COMMENT_INLINE : ("/*".equals(str)) ? ASParseState.COMMENT_MULTILINE : null;
    }

    /**
     * Check string is comment line(\n | \r | blank(0)) or multiline( * /) close
     */
    public static boolean checkForCommentClose(String state, String str) {
        return (state == ASParseState.COMMENT_INLINE
                && (str.charAt(0) == '\n' || str.charAt(0) == '\r' || str.charAt(0) == 0)) ? true
                : (state == ASParseState.COMMENT_MULTILINE && "*/".equals(str)) ? true : false;
    }

    /**
     * Check for open string (")
     */
    public static String checkForStringOpen(String str) {
        return (str == "\"") ? ASParseState.STRING_DOUBLE_QUOTE
                : (str == "'") ? ASParseState.STRING_SINGLE_QUOTE : null;
    }

    /**
     * Check for close string(")
     */
    public static boolean checkForStringClose(String state, String str) {
        return (state == ASParseState.STRING_DOUBLE_QUOTE && str == "\"") ? true
                : (state == ASParseState.STRING_SINGLE_QUOTE && str == "'") ? true : false;
    }

    /**
     * Remove curly brace '{'
     */
    private String removeCurlyBrace(String value) {
        if (StringUtils.isNullOrEmpty(value)) return "";
        int braceIndex = value.indexOf(Constants.CURLY_BRACE);
        if (braceIndex != -1) {
            return value.substring(braceIndex + 1);
        }
        return value;
    }

    private boolean argumentCheck(String src, int startIndex) {
        int endIndex = src.length();
        if (endIndex - startIndex > 10) {
            endIndex = 10;
        }
        for (int i = startIndex; i < startIndex + endIndex; i++) {
            if (src.charAt(i) == '=') {
                return true;
            }
        }
        return false;
    }

    /**
     * Remove '_AS' class suffix
     * ex: MG1001001_01_000_AS => MG1001001_01_000
     */
    private String removeSuffixAS(String value) {
        if (StringUtils.isNullOrEmpty(value)) {
            return value;
        }
        if (value.length() > 3 && value.endsWith(ASKeyword.ASSUFFIX)) {
            return value.substring(0, value.length() - 3);
        }
        return value;
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

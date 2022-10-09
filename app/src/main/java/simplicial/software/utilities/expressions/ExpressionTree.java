package simplicial.software.utilities.expressions;

import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ExpressionTree {
    private ExpressionNode root;
    private Map<String, MutableDouble> variables;
    private List<String> validSymbols;

    public void parse(String expression, List<String> validSymbols) {
        this.validSymbols = validSymbols;
        variables = new HashMap<>();
        root = parseInternal(expression);
    }

    private ExpressionNode parseInternal(String expression) {
        if (expression == null)
            return new OperandExpression(new MutableDouble(0));
        expression = expression.trim();
        int parenDepth = 0;
        while (expression.length() > 0 && expression.charAt(0) == '(' && expression.charAt(expression.length() - 1) == ')') {
            boolean matchedOuter = true;
            for (int i = 0; i < expression.length(); i++) {
                char currentChar = expression.charAt(i);
                if (currentChar == '(')
                    parenDepth++;
                else if (currentChar == ')') {
                    parenDepth--;
                    if (parenDepth == 0 && i != expression.length() - 1) {
                        matchedOuter = false;
                        break;
                    }
                }
            }
            if (!matchedOuter)
                break;
            expression = expression.substring(1, expression.length() - 1).trim();
        }

        SymbolType lastSymbolType = SymbolType.NONE;
        parenDepth = 0;
        for (int i = 0; i < expression.length(); i++) {
            char currentChar = expression.charAt(i);

            if (currentChar == '(')
                parenDepth++;
            else if (currentChar == ')')
                parenDepth--;
            else if (parenDepth == 0 && currentChar == '-' && lastSymbolType != SymbolType.OPERAND) {
                String pre = expression.substring(0, i);
                String post = expression.substring(i);
                expression = pre + '0' + post;
                i++;
            }

            switch (currentChar) {
                case '+':
                case '-':
                case '*':
                case '/':
                case '^':
                    lastSymbolType = SymbolType.OPERATOR;
                    break;
                case ' ':
                case '(':
                case ')':
                    break;
                default:
                    lastSymbolType = SymbolType.OPERAND;
                    break;
            }
        }
        if (parenDepth != 0) {
            throw new InvalidParameterException("Unbalanced parentheses: " + expression);
        }

        for (int i = expression.length() - 1; i >= 0; i--) {
            char currentChar = expression.charAt(i);

            if (currentChar == ')')
                parenDepth++;
            else if (currentChar == '(')
                parenDepth--;
            else if (parenDepth == 0 && (currentChar == '+' || currentChar == '-'))
                return new OperatorExpression(currentChar == '+' ? OperatorType.ADD :
                        OperatorType.SUBTRACT, parseInternal(expression.substring(0, i)),
                        parseInternal(expression.substring(i + 1)));
        }

        for (int i = expression.length() - 1; i >= 0; i--) {
            char currentChar = expression.charAt(i);

            if (currentChar == ')')
                parenDepth++;
            else if (currentChar == '(')
                parenDepth--;
            else if (parenDepth == 0 && (currentChar == '*' || currentChar == '/'))
                return new OperatorExpression(currentChar == '*' ? OperatorType.MULTIPLY :
                        OperatorType.DIVIDE, parseInternal(expression.substring(0, i)),
                        parseInternal(expression.substring(i + 1)));
        }

        for (int i = expression.length() - 1; i >= 0; i--) {
            char currentChar = expression.charAt(i);

            if (currentChar == ')')
                parenDepth++;
            else if (currentChar == '(')
                parenDepth--;
            else if (parenDepth == 0 && currentChar == '^')
                return new OperatorExpression(OperatorType.EXPONENTIATE,
                        parseInternal(expression.substring(0, i)),
                        parseInternal(expression.substring(i + 1
                )));
        }

        int indexSin = 0;
        int indexCos = 0;
        int indexASin = 0;
        int indexACos = 0;
        int indexAbs = 0;
        for (int i = 0; i < expression.length(); i++) {
            char currentChar = expression.charAt(i);

            if (currentChar == '(')
                parenDepth++;
            else if (currentChar == ')')
                parenDepth--;

            if (parenDepth == 0) {
                String sin = "sin";
                if (currentChar == sin.charAt(indexSin))
                    indexSin++;
                else indexSin = 0;

                String cos = "cos";
                if (currentChar == cos.charAt(indexCos))
                    indexCos++;
                else indexCos = 0;

                String asin = "asin";
                if (currentChar == asin.charAt(indexASin))
                    indexASin++;
                else indexASin = 0;

                String acos = "acos";
                if (currentChar == acos.charAt(indexACos))
                    indexACos++;
                else indexACos = 0;

                String abs = "abs";
                if (currentChar == abs.charAt(indexAbs))
                    indexAbs++;
                else indexAbs = 0;

                FunctionType functionType = FunctionType.NONE;
                if (indexASin == asin.length()) {
                    functionType = FunctionType.ASIN;
                } else if (indexACos == acos.length()) {
                    functionType = FunctionType.ACOS;
                } else if (indexSin == sin.length()) {
                    functionType = FunctionType.SIN;
                } else if (indexCos == cos.length()) {
                    functionType = FunctionType.COS;
                } else if (indexAbs == abs.length()) {
                    functionType = FunctionType.ABS;
                }

                if (functionType != FunctionType.NONE) {
                    i++;
                    int startIndex = i;
                    boolean latched = false;
                    for (; i < expression.length(); i++) {
                        currentChar = expression.charAt(i);

                        if (!latched) {
                            if (currentChar == '(') {
                                startIndex = i;
                                parenDepth++;
                                latched = true;
                            } else if (currentChar != ' ') {
                                throw new InvalidParameterException("Failed to parse function " +
                                        "argument.");
                            }
                        } else {
                            if (currentChar == '(')
                                parenDepth++;
                            else if (currentChar == ')') {
                                parenDepth--;
                                if (parenDepth == 0) {
                                    return new FunctionExpression(functionType,
                                            parseInternal(expression.substring(startIndex, i + 1)));
                                }
                            }
                        }
                    }

                    throw new InvalidParameterException("Failed to parse function argument.");
                }
            } else {
                indexSin = 0;
                indexCos = 0;
                indexASin = 0;
                indexACos = 0;
                indexAbs = 0;
            }
        }

        MutableDouble value = new MutableDouble(0);
        if (expression.length() > 0) {
            try {
                value.setValue(Double.parseDouble(expression));
            } catch (NumberFormatException e) {
                if (validSymbols != null) {
                    boolean valid = false;
                    for (int i = 0; i < validSymbols.size(); i++) {
                        if (expression.compareTo(validSymbols.get(i)) == 0) {
                            valid = true;
                            break;
                        }
                    }
                    if (!valid)
                        throw new InvalidParameterException("Failed to parse: " + expression);
                }

                if (variables.containsKey(expression)) {
                    value = variables.get(expression);
                } else {
                    variables.put(expression, value);
                }
            }
        } else {
            throw new InvalidParameterException("Failed to parse.");
        }

        return new OperandExpression(value);
    }

    public double evaluate(List<Variable> localVariables) {
        if (root == null)
            return 0;
        for (int i = 0; i < localVariables.size(); i++) {
            Variable localVariable = Objects.requireNonNull(localVariables.get(i));
            MutableDouble value = variables.get(localVariable.name);
            if (value != null)
                value.setValue(localVariable.value);
        }
        return root.evaluate();
    }
}

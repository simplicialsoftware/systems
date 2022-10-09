package simplicial.software.utilities.expressions;

public class OperatorExpression extends ExpressionNode {
    private final OperatorType type;
    private final ExpressionNode left;
    private final ExpressionNode right;

    public OperatorExpression(OperatorType operatorType, ExpressionNode left,
                              ExpressionNode right) {
        this.type = operatorType;
        this.left = left;
        this.right = right;
    }

    @Override
    public double evaluate() {
        switch (type) {
            case ADD:
                return left.evaluate() + right.evaluate();
            case SUBTRACT:
                return left.evaluate() - right.evaluate();
            case MULTIPLY:
                return left.evaluate() * right.evaluate();
            case DIVIDE:
                return left.evaluate() / right.evaluate();
            case EXPONENTIATE:
                return Math.pow(left.evaluate(), right.evaluate());
            default:
                return 0;
        }
    }
}

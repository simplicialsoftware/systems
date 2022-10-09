package simplicial.software.utilities.expressions;

public class FunctionExpression extends ExpressionNode {
    private final FunctionType type;
    private final ExpressionNode parameter;

    public FunctionExpression(FunctionType functionType, ExpressionNode parameter) {
        this.type = functionType;
        this.parameter = parameter;
    }

    @Override
    public double evaluate() {
        switch (type) {
            case ACOS:
                return Math.acos(parameter.evaluate());
            case ASIN:
                return Math.asin(parameter.evaluate());
            case SIN:
                return Math.sin(parameter.evaluate());
            case COS:
                return Math.cos(parameter.evaluate());
            case ABS:
                return Math.abs(parameter.evaluate());
            default:
                return parameter.evaluate();
        }
    }
}

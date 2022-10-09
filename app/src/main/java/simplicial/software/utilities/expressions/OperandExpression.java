package simplicial.software.utilities.expressions;

public class OperandExpression extends ExpressionNode {
    final MutableDouble value;

    public OperandExpression(MutableDouble value) {
        this.value = value;
    }

    @Override
    public double evaluate() {
        return value.getValue();
    }
}

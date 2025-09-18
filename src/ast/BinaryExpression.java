package ast;

public class BinaryExpression extends ASTNode {
    private ASTNode left;
    private String operator;
    private ASTNode right;

    public BinaryExpression(int lineNumber, ASTNode left, String operator, ASTNode right) {
        super(lineNumber);
        this.left = left;
        this.operator = operator;
        this.right = right;
    }

    public ASTNode getLeft() { return left; }
    public String getOperator() { return operator; }
    public ASTNode getRight() { return right; }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
        @Override
    public Object accept(ExpressionVisitor visitor) {
        return visitor.visit(this);
    }
}

package ast;

public class ExpressionStatementNode extends ASTNode {
    private ASTNode expression;

    public ExpressionStatementNode(int lineNumber, ASTNode expression) {
        super(lineNumber);
        this.expression = expression;
    }

    public ASTNode getExpression() { return expression; }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
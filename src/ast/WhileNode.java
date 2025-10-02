package ast;

public class WhileNode extends ASTNode {
    private ASTNode condition;
    private BlockNode body;

    public WhileNode(int lineNumber, ASTNode condition, BlockNode body) {
        super(lineNumber);
        this.condition = condition;
        this.body = body;
    }

    public ASTNode getCondition() { return condition; }
    public BlockNode getBody() { return body; }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
    
    @Override
    public <T> T accept(ExpressionVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
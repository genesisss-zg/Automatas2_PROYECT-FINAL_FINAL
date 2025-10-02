package ast;

public class LiteralNode extends ASTNode {
    private Object value;

    public LiteralNode(int lineNumber, Object value) {
        super(lineNumber);
        this.value = value;
    }

    public Object getValue() { return value; }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
    
    @Override
    public <T> T accept(ExpressionVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
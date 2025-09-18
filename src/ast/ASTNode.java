package ast;

public abstract class ASTNode {
    private int lineNumber;

    public ASTNode(int lineNumber) {
        this.lineNumber = lineNumber;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public abstract void accept(ASTVisitor visitor);
    
    // Nuevo método para expression visitor
    public Object accept(ExpressionVisitor visitor) {
        return null;
    }
}
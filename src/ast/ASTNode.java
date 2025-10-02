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
    
    // Nuevo método para el expression visitor
    public <T> T accept(ExpressionVisitor<T> visitor) {
        return null;
    }
}
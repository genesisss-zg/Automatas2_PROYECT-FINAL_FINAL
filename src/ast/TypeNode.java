package ast;

public class TypeNode extends ASTNode {
    private String typeName;

    public TypeNode(int lineNumber, String typeName) {
        super(lineNumber);
        this.typeName = typeName;
    }

    public String getTypeName() { return typeName; }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
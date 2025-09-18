package ast;

public class IdentifierNode extends ASTNode {
    private String name;

    public IdentifierNode(int lineNumber, String name) {
        super(lineNumber);
        this.name = name;
    }

    public String getName() { return name; }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
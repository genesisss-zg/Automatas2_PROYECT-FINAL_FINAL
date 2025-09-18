package ast;

public class VariableDeclNode extends ASTNode {
    private String variableName;
    private String type;
    private ASTNode initialValue;

    public VariableDeclNode(int lineNumber, String variableName, String type, ASTNode initialValue) {
        super(lineNumber);
        this.variableName = variableName;
        this.type = type;
        this.initialValue = initialValue;
    }

    public String getVariableName() { return variableName; }
    public String getType() { return type; }
    public ASTNode getInitialValue() { return initialValue; }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}

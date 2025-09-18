package ast;

import java.util.ArrayList;
import java.util.List;

public class FunctionNode extends ASTNode {
    private String functionName;
    private String returnType;
    private List<ASTNode> parameters;
    private BlockNode body;

    public FunctionNode(int lineNumber, String functionName, String returnType) {
        super(lineNumber);
        this.functionName = functionName;
        this.returnType = returnType;
        this.parameters = new ArrayList<>();
    }

    public void addParameter(ASTNode parameter) {
        parameters.add(parameter);
    }

    public void setBody(BlockNode body) {
        this.body = body;
    }

    public String getFunctionName() { return functionName; }
    public String getReturnType() { return returnType; }
    public List<ASTNode> getParameters() { return new ArrayList<>(parameters); }
    public BlockNode getBody() { return body; }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
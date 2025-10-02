package ast;

import java.util.ArrayList;
import java.util.List;

public class BlockNode extends ASTNode {
    private List<ASTNode> statements;

    public BlockNode(int lineNumber) {
        super(lineNumber);
        this.statements = new ArrayList<>();
    }

    public void addStatement(ASTNode statement) {
        statements.add(statement);
    }

    public List<ASTNode> getStatements() {
        return new ArrayList<>(statements);
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
    
    @Override
    public <T> T accept(ExpressionVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
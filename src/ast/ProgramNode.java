package ast;

import java.util.ArrayList;
import java.util.List;

public class ProgramNode extends ASTNode {
    private List<ASTNode> declarations;

    public ProgramNode() {
        super(0);
        this.declarations = new ArrayList<>();
    }

    public void addDeclaration(ASTNode declaration) {
        declarations.add(declaration);
    }

    public List<ASTNode> getDeclarations() {
        return new ArrayList<>(declarations);
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
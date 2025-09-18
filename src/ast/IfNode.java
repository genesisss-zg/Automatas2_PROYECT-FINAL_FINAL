package ast;

public class IfNode extends ASTNode {
    private ASTNode condition;
    private BlockNode thenBlock;
    private BlockNode elseBlock;

    public IfNode(int lineNumber, ASTNode condition, BlockNode thenBlock) {
        super(lineNumber);
        this.condition = condition;
        this.thenBlock = thenBlock;
    }

    public void setElseBlock(BlockNode elseBlock) {
        this.elseBlock = elseBlock;
    }

    public ASTNode getCondition() { return condition; }
    public BlockNode getThenBlock() { return thenBlock; }
    public BlockNode getElseBlock() { return elseBlock; }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
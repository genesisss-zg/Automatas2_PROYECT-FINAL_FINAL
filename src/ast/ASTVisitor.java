package ast;

public interface ASTVisitor {
    void visit(AssignmentNode node);
    void visit(BinaryExpression node);
    void visit(BlockNode node);
    void visit(CallNode node);
    void visit(ExpressionStatementNode node);
    void visit(FunctionNode node);
    void visit(IdentifierNode node);
    void visit(IfNode node);
    void visit(LiteralNode node);
    void visit(PrintNode node);
    void visit(ProgramNode node);
    void visit(ReturnNode node);
    void visit(TypeNode node);
    void visit(VariableDeclNode node);
    void visit(WhileNode node);
}
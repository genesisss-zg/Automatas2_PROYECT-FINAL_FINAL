package ast;

public interface ExpressionVisitor<T> {
    T visit(AssignmentNode node);
    T visit(BinaryExpression node);
    T visit(BlockNode node);
    T visit(CallNode node);
    T visit(ExpressionStatementNode node);
    T visit(FunctionNode node);
    T visit(IdentifierNode node);
    T visit(IfNode node);
    T visit(LiteralNode node);
    T visit(PrintNode node);
    T visit(ProgramNode node);
    T visit(ReturnNode node);
    T visit(TypeNode node);
    T visit(VariableDeclNode node);
    T visit(WhileNode node);
}
package ast;

public interface ExpressionVisitor<T> {
    T visit(AssignmentNode node);
    T visit(BinaryExpression node);
    T visit(CallNode node);
    T visit(IdentifierNode node);
    T visit(LiteralNode node);
}
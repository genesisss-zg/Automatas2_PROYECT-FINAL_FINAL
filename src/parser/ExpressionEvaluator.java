package parser;

import ast.*;
import java.util.Map;

public class ExpressionEvaluator implements ASTVisitor {
    private Map<String, Object> symbolTable;
    private Object result;

    public ExpressionEvaluator(Map<String, Object> symbolTable) {
        this.symbolTable = symbolTable;
    }

    public Object evaluate(ASTNode node) {
        node.accept(this);
        return result;
    }

    @Override
    public void visit(BinaryExpression node) {
        Object leftVal = evaluate(node.getLeft());
        Object rightVal = evaluate(node.getRight());

        if (leftVal == null || rightVal == null) {
            throw new RuntimeException("Variable no definida en expresión");
        }

        // Verificar tipos compatibles
        if (!leftVal.getClass().equals(rightVal.getClass())) {
            throw new RuntimeException("Tipos incompatibles: " + 
                leftVal.getClass().getSimpleName() + " y " + 
                rightVal.getClass().getSimpleName());
        }

        switch (node.getOperator()) {
            case "+":
                if (leftVal instanceof Number) {
                    result = ((Number) leftVal).doubleValue() + ((Number) rightVal).doubleValue();
                } else if (leftVal instanceof String) {
                    result = leftVal.toString() + rightVal.toString();
                }
                break;
            case "-":
                result = ((Number) leftVal).doubleValue() - ((Number) rightVal).doubleValue();
                break;
            case "*":
                result = ((Number) leftVal).doubleValue() * ((Number) rightVal).doubleValue();
                break;
            case "/":
                if (((Number) rightVal).doubleValue() == 0) {
                    throw new RuntimeException("División por cero");
                }
                result = ((Number) leftVal).doubleValue() / ((Number) rightVal).doubleValue();
                break;
            default:
                throw new RuntimeException("Operador no soportado: " + node.getOperator());
        }
    }
        @Override
    public void visit(PrintNode node) {
        // No necesita implementación para evaluación de expresiones
    }

    @Override
    public void visit(IdentifierNode node) {
        result = symbolTable.get(node.getName());
        if (result == null) {
            throw new RuntimeException("Variable no definida: " + node.getName());
        }
    }

    @Override
    public void visit(LiteralNode node) {
        result = node.getValue();
    }

    // Implementaciones vacías para otros nodos
    @Override public void visit(AssignmentNode node) {}
    @Override public void visit(BlockNode node) {}
    @Override public void visit(CallNode node) {}
    @Override public void visit(ExpressionStatementNode node) {}
    @Override public void visit(FunctionNode node) {}
    @Override public void visit(IfNode node) {}
    @Override public void visit(ProgramNode node) {}
    @Override public void visit(ReturnNode node) {}
    @Override public void visit(TypeNode node) {}
    @Override public void visit(VariableDeclNode node) {}
    @Override public void visit(WhileNode node) {}
}
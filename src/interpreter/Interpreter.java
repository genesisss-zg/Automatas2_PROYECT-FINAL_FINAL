package interpreter;

import ast.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

public class Interpreter implements ExpressionVisitor<Object> {
    private Map<String, Object> variables;
    private Map<String, FunctionNode> functions;
    private Stack<Map<String, Object>> scopeStack;
    
    public Interpreter() {
        this.variables = new HashMap<>();
        this.functions = new HashMap<>();
        this.scopeStack = new Stack<>();
        this.scopeStack.push(new HashMap<>()); // Scope global
    }

    public void interpret(ProgramNode program) {
        try {
            System.out.println("🔹 INTERPRETER: Iniciando interpretación");
            System.out.println("🔹 Número de declaraciones: " + program.getDeclarations().size());
            
            // Registrar funciones primero
            for (ASTNode node : program.getDeclarations()) {
                if (node instanceof FunctionNode) {
                    FunctionNode func = (FunctionNode) node;
                    functions.put(func.getFunctionName(), func);
                    System.out.println("📋 Función registrada: " + func.getFunctionName());
                }
            }

            // Ejecutar código global
            System.out.println("🔹 Ejecutando código global...");
            for (ASTNode node : program.getDeclarations()) {
                if (!(node instanceof FunctionNode)) {
                    System.out.println("➡️  Ejecutando: " + node.getClass().getSimpleName());
                    Object result = node.accept(this);
                    System.out.println("📊 Resultado: " + result);
                }
            }
            
            System.out.println("✅ INTERPRETER: Interpretación completada");
            
        } catch (ReturnException e) {
            System.out.println("↩️  Return encontrado: " + e.getValue());
        } catch (Exception e) {
            System.err.println("❌ ERROR EN INTERPRETER: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    private boolean isTruthy(Object value) {
        if (value instanceof Boolean) return (Boolean) value;
        if (value instanceof Number) return ((Number) value).doubleValue() != 0;
        if (value instanceof String) return !((String) value).isEmpty();
        return value != null;
    }

    // Métodos de ExpressionVisitor
    @Override
    public Object visit(AssignmentNode node) {
        Object value = node.getValue().accept(this);
        scopeStack.peek().put(node.getVariableName(), value);
        System.out.println("💾 Asignación: " + node.getVariableName() + " = " + value);
        return value;
    }

    @Override
    public Object visit(BinaryExpression node) {
        Object left = node.getLeft().accept(this);
        Object right = node.getRight().accept(this);

        System.out.println("🔢 Operación: " + left + " " + node.getOperator() + " " + right);

        switch (node.getOperator()) {
            case "+": 
                if (left instanceof Number && right instanceof Number) {
                    return ((Number) left).doubleValue() + ((Number) right).doubleValue();
                } else {
                    return left.toString() + right.toString();
                }
            case "-": 
                return ((Number) left).doubleValue() - ((Number) right).doubleValue();
            case "*": 
                return ((Number) left).doubleValue() * ((Number) right).doubleValue();
            case "/": 
                double divisor = ((Number) right).doubleValue();
                if (divisor == 0) throw new RuntimeException("División por cero");
                return ((Number) left).doubleValue() / divisor;
            case "==": 
                return left.equals(right);
            case "!=": 
                return !left.equals(right);
            case "<": 
                return ((Number) left).doubleValue() < ((Number) right).doubleValue();
            case ">": 
                return ((Number) left).doubleValue() > ((Number) right).doubleValue();
            case "<=": 
                return ((Number) left).doubleValue() <= ((Number) right).doubleValue();
            case ">=": 
                return ((Number) left).doubleValue() >= ((Number) right).doubleValue();
            case "&&": 
                return isTruthy(left) && isTruthy(right);
            case "||": 
                return isTruthy(left) || isTruthy(right);
            default: 
                throw new RuntimeException("Operador no soportado: " + node.getOperator());
        }
    }

    @Override
    public Object visit(BlockNode node) {
        System.out.println("🔲 Iniciando bloque");
        scopeStack.push(new HashMap<>());
        Object result = null;
        
        for (ASTNode stmt : node.getStatements()) {
            result = stmt.accept(this);
        }
        
        scopeStack.pop();
        System.out.println("🔲 Bloque finalizado");
        return result;
    }

    @Override
    public Object visit(CallNode node) {
        FunctionNode function = functions.get(node.getFunctionName());
        if (function == null) {
            throw new RuntimeException("Función no encontrada: " + node.getFunctionName());
        }

        System.out.println("📞 Llamando función: " + node.getFunctionName());

        // Evaluar argumentos
        Object[] args = new Object[node.getArguments().size()];
        for (int i = 0; i < node.getArguments().size(); i++) {
            args[i] = node.getArguments().get(i).accept(this);
            System.out.println("   Argumento " + i + ": " + args[i]);
        }

        // Crear nuevo scope para la función
        scopeStack.push(new HashMap<>());
        
        // Ejecutar cuerpo de la función
        Object result = null;
        try {
            if (function.getBody() != null) {
                result = function.getBody().accept(this);
            }
        } catch (ReturnException e) {
            result = e.getValue();
            System.out.println("↩️  Función retornó: " + result);
        }
        
        scopeStack.pop();
        return result;
    }

    @Override
    public Object visit(ExpressionStatementNode node) {
        return node.getExpression().accept(this);
    }

    @Override
    public Object visit(FunctionNode node) {
        // Solo registrar la función, no ejecutarla aquí
        functions.put(node.getFunctionName(), node);
        System.out.println("📋 Registrando función: " + node.getFunctionName());
        return null;
    }

    @Override
    public Object visit(IdentifierNode node) {
        // Buscar en scopes desde el más interno al más externo
        for (int i = scopeStack.size() - 1; i >= 0; i--) {
            Map<String, Object> scope = scopeStack.get(i);
            if (scope.containsKey(node.getName())) {
                Object value = scope.get(node.getName());
                System.out.println("🔍 Variable encontrada: " + node.getName() + " = " + value);
                return value;
            }
        }
        throw new RuntimeException("Variable no definida: " + node.getName());
    }

    @Override
    public Object visit(IfNode node) {
        Object condition = node.getCondition().accept(this);
        System.out.println("❓ Condición if: " + condition + " (truthy: " + isTruthy(condition) + ")");
        
        if (isTruthy(condition)) {
            System.out.println("✅ Ejecutando bloque then");
            return node.getThenBlock().accept(this);
        } else if (node.getElseBlock() != null) {
            System.out.println("⏭️  Ejecutando bloque else");
            return node.getElseBlock().accept(this);
        }
        return null;
    }

    @Override
    public Object visit(LiteralNode node) {
        System.out.println("📌 Literal: " + node.getValue());
        return node.getValue();
    }

    @Override
    public Object visit(PrintNode node) {
        Object value = node.getValue().accept(this);
        System.out.println("🖨️  PRINT EJECUTADO: " + value);
        return value;
    }

    @Override
    public Object visit(ProgramNode node) {
        System.out.println("🚀 Iniciando programa");
        Object result = null;
        for (ASTNode declaration : node.getDeclarations()) {
            result = declaration.accept(this);
        }
        System.out.println("🏁 Programa finalizado");
        return result;
    }

    @Override
    public Object visit(ReturnNode node) {
        Object value = null;
        if (node.getValue() != null) {
            value = node.getValue().accept(this);
        }
        System.out.println("↩️  Ejecutando return: " + value);
        throw new ReturnException(value);
    }

    @Override
    public Object visit(TypeNode node) {
        // Los TypeNode no producen valor en ejecución
        return null;
    }

    @Override
    public Object visit(VariableDeclNode node) {
        Object value = null;
        if (node.getInitialValue() != null) {
            value = node.getInitialValue().accept(this);
            System.out.println("📦 Declarando variable: " + node.getVariableName() + " = " + value);
        } else {
            System.out.println("📦 Declarando variable: " + node.getVariableName() + " = null");
        }
        scopeStack.peek().put(node.getVariableName(), value);
        return value;
    }

    @Override
    public Object visit(WhileNode node) {
        System.out.println("🔁 Iniciando while");
        Object result = null;
        int iteration = 0;
        
        while (true) {
            iteration++;
            Object condition = node.getCondition().accept(this);
            System.out.println("🔄 Iteración " + iteration + " - Condición: " + condition);
            
            if (!isTruthy(condition)) {
                System.out.println("⏹️  Condición falsa, terminando while");
                break;
            }
            
            result = node.getBody().accept(this);
            
            // Prevención de bucles infinitos
            if (iteration > 1000) {
                throw new RuntimeException("Bucle while posiblemente infinito");
            }
        }
        
        System.out.println("🔁 While finalizado");
        return result;
    }

    // Clase interna para manejar returns
    private static class ReturnException extends RuntimeException {
        private final Object value;
        
        public ReturnException(Object value) {
            this.value = value;
        }
        
        public Object getValue() {
            return value;
        }
    }
}
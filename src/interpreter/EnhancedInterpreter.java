package interpreter;

import ast.*;
import minecode.MineCodeNativeLib;
import minecode.MineCodeValue;
import minecode.MineCodeType;
import java.util.*;
import java.util.stream.Collectors;

public class EnhancedInterpreter implements ExpressionVisitor<MineCodeValue> {
    private Stack<Map<String, MineCodeValue>> scopes;
    private Map<String, FunctionNode> functions;
    private boolean debugMode;
    private int currentLine;
    private Set<Integer> breakpoints;
    private boolean paused;
    private DebugListener debugListener;

    public interface DebugListener {
        void onBreakpointHit(int line, Map<String, MineCodeValue> variables);
        void onLineExecuted(int line, Map<String, MineCodeValue> variables);
    }

    public EnhancedInterpreter() {
        this.scopes = new Stack<>();
        this.functions = new HashMap<>();
        this.debugMode = false;
        this.breakpoints = new HashSet<>();
        this.paused = false;
        enterScope(); // Scope global
    }

    public void setDebugMode(boolean debug) {
        this.debugMode = debug;
    }

    public void setDebugListener(DebugListener listener) {
        this.debugListener = listener;
    }

    public void addBreakpoint(int line) {
        breakpoints.add(line);
        System.out.println("📍 Breakpoint agregado en línea: " + line);
    }

    public void removeBreakpoint(int line) {
        breakpoints.remove(line);
        System.out.println("📍 Breakpoint removido en línea: " + line);
    }

    public void pauseExecution() {
        this.paused = true;
        System.out.println("⏸️  Ejecución pausada");
    }

    public void resumeExecution() {
        this.paused = false;
        System.out.println("▶️  Ejecución reanudada");
    }

    public Map<String, MineCodeValue> getCurrentVariables() {
        return new HashMap<>(scopes.peek());
    }

    public int getCurrentLine() {
        return currentLine;
    }

    private void checkBreakpoint(int line) {
        if (debugMode && breakpoints.contains(line)) {
            paused = true;
            currentLine = line;
            System.out.println("⚡ Breakpoint alcanzado en línea " + line);
            
            if (debugListener != null) {
                debugListener.onBreakpointHit(line, getCurrentVariables());
            }
            
            // Esperar hasta que se reanude la ejecución
            while (paused) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        } else if (debugMode && debugListener != null) {
            debugListener.onLineExecuted(line, getCurrentVariables());
        }
    }

    private void enterScope() {
        scopes.push(new HashMap<>());
    }

    private void exitScope() {
        if (scopes.size() > 1) {
            scopes.pop();
        }
    }

    private MineCodeValue getVariable(String name) {
        for (int i = scopes.size() - 1; i >= 0; i--) {
            if (scopes.get(i).containsKey(name)) {
                return scopes.get(i).get(name);
            }
        }
        throw new RuntimeException("Variable no definida: " + name);
    }

    private void setVariable(String name, MineCodeValue value) {
        scopes.peek().put(name, value);
    }

    public MineCodeValue interpret(ProgramNode program) {
        try {
            // Registrar funciones primero
            for (ASTNode node : program.getDeclarations()) {
                if (node instanceof FunctionNode) {
                    FunctionNode func = (FunctionNode) node;
                    functions.put(func.getFunctionName(), func);
                    if (debugMode) {
                        System.out.println("📋 Función registrada: " + func.getFunctionName());
                    }
                }
            }

            // Ejecutar código global
            MineCodeValue result = null;
            for (ASTNode node : program.getDeclarations()) {
                if (!(node instanceof FunctionNode)) {
                    result = node.accept(this);
                }
            }
            return result;

        } catch (ReturnException e) {
            return e.getValue();
        }
    }

    // Visitors implementados
    @Override
    public MineCodeValue visit(AssignmentNode node) {
        currentLine = node.getLineNumber();
        checkBreakpoint(currentLine);

        String varName = node.getVariableName();
        MineCodeValue value = node.getValue().accept(this);
        setVariable(varName, value);

        if (debugMode) {
            System.out.println("💾 Asignación: " + varName + " = " + value.getValue() + " (" + value.getType() + ")");
        }
        return value;
    }

    @Override
    public MineCodeValue visit(BinaryExpression node) {
        currentLine = node.getLineNumber();
        checkBreakpoint(currentLine);

        MineCodeValue left = node.getLeft().accept(this);
        MineCodeValue right = node.getRight().accept(this);

        if (debugMode) {
            System.out.println("🔢 Operación: " + left.getValue() + " " + node.getOperator() + " " + right.getValue());
        }

        switch (node.getOperator()) {
            case "+":
                if (left.getType() == MineCodeType.OBSIDIAN || right.getType() == MineCodeType.OBSIDIAN) {
                    return MineCodeValue.createObsidian(left.toString() + right.toString());
                } else if (left.getType() == MineCodeType.EMERALD || right.getType() == MineCodeType.EMERALD) {
                    return MineCodeValue.createEmerald(left.asEmerald() + right.asEmerald());
                } else {
                    return MineCodeValue.createRedstone(left.asRedstone() + right.asRedstone());
                }

            case "-":
                if (left.getType() == MineCodeType.EMERALD || right.getType() == MineCodeType.EMERALD) {
                    return MineCodeValue.createEmerald(left.asEmerald() - right.asEmerald());
                } else {
                    return MineCodeValue.createRedstone(left.asRedstone() - right.asRedstone());
                }

            case "*":
                if (left.getType() == MineCodeType.EMERALD || right.getType() == MineCodeType.EMERALD) {
                    return MineCodeValue.createEmerald(left.asEmerald() * right.asEmerald());
                } else {
                    return MineCodeValue.createRedstone(left.asRedstone() * right.asRedstone());
                }

            case "/":
                double divisor = right.asEmerald();
                if (divisor == 0) throw new RuntimeException("División por cero");
                return MineCodeValue.createEmerald(left.asEmerald() / divisor);

            case "==": return MineCodeValue.createNether(left.getValue().equals(right.getValue()));
            case "!=": return MineCodeValue.createNether(!left.getValue().equals(right.getValue()));
            case "<": return MineCodeValue.createNether(left.asEmerald() < right.asEmerald());
            case ">": return MineCodeValue.createNether(left.asEmerald() > right.asEmerald());
            case "<=": return MineCodeValue.createNether(left.asEmerald() <= right.asEmerald());
            case ">=": return MineCodeValue.createNether(left.asEmerald() >= right.asEmerald());
            case "&&": return MineCodeValue.createNether(isTruthy(left) && isTruthy(right));
            case "||": return MineCodeValue.createNether(isTruthy(left) || isTruthy(right));

            default: throw new RuntimeException("Operador no soportado: " + node.getOperator());
        }
    }

    @Override
    public MineCodeValue visit(BlockNode node) {
        currentLine = node.getLineNumber();
        checkBreakpoint(currentLine);

        if (debugMode) {
            System.out.println("🔲 Iniciando bloque");
        }
        
        enterScope();
        MineCodeValue result = null;
        
        for (ASTNode stmt : node.getStatements()) {
            result = stmt.accept(this);
        }
        
        exitScope();
        
        if (debugMode) {
            System.out.println("🔲 Bloque finalizado");
        }
        return result;
    }

    @Override
    public MineCodeValue visit(CallNode node) {
        currentLine = node.getLineNumber();
        checkBreakpoint(currentLine);

        String functionName = node.getFunctionName();

        // Verificar si es función nativa
        if (MineCodeNativeLib.NATIVE_FUNCTIONS.containsKey(functionName)) {
            List<MineCodeValue> args = node.getArguments().stream()
                .map(arg -> arg.accept(this))
                .collect(Collectors.toList());

            MineCodeValue result = MineCodeNativeLib.NATIVE_FUNCTIONS.get(functionName).execute(args);
            if (debugMode) {
                System.out.println("🏗️  Función nativa: " + functionName + " -> " + result.getValue());
            }
            return result;
        }

        // Función definida por usuario
        FunctionNode function = functions.get(functionName);
        if (function == null) {
            throw new RuntimeException("Función no encontrada: " + functionName);
        }

        if (debugMode) {
            System.out.println("📞 Llamando función: " + functionName);
        }

        // Evaluar argumentos
        List<MineCodeValue> args = new ArrayList<>();
        for (ASTNode arg : node.getArguments()) {
            args.add(arg.accept(this));
        }

        // Crear nuevo scope
        enterScope();

        // Asignar parámetros
        List<ASTNode> params = function.getParameters();
        for (int i = 0; i < params.size(); i++) {
            if (params.get(i) instanceof IdentifierNode) {
                String paramName = ((IdentifierNode) params.get(i)).getName();
                setVariable(paramName, i < args.size() ? args.get(i) : MineCodeValue.createObsidian(""));
            }
        }

        // Ejecutar función
        MineCodeValue result = null;
        try {
            if (function.getBody() != null) {
                result = function.getBody().accept(this);
            }
        } catch (ReturnException e) {
            result = e.getValue();
            if (debugMode) {
                System.out.println("↩️  Función retornó: " + result.getValue());
            }
        }

        exitScope();
        return result;
    }

    @Override
    public MineCodeValue visit(ExpressionStatementNode node) {
        currentLine = node.getLineNumber();
        checkBreakpoint(currentLine);
        return node.getExpression().accept(this);
    }

    @Override
    public MineCodeValue visit(FunctionNode node) {
        currentLine = node.getLineNumber();
        checkBreakpoint(currentLine);
        
        // Solo registrar la función, no ejecutarla aquí
        functions.put(node.getFunctionName(), node);
        if (debugMode) {
            System.out.println("📋 Registrando función: " + node.getFunctionName());
        }
        return null;
    }

    @Override
    public MineCodeValue visit(IdentifierNode node) {
        currentLine = node.getLineNumber();
        checkBreakpoint(currentLine);
        
        MineCodeValue value = getVariable(node.getName());
        if (debugMode) {
            System.out.println("🔍 Variable encontrada: " + node.getName() + " = " + value.getValue());
        }
        return value;
    }

    @Override
    public MineCodeValue visit(IfNode node) {
        currentLine = node.getLineNumber();
        checkBreakpoint(currentLine);

        MineCodeValue condition = node.getCondition().accept(this);
        if (debugMode) {
            System.out.println("❓ Condición if: " + condition.getValue() + " (truthy: " + isTruthy(condition) + ")");
        }
        
        if (isTruthy(condition)) {
            if (debugMode) {
                System.out.println("✅ Ejecutando bloque then");
            }
            return node.getThenBlock().accept(this);
        } else if (node.getElseBlock() != null) {
            if (debugMode) {
                System.out.println("⏭️  Ejecutando bloque else");
            }
            return node.getElseBlock().accept(this);
        }
        return null;
    }

    @Override
    public MineCodeValue visit(LiteralNode node) {
        currentLine = node.getLineNumber();
        checkBreakpoint(currentLine);

        Object value = node.getValue();
        MineCodeValue result;
        
        if (value instanceof Integer) {
            result = MineCodeValue.createRedstone((Integer) value);
        } else if (value instanceof Double) {
            result = MineCodeValue.createEmerald((Double) value);
        } else if (value instanceof Boolean) {
            result = MineCodeValue.createNether((Boolean) value);
        } else if (value instanceof String) {
            result = MineCodeValue.createObsidian((String) value);
        } else {
            result = MineCodeValue.createObsidian("null");
        }
        
        if (debugMode) {
            System.out.println("📌 Literal: " + result.getValue());
        }
        return result;
    }

    @Override
    public MineCodeValue visit(PrintNode node) {
        currentLine = node.getLineNumber();
        checkBreakpoint(currentLine);

        MineCodeValue value = node.getValue().accept(this);
        System.out.println("🖨️  " + value.getValue());
        return value;
    }

    @Override
    public MineCodeValue visit(ProgramNode node) {
        currentLine = node.getLineNumber();
        checkBreakpoint(currentLine);

        if (debugMode) {
            System.out.println("🚀 Iniciando programa");
        }
        
        MineCodeValue result = null;
        for (ASTNode declaration : node.getDeclarations()) {
            result = declaration.accept(this);
        }
        
        if (debugMode) {
            System.out.println("🏁 Programa finalizado");
        }
        return result;
    }

    @Override
    public MineCodeValue visit(ReturnNode node) {
        currentLine = node.getLineNumber();
        checkBreakpoint(currentLine);

        MineCodeValue value = null;
        if (node.getValue() != null) {
            value = node.getValue().accept(this);
        }
        
        if (debugMode) {
            System.out.println("↩️  Ejecutando return: " + (value != null ? value.getValue() : "void"));
        }
        
        throw new ReturnException(value);
    }

    @Override
    public MineCodeValue visit(TypeNode node) {
        currentLine = node.getLineNumber();
        checkBreakpoint(currentLine);
        
        // Los TypeNode no producen valor en ejecución
        return null;
    }

    @Override
    public MineCodeValue visit(VariableDeclNode node) {
        currentLine = node.getLineNumber();
        checkBreakpoint(currentLine);

        MineCodeValue value = null;
        if (node.getInitialValue() != null) {
            value = node.getInitialValue().accept(this);
            if (debugMode) {
                System.out.println("📦 Declarando variable: " + node.getVariableName() + " = " + value.getValue());
            }
        } else {
            if (debugMode) {
                System.out.println("📦 Declarando variable: " + node.getVariableName() + " = null");
            }
            value = MineCodeValue.createObsidian("null");
        }
        
        setVariable(node.getVariableName(), value);
        return value;
    }

    @Override
    public MineCodeValue visit(WhileNode node) {
        currentLine = node.getLineNumber();
        checkBreakpoint(currentLine);

        if (debugMode) {
            System.out.println("🔁 Iniciando while");
        }
        
        MineCodeValue result = null;
        int iteration = 0;
        
        while (true) {
            iteration++;
            MineCodeValue condition = node.getCondition().accept(this);
            
            if (debugMode) {
                System.out.println("🔄 Iteración " + iteration + " - Condición: " + condition.getValue());
            }
            
            if (!isTruthy(condition)) {
                if (debugMode) {
                    System.out.println("⏹️  Condición falsa, terminando while");
                }
                break;
            }
            
            result = node.getBody().accept(this);
            
            // Prevención de bucles infinitos
            if (iteration > 1000) {
                throw new RuntimeException("Bucle while posiblemente infinito");
            }
        }
        
        if (debugMode) {
            System.out.println("🔁 While finalizado");
        }
        return result;
    }

    private boolean isTruthy(MineCodeValue value) {
        if (value.getType() == MineCodeType.NETHER) {
            return value.asNether();
        }
        if (value.getType() == MineCodeType.REDSTONE) {
            return value.asRedstone() != 0;
        }
        if (value.getType() == MineCodeType.EMERALD) {
            return value.asEmerald() != 0.0;
        }
        if (value.getType() == MineCodeType.OBSIDIAN) {
            return !value.asObsidian().isEmpty();
        }
        return value.getValue() != null;
    }

    // Clase interna para manejar returns
    private static class ReturnException extends RuntimeException {
        private final MineCodeValue value;
        
        public ReturnException(MineCodeValue value) {
            this.value = value;
        }
        
        public MineCodeValue getValue() {
            return value;
        }
    }
}
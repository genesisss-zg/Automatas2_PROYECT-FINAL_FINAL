package gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import lexer.Lexer;
import lexer.Token;
import lexer.TokenType;
import parser.Parser;
import ast.ProgramNode;
import semantic.SemanticAnalyzer;
import interpreter.Interpreter;
import util.ManejadorErrores;
import util.ErrorSemantico;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class AnalizadorGUI1 extends JFrame {
    private JTextArea codeArea;
    private JTextArea resultArea;
    private JButton analyzeButton;
    private JButton clearButton;
    private JButton runButton;
    private JLabel statusLabel;
    private JLabel languageLabel;
    private ProgramNode currentProgram;
    private boolean isMineCode = false;

    public AnalizadorGUI1() {
        setTitle("Analizador de Código - Compilador MineCode");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 700);
        setLocationRelativeTo(null);
        
        initComponents();
        layoutComponents();
        setupEvents();
        
        setLocationRelativeTo(null);
    }

    private void initComponents() {
        // Área de código
        codeArea = new JTextArea();
        codeArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        codeArea.setBorder(BorderFactory.createTitledBorder("Código Fuente"));
        codeArea.setBackground(new Color(245, 245, 245));
        
        // Área de resultados
        resultArea = new JTextArea();
        resultArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        resultArea.setBorder(BorderFactory.createTitledBorder("Resultados del Análisis"));
        resultArea.setEditable(false);
        resultArea.setBackground(new Color(240, 240, 240));
        
        // Botones
        analyzeButton = new JButton("🔍 Analizar Código");
        analyzeButton.setToolTipText("Analizar código (Léxico, Sintáctico, Semántico)");
        
        runButton = new JButton("▶️ Ejecutar");
        runButton.setToolTipText("Ejecutar código (si no hay errores)");
        runButton.setEnabled(false);
        
        clearButton = new JButton("🧹 Limpiar");
        clearButton.setToolTipText("Limpiar áreas de texto");
        
        // Etiquetas
        statusLabel = new JLabel("Listo para analizar código MineCode");
        statusLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        
        languageLabel = new JLabel("Lenguaje: Detectando...");
        languageLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        languageLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
    }

    private void layoutComponents() {
        setLayout(new BorderLayout(10, 10));
        
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Panel superior con información del lenguaje
        JPanel infoPanel = new JPanel(new BorderLayout());
        infoPanel.add(languageLabel, BorderLayout.WEST);
        mainPanel.add(infoPanel, BorderLayout.NORTH);
        
        JScrollPane codeScroll = new JScrollPane(codeArea);
        codeScroll.setPreferredSize(new Dimension(0, 300));
        mainPanel.add(codeScroll, BorderLayout.CENTER);
        
        JScrollPane resultScroll = new JScrollPane(resultArea);
        resultScroll.setPreferredSize(new Dimension(0, 200));
        mainPanel.add(resultScroll, BorderLayout.SOUTH);
        
        JPanel southPanel = new JPanel(new BorderLayout(5, 5));
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        buttonPanel.add(analyzeButton);
        buttonPanel.add(runButton);
        buttonPanel.add(clearButton);
        
        southPanel.add(buttonPanel, BorderLayout.NORTH);
        southPanel.add(statusLabel, BorderLayout.SOUTH);
        
        mainPanel.add(southPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
    }

    private void setupEvents() {
        analyzeButton.addActionListener(e -> analyzeCode());
        runButton.addActionListener(e -> runCode());
        
        clearButton.addActionListener(e -> {
            codeArea.setText("");
            resultArea.setText("");
            statusLabel.setText("Áreas limpiadas");
            languageLabel.setText("Lenguaje: Detectando...");
            runButton.setEnabled(false);
            currentProgram = null;
            isMineCode = false;
        });
        
        // Detectar cambios en el código para identificar el lenguaje
        codeArea.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { detectLanguage(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { detectLanguage(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { detectLanguage(); }
        });
        
        // Código de ejemplo MineCode
        codeArea.setText("enchant_func main() crafting_table\n" +
                        "    x -> 10\n" +
                        "    redstone_if x -> 10 crafting_table\n" +
                        "        print(\"x es 10\")\n" +
                        "    slime_disc crafting_table\n" +
                        "        print(\"x no es 10\")\n" +
                        "    end_portal\n" +
                        "    print(\"Programa completado\")\n" +
                        "end_portal");
    }

    private void detectLanguage() {
        String code = codeArea.getText();
        if (code.contains("enchant_func") || code.contains("redstone_if") || 
            code.contains("crafting_table") || code.contains("->")) {
            isMineCode = true;
            languageLabel.setText("Lenguaje: MineCode ✅");
            languageLabel.setForeground(new Color(0, 100, 0));
        } else {
            isMineCode = false;
            languageLabel.setText("Lenguaje: Original");
            languageLabel.setForeground(Color.BLUE);
        }
    }

        private void analyzeCode() {
            String sourceCode = codeArea.getText().trim();
            if (sourceCode.isEmpty()) {
                showResult("Por favor, ingrese código para analizar.", Color.RED);
                return;
            }
            
            try {
                resultArea.setText("");
                resultArea.setForeground(Color.BLACK);
                
                // Detectar lenguaje automáticamente
                detectLanguage();
                
                StringBuilder result = new StringBuilder();
                result.append("=== ANÁLISIS DE CÓDIGO MINECODE ===\n\n");
                
                // 1. ANÁLISIS LÉXICO
                setStatus("Realizando análisis léxico...", Color.BLUE);
                result.append("📖 ANÁLISIS LÉXICO\n");
                result.append("------------------\n");
                
                Lexer lexer = new Lexer(sourceCode);
                ManejadorErrores erroresTotales = new ManejadorErrores();
                int tokenCount = 0;
                boolean hasLexicalErrors = false;
                
                try {
                    Token token;
                    do {
                        token = lexer.nextToken();
                        if (token.getType() == TokenType.ERROR) {
                            result.append("❌ Error léxico: '").append(token.getLexeme()).append("' en línea ").append(token.getLine()).append("\n");
                            hasLexicalErrors = true;
                            erroresTotales.agregarError(token.getLine(), "Token no reconocido: " + token.getLexeme(), "Léxico");
                        } else if (token.getType() != TokenType.EOF) {
                            tokenCount++;
                            // Mostrar solo algunos tokens para no saturar
                            if (tokenCount <= 20) {
                                result.append("   ").append(token.getType()).append(" -> '").append(token.getLexeme()).append("'\n");
                            }
                        }
                    } while (token.getType() != TokenType.EOF);
                    
                    if (tokenCount > 20) {
                        result.append("   ... y ").append(tokenCount - 20).append(" tokens más\n");
                    }
                    
                    if (hasLexicalErrors) {
                        result.append("❌ Se encontraron errores léxicos\n\n");
                    } else {
                        result.append("✅ Tokens reconocidos: ").append(tokenCount).append("\n");
                        result.append("✅ Análisis léxico exitoso\n\n");
                    }
                    
                } catch (Exception lexError) {
                    result.append("❌ Error en análisis léxico: ").append(lexError.getMessage()).append("\n\n");
                    erroresTotales.agregarError(0, "Error léxico: " + lexError.getMessage(), "Léxico");
                }
                
                // Si hay errores léxicos, detenerse aquí
                if (hasLexicalErrors) {
                    result.append("⏹️  Análisis detenido por errores léxicos\n");
                    resultArea.setText(result.toString());
                    runButton.setEnabled(false);
                    setStatus("Análisis fallido - Errores léxicos", Color.RED);
                    return;
                }
                
                // 2. ANÁLISIS SINTÁCTICO
                setStatus("Realizando análisis sintáctico...", Color.BLUE);
                result.append("📝 ANÁLISIS SINTÁCTICO\n");
                result.append("----------------------\n");
                
                ProgramNode program = null;
                try {
                    Lexer lexerForParser = new Lexer(sourceCode);
                    Parser parser = new Parser(lexerForParser);
                    program = parser.parse();
                    result.append("✅ Estructura sintáctica válida\n");
                    result.append("✅ Declaraciones encontradas: ").append(program.getDeclarations().size()).append("\n\n");
                    
                } catch (Exception parseError) {
                    result.append("❌ Error sintáctico: ").append(parseError.getMessage()).append("\n\n");
                    erroresTotales.agregarError(0, "Error sintáctico: " + parseError.getMessage(), "Sintáctico");
                    program = null;
                }
                
                // 3. ANÁLISIS SEMÁNTICO (solo si no hay errores sintácticos)
                if (program != null) {
                    result.append("🔍 ANÁLISIS SEMÁNTICO\n");
                    result.append("----------------------\n");
                    
                    try {
                        SemanticAnalyzer semanticAnalyzer = new SemanticAnalyzer();
                        semanticAnalyzer.analyze(program);
                        
                        ManejadorErrores erroresSemanticos = semanticAnalyzer.getManejadorErrores();
                        
                        if (erroresSemanticos.hayErrores()) {
                            result.append("❌ Errores semánticos encontrados:\n");
                            for (ErrorSemantico error : erroresSemanticos.getErrores()) {
                                result.append("   • Línea ").append(error.getLinea()).append(": ").append(error.getMensaje()).append("\n");
                                erroresTotales.agregarError(error);
                            }
                        } else {
                            result.append("✅ Análisis semántico sin errores\n");
                        }
                        
                        currentProgram = program;
                        
                    } catch (Exception semanticError) {
                        result.append("❌ Error en análisis semántico: ").append(semanticError.getMessage()).append("\n");
                        erroresTotales.agregarError(0, "Error semántico: " + semanticError.getMessage(), "Semántico");
                        currentProgram = null;
                    }
                }
                
                // RESUMEN FINAL
                result.append("\n📊 RESUMEN FINAL\n");
                result.append("================\n");
                
                if (erroresTotales.hayErrores()) {
                    result.append("❌ COMPILACIÓN FALLIDA\n");
                    result.append("Total de errores: ").append(erroresTotales.getCantidadErrores()).append("\n");
                    runButton.setEnabled(false);
                    setStatus("Compilación fallida", Color.RED);
                    resultArea.setForeground(Color.RED);
                } else {
                    result.append("✅ COMPILACIÓN EXITOSA\n");
                    result.append("✅ Código listo para ejecutar\n");
                    runButton.setEnabled(true);
                    setStatus("Compilación exitosa", new Color(0, 100, 0));
                    resultArea.setForeground(new Color(0, 100, 0));
                }
                
                resultArea.setText(result.toString());
                
            } catch (Exception ex) {
                System.err.println("ERROR GENERAL: " + ex.getMessage());
                ex.printStackTrace();
                showResult("❌ Error durante el análisis: " + ex.getMessage(), Color.RED);
                runButton.setEnabled(false);
                currentProgram = null;
            }
        }

    private void runCode() {
        if (currentProgram == null) {
            showResult("❌ Primero debe analizar el código", Color.RED);
            return;
        }
        
        try {
            setStatus("Ejecutando código...", new Color(0, 100, 0));
            
            // Capturar toda la salida del sistema
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintStream combinedStream = new PrintStream(baos);
            
            PrintStream oldOut = System.out;
            PrintStream oldErr = System.err;
            
            System.setOut(combinedStream);
            System.setErr(combinedStream);
            
            StringBuilder executionResult = new StringBuilder();
            executionResult.append("\n--- EJECUCIÓN ---\n");
            
            long startTime = System.currentTimeMillis();
            
            try {
                Interpreter interpreter = new Interpreter();
                interpreter.interpret(currentProgram);
                
                long executionTime = System.currentTimeMillis() - startTime;
                
                // Restaurar streams
                System.setOut(oldOut);
                System.setErr(oldErr);
                
                String output = baos.toString();
                if (!output.trim().isEmpty()) {
                    executionResult.append("Salida del programa:\n");
                    executionResult.append(output).append("\n");
                } else {
                    executionResult.append("El programa no generó salida visible\n");
                }
                
                executionResult.append("✅ Ejecución completada en ").append(executionTime).append("ms");
                
                resultArea.append(executionResult.toString());
                setStatus("Ejecución completada", new Color(0, 100, 0));
                
            } catch (Exception ex) {
                System.setOut(oldOut);
                System.setErr(oldErr);
                throw ex;
            }
            
        } catch (Exception ex) {
            String errorMessage = ex.getMessage();
            resultArea.append("\n❌ Error durante la ejecución: " + 
                (errorMessage != null ? errorMessage : "Error desconocido"));
            setStatus("Error en ejecución", Color.RED);
        }
    }

    private void setStatus(String message, Color color) {
        statusLabel.setText(message);
        statusLabel.setForeground(color);
    }

    private void showResult(String message, Color color) {
        resultArea.setText(message);
        resultArea.setForeground(color);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            AnalizadorGUI1 gui = new AnalizadorGUI1();
            gui.setVisible(true);
        });
    }
}
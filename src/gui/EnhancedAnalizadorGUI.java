package gui;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import lexer.Lexer;
import lexer.Token;
import lexer.TokenType;
import parser.Parser;
import ast.ProgramNode;
import semantic.SemanticAnalyzer;
import interpreter.EnhancedInterpreter;
import minecode.MineCodeValue;
import util.ManejadorErrores;
import util.ErrorSemantico;
import java.util.HashMap;
import java.util.Map;

public class EnhancedAnalizadorGUI extends JFrame {
    private JTextPane codeArea;
    private JTextArea resultArea;
    private JButton analyzeButton, runButton, clearButton, debugButton;
    private JLabel statusLabel, languageLabel;
    private JToggleButton breakpointButton;
    private ProgramNode currentProgram;
    private EnhancedInterpreter interpreter;
    private Map<Integer, Boolean> breakpoints;

    public EnhancedAnalizadorGUI() {
        setTitle("🐍 Compilador MineCode - Edición Mejorada");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 800);
        initComponents();
        layoutComponents();
        setupEvents();
        setupSyntaxHighlighting();
        setLocationRelativeTo(null);
    }

    private void initComponents() {
        // Editor con syntax highlighting
        codeArea = new JTextPane();
        codeArea.setFont(new Font("Consolas", Font.PLAIN, 14));
        codeArea.setBackground(new Color(30, 30, 30));
        codeArea.setForeground(Color.WHITE);

        // Área de resultados
        resultArea = new JTextArea();
        resultArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        resultArea.setBackground(new Color(240, 240, 240));
        resultArea.setEditable(false);

        // Botones
        analyzeButton = createStyledButton("🔍 Analizar", new Color(70, 130, 180));
        runButton = createStyledButton("▶️ Ejecutar", new Color(34, 139, 34));
        debugButton = createStyledButton("🐛 Depurar", new Color(218, 165, 32));
        clearButton = createStyledButton("🧹 Limpiar", new Color(220, 20, 60));
        breakpointButton = new JToggleButton("📍 Breakpoint");

        // Etiquetas
        statusLabel = new JLabel("Listo para programar en MineCode");
        statusLabel.setForeground(new Color(70, 130, 180));
        languageLabel = new JLabel("🐍 MineCode Mode");
        languageLabel.setForeground(new Color(46, 139, 87));
        languageLabel.setFont(new Font("SansSerif", Font.BOLD, 14));

        breakpoints = new HashMap<>();
        interpreter = new EnhancedInterpreter();
    }

    private JButton createStyledButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        return button;
    }

    private void setupSyntaxHighlighting() {
        StyledDocument doc = codeArea.getStyledDocument();
        StyleContext context = new StyleContext();
        Style defaultStyle = context.getStyle(StyleContext.DEFAULT_STYLE);
        
        // Definir estilos
        Style keywordStyle = context.addStyle("Keyword", defaultStyle);
        StyleConstants.setForeground(keywordStyle, new Color(86, 156, 214)); // Azul
        StyleConstants.setBold(keywordStyle, true);

        Style stringStyle = context.addStyle("String", defaultStyle);
        StyleConstants.setForeground(stringStyle, new Color(206, 145, 120)); // Naranja

        Style numberStyle = context.addStyle("Number", defaultStyle);
        StyleConstants.setForeground(numberStyle, new Color(181, 206, 168)); // Verde

        Style commentStyle = context.addStyle("Comment", defaultStyle);
        StyleConstants.setForeground(commentStyle, new Color(106, 153, 85)); // Verde oscuro
        StyleConstants.setItalic(commentStyle, true);

        // Listener para coloreado en tiempo real
        doc.addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { colorize(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { colorize(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { colorize(); }
        });
    }

    private void colorize() {
        SwingUtilities.invokeLater(() -> {
            try {
                String text = codeArea.getText();
                StyledDocument doc = codeArea.getStyledDocument();
                doc.setCharacterAttributes(0, text.length(), 
                    doc.getStyle(StyleContext.DEFAULT_STYLE), true);

                // Palabras clave de MineCode
                String[] keywords = {"enchant_func", "redstone_if", "slime_else", 
                                   "piston_loop", "nether_return", "crafting_table", 
                                   "end_portal", "torch_on", "torch_off"};

                for (String keyword : keywords) {
                    int pos = 0;
                    while ((pos = text.indexOf(keyword, pos)) >= 0) {
                        doc.setCharacterAttributes(pos, keyword.length(), 
                            doc.getStyle("Keyword"), true);
                        pos += keyword.length();
                    }
                }

                // Colorear strings
                int start = -1;
                for (int i = 0; i < text.length(); i++) {
                    if (text.charAt(i) == '"') {
                        if (start == -1) {
                            start = i;
                        } else {
                            doc.setCharacterAttributes(start, i - start + 1, 
                                doc.getStyle("String"), true);
                            start = -1;
                        }
                    }
                }

                // Colorear comentarios
                int commentStart = text.indexOf("//");
                if (commentStart >= 0) {
                    int commentEnd = text.indexOf("\n", commentStart);
                    if (commentEnd == -1) commentEnd = text.length();
                    doc.setCharacterAttributes(commentStart, commentEnd - commentStart, 
                        doc.getStyle("Comment"), true);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void layoutComponents() {
        setLayout(new BorderLayout(10, 10));

        // Panel superior
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(languageLabel, BorderLayout.WEST);
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));

        // Editor de código
        JScrollPane codeScroll = new JScrollPane(codeArea);
        codeScroll.setBorder(BorderFactory.createTitledBorder("💎 Editor MineCode"));
        codeScroll.setPreferredSize(new Dimension(0, 400));

        // Panel de botones
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        buttonPanel.add(analyzeButton);
        buttonPanel.add(runButton);
        buttonPanel.add(debugButton);
        buttonPanel.add(breakpointButton);
        buttonPanel.add(clearButton);

        // Área de resultados
        JScrollPane resultScroll = new JScrollPane(resultArea);
        resultScroll.setBorder(BorderFactory.createTitledBorder("📊 Resultados"));
        resultScroll.setPreferredSize(new Dimension(0, 200));

        // Panel principal
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(codeScroll, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.NORTH);
        mainPanel.add(resultScroll, BorderLayout.SOUTH);

        add(mainPanel, BorderLayout.CENTER);
        
        // Panel inferior con status
        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.add(statusLabel, BorderLayout.WEST);
        add(southPanel, BorderLayout.SOUTH);
    }

    private void setupEvents() {
        analyzeButton.addActionListener(e -> analyzeCode());
        runButton.addActionListener(e -> runCode());
        debugButton.addActionListener(e -> debugCode());
        clearButton.addActionListener(e -> clearAll());

        breakpointButton.addActionListener(e -> {
            if (breakpointButton.isSelected()) {
                statusLabel.setText("Modo breakpoint activado - haz clic en una línea");
                setupBreakpointMode();
            } else {
                statusLabel.setText("Modo breakpoint desactivado");
                disableBreakpointMode();
            }
        });

        // Código de ejemplo mejorado
        codeArea.setText("// 🐍 Ejemplo MineCode para depuración\n" +
                        "enchant_func main() crafting_table\n" +
                        "    x = 10\n" +
                        "    y = 20\n" +
                        "    resultado = x + y\n" +
                        "    print(\"Suma: \" + resultado)\n" +
                        "    \n" +
                        "    redstone_if resultado > 25 crafting_table\n" +
                        "        print(\"Es mayor que 25\")\n" +
                        "    slime_else crafting_table\n" +
                        "        print(\"Es 30 o menos\")\n" +
                        "    end_portal\n" +
                        "end_portal");
    }

    private void setupBreakpointMode() {
        // Agregar listener para clicks en el área de código
        codeArea.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (breakpointButton.isSelected()) {
                    int pos = codeArea.viewToModel(e.getPoint());
                    try {
                        int line = codeArea.getDocument().getDefaultRootElement().getElementIndex(pos) + 1;
                        toggleBreakpoint(line);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });
    }

    private void disableBreakpointMode() {
        // Limpiar todos los breakpoints
        for (Integer line : breakpoints.keySet()) {
            interpreter.removeBreakpoint(line);
        }
        breakpoints.clear();
    }

    private void toggleBreakpoint(int line) {
        if (breakpoints.containsKey(line)) {
            breakpoints.remove(line);
            interpreter.removeBreakpoint(line);
            statusLabel.setText("Breakpoint removido en línea " + line);
        } else {
            breakpoints.put(line, true);
            interpreter.addBreakpoint(line);
            statusLabel.setText("Breakpoint agregado en línea " + line);
        }
        // Forzar repintado para mostrar breakpoints visualmente
        codeArea.repaint();
    }

    private void setupDebugging() {
        interpreter.setDebugListener(new EnhancedInterpreter.DebugListener() {
            @Override
            public void onBreakpointHit(int line, Map<String, MineCodeValue> variables) {
                SwingUtilities.invokeLater(() -> {
                    resultArea.append("\n⚡ BREAKPOINT Línea " + line + "\n");
                    resultArea.append("Variables actuales:\n");
                    for (Map.Entry<String, MineCodeValue> entry : variables.entrySet()) {
                        resultArea.append("  " + entry.getKey() + " = " + entry.getValue().getValue() + "\n");
                    }
                    resultArea.append("⏸️  Ejecución pausada. Presiona 'Continuar'...\n");
                    
                    // Mostrar diálogo de breakpoint
                    showBreakpointDialog(line, variables);
                });
            }
            
            @Override
            public void onLineExecuted(int line, Map<String, MineCodeValue> variables) {
                SwingUtilities.invokeLater(() -> {
                    // Mostrar línea ejecutada en modo detallado
                    if (debugButton.getText().contains("Detallado")) {
                        resultArea.append("➡️  Línea " + line + " ejecutada\n");
                    }
                });
            }
        });
    }

    private void showBreakpointDialog(int line, Map<String, MineCodeValue> variables) {
        JDialog breakpointDialog = new JDialog(this, "⚡ Breakpoint Línea " + line, true);
        breakpointDialog.setSize(400, 300);
        breakpointDialog.setLocationRelativeTo(this);
        
        JTextArea variableArea = new JTextArea();
        variableArea.setEditable(false);
        variableArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        variableArea.setText("=== VARIABLES EN LÍNEA " + line + " ===\n\n");
        
        for (Map.Entry<String, MineCodeValue> entry : variables.entrySet()) {
            variableArea.append("🔹 " + entry.getKey() + " = " + entry.getValue().getValue() + 
                              " (" + entry.getValue().getType() + ")\n");
        }
        
        JButton continueButton = new JButton("▶️ Continuar");
        continueButton.addActionListener(e -> {
            interpreter.resumeExecution();
            breakpointDialog.dispose();
        });
        
        JButton stepButton = new JButton("🐛 Paso a Paso");
        stepButton.addActionListener(e -> {
            interpreter.resumeExecution();
            breakpointDialog.dispose();
        });
        
        JButton stopButton = new JButton("⏹️ Detener");
        stopButton.addActionListener(e -> {
            interpreter.resumeExecution(); // Para salir del bucle de pausa
            breakpointDialog.dispose();
        });
        
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(continueButton);
        buttonPanel.add(stepButton);
        buttonPanel.add(stopButton);
        
        breakpointDialog.add(new JScrollPane(variableArea), BorderLayout.CENTER);
        breakpointDialog.add(buttonPanel, BorderLayout.SOUTH);
        breakpointDialog.setVisible(true);
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
                debugButton.setEnabled(false);
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
                currentProgram = program;
                
            } catch (Exception parseError) {
                result.append("❌ Error sintáctico: ").append(parseError.getMessage()).append("\n\n");
                erroresTotales.agregarError(0, "Error sintáctico: " + parseError.getMessage(), "Sintáctico");
                program = null;
                currentProgram = null;
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
                    
                } catch (Exception semanticError) {
                    result.append("❌ Error en análisis semántico: ").append(semanticError.getMessage()).append("\n");
                    erroresTotales.agregarError(0, "Error semántico: " + semanticError.getMessage(), "Semántico");
                }
            }
            
            // RESUMEN FINAL
            result.append("\n📊 RESUMEN FINAL\n");
            result.append("================\n");
            
            if (erroresTotales.hayErrores()) {
                result.append("❌ COMPILACIÓN FALLIDA\n");
                result.append("Total de errores: ").append(erroresTotales.getCantidadErrores()).append("\n");
                runButton.setEnabled(false);
                debugButton.setEnabled(false);
                setStatus("Compilación fallida", Color.RED);
                resultArea.setForeground(Color.RED);
            } else {
                result.append("✅ COMPILACIÓN EXITOSA\n");
                result.append("✅ Código listo para ejecutar\n");
                runButton.setEnabled(true);
                debugButton.setEnabled(true);
                setStatus("Compilación exitosa", new Color(0, 100, 0));
                resultArea.setForeground(new Color(0, 100, 0));
            }
            
            resultArea.setText(result.toString());
            
        } catch (Exception ex) {
            System.err.println("ERROR GENERAL: " + ex.getMessage());
            ex.printStackTrace();
            showResult("❌ Error durante el análisis: " + ex.getMessage(), Color.RED);
            runButton.setEnabled(false);
            debugButton.setEnabled(false);
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
            interpreter.setDebugMode(false);
            MineCodeValue result = interpreter.interpret(currentProgram);
            resultArea.append("\n\n✅ Ejecución completada. Resultado: " + 
                (result != null ? result.getValue() : "void"));
            setStatus("Ejecución completada", new Color(0, 100, 0));
        } catch (Exception ex) {
            resultArea.append("\n❌ Error durante la ejecución: " + ex.getMessage());
            setStatus("Error en ejecución", Color.RED);
        }
    }

    private void debugCode() {
        if (currentProgram == null) {
            showResult("❌ Primero debe analizar el código", Color.RED);
            return;
        }
        
        // Configurar debugging
        setupDebugging();
        interpreter.setDebugMode(true);
        
        // Limpiar breakpoints anteriores y configurar algunos por defecto
        disableBreakpointMode();
        interpreter.addBreakpoint(3); // Línea con x = 10
        interpreter.addBreakpoint(4); // Línea con y = 20
        interpreter.addBreakpoint(5); // Línea con resultado = x + y
        
        resultArea.append("\n\n=== 🐛 MODO DEPURACIÓN ===\n");
        resultArea.append("Breakpoints configurados en líneas: 3, 4, 5\n");
        resultArea.append("Iniciando ejecución paso a paso...\n");
        
        new Thread(() -> {
            try {
                MineCodeValue result = interpreter.interpret(currentProgram);
                SwingUtilities.invokeLater(() -> {
                    resultArea.append("\n🐛 Depuración completada. Resultado: " + 
                        (result != null ? result.getValue() : "void"));
                    setStatus("Depuración completada", new Color(0, 100, 0));
                });
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> {
                    resultArea.append("\n❌ Error en depuración: " + ex.getMessage());
                    setStatus("Error en depuración", Color.RED);
                });
            }
        }).start();
        
        setStatus("Modo depuración activado - Breakpoints configurados", new Color(218, 165, 32));
    }

    private void clearAll() {
        codeArea.setText("");
        resultArea.setText("");
        breakpoints.clear();
        breakpointButton.setSelected(false);
        disableBreakpointMode();
        statusLabel.setText("Áreas limpiadas");
        runButton.setEnabled(false);
        debugButton.setEnabled(false);
        currentProgram = null;
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
                new EnhancedAnalizadorGUI().setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
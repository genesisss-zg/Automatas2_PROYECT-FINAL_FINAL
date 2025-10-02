import gui.EnhancedAnalizadorGUI;

public class Main {
    public static void main(String[] args) {
        System.out.println("🚀 Iniciando Compilador MineCode Mejorado...");
        System.out.println("💎 Características disponibles:");
        System.out.println("   • Syntax highlighting avanzado");
        System.out.println("   • Funciones nativas de Minecraft");
        System.out.println("   • Sistema de depuración integrado");
        System.out.println("   • Tipos de datos MineCode");
        
        javax.swing.SwingUtilities.invokeLater(() -> {
            try {
                javax.swing.UIManager.setLookAndFeel(
                    javax.swing.UIManager.getSystemLookAndFeelClassName()
                );
                
                EnhancedAnalizadorGUI gui = new EnhancedAnalizadorGUI();
                gui.setVisible(true);
                
                System.out.println("✅ Interfaz mejorada cargada correctamente");
                
            } catch (Exception e) {
                System.err.println("❌ Error al cargar la interfaz:");
                e.printStackTrace();
            }
        });
    }
}
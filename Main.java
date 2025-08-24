import java.awt.Color;
import javax.swing.JFrame;

class Main {
    public static void main(String args[]) {
        JFrame frame = new JFrame("Chaikin's algorithm");

        DrawingPanel panel = new DrawingPanel();

        frame.setBackground(Color.BLACK);

        frame.add(panel);
        frame.setSize(800, 600);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import javax.swing.JPanel;
import javax.swing.Timer;


class DrawingPanel extends JPanel {
    private ArrayList<Point> points = new ArrayList<>();
    private ArrayList<Point> currentCurve = new ArrayList<>();
    private final int MIN_DISTANCE = 10;
    private String statusMessage = "";
    private boolean canAddPoints = true;
    private boolean isAnimating = false;
    private int animationStep = 0;
    private Timer animationTimer;

    public DrawingPanel() {
        setBackground(Color.BLACK);
        setFocusable(true);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1 && canAddPoints){
                    Point newPoint = new Point(e.getX(), e.getY());
                    if (!isTooClose(newPoint)) {
                        points.add(newPoint);
                        repaint();
                    }
                }
            }
        });

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e){
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                   handleEnterPressed();
                } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE){
                    System.exit(0);
                } else if (e.getKeyCode() == KeyEvent.VK_C) {
                    resetCanvas();
                }
            }
        });

        animationTimer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                performChaikinStep();
            }
        });
    }

    private void handleEnterPressed() {
        if (points.size() <= 0) {
            statusMessage = "Please add some control points first!";
            repaint();
        } else if (points.size() == 1) {
            statusMessage = "Need at least 2 points for a curve";
            repaint();
        } else if (points.size() == 2) {
            drawStraightLine();
        } else {
            startChaikinAnimation();
        }
    }

    private void startChaikinAnimation() {
        if (!isAnimating) {
            isAnimating = true;
            canAddPoints = false;
            animationStep = 0;
            currentCurve = new ArrayList<>(points);
            statusMessage = "";
            repaint();
            animationTimer.start();
        }
    }
    
    private void performChaikinStep() {
        if (animationStep >= 7) {
            animationStep = 0;
            currentCurve = new ArrayList<>(points);
            statusMessage = "";
        } else {
            currentCurve = applyChaikinStep(currentCurve);
            animationStep++;
            statusMessage = "Chaikin's Algorithm - Step " + animationStep;
        }
        repaint();
    }

    private ArrayList<Point> applyChaikinStep(ArrayList<Point> inputPoints) {
        if (inputPoints.size() < 2) return inputPoints;

        ArrayList<Point> newPoints = new ArrayList<>();
        
        newPoints.add(new Point(inputPoints.get(0).x, inputPoints.get(0).y));
        
        for (int i = 0; i < inputPoints.size() - 1; i++) {
            Point p1 = inputPoints.get(i);
            Point p2 = inputPoints.get(i + 1);
            
            double deltaX = p2.x - p1.x;
            double deltaY = p2.y - p1.y;

            double qx = p1.x + 0.25 * deltaX;
            double qy = p1.y + 0.25 * deltaY;
            
            double rx = p1.x + 0.75 * deltaX;
            double ry = p1.y + 0.75 * deltaY;
            
            newPoints.add(new Point((int)Math.round(qx), (int)Math.round(qy)));
            newPoints.add(new Point((int)Math.round(rx), (int)Math.round(ry)));
        }
        
        newPoints.add(new Point(inputPoints.get(inputPoints.size()-1).x, inputPoints.get(inputPoints.size()-1).y));
        
        return newPoints;
    }

    private void resetCanvas() {
        points.clear();
        currentCurve.clear();
        isAnimating = false;
        canAddPoints = true;
        animationStep = 0;
        statusMessage = "";
        if (animationTimer.isRunning()) {
            animationTimer.stop();
        }
        repaint();
    }

    private void drawStraightLine() {
        statusMessage = "";
        canAddPoints = false;
        repaint();
    }

    private boolean isTooClose(Point newPoint) {
        for (Point p : points){
            double distance = Math.sqrt(
                Math.pow(newPoint.x - p.x, 2) + 
                Math.pow(newPoint.y - p.y, 2)
            ); 

            if (distance < MIN_DISTANCE){
                return true;
            }
        }
        return false;
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        g2d.setColor(Color.WHITE);

        for (Point p : points) {
            g2d.drawOval(p.x-3, p.y-3, 6, 6);
        }

        if (points.size() == 2 && !isAnimating && !canAddPoints) {
            g2d.setColor(Color.WHITE);
            Point p1 = points.get(0);
            Point p2 = points.get(1);
            g2d.drawLine(p1.x, p1.y, p2.x, p2.y);
        }

        if (isAnimating && currentCurve.size() > 1) {
            g2d.setColor(Color.WHITE);
            for (int i = 0; i < currentCurve.size() - 1; i++) {
                Point p1 = currentCurve.get(i);
                Point p2 = currentCurve.get(i + 1);
                g2d.drawLine(p1.x, p1.y, p2.x, p2.y);
            }
        }

        drawInstructions(g2d);
    }

    private void drawInstructions(Graphics g) {
        g.setFont(new Font("Arial", Font.BOLD, 14));
        g.setColor(Color.CYAN);

        String[] instructions = {
            "Left Click: Add control point",
            "Enter: Start animation",
            "C: Clear all points", 
            "Escape: Exit program",
        };

        int x = 15;
        int y = 25;
        int lineHeight = 18;

        for (String s : instructions) {
            g.drawString(s, x, y);
            y += lineHeight;
        }

        if (!statusMessage.isEmpty()) {
            g.setColor(Color.RED);
            g.setFont(new Font("Arial", Font.BOLD, 16));
            g.drawString(statusMessage, 15, getHeight() - 50);
        }
    }

    class Point {
        int x,y;
        Point(int p1, int p2) {
            x = p1;
            y = p2;
        }
    }
}
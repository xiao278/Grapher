import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class Grapher {
    private static Expression expression;
    private static JFrame frame;
    private static JPanel canvas;
    private static Color MARKER = new Color(232,232,232);
    private static double offsetX = 0;
    private static double offsetY = 0;
    private static Point lastMousePos;
    private static double SCALE = 0.01;
    private static int samples = 250;

    private static final int LEFT = 0;
    private static final int RIGHT = 1;
    private static final int DOWN = 2;
    private static final int UP = 3;

    public static void main(String[] args) {
        frame = new JFrame("Grapher");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1000,600);
        JPanel gui = new JPanel();
        JTextField input = new JTextField(18);
        JButton inputButton = new JButton("Graph it");
        inputButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                String raw = input.getText();
                if (raw == "") {
                    showError("Input cannot be blank!");
                    return;
                }
                try {
                    expression = new ExpressionBuilder(raw).variable("x").build();
                } catch (IllegalArgumentException e) {
                    showError(e.getMessage());
                    return;
                }
                //temporary
                graph();
            }
        });
        gui.add(input);
        gui.add(inputButton);
        frame.add(gui, BorderLayout.SOUTH);
        frame.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                super.mousePressed(e);
                lastMousePos = e.getPoint();
            }
        });
        frame.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                super.mouseDragged(e);
                offsetX += (lastMousePos.x - e.getX()) / (double) frame.getWidth() * 2 * frame.getWidth() * SCALE;
                offsetY += (e.getY() - lastMousePos.y) / (double) frame.getHeight() * 2 * frame.getHeight() * SCALE;
                lastMousePos = e.getPoint();
                graph();
            }
        });
        canvas = new JPanel();
        canvas.setBackground(Color.WHITE);
        frame.add(canvas);
        frame.setVisible(true);
    }
    private static void showError(String message) {
        JOptionPane.showMessageDialog(null,message,"Error",JOptionPane.ERROR_MESSAGE);
    }
    private static void implicitGraph(){
        showError("not ready");
    }
    private static void graph(){
        double leftBound = offsetX - frame.getWidth() * SCALE;
        double rightBound = offsetX + frame.getWidth() * SCALE;
        double downBound = offsetY - frame.getHeight() * SCALE;
        double upBound = offsetY + frame.getHeight() * SCALE;

        double[] bounds = {leftBound, rightBound, downBound, upBound};
        double widthSize = rightBound - leftBound;
        double dx = widthSize / (samples + 1);
        Graphics graphics = canvas.getGraphics();
        clear();
        drawAxis(bounds);
        graphics.setColor(Color.BLACK);
        for (int i = 0; i < samples + 2; i++) {
            double x = leftBound + (i * dx);
            double y = expression.setVariable("x",x).evaluate();
            if ((y >= downBound) && (y <= upBound)) {
                Point p = cartToPix(x, y, bounds);
                graphics.drawOval(p.x, p.y, 1, 1);
            }
        }
        canvas.paintComponents(graphics);
    }
    private static void clear() {
        Graphics graphics = canvas.getGraphics();
        //Color temp = graphics.getColor();
        graphics.setColor(Color.WHITE);
        graphics.fillRect(0, 0, frame.getWidth(), frame.getHeight());
        //graphics.setColor(temp);
    }
    private static Point cartToPix(double leftBound, double rightBound, double downBound, double upBound, double x, double y) {
        double widthSize = rightBound - leftBound;
        double heightSize = upBound - downBound;
        double yTemp = (y - downBound) / heightSize;
        yTemp *= frame.getHeight();
        double xTemp = (x - leftBound) / widthSize;
        xTemp *= frame.getWidth();

        return new Point((int) Math.round(xTemp), frame.getHeight() - (int) Math.round(yTemp));
    }
    private static Point cartToPix(double x, double y, double... bounds) {
        return cartToPix(bounds[0], bounds[1], bounds[2], bounds[3], x, y);
    }
    private static double[] pixToCart(double leftBound, double rightBound, double downBound, double upBound, int x, int y) {
        double widthSize = rightBound - leftBound;
        double heightSize = upBound - downBound;
        double xTemp = (x / (double) frame.getWidth()) * widthSize + leftBound;
        double yTemp = upBound - (y / (double) frame.getHeight()) * heightSize;
        return new double[]{xTemp, yTemp};
    }
    private static void drawAxis(double... bounds) {
        Graphics graphics = canvas.getGraphics();
        graphics.setColor(MARKER);
        //draw vertical marker line
        Point center = cartToPix( 0, 0, bounds);
        if (bounds[LEFT] <= 0 && bounds[RIGHT] >= 0) {
            graphics.drawLine(center.x, 0, center.x, frame.getHeight());
        }
        if (bounds[DOWN] <= 0 && bounds[UP] >= 0) {
            graphics.drawLine(0, center.y, frame.getWidth(), center.y);
        }
    }
}

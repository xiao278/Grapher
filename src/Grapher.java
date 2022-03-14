import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Grapher {
    private static Expression expression;
    private static JFrame frame;
    private static JPanel canvas;
    private static Color MARKER = new Color(232,232,232);
    public static void main(String[] args) {
        frame = new JFrame("Grapher");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800,600);
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
                graph(-5,5,-10,10, 250);
            }
        });
        gui.add(input);
        gui.add(inputButton);
        frame.add(gui, BorderLayout.SOUTH);
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
    private static void graph(double leftBound, double rightBound, double downBound, double upBound, int samples){
        double[] bounds = {leftBound, rightBound, downBound, upBound};
        double widthSize = rightBound - leftBound;
        double dx = widthSize / (samples + 1);
        Graphics graphics = canvas.getGraphics();
        clear();
        graphics.setColor(MARKER);
        //draw vertical marker line
        Point center = cartToPix( 0, 0, bounds);
        if (leftBound <= 0 && rightBound >= 0) {
            graphics.drawLine(center.x, 0, center.x, frame.getHeight());
        }
        if (downBound <= 0 && upBound >= 0) {
            graphics.drawLine(0, center.y, frame.getWidth(), center.y);
        }
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
}

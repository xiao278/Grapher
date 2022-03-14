import net.objecthunter.exp4j.*;
import java.util.Scanner;

public class Demo {
    public static void main(String[] args) {
        Expression e;
        try{
            e = new ExpressionBuilder("(5x+x^2)/log(x)").variable("x").build();
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }
}


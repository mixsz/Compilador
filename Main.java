import java.util.List;
import lexer.*;

public class Main {
    public static void main(String[] args) {
        String codigo = "SE (x > 5) { x = 1; !}";

        try{
            Lexer lexer = new Lexer(codigo);
            List<Token> tokens = lexer.analisar();
            for(Token t : tokens) System.out.println(t);
        }
        catch(RuntimeException e) {
            System.err.println(e.getMessage()); 
        }
    }
}

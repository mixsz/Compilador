import java.util.List;
import lexer.*;
import parser.*;

public class Main {
    public static void main(String[] args) {
        String codigo = "INICIE: SE (x > 5) { x = 1; }";

        try{
            // LEXER
            System.out.println("\nTokens:");
            Lexer lexer = new Lexer(codigo);
            List<Token> tokens = lexer.analiseLexica();
            for(Token t : tokens) System.out.println(t);
            
            System.out.println("----------------------------");
            
            System.out.println("Regras:");
            Parser parser = new Parser(tokens);
            parser.analiseSintatica();
        }
        catch(RuntimeException e) {
            System.err.println(e.getMessage()); 
        }
    }
}

import java.util.List;
import lexer.*;
import parser.*;

public class Main {
    public static void main(String[] args) {
        String codigo = "INICIE: ENQUANTO (x > 5) { i--; }";
        try{
            // LEXER
            System.out.println("\nTokens:");
            Lexer lexer = new Lexer(codigo);
            List<Token> tokens = lexer.analiseLexica();
            for(Token t : tokens) System.out.println(t);
                        
            Parser parser = new Parser(tokens);
            Tree arvore = parser.analiseSintatica();
            arvore.printTree();
        }
        catch(RuntimeException e) {
            System.err.println(e.getMessage()); 
        }
    }
}

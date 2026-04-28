import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.io.IOException;
import lexer.*;
import parser.*;

public class Main {
    public static void main(String[] args) {

        try{
            String codigo = Files.readString(Path.of("main.67"));
            System.out.println("\nTokens:");
            Lexer lexer = new Lexer(codigo);
            List<Token> tokens = lexer.analiseLexica();
            for(Token t : tokens) System.out.println(t);
                        
            Parser parser = new Parser(tokens);
            Tree arvore = parser.analiseSintatica();
            arvore.printTree();
        }
        catch(IOException e){
            System.err.println("Erro ao ler arquivo: " + e.getMessage());
        }
        catch(RuntimeException e){
            System.err.println(e.getMessage()); 
        }
    }
}

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.io.IOException;

import codegen.CodeGenerator;
import semantic.*;
import lexer.*;
import parser.*;

public class Main {
    public static void main(String[] args) {

        try{
            String codigo = Files.readString(Path.of("main.ABCD"));
            
            //lexico
            System.out.println("\nTokens:");
            Lexer lexer = new Lexer(codigo);
            List<Token> tokens = lexer.analiseLexica();
            for(Token t : tokens) System.out.println(t);
            
            //sintatico
            Parser parser = new Parser(tokens);
            Tree arvore = parser.analiseSintatica();
            arvore.printTree();

            //semantico
            Semantic semantico = new Semantic(arvore);
            semantico.analiseSemantica();
            
            //traducao
            CodeGenerator gerador = new CodeGenerator(arvore);
            String codigoGo = gerador.traduz();
            Files.writeString(Path.of("saida.go"), codigoGo);

            //run
            try{
                System.out.println("_________________________________\n");
                ProcessBuilder processo = new ProcessBuilder("go", "run", "saida.go");
                processo.inheritIO();
                Process process = processo.start();
                process.waitFor(); // faz o processo do java esperar ate o processo do go terminar
            }
            catch(IOException | InterruptedException e){
                System.err.println("Erro ao executar em Go: " + e.getMessage());
            }

        }
        catch(IOException e){
            System.err.println("Erro ao ler arquivo: " + e.getMessage());
        }
        catch(RuntimeException e){
            System.err.println(e.getMessage()); 
        }
    }
}

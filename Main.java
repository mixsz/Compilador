import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import codegen.CodeGenerator;

import java.io.IOException;
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
            
            //traducao
            CodeGenerator gerador = new CodeGenerator(arvore);
            String codigoGo = gerador.traduz();
            Files.writeString(Path.of("saida.go"), codigoGo);

            //run
            try{
                System.out.println("Output:");
                ProcessBuilder p = new ProcessBuilder("go", "run", "saida.go");
                p.inheritIO();
                Process process = p.start();
                process.waitFor();
            }
            catch (IOException | InterruptedException e) {
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

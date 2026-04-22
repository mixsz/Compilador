package parser;
import java.util.List;
import lexer.Token;
// javac -encoding UTF-8 *.java
// java -Dfile.encoding=UTF-8 -cp . Main

public class Parser{
    List<Token> tokens;
    Token token;
    String mensagem;

    public Parser(List<Token> tokens){
        this.tokens = tokens;
    }

    private Token getNextToken(){
        if (tokens.size() > 0) {
            return tokens.remove(0);
        }
        return null;
    }

    public void analiseSintatica(){
        token = getNextToken();
        if(iniciar()){
            if (token.tipo.equals("EOF")){
                System.out.println("\nAnálise Sintática concluída com sucesso!\n");
                return;
            }
        }
        throw new RuntimeException("ERRO SINTATICO: " + mensagem);
    }

    private boolean iniciar(){
        if(token.tipo.equals("inicie")){
            token = getNextToken();
            if(token.tipo.equals("doisP")){
                token = getNextToken();
                return bloco();
            }
            else{
                mensagem = "É necessário utilizar ':' para inicializar o código";
                return false;
            }
        }
        else{
            mensagem = "Inicialização de programa inválido!";
            return false;
        }
    }

    private boolean bloco(){
        if(instrucao()){
            return bloco();
        }
        return true;
    }

    private boolean instrucao(){
        if(declarar()){
            if(token.tipo.equals("fim")){
                token = getNextToken();
                return true;
            }
            else{
                mensagem = "Instrução não finalizada, esperado ';'";
                return false;
            }
        }
        else if(atribuir()){
            if(token.tipo.equals("fim")){
                token = getNextToken();
                return true;
            }
            else{
                mensagem = "Instrução não finalizada, esperado ';'";
                return false;
            }
        }
        else if(crementar()){
            if(token.tipo.equals("fim")){
                token = getNextToken();
                return true;
            }
            else{
                mensagem = "Instrução não finalizada, esperado ';'";
                return false;
            }
        }
        else if(estruturaIf()){
           return true;
        }
        else if(estruturaWhile()){
            return true;
        }
        else if(estruturaFor()){
            return true;
        }
        else if(estruturaEscrever()){
            if(token.tipo.equals("fim")){
                token = getNextToken();
                return true;
            }
            else{
                mensagem = "Instrução não finalizada, esperado ';'";
                return false;
            }
        }
        else if(token.tipo.equals("quebre")){
            token = getNextToken();
            if(token.tipo.equals("fim")){
                token = getNextToken();
                return true;
            }
            else{
                mensagem = "Instrução não finalizada, esperado ';'";
                return false;
            }
        }
        else if(token.tipo.equals("continue")){
            token = getNextToken();
            if(token.tipo.equals("fim")){
                token = getNextToken();
                return true;
            }
            else{
                mensagem = "Instrução não finalizada, esperado ';'";
                return false;
            }
        }
        else if(token.tipo.equals("comentar")){
            token = getNextToken();
            if(token.tipo.equals("fim")){
                token = getNextToken();
                return true;
            }
            else{
                mensagem = "Instrução não finalizada, esperado ';'";
                return false;
            }
        }
        return false;
    }

    private boolean declarar(){
        return true;
    }

    private boolean atribuir(){
        return true;
    }

    private boolean crementar(){
        return true;
    }

    private boolean estruturaIf(){
        return true;
    }

    private boolean estruturaWhile(){
        return true;
    }

    private boolean estruturaFor(){
        return true;
    }

    private boolean estruturaEscrever(){
        return true;
    }


}


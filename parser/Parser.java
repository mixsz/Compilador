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
        else if(comentar()){
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
        return false;
    }

    private boolean expressao(){
        return soma();
    }

    private boolean soma(){
        if(mult()){
           return somaResto();
        }
        return false;
    }

    private boolean somaResto(){
        if(token.tipo.equals("opArit") && token.lexema.equals("+")){
            token = getNextToken();
            if(mult()){
                return somaResto();
            }
            else{
                return false;
            }
        }
        else if(token.tipo.equals("opArit") && token.lexema.equals("-")){
            token = getNextToken();
            if(mult()){
                return somaResto();
            }
            else{
                return false;
            }
        }
        return true;
    }

    private boolean mult(){
        if(valor()){
            return multResto();
        }
        return false;
    }

    private boolean multResto(){
         if(token.tipo.equals("opArit") && token.lexema.equals("*")){
            token = getNextToken();
            if(mult()){
                return multResto();
            }
            else{
                return false;
            }
        }
        else if(token.tipo.equals("opArit") && token.lexema.equals("/")){
            token = getNextToken();
            if(mult()){
                return multResto();
            }
            else{
                return false;
            }
        }
        return true;
    }

    private boolean valor(){
        if(token.tipo.equals("inteiro")){
            token = getNextToken();
            return true;
        }
        else if(token.tipo.equals("decimal")){
            token = getNextToken();
            return true;
        }
         else if(token.tipo.equals("id")){
            token = getNextToken();
            return true;
        }
         else if(token.tipo.equals("abreP")){
            token = getNextToken();
            if(expressao()){
                if(token.tipo.equals("fechaP")){
                    token = getNextToken();
                    return true;
                }
                else{
                    mensagem = "Parentese de fechamento ausente!";
                    return false;
                }
            }
            else{
                return false;
            }
        }
        else if(token.tipo.equals("opArit") && token.lexema.equals("-")){
            token = getNextToken();
            return valor();
        }
        else if(token.tipo.equals("texto")){
            token = getNextToken();
            return true;
        }
        return false;
    }

    private boolean tipos(){
        if(token.tipo.equals("tipoInt") || token.tipo.equals("tipoFloat") || token.tipo.equals("tipoString")){
            token = getNextToken();
            return true;
        }
        return false;
    }

    private boolean declarar(){
        if(tipos()){
            if(token.tipo.equals("id")){
                token = getNextToken();
                return inicializar();
            }
            else{
                mensagem = "Esperado um identificador apos o tipo";
                return false;
            }
        }
        return false;
    }

    private boolean inicializar(){
        if(token.tipo.equals("opAtrib")){
            token = getNextToken();
            return atributo();
        }
        return true;
    }

    private boolean atributo(){
        if(expressao()){
            return true;
        }
        else if(token.tipo.equals("leia")){
            token = getNextToken();
            if(token.tipo.equals("abreP")){
                token = getNextToken();
                if (tipos()){
                    if(token.tipo.equals("fechaP")){
                        token = getNextToken();
                        return true;
                    }
                    else{
                        mensagem = "Parentese de fechamento ausente!";
                        return false;
                    }
                }
                else{
                    mensagem = "É necessário inserir o tipo de variável!";
                    return false;
                }
            }
            mensagem = "Esperado '(' após LEIA!";
            return false;
        }
        return false;
    }

    private boolean atribuir(){
        if(token.tipo.equals("id")){
            token = getNextToken();
            if(token.tipo.equals("opAtrib")){
                token = getNextToken();
                return atributo();
            }
            else{
                mensagem = "Esperado '=' após o identificador!";
                return false;
            }
        }
        return false;
    }

    private boolean comentar(){
        if(token.tipo.equals("comente")){
            token = getNextToken();
            if(token.tipo.equals("texto")){
                token = getNextToken();
                return true;
            }
            else{
                mensagem = "Esperado um texto após COMENTE";
                return false;
            }
        }
        return false;
    }

    private boolean crementar(){
        if(token.tipo.equals("id")){
            token = getNextToken();
            if(token.tipo.equals("opCremento")){
                token = getNextToken();
                return true;
            }
            else{
                return false;
            }
        }
        return false;
    }

    private boolean estruturaIf(){
        if(token.tipo.equals("se")){
            token = getNextToken();
            if(token.tipo.equals("abreP")){
                token = getNextToken();
                if(condicao()){
                    if(token.tipo.equals("fechaP")){
                        token = getNextToken();
                        if(token.tipo.equals("abreC")){
                            token = getNextToken();
                            if(bloco()){
                                if(token.tipo.equals("fechaC")){
                                    token = getNextToken();
                                    if(estruturaElseif()){
                                        return estruturaElse();
                                    }
                                    return false;
                                }
                                mensagem = "Esperado '}' para fechar o SE";
                                return false;
                            }
                        }
                        mensagem = "Esperado '{' para abrir o bloco do SE";
                        return false;
                    }
                    mensagem = "Esperado ')' para fechar a condicao do SE";
                    return false;
                }
                mensagem = "Condicao invalida no SE";
                return false;
            }
            mensagem = "Esperado '(' apos SE";
            return false;
        }
        return false;
    }

    private boolean estruturaElseif(){
        if(token.tipo.equals("senaose")){
            token = getNextToken();
            if(token.tipo.equals("abreP")){
                token = getNextToken();
                if(condicao()){
                    if(token.tipo.equals("fechaP")){
                        token = getNextToken();
                        if(token.tipo.equals("abreC")){
                            token = getNextToken();
                            if(bloco()){
                                if(token.tipo.equals("fechaC")){
                                    token = getNextToken();
                                    return estruturaElseif();
                                }
                                mensagem = "Esperado '}' para fechar o SENAOSE";
                                return false;
                            }
                        }
                        mensagem = "Esperado '{' para abrir o bloco do SENAOSE";
                        return false;
                    }
                    mensagem = "Esperado ')' para fechar a condicao do SENAOSE";
                    return false;
                }
                mensagem = "Condicao invalida no SENAOSE";
                return false;
            }
            mensagem = "Esperado '(' apos SENAOSE";
            return false;
        }
        return true;
    }

    private boolean estruturaElse(){
        if(token.tipo.equals("senao")){
            token = getNextToken();
            if(token.tipo.equals("abreC")){
                token = getNextToken();
                if(bloco()){
                    if(token.tipo.equals("fechaC")){
                        token = getNextToken();
                        return true;
                    }
                    else{
                        mensagem = "Esperado '}' para fechar SENAO";
                        return false;
                    }
                }
                else{
                    return false;
                }
            }
            else{
                mensagem = "Esperado '{' para abrir o bloco SENAO";
                return false;
            }
        }
        return true;
    }

    private boolean condicao(){
        
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


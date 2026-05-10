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

    public Tree analiseSintatica(){
        Node raiz = new Node("iniciar");
        token = getNextToken();
        Tree arvore = new Tree(raiz);
        if(iniciar(raiz)){
            if (token.tipo.equals("EOF")){
                System.out.println("\nAnálise Sintática concluída com sucesso!\n");
                return arvore;
            }
        }
        if(mensagem == null) mensagem = "Token em excesso -> '" + token.lexema + "'";
        throw new RuntimeException("ERRO SINTATICO: " + mensagem);
    }

    private boolean iniciar(Node pai){
        if(token.tipo.equals("inicie")){
            pai.addNode(token.lexema);
            token = getNextToken();
            if(token.tipo.equals("doisP")){
                pai.addNode(token.lexema);
                token = getNextToken();
                return bloco(pai);
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

    private boolean bloco(Node pai){
        Node noBloco = new Node("bloco");
        if(instrucao(noBloco)){
            pai.addNode(noBloco);
            return bloco(pai);
        }
        if(mensagem != null) return false;
        return true;
    }

    private boolean instrucao(Node pai){
        Node noInstrucao = pai.addNode("instrucao");
        if(declarar(noInstrucao)){
            if(token.tipo.equals("fim")){
                noInstrucao.addNode(token.lexema);
                token = getNextToken();
                return true;
            }
            else{
                if(mensagem == null) mensagem = "Instrução não finalizada, esperado ';'";
                return false;
            }
        }
        else if(token.tipo.equals("id")){
            Token proximo = tokens.get(0); // lookahead 
            if(proximo.tipo.equals("opCremento")){
                if(crementar(noInstrucao)){
                    if(token.tipo.equals("fim")){
                        noInstrucao.addNode(token.lexema);
                        token = getNextToken();
                        return true;
                    }
                    else{
                        mensagem = "Esperado ';'";
                        return false;
                    }
                }
            }
            else{
                if(atribuir(noInstrucao)){
                    if(token.tipo.equals("fim")){
                        noInstrucao.addNode(token.lexema);
                        token = getNextToken();
                        return true;
                    }
                    else{
                        mensagem = "Esperado ';'";
                        return false;
                    }
                }
            }
        }
        else if(estruturaIf(noInstrucao)){
            return true;
        }
        else if(estruturaWhile(noInstrucao)){
            return true;
        }
        else if(estruturaFor(noInstrucao)){
            return true;
        }
        else if(estruturaEscrever(noInstrucao)){
            if(token.tipo.equals("fim")){
                noInstrucao.addNode(token.lexema);
                token = getNextToken();
                return true;
            }
            else{
                mensagem = "Instrução não finalizada, esperado ';'";
                return false;
            }
        }
        else if(comentar(noInstrucao)){
            if(token.tipo.equals("fim")){
                noInstrucao.addNode(token.lexema);
                token = getNextToken();
                return true;
            }
            else{
                mensagem = "Instrução não finalizada, esperado ';'";
                return false;
            }
        }
        else if(token.tipo.equals("quebre")){
            noInstrucao.addNode(token.lexema);
            token = getNextToken();
            if(token.tipo.equals("fim")){
                noInstrucao.addNode(token.lexema);
                token = getNextToken();
                return true;
            }
            else{
                mensagem = "Instrução não finalizada, esperado ';'";
                return false;
            }
        }
        else if(token.tipo.equals("continue")){
            noInstrucao.addNode(token.lexema);
            token = getNextToken();
            if(token.tipo.equals("fim")){
                noInstrucao.addNode(token.lexema);
                token = getNextToken();
                return true;
            }
            else{
                mensagem = "Instrução não finalizada, esperado ';'";
                return false;
            }
        }
        if(mensagem == null && !token.tipo.equals("fechaC") && !token.tipo.equals("EOF")){  
            mensagem = "Token em excesso -> '" + token.lexema + "'";
        }
        return false;
    }

    private boolean expressao(Node pai){
        //first
        if(token.tipo.equals("inteiro") || token.tipo.equals("decimal") ||
            token.tipo.equals("id") || token.tipo.equals("abreP") ||
            token.tipo.equals("texto") ||
            (token.tipo.equals("opArit") && token.lexema.equals("-"))){
            Node noExpressao = pai.addNode("expressao");
            return soma(noExpressao);
        }
        return false;
    }

    private boolean soma(Node pai){
        Node noSoma = pai.addNode("soma");
        if(mult(noSoma)){
           return somaResto(noSoma);
        }
        return false;
    }

    private boolean somaResto(Node pai){
        if(token.tipo.equals("opArit") && token.lexema.equals("+")){
            Node noSomaResto = pai.addNode("somaResto");
            Node noOpArit = noSomaResto.addNode("opArit");
            noOpArit.addNode(token.lexema);
            token = getNextToken();
            if(mult(noSomaResto)){
                return somaResto(pai);
            }
            if(mensagem == null) mensagem = "Expressão inválida após '+'";
            return false;
        }
        else if(token.tipo.equals("opArit") && token.lexema.equals("-")){
            Node noSomaResto = pai.addNode("somaResto");
            Node noOpArit = noSomaResto.addNode("opArit");
            noOpArit.addNode(token.lexema);
            token = getNextToken();
            if(mult(noSomaResto)){
                return somaResto(pai);
            }
            if(mensagem == null) mensagem = "Expressão inválida após '-'";
            return false;
        }
        return true;
    }

    private boolean mult(Node pai){
        Node noMult = pai.addNode("mult");
        if(valor(noMult)){
            return multResto(noMult);
        }
        return false;
    }

    private boolean multResto(Node pai){
        if(token.tipo.equals("opArit") && token.lexema.equals("*")){
            Node noMultResto = pai.addNode("multResto");
            Node noOpArit = noMultResto.addNode("opArit");
            noOpArit.addNode(token.lexema);
            token = getNextToken();
            if(mult(noMultResto)){
                return multResto(pai);
            }
            if(mensagem == null) mensagem = "Expressão inválida após '*'";
            return false;
        }
        else if(token.tipo.equals("opArit") && token.lexema.equals("/")){
            Node noMultResto = pai.addNode("multResto");
            Node noOpArit = noMultResto.addNode("opArit");
            noOpArit.addNode(token.lexema);
            token = getNextToken();
            if(mult(noMultResto)){
                return multResto(pai);
            }
            if(mensagem == null) mensagem = "Expressão inválida após '/'";
            return false;
        }
        return true;
    }

    private boolean valor(Node pai){
        Node noValor = pai.addNode("valor");
        if(token.tipo.equals("inteiro")){
            Node noInteiro = noValor.addNode("inteiro");
            noInteiro.addNode(token.lexema);
            token = getNextToken();
            return true;
        }
        else if(token.tipo.equals("decimal")){
            Node noInteiro = noValor.addNode("decimal");
            noInteiro.addNode(token.lexema);
            token = getNextToken();
            return true;
        }
         else if(token.tipo.equals("id")){
            Node noId = noValor.addNode("id");
            noId.addNode(token.lexema);
            token = getNextToken();
            return true;
        }
         else if(token.tipo.equals("abreP")){
            noValor.addNode(token.lexema);
            token = getNextToken();
            if(expressao(noValor)){
                if(token.tipo.equals("fechaP")){
                    noValor.addNode(token.lexema);
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
            noValor.addNode(token.lexema);
            token = getNextToken();
            return valor(noValor);
        }
        else if(token.tipo.equals("texto")){
            Node noTexto = noValor.addNode("texto");
            noTexto.addNode(token.lexema);
            token = getNextToken();
            return true;
        }
        return false;
    }

    private boolean tipos(Node pai){
        if(token.tipo.equals("tipoInt") || token.tipo.equals("tipoFloat") || token.tipo.equals("tipoString")){
            Node noTipos = pai.addNode("tipos");
            noTipos.addNode(token.lexema);
            token = getNextToken();
            return true;
        }
        return false;
    }

    private boolean declarar(Node pai){
        // first
        if(token.tipo.equals("tipoInt") || 
        token.tipo.equals("tipoFloat") || 
        token.tipo.equals("tipoString")){
            Node noDeclarar = pai.addNode("declarar");
            if(tipos(noDeclarar)){
                if(token.tipo.equals("id")){
                    Node noId = noDeclarar.addNode("id");
                    noId.addNode(token.lexema);
                    token = getNextToken();
                    return inicializar(noDeclarar);
                }
                else{
                    mensagem = "Esperado um identificador após o tipo";
                    return false;
                }
            }
        }
        return false;
    }

    private boolean inicializar(Node pai){
        if(token.tipo.equals("opAtrib")){
            Node noInicializar = pai.addNode("inicializar");
            Node noOpAtrib = noInicializar.addNode("opAtrib");
            noOpAtrib.addNode(token.lexema);
            token = getNextToken();
            return atributo(noInicializar);
        }
        if(!token.tipo.equals("fim") && !token.tipo.equals("opAtrib")){ // caso INTEIRO x 1;
            mensagem = "Declaração inválida!";
            return false;
        }
        return true;
    }

    private boolean atributo(Node pai){
        Node noAtributo = pai.addNode("atributo");
        if(expressao(noAtributo)){
            return true;
        }
        else if(token.tipo.equals("leia")){
            noAtributo.addNode(token.lexema);
            token = getNextToken();
            if(token.tipo.equals("abreP")){
                noAtributo.addNode(token.lexema);
                token = getNextToken();
                if (tipos(noAtributo)){
                    if(token.tipo.equals("fechaP")){
                        noAtributo.addNode(token.lexema);
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
        if(mensagem == null) mensagem = "Erro de atribuição!";
        return false;
    }

    private boolean atribuir(Node pai){
        if(token.tipo.equals("id")){
            Node noAtribuir = pai.addNode("atribuir");
            Node noId = noAtribuir.addNode("id");
            noId.addNode(token.lexema);
            Token a = token;
            token = getNextToken();
            if(token.tipo.equals("opAtrib")){
                Node noOpAtrib = noAtribuir.addNode("opAtrib");
                noOpAtrib.addNode(token.lexema);
                token = getNextToken();
                a = null;
                return atributo(noAtribuir);
            }
            else{
                mensagem = "Instrução inválida após '" + a.lexema + "'";
                return false;
            }
        }
        return false;
    }

    private boolean comentar(Node pai){
        if(token.tipo.equals("comente")){
            Node noComentar = pai.addNode("comentar");
            noComentar.addNode(token.lexema);
            token = getNextToken();
            if(token.tipo.equals("texto")){
                Node noTexto = noComentar.addNode("texto");
                noTexto.addNode(token.lexema);
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

    private boolean crementar(Node pai){
        if(token.tipo.equals("id")){
            Node noCrementar = pai.addNode("crementar");
            Node noId = noCrementar.addNode("id");
            noId.addNode(token.lexema);
            token = getNextToken();
            if(token.tipo.equals("opCremento")){
                Node noOpCremento = noCrementar.addNode("opCremento");
                noOpCremento.addNode(token.lexema);
                token = getNextToken();
                return true;
            }
            else{
                return false;
            }
        }
        return false;
    }

    private boolean estruturaIf(Node pai){
        if(token.tipo.equals("se")){
            Node noEstruturaIf = pai.addNode("estruturaIf");
            noEstruturaIf.addNode(token.lexema);
            token = getNextToken();
            if(token.tipo.equals("abreP")){
                noEstruturaIf.addNode(token.lexema);
                token = getNextToken();
                if(condicao(noEstruturaIf)){
                    if(token.tipo.equals("fechaP")){
                        noEstruturaIf.addNode(token.lexema);
                        token = getNextToken();
                        if(token.tipo.equals("abreC")){
                            noEstruturaIf.addNode(token.lexema);
                            token = getNextToken();
                            if(bloco(noEstruturaIf)){
                                if(token.tipo.equals("fechaC")){
                                    noEstruturaIf.addNode(token.lexema);
                                    token = getNextToken();
                                    if(estruturaElseif(noEstruturaIf)){
                                        return estruturaElse(noEstruturaIf);
                                    }
                                    return false;
                                }
                                mensagem = "Esperado '}' para fechar o SE";
                                return false;
                            }
                        }
                        if(mensagem == null) mensagem = "Esperado '{' para abrir o bloco do SE";
                        return false;
                    }
                    mensagem = "Esperado ')' para fechar a condicao do SE";
                    return false;
                }
                if(mensagem == null) mensagem = "Condicao invalida no SE";
                return false;
            }
            mensagem = "Esperado '(' apos SE";
            return false;
        }
        return false;
    }

    private boolean estruturaElseif(Node pai){
        if(token.tipo.equals("senaose")){
            Node noEstruturaElseIf = pai.addNode("estruturaElseif");
            noEstruturaElseIf.addNode(token.lexema);
            token = getNextToken();
            if(token.tipo.equals("abreP")){
                noEstruturaElseIf.addNode(token.lexema);
                token = getNextToken();
                if(condicao(noEstruturaElseIf)){
                    if(token.tipo.equals("fechaP")){
                        noEstruturaElseIf.addNode(token.lexema);
                        token = getNextToken();
                        if(token.tipo.equals("abreC")){
                            noEstruturaElseIf.addNode(token.lexema);
                            token = getNextToken();
                            if(bloco(noEstruturaElseIf)){
                                if(token.tipo.equals("fechaC")){
                                    noEstruturaElseIf.addNode(token.lexema);
                                    token = getNextToken();
                                    return estruturaElseif(pai);
                                }
                                mensagem = "Esperado '}' para fechar o SENAOSE";
                                return false;
                            }
                        }
                        if(mensagem == null) mensagem = "Esperado '{' para abrir o bloco do SENAOSE";;
                        return false;
                    }
                    mensagem = "Esperado ')' para fechar a condicao do SENAOSE";
                    return false;
                }
                if(mensagem == null) mensagem = "Condicao invalida no SENAOSE";
                return false;
            }
            mensagem = "Esperado '(' apos SENAOSE";
            return false;
        }
        return true;
    }

    private boolean estruturaElse(Node pai){
        if(token.tipo.equals("senao")){
            Node noEstruturaElse = pai.addNode("estruturaElse");
            noEstruturaElse.addNode(token.lexema);
            token = getNextToken();
            if(token.tipo.equals("abreC")){
                noEstruturaElse.addNode(token.lexema);
                token = getNextToken();
                if(bloco(noEstruturaElse)){
                    if(token.tipo.equals("fechaC")){
                        noEstruturaElse.addNode(token.lexema);
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

    private boolean condicao(Node pai){
        Node noCondicao = pai.addNode("condicao");
        if(termoE(noCondicao)){
            return(condicaoResto(noCondicao));
        }
        if(mensagem == null) mensagem = "Condição inválida!";
        return false;
    }

    private boolean condicaoResto(Node pai){
        if(token.tipo.equals("ou")){
            Node noCondicaoResto = pai.addNode("condicaoResto");
            noCondicaoResto.addNode(token.lexema);
            token = getNextToken();
            if(termoE(noCondicaoResto)){
                return condicaoResto(pai);
            }
            else{
                return false;
            }
        }
        return true;
    }

    private boolean termoE(Node pai){
        Node noTermoE = pai.addNode("termoE");
        if(relacional(noTermoE)){
            return termoEResto(noTermoE);
        }
        return false;
    }

    private boolean termoEResto(Node pai){
        if(token.tipo.equals("e")){
            Node noTermoEResto = pai.addNode("termoEResto");
            noTermoEResto.addNode(token.lexema);
            token = getNextToken();
            if(relacional(noTermoEResto)){
                return termoEResto(pai);
            }
            else{
                return false;
            }
        }
        if(!token.tipo.equals("ou") && 
        !token.tipo.equals("fechaP") && 
        !token.tipo.equals("abreC") &&  
        !token.tipo.equals("fechaC") &&
        !token.tipo.equals("fim") &&
        !token.tipo.equals("EOF")){
            mensagem = "Esperado 'E' ou 'OU'!";
            return false;
        }
        return true;
    }

    private boolean relacional(Node pai){
        Node noRelacional = pai.addNode("relacional");
        if(token.tipo.equals("abreP")){
            noRelacional.addNode(token.lexema);
            token = getNextToken();
            if(condicao(noRelacional)){
                if(token.tipo.equals("fechaP")){
                    noRelacional.addNode(token.lexema);
                    token = getNextToken();
                    return true;
                }
                else{
                    mensagem = "Esperado ')' para fechar a condição!";
                    return false;
                }
            }
            mensagem = "Condição inválida!";
            return false;
        }
        else{
            if(expressao(noRelacional)){
                if(token.tipo.equals("opRelac")){
                    Node noOpRelac = noRelacional.addNode("opRelac");
                    noOpRelac.addNode(token.lexema);
                    token = getNextToken();
                    return expressao(noRelacional);
                }
                else{
                    mensagem = "Operador relacional esperado!";
                    return false;
                }
            }
            return false;
        }
    }


    private boolean estruturaWhile(Node pai){
        if(token.tipo.equals("enquanto")){
            Node noEstruturaWhile = pai.addNode("estruturaWhile");
            noEstruturaWhile.addNode(token.lexema);
            token = getNextToken();
            if(token.tipo.equals("abreP")){
                noEstruturaWhile.addNode(token.lexema);
                token = getNextToken();
                if(condicao(noEstruturaWhile)){
                    if(token.tipo.equals("fechaP")){
                        noEstruturaWhile.addNode(token.lexema);
                        token = getNextToken();
                        if(token.tipo.equals("abreC")){
                            noEstruturaWhile.addNode(token.lexema);
                            token = getNextToken();
                            if(bloco(noEstruturaWhile)){
                                if(token.tipo.equals("fechaC")){
                                    noEstruturaWhile.addNode(token.lexema);
                                    token = getNextToken();
                                    return true;
                                }
                                else{
                                    mensagem = "Esperado '}' para fechar bloco!";
                                    return false;
                                }
                            }
                            else{
                                return false;
                            }
                        }
                        else{
                            mensagem = "Esperado '{' para abrir bloco!";
                            return false;
                        }
                    }
                    else{
                        mensagem = "Esperado ')' para fechar a condição!";
                        return false;
                    }
                }
                else{
                    return false;
                }
            }
            else{
                mensagem = "Esperado '(' após ENQUANTO!";
                return false;
            }
        }
        return false;
    }

    private boolean estruturaFor(Node pai){
        if(token.tipo.equals("para")){
            Node noEstruturaFor = pai.addNode("estruturaFor");
            noEstruturaFor.addNode(token.lexema);
            token = getNextToken();
            if(token.tipo.equals("abreP")){
                noEstruturaFor.addNode(token.lexema);
                token = getNextToken();
                if(comeco(noEstruturaFor)){
                    if(token.tipo.equals("fim")){
                        noEstruturaFor.addNode(token.lexema);
                        token = getNextToken();
                        if(condicao(noEstruturaFor)){
                            if(token.tipo.equals("fim")){
                                noEstruturaFor.addNode(token.lexema);
                                token = getNextToken();
                                if(finall(noEstruturaFor)){
                                    if(token.tipo.equals("fechaP")){
                                        noEstruturaFor.addNode(token.lexema);
                                        token = getNextToken();
                                        if(token.tipo.equals("abreC")){
                                            noEstruturaFor.addNode(token.lexema);
                                            token = getNextToken();
                                            if(bloco(noEstruturaFor)){
                                                if(token.tipo.equals("fechaC")){
                                                    noEstruturaFor.addNode(token.lexema);
                                                    token = getNextToken();
                                                    return true;
                                                }
                                                mensagem = "Esperado '}' para fechar o FOR!";
                                                return false;
                                            }
                                            return false;
                                        }
                                        mensagem = "Esperado '{' para abrir o bloco do FOR!";
                                        return false;
                                    }
                                    mensagem = "Esperado ')' para fechar o FOR!";
                                    return false;
                                }
                                return false;
                            }
                            if(mensagem == null) mensagem = "Esperado ';' após a condição do FOR!";
                            return false;
                        }
                        if(mensagem == null) mensagem = "Condição inválida no FOR!";
                        return false;
                    }
                    mensagem = "Esperado ';' após o início do FOR!";
                    return false;
                }
                return false;
            }
            mensagem = "Esperado '(' após PARA!";
            return false;
        }
        return false;
    }

    private boolean comeco(Node pai){
        Node noComeco = pai.addNode("comeco");
        if(declarar(noComeco)){
            return true;
        }
        return atribuir(noComeco);
    }

    private boolean finall(Node pai){
        Node noFinall = pai.addNode("finall");
        if(token.tipo.equals("id")){ // lookahead
            Token proximo = tokens.get(0);
            if(proximo.tipo.equals("opCremento")){
                return crementar(noFinall);
            }
            else{
                return atribuir(noFinall);
            }
        }
        mensagem = "Esperado incremento ou atribuição no FOR!";
        return false;
    }

    private boolean estruturaEscrever(Node pai){
        if(token.tipo.equals("escreva")){
            Node noEstruturaEscrever = pai.addNode("estruturaEscrever");
            noEstruturaEscrever.addNode(token.lexema);
            token = getNextToken();
            if(token.tipo.equals("abreP")){
                noEstruturaEscrever.addNode(token.lexema);
                token = getNextToken();
                if(expressao(noEstruturaEscrever)){
                    if(restoTexto(noEstruturaEscrever)){
                        if(token.tipo.equals("fechaP")){
                            noEstruturaEscrever.addNode(token.lexema);
                            token = getNextToken();
                            return true;
                        }
                        if(mensagem == null) mensagem = "Esperado ')' para fechar o ESCREVA!";
                        return false;       
                    }
                    return false;
                }
                mensagem = "Esperado uma expressão após '('";
                return false;
            }
            mensagem = "Esperado '(' após ESCREVA!";
            return false;
        }
        return false;
    }

    private boolean restoTexto(Node pai){
        if(token.lexema.equals("+")){
            Node noRestoTexto = pai.addNode("restoTexto");
            noRestoTexto.addNode(token.lexema);
            token = getNextToken();
            if(expressao(noRestoTexto)){
                return restoTexto(pai);
            }
            else{
                mensagem = "Esperado uma expressão após '+'";
                return false;
            }
        }
        if(token.tipo.equals("id") || token.tipo.equals("texto")){
            mensagem = "Esperado '+' para concatenação dos argumentos da instrução ESCREVA";
        }
        return true;
    }
}
package semantic;

import java.util.HashMap;
import java.util.Stack;


import parser.Node;
import parser.Tree;

public class Semantic{
    Tree arvore;
    boolean loop = false;
    Stack<HashMap<String, String[]>> pilha = new Stack<>(); //  pilha de hash
    /*
    * pilha:[ {"x":["INTEIRO","usado"], "y":["INTEIRO","nao_usado"]} , {"z":["INTEIRO","nao_usado"]} ]
*               id    tipo      uso                                  novo escopo
    */


    public Semantic(Tree arvore){
        this.arvore = arvore;
        pilha.push(new HashMap<>());
    }

    public void analiseSemantica(){
        analisarNo(arvore.root);
        verificarUso();
    }

    private void analisarNo(Node no){
        switch(no.nome){
            case "declarar":
                analisarDeclarar(no);
                break;

            case "atribuir":
                analisarAtribuir(no);
                break;

            case "estruturaFor":
                analisarFor(no);
                break;

            case "crementar":
                analisarCrementar(no);
                break;

            case "estruturaEscrever":
                analisarEscrever(no);
                break;
                
            case "estruturaIf":
            case "estruturaWhile":
                analisarBloco(no);
                break;

            case "QUEBRE":
            case "CONTINUE":
                if(!loop){
                    throw new RuntimeException("ERRO SEMÂNTICO: '" + no.nome + "' fora de um laço!");
                }
                break;

            default:
                for(Node filho : no.nodes){
                    analisarNo(filho);
                }
                break;
        }
    }

    private boolean existe(String id){
        for(HashMap<String, String[]> tabela : pilha){ // verifica se o id ja existe na pilha (ja foi declarado)
            if(tabela.containsKey(id)){
                return true;
            }
        }
        return false;
    }

    private String buscaTipo(String id){
        for(HashMap<String, String[]> tabela : pilha){
            if(tabela.containsKey(id)){
                return tabela.get(id)[0]; // retorna o tipo
            }
        }
        return "";
    }

    private void marcarUsado(String id){
        for(HashMap<String, String[]> tabela : pilha){
            if(tabela.containsKey(id)){
                tabela.get(id)[1] = "usado";
                return;
            }
        }
    }

    private void verificarUso(){
        for(String id : pilha.peek().keySet()){
            if(pilha.peek().get(id)[1].equals("nao_usado")){
                throw new RuntimeException("ERRO SEMÂNTICO: variável '" + id + "' declarada mas não utilizada!");
            }
        }
    }

    private void analisarDeclarar(Node no){
        String tipo = "";
        String id = "";

        for(Node filho : no.nodes){
            if(filho.nome.equals("tipos")){
                tipo = filho.nodes.get(0).nome;
            }
            else if(filho.nome.equals("id")){
                id = filho.nodes.get(0).nome;
            }
        }

        if(existe(id)){
            throw new RuntimeException("ERRO SEMÂNTICO: variável " + id + " já foi declarada!");
        }

        pilha.peek().put(id, new String[]{tipo, "nao_usado"}); // adiciona antes de analisar a expressao para casos como INTEIRO x = x + 1 (o x n vai dar problema)

        for(Node filho : no.nodes){
            if(filho.nome.equals("inicializar")){
                Node atributo = filho.nodes.get(1);
                if(atributo.nodes.get(0).nome.equals("LEIA")){ // verifica se o tipo declarado é o mesmo tipo do LEIA
                    String tipoLeia = atributo.nodes.get(2).nodes.get(0).nome;
                    if(!tipoLeia.equals(tipo)){
                        throw new RuntimeException("ERRO SEMÂNTICO: tipo incompatível no LEIA de '" + id + "'");
                    }
                }
                else{
                    String tipoValor = analisarExpressao(atributo);
                    if(!tipoValor.isEmpty() && !tipoValor.equals(tipo)){
                        throw new RuntimeException("ERRO SEMÂNTICO: tipo incompatível na declaração de '" + id + "'");
                    }
                }
            }
        }
    }

    private void analisarAtribuir(Node no){
        String id = "";
        for(Node filho : no.nodes){
            if(filho.nome.equals("id")){
                id = filho.nodes.get(0).nome;
            }
        }

        if(!existe(id)){
            throw new RuntimeException("ERRO SEMÂNTICO: variável '" + id + "' não foi declarada!");
        }

        String tipoId = buscaTipo(id);

        for(Node filho : no.nodes){
            if(filho.nome.equals("atributo")){
                if(filho.nodes.get(0).nome.equals("LEIA")){ // verifica se o tipo declarado é o mesmo tipo do LEIA
                    String tipoLeia = filho.nodes.get(2).nodes.get(0).nome;
                    if(!tipoLeia.equals(tipoId)){
                        throw new RuntimeException("ERRO SEMÂNTICO: tipo incompatível no LEIA de '" + id + "'");
                    }
                }
                else{
                    String tipoValor = analisarExpressao(filho);
                    if(!tipoValor.isEmpty() && !tipoValor.equals(tipoId)){
                        throw new RuntimeException("ERRO SEMÂNTICO: tipo incompatível na atribuição de '" + id + "'");
                    }
                }
            }
        }
    }

    private void analisarBloco(Node no){
        boolean eraLoop = loop;
        if(no.nome.equals("estruturaWhile")){ // verifica while pra ativar o uso do break e continue
            loop = true;
        }
        pilha.push(new HashMap<>());
        for(Node filho : no.nodes){
            if(filho.nome.equals("bloco")){
                pilha.push(new HashMap<>());  // escopo do bloco
                analisarNo(filho);
                verificarUso();
                pilha.pop();
            }
            else if(filho.nome.equals("condicao")){
                analisarExpressao(filho);
            }
            else if(filho.nome.equals("estruturaElseif")){ 
                analisarBloco(filho); // reutiliza
            }
            else if(filho.nome.equals("estruturaElse")){ 
                analisarBloco(filho);
            }
            else{
                analisarNo(filho);
            }
        }
        verificarUso();
        pilha.pop();
        loop = eraLoop;
    }

    private void analisarFor(Node no){
        boolean eraLoop = loop;
        loop = true;
        pilha.push(new HashMap<>());  // escopo do parenteses do for: 'for(INTEIRO i = .....)'

        for(Node filho : no.nodes){
            if(filho.nome.equals("comeco")){
                analisarNo(filho.nodes.get(0));
            }
            else if(filho.nome.equals("condicao")){  // pra dar como "usado" dentro do for(aqui) 
                analisarExpressao(filho);
            }
            else if(filho.nome.equals("bloco")){
                pilha.push(new HashMap<>());  // escopo do bloco
                for(Node neto : filho.nodes){
                    analisarNo(neto);
                }
                verificarUso();
                pilha.pop();
            }
            else{
                analisarNo(filho);
            }
        }
        verificarUso();
        loop = eraLoop;
        pilha.pop(); 
    }

    private void analisarCrementar(Node no){
        String id = no.nodes.get(0).nodes.get(0).nome;
        if(!existe(id)){
            throw new RuntimeException("ERRO SEMÂNTICO: variável '" + id + "' não foi declarada!");
        }
        marcarUsado(id);
    }

    private String analisarExpressao(Node no){
        if(no.nome.equals("id")){
            String id = no.nodes.get(0).nome;
            if(!existe(id)){
                throw new RuntimeException("ERRO SEMÂNTICO: variável '" + id + "' não foi declarada!");
            }
            marcarUsado(id); // marca a variavel como usada
            return buscaTipo(id);
        }
        if(no.nome.equals("inteiro")) return "INTEIRO";
        if(no.nome.equals("decimal")) return "DECIMAL";
        if(no.nome.equals("texto"))   return "TEXTO";

        if(no.nome.equals("multResto")){ // parte que vai verificar se possui / ou * em expressoes de string
            String operador = no.nodes.get(0).nodes.get(0).nome; 
            Node operando = no.nodes.get(1);

            // antes de verificar string, verifica se possui divisao por 0 (literal)
            if(operador.equals("/") && operando.nodes.isEmpty() == false){
                if(operando.nome.equals("mult")){
                    Node valor = operando.nodes.get(0); // valor
                    if(!valor.nodes.isEmpty() && valor.nodes.get(0).nome.equals("inteiro")){
                        if(valor.nodes.get(0).nodes.get(0).nome.equals("0")){
                            throw new RuntimeException("ERRO SEMÂNTICO: divisão por zero!");
                        }
                    }
                }
            }

            String opArit = analisarExpressao(no.nodes.get(1));
            if(opArit.equals("TEXTO")){
                throw new RuntimeException("ERRO SEMÂNTICO: operador '" + operador + "' não pode ser usado com TEXTO!");
            }
            return opArit;
        }

        if(no.nome.equals("somaResto")){ // parte que vai verificar se possui - em expressoes de string
            String operador = no.nodes.get(0).nodes.get(0).nome;
            String opArit = analisarExpressao(no.nodes.get(1));
            if(operador.equals("-") && opArit.equals("TEXTO")){
                throw new RuntimeException("ERRO SEMÂNTICO: operador '-' não pode ser usado com TEXTO!");
            }
            return opArit;
        }

        String tipoAtual = "";
        for(Node filho : no.nodes){ // desce de soma, mult, valor, etc
            String tipo = analisarExpressao(filho);
            if(!tipo.isEmpty()){ // ignora nó q n tem tipo, exemplo: +, -, ....
                if(tipoAtual.isEmpty()){
                    tipoAtual = tipo; // tudo vai ser em relacao ao primeiro tipo encontrado
                }
                else if(!tipoAtual.equals(tipo)){
                    throw new RuntimeException("ERRO SEMÂNTICO: tipos incompatíveis na expressão!");
                }
            }
        }
        return tipoAtual;
    }

    private void analisarEscrever(Node no){
        for(Node filho : no.nodes){
            if(filho.nome.equals("expressao")){
                analisarExpressaoEscrever(filho); // precisa fazer uma verificacao inididual porque nesse caso os tipos podem ser diferentes
            }
        }
    }

    private String analisarExpressaoEscrever(Node no){
        if(no.nome.equals("expressao")){
            for(Node filho : no.nodes){
                analisarExpressaoEscrever(filho);
            }
            return "";
        }

        if(no.nome.equals("soma")){
            String tipoEsq = analisarExpressao(no.nodes.get(0)); // tipo do primeiro mult
            for(Node filho : no.nodes){
                if(filho.nome.equals("somaResto")){
                    String operador = filho.nodes.get(0).nodes.get(0).nome;
                    if(operador.equals("-") && tipoEsq.equals("TEXTO")){ // verifica se é '-'
                        throw new RuntimeException("ERRO SEMÂNTICO: operador '-' não pode ser usado com TEXTO!");
                    }
                    analisarExpressaoEscrever(filho.nodes.get(1));
                }
            }
            return "";
        }

        if(no.nome.equals("somaResto")){
            String operador = no.nodes.get(0).nodes.get(0).nome;
            if(operador.equals("-")){
                String opArit = analisarExpressao(no.nodes.get(1)); // usa o normal para pegar o tipo real
                if(opArit.equals("TEXTO")){
                    throw new RuntimeException("ERRO SEMÂNTICO: operador '-' não pode ser usado com TEXTO!");
                }
                return opArit;
            }
            analisarExpressaoEscrever(no.nodes.get(1));
            return "";
        }

        if(no.nome.equals("mult")){
            for(Node filho : no.nodes){
                analisarExpressaoEscrever(filho);
            }
            return "";
        }

        if(no.nome.equals("valor")){
            for(Node filho : no.nodes){
                analisarExpressaoEscrever(filho);
            }
            return "";
        }

        return analisarExpressao(no); // '*'' e '/'' cai aqui, ai vai para a funcao normal que automaticamente lanca um erro quando tem op diferente de + com texto   
    }
}
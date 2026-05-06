package parser;

import java.util.List;

public class CodeGenerator {
    Tree arvore;
    String codigo = "";
    int tabs = 0;
    boolean fmt = false;
    public CodeGenerator(Tree arvore){
        this.arvore = arvore;
    }

    private void gerarFilhos(Node no){
        for(Node filho : no.nodes){
            gerarNo(filho);
        }
    }

  private String tabs(){
        return "\t".repeat(tabs);
    }

    public String traduz(){
        gerarNo(arvore.root);
        return codigo.toString();
    }

    private void gerarNo(Node no){

        switch(no.nome){

            case "iniciar":
                gerarIniciar(no);
                break;

            case "declarar":
                gerarDeclarar(no);
                break;

            case "atribuir":
                gerarAtribuir(no);
                break;

            case "crementar":
                gerarCrementar(no);
                break;

            case "estruturaIf":
                gerarEstruturaIf(no);
                break;
            
            case "estruturaWhile":
                gerarEstruturaWhile(no);
                break;

            case "estruturaFor":
                gerarEstruturaFor(no);
                break;

            case "QUEBRE":
                codigo +=  tabs() + "break\n";
                break;

            case "CONTINUE":
                codigo += tabs() + "continue\n";
                break;

            case "estruturaEscrever":
                gerarEstruturaEscrever(no);
                break;

            case "comentar":
                gerarComentar(no);
                break;
            
            default:
                gerarFilhos(no);
                break;
        }
    }

    private String buscaFolha(Node no){
        if(no.nodes.isEmpty()){
            if(no.nome.equals("E")){
                return " && ";
            }
            if(no.nome.equals("OU")){
                return " || ";
            }
          
            return no.nome;
        }
        if(no.nome.equals("id")){
            return no.nodes.get(0).nome;
        }
        
        String resultado = "";
        for(Node filho : no.nodes){
            resultado += buscaFolha(filho);
        }
        return resultado;
    }

    private void gerarIniciar(Node no){
        codigo += "func main(){\n";
        tabs++;
        gerarFilhos(no);
        tabs--;
        codigo += "}\n";

        String imp = fmt ? "import \"fmt\"\n\n" : ""; // verifica se tem LEIA (LEIA precisa de import fmt)
        codigo = "package main\n\n" + imp + codigo;
    }

    private void gerarDeclarar(Node no){
        String tipo = "";
        String id = "";
        String valor = null;

        for(Node filho : no.nodes){
            if(filho.nome.equals("tipos")){
                tipo = filho.nodes.get(0).nome;
                if(tipo.equals("INTEIRO")) tipo = "int";
                else if(tipo.equals("DECIMAL")) tipo = "float64";
                else if(tipo.equals("TEXTO")) tipo = "string";
            }
            else if(filho.nome.equals("inicializar")){
                Node atributo = filho.nodes.get(1);
                if(atributo.nodes.get(0).nome.equals("LEIA")){
                    fmt = true;
                    // vai declarar a variavel, ir para a proxima linha e ler a variavel
                    codigo += tabs() + "var " + id + " " + tipo + "\n";
                    codigo += tabs() + "fmt.Scan(&" + id + ")\n";
                    return;
                }
                valor = buscaFolha(atributo);
            }
            else if(filho.nome.equals("id")){
                id = filho.nodes.get(0).nome;
            }
        }
        if(valor == null){
            codigo += tabs() + "var " + id + " " + tipo + "\n";
        }
        else{
            codigo += tabs() + "var " + id + " " + tipo + " = " + valor + "\n";
        }
    }
    
    private void gerarAtribuir(Node no){
        String id = "";
        String valor = "";

        for(Node filho : no.nodes){
            if(filho.nome.equals("opAtrib")){
                continue;
            }
            else if(filho.nome.equals("atributo")){
                Node atributo = filho.nodes.get(0);
                if(atributo.nome.equals("LEIA")){
                    fmt = true;
                    codigo += tabs() + "fmt.Scan(&" + id + ")\n"; // aqui gera fmt.Scan(&id) e sai do loop
                    return;
                }
                valor = buscaFolha(atributo);
            }
            else if(filho.nome.equals("id")){
                id = filho.nodes.get(0).nome;
            }
        }

        codigo += tabs() + id + " = " + valor + "\n";
    }

    private void gerarCrementar(Node no){
        String cremento = no.nodes.get(1).nodes.get(0).nome;
        String id = no.nodes.get(0).nodes.get(0).nome;

        codigo += tabs() + id + cremento + "\n";
    }

    private void gerarEstruturaIf(Node no){
        List<Node> cadeia = new java.util.ArrayList<>();
        for(Node filho : no.nodes){
            if(filho.nome.equals("condicao")){
                codigo += tabs() + "if(" + buscaFolha(filho) + "){\n";
                tabs++;
            }
            else if(filho.nome.equals("bloco")){
                gerarFilhos(filho);
            }
            else if(filho.nome.equals("estruturaElseif") || filho.nome.equals("estruturaElse")){
                cadeia.add(filho);
            }
        }
        tabs--;
        codigo += tabs() + "}";
        for(int i = 0; i < cadeia.size(); i++){
            Node c = cadeia.get(i);
            boolean ultimo = (i == cadeia.size() - 1);
            if(c.nome.equals("estruturaElseif")) gerarEstruturaElseif(c, ultimo);
            else gerarEstruturaElse(c);
        }
        if(cadeia.isEmpty()) codigo += "\n";
    }

    private void gerarEstruturaElseif(Node no, boolean ultimo){
        for(Node filho : no.nodes){
            if(filho.nome.equals("condicao")){
                codigo += "else if(" + buscaFolha(filho) + "){\n";
                tabs++;
            }
            else if(filho.nome.equals("bloco")){
                gerarFilhos(filho);
            }
        }
        tabs--;
        codigo += tabs() + "}";
        if(ultimo) codigo += "\n";
    }

    private void gerarEstruturaElse(Node no){
        codigo += "else{\n";
        tabs++;
        for(Node filho : no.nodes){
            if(filho.nome.equals("bloco")){
                gerarFilhos(filho);
            }
        }
        tabs--;
        codigo += tabs() + "}\n";
    }

    private void gerarEstruturaWhile(Node no){
        for(Node filho : no.nodes){
            if(filho.nome.equals("condicao")){
                codigo += tabs() + "for " + buscaFolha(filho) + "{\n";
                tabs++;
            }
            else if(filho.nome.equals("bloco")){
                gerarFilhos(filho);
            }
        }
        tabs--;
        codigo+= tabs() + "}\n";
    }

    private void gerarEstruturaFor(Node no){
        for(Node filho : no.nodes){
            if(filho.nome.equals("comeco")){
                codigo += geraComeco(filho);
            }
            else if(filho.nome.equals("condicao")){
                codigo += buscaFolha(filho) + ";";
            }
            else if(filho.nome.equals("finall")){
                codigo += buscaFolha(filho) + "{\n";
                tabs++;
            }
            else if(filho.nome.equals("bloco")){
                gerarFilhos(filho);
            }
        }
        tabs--;
        codigo += tabs() + "}\n";
    }

    private String geraComeco(Node noComeco){
        Node noDeclarar = noComeco.nodes.get(0); // verifica se é declararacao de variavel ou atribuir
        
        if(noDeclarar.nome.equals("declarar")){
            String id = "";
            String valor = "";
            for(Node filho : noDeclarar.nodes){
                if(filho.nome.equals("id")){
                    id = filho.nodes.get(0).nome;
                }
                else if(filho.nome.equals("inicializar")){
                    valor = buscaFolha(filho.nodes.get(1)); // pula o opAtrib
                }
            }
            return tabs() + "for " + id + " := " + valor + "; ";
        }
        else{ 
            return tabs() + "for " + buscaFolha(noDeclarar) + "; ";
        }
    }

    private void gerarEstruturaEscrever(Node no){
        fmt = true;
        for(Node filho : no.nodes){
            if(filho.nome.equals("expressao")){
                codigo += tabs() + "fmt.Println(" + buscaEscrever(filho) + ")\n";
            }
        }
    }

    private String buscaEscrever(Node no){ // esse metodo serve para evitar que o '+' de uma soma seja alterado por ','
        if(no.nome.equals("soma")){
            String resultado = buscaFolha(no.nodes.get(0)); // primeiro mult
            for(Node filho : no.nodes){
                if(filho.nome.equals("somaResto")){
                    // pula o opArit e pega o mult
                    resultado += ", " + buscaFolha(filho.nodes.get(1));
                }
            }
            return resultado;
        }
        String resultado = "";
        for(Node filho : no.nodes){
            resultado += buscaEscrever(filho);
        }
        return resultado;
    }

    private void gerarComentar(Node no){
        String texto = "";
        for(Node filho : no.nodes){
            if(filho.nome.equals("texto")){
                texto = buscaFolha(filho);
            }
        }
        // remove as aspas do texto
        texto = texto.substring(1, texto.length() - 1);
        codigo += tabs() + "// " + texto + "\n";
    }
}
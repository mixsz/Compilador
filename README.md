# Compilador ABCD

Compilador da linguagem ABCD, desenvolvido em Java como projetode Compiladores. A linguagem ABCD é traduzida para Go, gerando o arquivo `saida.go`.

---

## Requisitos

- Java JDK instalado
- Go instalado

---

## Como Executar

1. Acesse o repositório em [https://github.com/mixsz/Compilador](https://github.com/mixsz/Compilador) e instale em sua máquina (ZIP ou git clone)
2. Compile com:
```
javac lexer/*.java parser/*.java Main.java
```
3. Escreva seu código no arquivo `main.ABCD`
4. Execute com:
```
java -cp . Main
```
5. O código Go será executado automaticamente e o resultado aparecerá no terminal

---

## Características da Linguagem

- Identificadores devem obrigatoriamente começar com letra minúscula e não podem conter caracteres especiais.
- As palavras reservadas são sempre escritas em letras maiúsculas e em português (ex: `SE`, `ENQUANTO`, `INTEIRO`).
- Todas as instruções, incluindo comentários, só são válidas após a declaração `INICIE:`.
- É possível declarar e ler uma variável na mesma linha, por exemplo: `INTEIRO id = LEIA(INTEIRO);`.
- A linguagem possui apenas 3 tipos de variáveis: `INTEIRO` (int), `DECIMAL` (float) e `TEXTO` (string).
- Operadores aritméticos suportados: `+`, `-`, `*`, `/`
- Operadores relacionais suportados: `==`, `!=`, `<`, `>`, `<=`, `>=`
- Operadores lógicos suportados: `E` (and) e `OU` (or)
- Estruturas de controle disponíveis: `SE`, `SENAOSE`, `SENAO`, `ENQUANTO`, `PARA`
- Suporte a `QUEBRE` e `CONTINUE` dentro de laços
- Saída de dados com `ESCREVA` e entrada com `LEIA`
- Comentários de linha com `COMENTE`
- A linguagem é traduzida para Go, gerando o arquivo `saida.go`

---

## Exemplos de Código

### Exemplo 1 — Soma de 1 até N
```
INICIE:
    INTEIRO soma = 0;
    ESCREVA("Digite o número desejado: ");
    INTEIRO n = LEIA(INTEIRO);
    PARA(INTEIRO i = 1; i <= n; i++){
        soma = soma + i;
    }
    ESCREVA("Soma: " + soma);
```

### Exemplo 2 — Positivo, Negativo ou Zero
```
INICIE:
    INTEIRO num = LEIA(INTEIRO);
    SE(num < 0){
        ESCREVA("Negativo!");
    }SENAOSE(num > 0){
        ESCREVA("Positivo!");
    }SENAO{
        ESCREVA("Zero!");
    }
```

### Exemplo 3 — Fatorial
```
INICIE:
    INTEIRO numero = LEIA(INTEIRO);
    INTEIRO valor = 1;
    ENQUANTO(numero > 0){
        valor = valor * numero;
        numero--;
    }
    ESCREVA("Fatorial: " + valor);
```

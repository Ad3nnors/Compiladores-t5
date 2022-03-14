# Construção de Compiladores

Trabalho 4 de Construção de Compiladores, matéria ministrada por professor Daniel Lucredio.

Autores: 
- Anderson H. Giacomini RA: 769720
- Sophia S. Schuster RA: 760936

## Conteúdo
- [Descrição](#descricao)
- [Implementação](#implementacao)
- [Como executar](#como)
- [Observações](#obs)

*******

<div id='descricao'>

## Descrição

O trabalho 4 (T4) da disciplina consiste em implementar uma segunda parte de um analisador semântico para a linguagem LA (Linguagem Algorítmica) desenvolvida pelo prof. Jander, no âmbito do DC/UFSCar.


O analisador semântico detecta 4 tipos de erros:
  
1. Identificador (variável, constante, procedimento, função, tipo) já declarado anteriormente no escopo, **mas agora envolvendo também ponteiros, registros, funções**
   - O mesmo identificador não pode ser usado novamente no mesmo escopo mesmo que para categorias diferentes
2. Tipo não declarado
3. Identificador (variável, constante, procedimento, função) não declarado, **mas agora envolvendo também ponteiros, registros, funções**
4. Atribuição não compatível com o tipo declarado, **agora envolvendo ponteiros e registros**
   - Atribuições possíveis
     - ponteiro ← endereço
     - (real | inteiro) ← (real | inteiro)
     - literal ← literal
     - logico ← logico
     - registro ← registro (com mesmo nome de tipo)

   - As mesmas restrições são válidas para expressões, por exemplo, ao tentar combinar um literal com um logico (como em literal + logico) deve dar tipo_indefinido e inviabilizar a atribuição

Ao encontrar um erro, o analisador NÃO interrompe sua execução. Ele continua reportando erros até o final do arquivo.

<div id='implementacao'>

## Implementação

Para a implementação do trabalho foi utilizado a ferramenta ANTLR (antlr.org) no ambiente Netbeans juntamente com o Maven. As informações de como trabalhar com ANTLR no maven encontram-se em https://www.antlr.org/api/maven-plugin/latest/usage.html

<div id='como'>

## Como rodar

<strike>Após importar e compilar o projeto, será gerado uma pasta target com o arquivo .jar já com todas dependencias.</strike>
  
O analisador pode ser executado em linha de comando (windows, mac ou linux), com DOIS ARGUMENTOS OBRIGATORIAMENTE:
  
Argumento 1: arquivo de entrada (caminho completo)<br>
Argumento 2: arquivo de saída (caminho completo)

Exemplo de como rodar o analisador:

```
java -jar C:\compilatorest3\target\expr-parser2-1.0-SNAPSHOT-jar-with-dependencies.jar C:\compilatorest3\casos-de-teste\3.casos_teste_t3\entrada\8.algoritmo_6-2_apostila_LA.txt C:\compiladorest3\saida.txt
```

Como resultado, seu compilador deve ler a entrada C:\compilatorest3\casos-de-teste\3.casos_teste_t3\entrada\8.algoritmo_6-2_apostila_LA.txt e salvar a saída no diretório C:\compiladorest3\saida.txt

<div id='obs'>

## Observações
  
Esse trabalho, T4, é continuação do T3. Por isso alguns nomes encontram-se como T3.

Encontram-se também nesse projeto:
  
Os casos de teste <br>
  <a href="https://github.com/dlucredio/compiladores-corretor-automatico/blob/master/Compiladores.CorretorT1T2T3.docx.pdf">O compilador automático </a>

Todo esse conteúdo foi disponibilizado pelo professor. 


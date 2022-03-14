package br.ufscar.dc.compiladores.compiladorest3;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TabelaDeSimbolos {

    void imprimir() {
        tabela.forEach((k, v) -> System.out.println((k + " : " + v)));
    }

    public enum TipoEntrada {
        variavel,
        procedimento,
        funcao,
        estendido
    }
     
    public enum TipoLA {
        literal,
        inteiro,
        real,
        logico,
        registro,
        estendido,
        invalido
    }
    
    class EntradaTabelaDeSimbolos {
        TipoEntrada tipoEntrada;
        String nome;
        String tipo;
        List<String> parametros;

        private EntradaTabelaDeSimbolos(TipoEntrada tipoEntrada, String nome, String tipo, List<String> parametros) {
            this.tipoEntrada = tipoEntrada;
            this.nome = nome;
            this.tipo = tipo;
            this.parametros = parametros;
        }

        @Override
        public String toString() {
            return tipoEntrada + " " + tipo;
        }   
    }
    
    private final Map<String, EntradaTabelaDeSimbolos> tabela;
    
    public TabelaDeSimbolos() {
        this.tabela = new HashMap<>();
    }
    
    public void adicionarVariavel(String nome, String tipo) {
        tabela.put(nome, new EntradaTabelaDeSimbolos(TipoEntrada.variavel, nome, tipo, null));
    }
    
    public void adicionarFuncao(String nome, String tipo, List<String> parametros) {
        tabela.put(nome, new EntradaTabelaDeSimbolos(TipoEntrada.funcao, nome, tipo, parametros));
    }
    
    public void adicionarProcedimento(String nome, List<String> parametros) {
        tabela.put(nome, new EntradaTabelaDeSimbolos(TipoEntrada.procedimento, nome, null, parametros));
    }
    
    public boolean existe(String nome) {
        return tabela.containsKey(nome) || tabela.containsKey("^" + nome) ;
    }
    
    public String verificar(String nome) {
        return tabela.get(nome).tipo;
    }
    
    public List<String> getParametros(String nome){
        return tabela.get(nome).parametros;
    }
}
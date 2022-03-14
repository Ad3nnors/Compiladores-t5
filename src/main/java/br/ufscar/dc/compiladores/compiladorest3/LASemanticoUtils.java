
package br.ufscar.dc.compiladores.compiladorest3;

import br.ufscar.dc.compiladores.compiladorest3.semantico.LAParser;
import java.util.ArrayList;
import java.util.List;
import org.antlr.v4.runtime.Token;

public class LASemanticoUtils {
    public static List<String> errosSemanticos = new ArrayList<>();
    
    public static void adicionarErroSemantico(Token t, String mensagem) {
        int linha = t.getLine();
        // int coluna = t.getCharPositionInLine();
        errosSemanticos.add(String.format("Linha %d: %s", linha, mensagem));
    }
    
    // função auxiliar para detectar compatibilidade entre inteiro e real
    public static boolean ehvalido(String a1, String a2) {
        return (a1.equals("inteiro") || a1.equals("real")) 
                && (a2.equals("inteiro") || a2.equals("real"));    
    }
    
    public static String verificarTipo(TabelaDeSimbolos tabela, LAParser.ExpressaoContext ctx) {
        String ret = null;

        for (var fa : ctx.termo_logico()) {
            String aux = verificarTipo(tabela, fa);
            if (ret == null) {
                ret = aux;
            } else if (!ret.equals(aux) && !ehvalido(ret, aux) && !aux.equals("invalido")) {
                // adicionarErroSemantico(ctx.start, "Termo " + ctx.getText() + " contém tipos incompatíveis");
                ret = "invalido";
            }
        }
        return ret;
    }
    
    public static String verificarTipo(TabelaDeSimbolos tabela, LAParser.Termo_logicoContext ctx) {
        String ret = null;

        for (var fa : ctx.fator_logico()) {
            String aux = verificarTipo(tabela, fa);
            if (ret == null) {
                ret = aux;
            } else if (!ret.equals(aux) && !ehvalido(ret, aux) && !aux.equals("invalido")) {
                // adicionarErroSemantico(ctx.start, "Termo " + ctx.getText() + " contém tipos incompatíveis");
                ret = "invalido";
            }
        }
        return ret;
    }
    
    public static String verificarTipo(TabelaDeSimbolos tabela, LAParser.Fator_logicoContext ctx) {
        String ret;
        ret = verificarTipo(tabela, ctx.parcela_logica());
        return ret;
    }
    
    public static String verificarTipo(TabelaDeSimbolos tabela, LAParser.Parcela_logicaContext ctx) {
        String ret;
        if (ctx.exp_relacional()!= null) {
            ret = verificarTipo(tabela, ctx.exp_relacional());  
        }
        else ret = "logico";
        return ret;
    }
        public static String verificarTipo(TabelaDeSimbolos tabela, LAParser.Exp_relacionalContext ctx) {
        String ret;
        ret = verificarTipo(tabela, ctx.exp_aritmetica(0)); //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        if (ctx.op_relacional()!= null) {
            // verificarTipo(tabela, ctx.exp2); 
            ret = "logico";
        }
        return ret;
    }
        
    public static String verificarTipo(TabelaDeSimbolos tabela, LAParser.Exp_aritmeticaContext ctx) {
        String ret = null;
        for (var ta : ctx.termo()) {
            String aux = verificarTipo(tabela, ta);
            if (ret == null) {
                ret = aux;
            } else if (!ret.equals(aux) && !ehvalido(ret, aux) && !aux.equals("invalido")) {
                //adicionarErroSemantico(ctx.start, "Expressão " + ctx.getText() + " contém tipos incompatíveis");
                ret = "invalido";
            }
        }

        return ret;
    }

    public static String verificarTipo(TabelaDeSimbolos tabela, LAParser.TermoContext ctx) {
        String ret = null;

        for (var fa : ctx.fator()) {
            String aux = verificarTipo(tabela, fa);
            if (ret == null) {
                ret = aux;
            } else if (!ret.equals(aux) && !ehvalido(ret, aux) && !aux.equals("invalido")) {
                //adicionarErroSemantico(ctx.start, "Termo " + ctx.getText() + " contém tipos incompatíveis");
                ret = "invalido";
            }
        }
        return ret;
    }

    public static String verificarTipo(TabelaDeSimbolos tabela, LAParser.FatorContext ctx) {
        String ret = null;

        for (var fa : ctx.parcela()) {
            String aux = verificarTipo(tabela, fa);
            if (ret == null) {
                ret = aux;
            } else if (!ret.equals(aux) && !ehvalido(ret, aux) && !aux.equals("invalido")) {
                //adicionarErroSemantico(ctx.start, "Termo " + ctx.getText() + " contém tipos incompatíveis");
                ret = "invalido";
            }
        }
        return ret;
    }
    
    public static String verificarTipo(TabelaDeSimbolos tabela, LAParser.ParcelaContext ctx) {
        String ret = null;
        if (ctx.parcela_unario()!= null) {
            ret = verificarTipo(tabela, ctx.parcela_unario());  
        }
        else if (ctx.parcela_nao_unario()!= null) {
            ret = verificarTipo(tabela, ctx.parcela_nao_unario());  
        }
        return ret;
    }
    
    public static String verificarTipo(TabelaDeSimbolos tabela, LAParser.Parcela_unarioContext ctx) {
        if (ctx.NUM_INT() != null) {
            return "inteiro";
        }
        if (ctx.NUM_REAL() != null) {
            return "real";
        }
        if (ctx.identificador()!= null) {
            String nome = ctx.identificador().getText();
            if (ctx.identificador().dimensao()!= null) {
                nome = nome.split("\\[")[0];
            }          
            String aux = nome;
            if (nome.startsWith("^")) aux = nome.substring(1);
            if (!tabela.existe(aux)) {
                adicionarErroSemantico(ctx.identificador().start, "identificador " + nome + " nao declarado");
                return "invalido";
            }
            else return verificarTipo(tabela, nome);
            
        }
        if (ctx.IDENT() != null) {
            // chamada de procedimento/função
            if (!tabela.existe(ctx.IDENT().getText())) {
                adicionarErroSemantico(ctx.identificador().start, "identificador " + ctx.IDENT().getText() + " nao declarado");
                return "invalido";
            }
            else {
                // verifica parametros
                List<String> getParametros = tabela.getParametros(ctx.IDENT().getText());
                int i = 0;
                 for (LAParser.ExpressaoContext fa : ctx.expressao()) {
                    String aux = verificarTipo(tabela, fa);
                    if (!getParametros.get(i).equals(aux)){
                        adicionarErroSemantico(ctx.start, "incompatibilidade de parametros na chamada de " + ctx.IDENT().getText());
                    }
                    i++; 
                }
                // verifica se faltou algum parametro
                if (getParametros.size() != i) adicionarErroSemantico(ctx.start, "incompatibilidade de parametros na chamada de " + ctx.IDENT().getText());
                // procedimento retorna tipo inválido, função retorna o tipo de retorno da função


                    if (verificarTipo(tabela, ctx.IDENT().getText())== null)
                        return "invalido";
                    else return verificarTipo(tabela, ctx.IDENT().getText());
            }
        }
        // se não for nenhum dos tipos acima, só pode ser uma expressão
        // entre parêntesis
        return verificarTipo(tabela, ctx.expressao(0)); // 0?!
    }
        
    public static String verificarTipo(TabelaDeSimbolos tabela, LAParser.Parcela_nao_unarioContext ctx) {
        if (ctx.identificador()!= null) {
            // endereco com '&' --- sempre inteiro
            return "inteiro";
        }
        // se não for nenhum dos tipos acima, só pode ser uma cadeia
        return "literal";
    }
    
    public static String verificarTipo(TabelaDeSimbolos tabela, String nomeVar) {
        String nomeConsulta = nomeVar;
        boolean ehPonteiro = false;
        if (nomeVar.startsWith("^")){
            ehPonteiro = true;
            nomeConsulta = nomeVar.substring(1);
        }
        String resultado = tabela.verificar(nomeConsulta);
        if (resultado.startsWith("^")){
            // ehPonteiro ? ok : erro semantico
            return resultado.substring(1);
        }
        if (resultado.startsWith("&")){
            // endereço
            return "inteiro";
        }
        return resultado;
    }
    
    public static boolean ehTipoBasico(String tipo) {
        return tipo.equals("real") || tipo.equals("inteiro") || tipo.equals("logico") || tipo.equals("literal") ||
                 tipo.equals("^real") || tipo.equals("^inteiro") || tipo.equals("^logico") || tipo.equals("^literal") ;
    }
}

package br.ufscar.dc.compiladores.compiladorest3;

import br.ufscar.dc.compiladores.compiladorest3.parser.LAParser;
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
    public static boolean ehvalido(TabelaDeSimbolos.TipoLA a1, TabelaDeSimbolos.TipoLA a2) {
        return (a1 == TabelaDeSimbolos.TipoLA.inteiro || a1 == TabelaDeSimbolos.TipoLA.real) 
                && (a2 == TabelaDeSimbolos.TipoLA.inteiro || a2 == TabelaDeSimbolos.TipoLA.real);    
    }
    
    public static TabelaDeSimbolos.TipoLA verificarTipo(TabelaDeSimbolos tabela, LAParser.ExpressaoContext ctx) {
        TabelaDeSimbolos.TipoLA ret = null;

        for (var fa : ctx.termo_logico()) {
            TabelaDeSimbolos.TipoLA aux = verificarTipo(tabela, fa);
            if (ret == null) {
                ret = aux;
            } else if (ret != aux && !ehvalido(ret, aux) && aux != TabelaDeSimbolos.TipoLA.invalido) {
                // adicionarErroSemantico(ctx.start, "Termo " + ctx.getText() + " contém tipos incompatíveis");
                ret = TabelaDeSimbolos.TipoLA.invalido;
            }
        }
        return ret;
    }
    
    public static TabelaDeSimbolos.TipoLA verificarTipo(TabelaDeSimbolos tabela, LAParser.Termo_logicoContext ctx) {
        TabelaDeSimbolos.TipoLA ret = null;

        for (var fa : ctx.fator_logico()) {
            TabelaDeSimbolos.TipoLA aux = verificarTipo(tabela, fa);
            if (ret == null) {
                ret = aux;
            } else if (ret != aux && !ehvalido(ret, aux) && aux != TabelaDeSimbolos.TipoLA.invalido) {
                // adicionarErroSemantico(ctx.start, "Termo " + ctx.getText() + " contém tipos incompatíveis");
                ret = TabelaDeSimbolos.TipoLA.invalido;
            }
        }
        return ret;
    }
    
    public static TabelaDeSimbolos.TipoLA verificarTipo(TabelaDeSimbolos tabela, LAParser.Fator_logicoContext ctx) {
        TabelaDeSimbolos.TipoLA ret;
        ret = verificarTipo(tabela, ctx.parcela_logica());
        return ret;
    }
    
    public static TabelaDeSimbolos.TipoLA verificarTipo(TabelaDeSimbolos tabela, LAParser.Parcela_logicaContext ctx) {
        TabelaDeSimbolos.TipoLA ret;
        if (ctx.exp_relacional()!= null) {
            ret = verificarTipo(tabela, ctx.exp_relacional());  
        }
        else ret = TabelaDeSimbolos.TipoLA.logico;
        return ret;
    }
        public static TabelaDeSimbolos.TipoLA verificarTipo(TabelaDeSimbolos tabela, LAParser.Exp_relacionalContext ctx) {
        TabelaDeSimbolos.TipoLA ret;
        ret = verificarTipo(tabela, ctx.exp_aritmetica(0)); //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        if (ctx.op_relacional()!= null) {
            // verificarTipo(tabela, ctx.exp2); 
            ret = TabelaDeSimbolos.TipoLA.logico;
        }
        return ret;
    }
        
    public static TabelaDeSimbolos.TipoLA verificarTipo(TabelaDeSimbolos tabela, LAParser.Exp_aritmeticaContext ctx) {
        TabelaDeSimbolos.TipoLA ret = null;
        for (var ta : ctx.termo()) {
            TabelaDeSimbolos.TipoLA aux = verificarTipo(tabela, ta);
            if (ret == null) {
                ret = aux;
            } else if (ret != aux && !ehvalido(ret, aux) && aux != TabelaDeSimbolos.TipoLA.invalido) {
                //adicionarErroSemantico(ctx.start, "Expressão " + ctx.getText() + " contém tipos incompatíveis");
                ret = TabelaDeSimbolos.TipoLA.invalido;
            }
        }

        return ret;
    }

    public static TabelaDeSimbolos.TipoLA verificarTipo(TabelaDeSimbolos tabela, LAParser.TermoContext ctx) {
        TabelaDeSimbolos.TipoLA ret = null;

        for (var fa : ctx.fator()) {
            TabelaDeSimbolos.TipoLA aux = verificarTipo(tabela, fa);
            if (ret == null) {
                ret = aux;
            } else if (ret != aux && !ehvalido(ret, aux) && aux != TabelaDeSimbolos.TipoLA.invalido) {
                //adicionarErroSemantico(ctx.start, "Termo " + ctx.getText() + " contém tipos incompatíveis");
                ret = TabelaDeSimbolos.TipoLA.invalido;
            }
        }
        return ret;
    }

    public static TabelaDeSimbolos.TipoLA verificarTipo(TabelaDeSimbolos tabela, LAParser.FatorContext ctx) {
        TabelaDeSimbolos.TipoLA ret = null;

        for (var fa : ctx.parcela()) {
            TabelaDeSimbolos.TipoLA aux = verificarTipo(tabela, fa);
            if (ret == null) {
                ret = aux;
            } else if (ret != aux && !ehvalido(ret, aux) && aux != TabelaDeSimbolos.TipoLA.invalido) {
                //adicionarErroSemantico(ctx.start, "Termo " + ctx.getText() + " contém tipos incompatíveis");
                ret = TabelaDeSimbolos.TipoLA.invalido;
            }
        }
        return ret;
    }
    
    public static TabelaDeSimbolos.TipoLA verificarTipo(TabelaDeSimbolos tabela, LAParser.ParcelaContext ctx) {
        TabelaDeSimbolos.TipoLA ret = null;
        if (ctx.parcela_unario()!= null) {
            ret = verificarTipo(tabela, ctx.parcela_unario());  
        }
        else if (ctx.parcela_nao_unario()!= null) {
            ret = verificarTipo(tabela, ctx.parcela_nao_unario());  
        }
        return ret;
    }
    
    public static TabelaDeSimbolos.TipoLA verificarTipo(TabelaDeSimbolos tabela, LAParser.Parcela_unarioContext ctx) {
        if (ctx.NUM_INT() != null) {
            return TabelaDeSimbolos.TipoLA.inteiro;
        }
        if (ctx.NUM_REAL() != null) {
            return TabelaDeSimbolos.TipoLA.real;
        }
        if (ctx.identificador()!= null) {
            String nomeVar = ctx.identificador().getText();
            if (!tabela.existe(nomeVar)) {
                adicionarErroSemantico(ctx.identificador().start, "identificador " + nomeVar + " nao declarado");
                return TabelaDeSimbolos.TipoLA.invalido;
            }
            return verificarTipo(tabela, nomeVar);
        }
        if (ctx.IDENT() != null) {
            // ?????
        }
        // se não for nenhum dos tipos acima, só pode ser uma expressão
        // entre parêntesis
        return verificarTipo(tabela, ctx.expressao(0)); // 0?!
    }
    
    public static TabelaDeSimbolos.TipoLA verificarTipo(TabelaDeSimbolos tabela, LAParser.Parcela_nao_unarioContext ctx) {
        if (ctx.identificador()!= null) {
            // endereco com '&' --- NAO IMPLEMENTADO
            return TabelaDeSimbolos.TipoLA.invalido;
        }
        // se não for nenhum dos tipos acima, só pode ser uma cadeia
        return TabelaDeSimbolos.TipoLA.literal;
    }
    
    public static TabelaDeSimbolos.TipoLA verificarTipo(TabelaDeSimbolos tabela, String nomeVar) {
        return tabela.verificar(nomeVar);
    }
}
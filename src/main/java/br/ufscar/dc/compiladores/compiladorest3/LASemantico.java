package br.ufscar.dc.compiladores.compiladorest3;

import br.ufscar.dc.compiladores.compiladorest3.parser.LABaseVisitor;
import br.ufscar.dc.compiladores.compiladorest3.parser.LAParser;
import br.ufscar.dc.compiladores.compiladorest3.TabelaDeSimbolos.*;

public class LASemantico extends LABaseVisitor<Void>{
    TabelaDeSimbolos tabela;

    @Override
    public Void visitPrograma(LAParser.ProgramaContext ctx) {
        tabela = new TabelaDeSimbolos();
        return super.visitPrograma(ctx);
    }
    
    // função auxiliar para detectar compatibilidade entre inteiro e real
    public boolean ehvalido(TipoLA a1, TipoLA a2) {
        return (a1 == TabelaDeSimbolos.TipoLA.inteiro || a1 == TabelaDeSimbolos.TipoLA.real) 
                && (a2 == TabelaDeSimbolos.TipoLA.inteiro || a2 == TabelaDeSimbolos.TipoLA.real);    
    }
    
/*    
    @Override
    public Void visitDecl_local_global(LAParser.Decl_local_globalContext ctx) {
        if (ctx.declaracao_global() != null) {
            visitDeclaracao_global(ctx.declaracao_global());
        } else if (ctx.declaracao_local() != null) {
            visitDeclaracao_local(ctx.declaracao_local());
        }
        return super.visitDecl_local_global(ctx);
    }
*/
    /*
    @Override
    public Void visitDeclaracao_local(LAParser.Declaracao_localContext ctx) {
        if (ctx.variavel()!= null) {
            visitVariavel(ctx.variavel());  
        }
        if (ctx.valor_constante()!= null) {
            // const
        }
        if (ctx.tipo()!= null) {
            // declaração de tipo
        }
        return super.visitDeclaracao_local(ctx);
    }
*/
    @Override
    public Void visitDeclaracao_global(LAParser.Declaracao_globalContext ctx) {
        if (ctx.tipo_estendido()!= null) {
            // declara função
        }
        else if (ctx.IDENT()!= null) {
            // declara procedimento
        }
        return super.visitDeclaracao_global(ctx);
    }

    @Override
    public Void visitVariavel(LAParser.VariavelContext ctx) {
        String strTipoVar = ctx.tipo().getText();
        TipoLA tipoVar = TipoLA.invalido;
        switch (strTipoVar) {
            case "inteiro":
                tipoVar = TipoLA.inteiro;
                break;
            case "real":
                tipoVar = TipoLA.real;
                break;
            case "logico":
                tipoVar = TipoLA.logico;
                break;
            case "literal":
                tipoVar = TipoLA.literal;
                break;
            default:
                // Nunca irá acontecer, pois o analisador sintático
                // não permite
                break;
        }
        
        for (LAParser.IdentificadorContext fa : ctx.identificador()) {
            // Verificar se a variável já foi declarada
            if (tabela.existe(fa.getText())) {
                LASemanticoUtils.adicionarErroSemantico(fa.start, "identificador " + fa.getText() + " ja declarado anteriormente");
            } else {
                tabela.adicionar(fa.getText(), tipoVar);
                // System.out.print("inseriu " + fa.getText() + "\n");
            }
            if (tipoVar == TipoLA.invalido) {
                LASemanticoUtils.adicionarErroSemantico(fa.start, "tipo " + strTipoVar + " nao declarado");
            }
            
        }
        return super.visitVariavel(ctx);
    }

    @Override // reescreve visitCmdAtribuição para verificar identificadores não declarados e atribuições incompatíveis
    public Void visitCmdAtribuicao(LAParser.CmdAtribuicaoContext ctx) {
        TipoLA tipoExpressao = LASemanticoUtils.verificarTipo(tabela, ctx.expressao());
        String nomeVar = ctx.identificador().getText();
        if (tipoExpressao != TipoLA.invalido) {
            if (!tabela.existe(nomeVar)) {
                LASemanticoUtils.adicionarErroSemantico(ctx.identificador().start, "identificador " + nomeVar + " nao declarado");
            } else {
                TipoLA tipoVariavel = LASemanticoUtils.verificarTipo(tabela, nomeVar);
                if (tipoVariavel != tipoExpressao && !ehvalido(tipoVariavel, tipoExpressao)) {
                    LASemanticoUtils.adicionarErroSemantico(ctx.identificador().start, "atribuicao nao compativel para " + nomeVar);
                }
            }
        }
        else LASemanticoUtils.adicionarErroSemantico(ctx.identificador().start, "atribuicao nao compativel para " + nomeVar);
        return super.visitCmdAtribuicao(ctx); 
    }

    @Override // reescreve visitCmdLeia para verificar se o identificador do comando já foi declarado
    public Void visitCmdLeia(LAParser.CmdLeiaContext ctx) {
        for (LAParser.IdentificadorContext fa : ctx.identificador()) {
            if (!tabela.existe(fa.getText())) {
                LASemanticoUtils.adicionarErroSemantico(ctx.start, "identificador " + fa.getText() + " nao declarado");
            }
        }
        return super.visitCmdLeia(ctx);
    }

    @Override // procura erros para todas expressões -- está redundante, pode ser necessário mudanças no futuro
    public Void visitExp_aritmetica(LAParser.Exp_aritmeticaContext ctx) {
        LASemanticoUtils.verificarTipo(tabela, ctx);
        return super.visitExp_aritmetica(ctx);
    }
}

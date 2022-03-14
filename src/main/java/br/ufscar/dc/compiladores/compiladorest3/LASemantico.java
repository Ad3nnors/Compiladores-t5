package br.ufscar.dc.compiladores.compiladorest3;

import static br.ufscar.dc.compiladores.compiladorest3.LASemanticoUtils.ehTipoBasico;
import static br.ufscar.dc.compiladores.compiladorest3.LASemanticoUtils.ehvalido;
import br.ufscar.dc.compiladores.compiladorest3.semantico.LABaseVisitor;
import br.ufscar.dc.compiladores.compiladorest3.semantico.LAParser;
import java.util.ArrayList;
import java.util.List;

public class LASemantico extends LABaseVisitor<Void> {
    Escopos escoposAninhados = new Escopos();
    TabelaDeSimbolos tabela;

    @Override
    public Void visitPrograma(LAParser.ProgramaContext ctx) {
        tabela = new TabelaDeSimbolos();
        return super.visitPrograma(ctx);
    }

    /*   
    @Override
    public Void visitDecl_local_global(LAParser.Decl_local_globalContext ctx) {
        if (ctx.declaracao_global() != null) {
           if (!escoposAninhados.isEmpty()) escoposAninhados.abandonarEscopo();
        }
        return super.visitDecl_local_global(ctx);
    }
    */
 
    @Override
    public Void visitDeclaracao_local(LAParser.Declaracao_localContext ctx) {
        if (ctx.variavel() != null) {
            String tipoVar = ctx.variavel().tipo().getText();  
            for (LAParser.IdentificadorContext fa : ctx.variavel().identificador()) {
                String nome = fa.getText();    
                if (fa.dimensao()!= null){
                    nome = nome.split("\\[")[0];
                }
                
                // Verificar se a variável já foi declarada
                if (tabela.existe(nome)) {
                    LASemanticoUtils.adicionarErroSemantico(fa.start, "identificador " + nome + " ja declarado anteriormente");
                } else {
                    if (tipoVar.startsWith("registro")) {
                        //List<String> variaveis = new ArrayList<>();
                        for (LAParser.VariavelContext vctx : ctx.variavel().tipo().registro().variavel()) {
                            //variaveis.add(vctx.getText());
                            String auxTipoVar = vctx.tipo().getText(); 
                            for (LAParser.IdentificadorContext favctx : vctx.identificador()) {
                                String nomeVar = nome+'.'+favctx.getText();
                                if (tabela.existe(nome)) {
                                    LASemanticoUtils.adicionarErroSemantico(fa.start, "identificador " + nomeVar + " ja declarado anteriormente");
                                } else {
                                    tabela.adicionarVariavel(nomeVar, auxTipoVar);
                                }
                            }
                        } 
                        //String registro[] = tipoVar.substring(8).substring(0, tipoVar.length()-12).split(":");
                        //String vars[] = registro[0].split(",");
                        //String tipo = registro[1];
                        tabela.adicionarVariavel(nome, "registro");
                    } else {
                        if (tabela.existe(tipoVar)){
                            String auxTipo = tabela.verificar(tipoVar);
                            if (auxTipo.startsWith("registro")){
                                String registro[] = auxTipo.substring(8).split(";");
                                for (var vars : registro) {
                                    String nomeVarTipo[] = vars.split(":"); 
                                    String nomesVar[] = nomeVarTipo[0].split(",");
                                    String varTipo = nomeVarTipo[1];
                                    for (var var : nomesVar){
                                        tabela.adicionarVariavel(nome+"."+var, varTipo);
                                    }
                                }
                            }
                        }
                        tabela.adicionarVariavel(nome, tipoVar);
                        // System.out.print("inseriu " + fa.getText() + "\n");
                    }
                }
                //verificar na tabela de simbolos e verificar se nao é nenhum dos tipos basicos

                if (!LASemanticoUtils.ehTipoBasico(tipoVar) && !tabela.existe(tipoVar) && !tipoVar.startsWith("registro")) {
                    LASemanticoUtils.adicionarErroSemantico(fa.start, "tipo " + tipoVar + " nao declarado");
                }
            }
        }
        if (ctx.valor_constante()!= null) {
            // const
            tabela.adicionarVariavel(ctx.IDENT().getText(), ctx.tipo_basico().getText());
        }
        if (ctx.tipo() != null) {
            // declaração de tipo
            if (ctx.tipo().registro() != null){
                String variaveis = "";
                for (LAParser.VariavelContext va : ctx.tipo().registro().variavel()) {
                    if ("".equals(variaveis)) variaveis = va.getText();
                    else variaveis += ";" + va.getText();
                }
                tabela.adicionarVariavel(ctx.IDENT().getText(), "registro" + variaveis);
            }
            if (ctx.tipo().tipo_estendido()!= null) {
            
            }
        }
        return super.visitDeclaracao_local(ctx);
    }

    @Override
    public Void visitDeclaracao_global(LAParser.Declaracao_globalContext ctx) {
        List<String> parametros = new ArrayList<>();
        if (ctx.parametros() != null) {
            for (LAParser.ParametroContext pctx : ctx.parametros().parametro()) {
                String tipoEstendido = pctx.tipo_estendido().getText();
                parametros.add(tipoEstendido);
                for (LAParser.IdentificadorContext ictx : pctx.identificador()) {     
                    if (tabela.existe(tipoEstendido)){
                        String auxTipo = tabela.verificar(tipoEstendido);
                        if (auxTipo.startsWith("registro")){
                            String registro[] = auxTipo.substring(8).split(";");
                            for (var vars : registro) {
                                String nomeVarTipo[] = vars.split(":"); 
                                String nomesVar[] = nomeVarTipo[0].split(",");
                                String varTipo = nomeVarTipo[1];
                                for (var var : nomesVar){
                                    tabela.adicionarVariavel(ictx.getText()+"."+var, varTipo);
                                }
                            }
                        }
                        else tabela.adicionarVariavel(ictx.getText(), tipoEstendido);
                    }
                    else if (ehTipoBasico(tipoEstendido)) tabela.adicionarVariavel(ictx.getText(), tipoEstendido);
                }
            }     
        }
        if (ctx.tipo_estendido() != null) {
            // funcao
            if (escoposAninhados.isEmpty())
                escoposAninhados.criarNovoEscopo();
            tabela.adicionarFuncao(ctx.IDENT().getText(), ctx.tipo_estendido().getText(), parametros);
        } else {
            // procedimento
            tabela.adicionarProcedimento(ctx.IDENT().getText(), parametros);
        }
        
        return super.visitDeclaracao_global(ctx);
    }

    @Override // reescreve visitCmdAtribuição para verificar identificadores não declarados e atribuições incompatíveis
    public Void visitCmdAtribuicao(LAParser.CmdAtribuicaoContext ctx) {
        String tipoExpressao = LASemanticoUtils.verificarTipo(tabela, ctx.expressao());
        String nomeVar = ctx.identificador().getText();
        
        String tipoVariavel, realNomeVar;
        realNomeVar = nomeVar;
        if (ctx.identificador().dimensao()!= null) {
            nomeVar = nomeVar.split("\\[")[0];
        }
        if (ctx.ponteiro!=null){
            realNomeVar = "^"+nomeVar; 
        }
        if (!tipoExpressao.equals("invalido")) {
            if (!tabela.existe(nomeVar)) {
                //if (!tabela.existePonteiro(nomeVar)){
                    LASemanticoUtils.adicionarErroSemantico(ctx.identificador().start, "identificador " + nomeVar + " nao declarado");
                /*} else {
                    tipoVariavel = "inteiro";
                    if (!tipoVariavel.equals(tipoExpressao)) { // && !ehvalido(tipoVariavel, tipoExpressao)) {
                        LASemanticoUtils.adicionarErroSemantico(ctx.identificador().start, "atribuicao nao compativel para " + nomeVar);
                    }
                }*/
            } else { 
                tipoVariavel = LASemanticoUtils.verificarTipo(tabela, nomeVar);
                if (!tipoVariavel.equals(tipoExpressao) && !ehvalido(tipoVariavel, tipoExpressao)) {
                    LASemanticoUtils.adicionarErroSemantico(ctx.identificador().start, "atribuicao nao compativel para " + realNomeVar);
                }
            }
        } else {
            LASemanticoUtils.adicionarErroSemantico(ctx.identificador().start, "atribuicao nao compativel para " + realNomeVar);
        }
        return super.visitCmdAtribuicao(ctx);
    }

    @Override // reescreve visitCmdLeia para verificar se o identificador do comando já foi declarado
    public Void visitCmdLeia(LAParser.CmdLeiaContext ctx) {
        for (LAParser.IdentificadorContext fa : ctx.identificador()) {
            String nomeVar = fa.getText();
            if (fa.dimensao()!= null) {
                nomeVar = nomeVar.split("\\[")[0];
            }
            if (!tabela.existe(nomeVar)) {
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

    @Override
    public Void visitCorpo(LAParser.CorpoContext ctx) {
        if (!escoposAninhados.isEmpty()) escoposAninhados.abandonarEscopo();
        return super.visitCorpo(ctx);
    }
    
    
    
    @Override
    public Void visitCmdRetorne(LAParser.CmdRetorneContext ctx) {
        if (escoposAninhados.isEmpty())
            LASemanticoUtils.adicionarErroSemantico(ctx.start, "comando retorne nao permitido nesse escopo");
        return super.visitCmdRetorne(ctx);
    }

    
}

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package br.ufscar.dc.compiladores.compiladorest3;

import static br.ufscar.dc.compiladores.compiladorest3.LASemanticoUtils.ehTipoBasico;
import br.ufscar.dc.compiladores.compiladorest3.TabelaDeSimbolos.TipoLA;
import br.ufscar.dc.compiladores.compiladorest3.semantico.LABaseVisitor;
import br.ufscar.dc.compiladores.compiladorest3.semantico.LAParser;
import java.util.ArrayList;
import java.util.List;



public class LAGeradorC extends LABaseVisitor<Void> {

    StringBuilder saida;
    TabelaDeSimbolos tabela;

    public LAGeradorC() {
        saida = new StringBuilder();
        this.tabela = new TabelaDeSimbolos();
    }

    @Override
    public Void visitPrograma(LAParser.ProgramaContext ctx) {
        // declaracoes 'algoritmo' corpo 'fim_algoritmo' EOF 
        saida.append("#include <stdio.h>\n");
        saida.append("#include <stdlib.h>\n");
        saida.append("\n");
        visitDeclaracoes(ctx.declaracoes());
        saida.append("\n");
        saida.append("int main() {\n");
        visitCorpo(ctx.corpo());
        saida.append("return 0;\n}\n");
        return null;
    }

    @Override
    public Void visitDeclaracoes(LAParser.DeclaracoesContext ctx) {
        // decl_local_global*
        ctx.decl_local_global().forEach(dec -> visitDecl_local_global(dec));
        return null;
    }

    @Override
    public Void visitCorpo(LAParser.CorpoContext ctx) {
        // (declaracao_local)* (cmd)*
        ctx.declaracao_local().forEach(dec -> visitDeclaracao_local(dec));
        ctx.cmd().forEach(cmd -> visitCmd(cmd));
        return null;
    }

    @Override
    public Void visitDecl_local_global(LAParser.Decl_local_globalContext ctx) {
        // declaracao_local | declaracao_global
        if (ctx.declaracao_local() != null) 
            visitDeclaracao_local(ctx.declaracao_local());
        else
            visitDeclaracao_global(ctx.declaracao_global());        
        return null;
    }

    @Override
    public Void visitDeclaracao_local(LAParser.Declaracao_localContext ctx) {
        // 'declare' variavel | 'constante' IDENT ':' tipo_basico '=' valor_constante | 'tipo' IDENT ':' tipo
        if (ctx.variavel() != null) {
            // variavel
            visitVariavel(ctx.variavel());
            // insere na tabela
            String tipoVar = ctx.variavel().tipo().getText();  
            for (LAParser.IdentificadorContext fa : ctx.variavel().identificador()) {
                String nome = fa.getText();    
                if (fa.dimensao()!= null){
                    nome = nome.split("\\[")[0];
                }
                
                // Verificar se a variável já foi declarada
                if (tipoVar.startsWith("registro")) {
                    //List<String> variaveis = new ArrayList<>();
                    for (LAParser.VariavelContext vctx : ctx.variavel().tipo().registro().variavel()) {
                        //variaveis.add(vctx.getText());
                        String auxTipoVar = vctx.tipo().getText(); 
                        for (LAParser.IdentificadorContext favctx : vctx.identificador()) {
                            String nomeVar = nome+'.'+favctx.getText();
                            tabela.adicionarVariavel(nomeVar, auxTipoVar);
                        }
                    } 
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
        }
  
        if (ctx.tipo_basico() != null) {
            // 'constante' IDENT ':' tipo_basico '=' valor_constante
            saida.append("#define " + ctx.IDENT().getText() + " " + ctx.valor_constante().getText() + "\n");
            tabela.adicionarVariavel(ctx.IDENT().getText(), ctx.tipo_basico().getText());
        }
        if (ctx.tipo() != null) {
            // 'tipo' IDENT ':' tipo
            saida.append("typedef ");
            visitTipo(ctx.tipo());
            saida.append(" " + ctx.IDENT().getText() + ";\n");
            if (ctx.tipo().registro() != null){
                // insere na tabela de simbolos
                String variaveis = "";
                for (LAParser.VariavelContext va : ctx.tipo().registro().variavel()) {
                    if ("".equals(variaveis)) variaveis = va.getText();
                    else variaveis += ";" + va.getText();
                }
                tabela.adicionarVariavel(ctx.IDENT().getText(), "registro" + variaveis);
            }
        }
        return null;       
    }

    @Override
    public Void visitVariavel(LAParser.VariavelContext ctx) {
        // identificador (',' identificador)* ':' tipo
        visitTipo(ctx.tipo());
        saida.append(" ");
        int i = 0;
        for (LAParser.IdentificadorContext id : ctx.identificador()) {
            if (i == 0) saida.append(id.getText());
            else saida.append(", " + id.getText());
            i = 1;
        }
        saida.append(";\n");
        return null;
    }

    @Override
    public Void visitTipo(LAParser.TipoContext ctx) {
        // registro | tipo_estendido
        if (ctx.registro() != null) visitRegistro(ctx.registro());
        else visitTipo_estendido(ctx.tipo_estendido());
        return null;
    }
    
    @Override
    public Void visitTipo_basico(LAParser.Tipo_basicoContext ctx) {
        // 'literal' | 'inteiro' | 'real' | 'logico'
        String aux = ctx.getText();
        switch (aux) {
            case "literal":
                    saida.append("char*");
                    break;
            case "inteiro":
                    saida.append("int");
                    break;
            case "real":
                    saida.append("float");
                    break;
            case "logico":
                    saida.append("boolean");
                    break;    
        }
        return null;
    }    

    @Override
    public Void visitTipo_basico_ident(LAParser.Tipo_basico_identContext ctx) {
        // tipo_basico | IDENT
        if (ctx.IDENT() != null) saida.append(ctx.getText());
        else visitTipo_basico(ctx.tipo_basico());
        return null;
    }
    
    @Override
    public Void visitTipo_estendido(LAParser.Tipo_estendidoContext ctx) {
        // ponteiro = '^'? tipo_basico_ident
        visitTipo_basico_ident(ctx.tipo_basico_ident());
        if (ctx.ponteiro != null) saida.append("*");
        return null;
    }

    @Override
    public Void visitValor_constante(LAParser.Valor_constanteContext ctx) {
        // CADEIA | NUM_INT | NUM_REAL | 'verdadeiro' | 'falso'
        switch (ctx.getText()) {
            case "verdadeiro":
                saida.append("true");
                break;
            case "falso":
                saida.append("false");
                break;
            default:
                saida.append(ctx.getText());
                break;
        }
        return null;
    }

    @Override
    public Void visitRegistro(LAParser.RegistroContext ctx) {
        // 'registro' (variavel)* 'fim_registro'
        saida.append("struct {\n");
        ctx.variavel().forEach(var -> visitVariavel(var));
        saida.append("}");
        return null;
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
            // 'funcao' IDENT '(' parametros? ')' ':' tipo_estendido declaracao_local* cmd* 'fim_funcao'
            visitTipo_estendido(ctx.tipo_estendido());
            saida.append(" " + ctx.IDENT().getText() + "(");
            if (ctx.parametros() != null) visitParametros(ctx.parametros());
            saida.append(") {\n");
            ctx.cmd().forEach(cmd -> visitCmd(cmd));
            saida.append("}\n");
            tabela.adicionarFuncao(ctx.IDENT().getText(), ctx.tipo_estendido().getText(), parametros);
        } else {
            // 'procedimento' IDENT '(' parametros? ')' declaracao_local* cmd* 'fim_procedimento' 
            saida.append("void " + ctx.IDENT().getText() + "(");
            if (ctx.parametros() != null) visitParametros(ctx.parametros());
            saida.append(") {\n");
            ctx.declaracao_local().forEach(dec -> visitDeclaracao_local(dec));
            ctx.cmd().forEach(cmd -> visitCmd(cmd));
            saida.append("}\n");
            
            tabela.adicionarProcedimento(ctx.IDENT().getText(), parametros);
        }
        return null;
    }

    @Override
    public Void visitParametros(LAParser.ParametrosContext ctx) {
        // parametro (',' parametro)*
        int i = 0;
        for (LAParser.ParametroContext pa : ctx.parametro()) {
            if (i == 0) visitParametro(pa);
            else {
                saida.append(", ");
                visitParametro(pa);
            }
            i = 1;
        }
        return null;
    }

    @Override
    public Void visitParametro(LAParser.ParametroContext ctx) {
        // 'var'? identificador (',' identificador)* ':' tipo_estendido
        visitTipo_estendido(ctx.tipo_estendido());
        int i = 0;
        for (LAParser.IdentificadorContext id : ctx.identificador()) {
            if (i == 0) saida.append(" " + id.getText());
            else {
                saida.append(", " + id.getText());
            }
            i = 1;
        }
        return null;
    }  
    
    @Override
    public Void visitCmd(LAParser.CmdContext ctx) {
    if (ctx.cmdLeia() != null)
        visitCmdLeia(ctx.cmdLeia());
    if (ctx.cmdEscreva() != null)
        visitCmdEscreva(ctx.cmdEscreva());
    if (ctx.cmdSe() != null)
        visitCmdSe(ctx.cmdSe());
    if (ctx.cmdCaso() != null)
        visitCmdCaso(ctx.cmdCaso());
    if (ctx.cmdPara() != null)
        visitCmdPara(ctx.cmdPara());
    if (ctx.cmdEnquanto() != null)
        visitCmdEnquanto(ctx.cmdEnquanto());
    if (ctx.cmdFaca() != null)
        visitCmdFaca(ctx.cmdFaca());
    if (ctx.cmdAtribuicao() != null)
        visitCmdAtribuicao(ctx.cmdAtribuicao());
    if (ctx.cmdChamada() != null)
        visitCmdChamada(ctx.cmdChamada());
    if (ctx.cmdRetorne() != null)
        visitCmdRetorne(ctx.cmdRetorne());

        return null;
    }

    @Override
    public Void visitCmdLeia(LAParser.CmdLeiaContext ctx) {
        String nomeVars = "";
        String tipoVars = "";
        int i = 0;
        for (LAParser.IdentificadorContext ident : ctx.identificador()) {
            String nomeVar = ident.getText();
            String tipoVariavel = LASemanticoUtils.verificarTipo(tabela, nomeVar);
            String aux = "%s";
            switch (tipoVariavel) {
                case "inteiro":
                    aux = "%d";
                    break;
                case "real":
                    aux = "%f";
                    break;
                case "literal":
                    aux = "%s";
                    break;
            }
            if (i == 0) {
                nomeVars += nomeVar;
                tipoVars += aux;
            }
            else {
                nomeVars += ", " + nomeVar;
                tipoVars += ", " + aux;
            }
        }
        
        saida.append("scanf(\"" + tipoVars + "\", &" + nomeVars + ");\n");
        return null;
    }

    @Override
    public Void visitCmdEscreva(LAParser.CmdEscrevaContext ctx) {
        for (LAParser.ExpressaoContext exp : ctx.expressao()) {
            if (exp.getText().startsWith("\"") && exp.getText().endsWith("\"")) {
                String aux = exp.getText();
                aux = aux.substring(1, aux.length() - 1);
                saida.append("printf(\"" + aux + "\");\n"); 
                // nao entendi pq isso, o '"' daria problema no printf?
            } else {
                String tipoExpressao = LASemanticoUtils.verificarTipo(tabela, exp);
                String aux = "";
                switch (tipoExpressao) {
                    case "inteiro":
                        aux = "%d";
                        break;
                    case "real":
                        aux = "%f";
                        break;
                    case "literal":
                        aux = "%s";
                        break;
                }
                saida.append("printf(\"" + aux + "\", ");
                visitExpressao(exp);
                saida.append(");\n");
            }
        }
        return null;
    }

    @Override
    public Void visitCmdSe(LAParser.CmdSeContext ctx) {
        saida.append("if (");
        visitExpressao(ctx.expressao());
        saida.append(")\n");
        visitCmd(ctx.cmd(0));
        if (ctx.cmd().size() > 1) { // tem else
            saida.append("else\n");
            visitCmd(ctx.cmd(1));
        }
        return null;
    }

    @Override
    public Void visitCmdCaso(LAParser.CmdCasoContext ctx) {
        // 'caso' exp_aritmetica 'seja' selecao ('senao' senao = cmd*)? 'fim_caso'
        saida.append("switch (");
        visitExp_aritmetica(ctx.exp_aritmetica());
        saida.append(") {\n");
        if (ctx.selecao() != null && ctx.selecao().item_selecao() != null){
                for (LAParser.Item_selecaoContext itm : ctx.selecao().item_selecao()) {
                    for (LAParser.Numero_intervaloContext nit : itm.constantes().numero_intervalo()) {
                        if (nit.NUM_INT().size() > 1){
                            int i = Integer.parseInt(nit.NUM_INT(0).getText());
                            for (i = i; i <= Integer.parseInt(nit.NUM_INT(1).getText()); i++){
                                saida.append("case "+ i+":\n");
                            }
                            itm.cmd().forEach(itmcmd -> visitCmd(itmcmd));
                            saida.append("break;\n");
                        }
                        else saida.append("case "+ nit.NUM_INT(0) +":\n");
                    }
                }
        }
        if (ctx.cmd() != null){
            saida.append("default:\n");
            ctx.cmd().forEach(cmd -> visitCmd(cmd));
            saida.append("break;\n");
        }
        saida.append("}\n");
        return null;
    }

    @Override
    public Void visitCmdPara(LAParser.CmdParaContext ctx) {
        // 'para' IDENT '<-' exp_aritmetica 'ate' exp_aritmetica 'faca' cmd* 'fim_para'
        saida.append("for (" + ctx.IDENT().getText() + " = ");
        visitExp_aritmetica(ctx.exp_aritmetica(0));
        saida.append("; " + ctx.IDENT().getText() + " <= ");
        visitExp_aritmetica(ctx.exp_aritmetica(1));
        saida.append("; " + ctx.IDENT().getText() + "++) {\n");
        ctx.cmd().forEach(cmd -> visitCmd(cmd));
        saida.append("}\n");
        
        return null;
    }

    @Override
    public Void visitCmdEnquanto(LAParser.CmdEnquantoContext ctx) {
        // 'enquanto' expressao 'faca' cmd* 'fim_enquanto'
        saida.append("while (");
        visitExpressao(ctx.expressao());
        saida.append(") {\n");
        ctx.cmd().forEach(cmd -> visitCmd(cmd));
        saida.append("}\n");
        return null;
    }

    @Override
    public Void visitCmdFaca(LAParser.CmdFacaContext ctx) {
        // 'faca' cmd* 'ate' expressao
        saida.append("do {\n");
        ctx.cmd().forEach(cmd -> visitCmd(cmd));
        saida.append("} while (");
        visitExpressao(ctx.expressao());
        saida.append(");\n");
        return null;
    }

    @Override
    public Void visitCmdAtribuicao(LAParser.CmdAtribuicaoContext ctx) {
        // (ponteiro = '^')? identificador '<-' expressao
        if (ctx.ponteiro != null) saida.append("*");
        saida.append(ctx.identificador().getText() + " = ");
        visitExpressao(ctx.expressao());
        saida.append(";\n");
        return null;
    }

    @Override
    public Void visitCmdChamada(LAParser.CmdChamadaContext ctx) {
        // IDENT '(' expressao (',' expressao)* ')'
        saida.append(ctx.getText() + "\n");
        return null;
    }

    @Override
    public Void visitCmdRetorne(LAParser.CmdRetorneContext ctx) {
        // 'retorne'expressao
        saida.append("return ");
        visitExpressao(ctx.expressao());
        saida.append(";\n");
        return null;
    }

    @Override
    public Void visitExpressao(LAParser.ExpressaoContext ctx) {
        // termo_logico (op_logico_1 termo_logico)*
        visitTermo_logico(ctx.termo_logico(0));
        if (ctx.termo_logico().size() > 1)
        for (int i = 1; i < ctx.termo_logico().size(); i++) {
            saida.append(" || ");
            visitTermo_logico(ctx.termo_logico(i));
        }
        return null;
    }

    @Override
    public Void visitTermo_logico(LAParser.Termo_logicoContext ctx) {
        // fator_logico (op_logico_2 fator_logico)*
        visitFator_logico(ctx.fator_logico(0));
        if (ctx.fator_logico().size() > 1)
        for (int i = 1; i < ctx.fator_logico().size(); i++) {
            saida.append(" && ");
            visitFator_logico(ctx.fator_logico(i));
        }
        return null;
    }

    @Override
    public Void visitFator_logico(LAParser.Fator_logicoContext ctx) {
        // nao = 'nao'? parcela_logica
        if (ctx.nao != null) saida.append("!");
        visitParcela_logica(ctx.parcela_logica());
        return null;
    }

    @Override
    public Void visitParcela_logica(LAParser.Parcela_logicaContext ctx) {
        // ( 'verdadeiro' | 'falso' ) | exp_relacional
        if (ctx.exp_relacional() != null) visitExp_relacional(ctx.exp_relacional());
        else if (ctx.getText().equals("verdadeiro")) saida.append("true");
        else saida.append("false");
        return null;
    }

    @Override
    public Void visitExp_relacional(LAParser.Exp_relacionalContext ctx) {
        // exp1=exp_aritmetica (op_relacional exp2=exp_aritmetica)?
        visitExp_aritmetica(ctx.exp1);
        if (ctx.exp2 != null) {
            String aux = ctx.op_relacional().getText();
            if (aux.equals("<>")) {
                aux = "!=";
            } else if (aux.equals("=")) {
                aux = "==";
            }
            saida.append(" " + aux + " ");
            visitExp_aritmetica(ctx.exp2);
        }
        return null;
    }

    @Override
    public Void visitExp_aritmetica(LAParser.Exp_aritmeticaContext ctx) {
        // termo (op1 termo)*
        visitTermo(ctx.termo(0));
        if (ctx.termo().size() > 1)
        for (int i = 1; i < ctx.termo().size(); i++) {
            saida.append(" " + ctx.op1(i-1).getText() + " ");
            visitTermo(ctx.termo(i));
        }
        return null;
    }

    @Override
    public Void visitTermo(LAParser.TermoContext ctx) {
        // fator (op2 fator)*
        visitFator(ctx.fator(0));
        if (ctx.fator().size() > 1)
        for (int i = 1; i < ctx.fator().size(); i++) {
            saida.append(" " + ctx.op2(i-1).getText() + " ");
            visitFator(ctx.fator(i));
        }
        return null;
    }

    @Override
    public Void visitFator(LAParser.FatorContext ctx) {
        // parcela (op3 parcela)*
        visitParcela(ctx.parcela(0));
        if (ctx.parcela().size() > 1)
        for (int i = 1; i < ctx.parcela().size(); i++) {
            saida.append(" " + ctx.op3(i-1).getText() + " ");
            visitParcela(ctx.parcela(i));
        }
        return null;
    }

    @Override
    public Void visitParcela(LAParser.ParcelaContext ctx) {
        //  op_unario? parcela_unario | parcela_nao_unario
        if (ctx.parcela_nao_unario() != null) saida.append(ctx.getText());
        else {
            if (ctx.op_unario() != null) saida.append("-");
            visitParcela_unario(ctx.parcela_unario());
        }
        return null;
    }

    @Override
    public Void visitParcela_unario(LAParser.Parcela_unarioContext ctx) {
/*              '^'? identificador
            |   IDENT '(' expressao (',' expressao)* ')'
            |   NUM_INT 
            |   NUM_REAL 
            |   '(' exp1=expressao ')'
*/
        if (ctx.identificador() != null) {
            if (ctx.ponteiro != null) saida.append("*");
            saida.append(ctx.identificador().getText());
        }
        else if (ctx.IDENT() != null) {
            saida.append(ctx.IDENT().getText() + "(");
            visitExpressao(ctx.expressao(0));
            if (ctx.expressao().size() > 1)
                for (int i = 1; i < ctx.expressao().size(); i++) {
                    saida.append(", ");
                    visitExpressao(ctx.expressao(i));
                }
            saida.append(")");
        }
        else if (ctx.exp1 != null) {
            saida.append("(");
            visitExpressao(ctx.exp1);
            saida.append(")");
            }
        else saida.append(ctx.getText());
        return null;
    }
}

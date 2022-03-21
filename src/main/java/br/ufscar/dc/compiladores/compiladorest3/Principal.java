package br.ufscar.dc.compiladores.compiladorest3;

import br.ufscar.dc.compiladores.compiladorest3.semantico.LALexer;
import br.ufscar.dc.compiladores.compiladorest3.semantico.LAParser;
import br.ufscar.dc.compiladores.compiladorest3.semantico.LAParser.ProgramaContext;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

public class Principal {

    public static void main(String args[]) throws IOException {
        // args[0] é o arquivo de leitura
        // args[1] é o de escrita
        CharStream cs = CharStreams.fromFileName(args[0]);
        LALexer lexer = new LALexer(cs);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        LAParser parser = new LAParser(tokens);
        ProgramaContext arvore = parser.programa();
        LASemantico as = new LASemantico();
        as.visitPrograma(arvore);
        /*
        // escreve arquivo 
        FileWriter arq = new FileWriter(args[1]);
        PrintWriter gravarArq = new PrintWriter(arq);
        LASemanticoUtils.errosSemanticos.forEach((s) -> gravarArq.println(s));
        gravarArq.println("Fim da compilacao");
        arq.close();
        */
        LASemanticoUtils.errosSemanticos.forEach((s) -> System.out.println(s));
        
        if(LASemanticoUtils.errosSemanticos.isEmpty()) {
            LAGeradorC agc = new LAGeradorC();
            agc.visitPrograma(arvore);
            try(PrintWriter pw = new PrintWriter(args[1])) {
                pw.print(agc.saida.toString());
            }
        }
        // as.tabela.imprimir();
    }
}
import java.util.Date;


public class ImpressoraDeRecibo {

    private static final String LINHA_DUPLA = "=====================================";
    private static final String LINHA_SIMPLES = "-------------------------------------";

    public void imprimir(Cliente cliente, double subtotal, double impostos, double frete, double totalFinal) {
        System.out.println(LINHA_DUPLA);
        System.out.println("RECIBO DE VENDA");
        System.out.println(LINHA_DUPLA);
        System.out.println("Data: " + new Date());
        System.out.println("Cliente: " + cliente.getNome());
        System.out.println("Documento: " + cliente.getDocumento());
        System.out.println(LINHA_SIMPLES);
        System.out.println("Subtotal Itens: R$ " + subtotal);
        System.out.println("Total Impostos: R$ " + impostos);
        System.out.println("Frete: R$ " + frete);
        System.out.println(LINHA_SIMPLES);
        System.out.println("TOTAL A PAGAR: R$ " + totalFinal);
        System.out.println(LINHA_DUPLA);
    }
}

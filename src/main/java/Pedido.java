
public class Pedido {

    private static final double LIMITE_PRODUTO_CARO = 100;
    private static final double ALIQUOTA_PRODUTO_CARO = 0.15;
    private static final double ALIQUOTA_PRODUTO_BARATO = 0.05;

    private final double[] precosUnitarios;
    private final int[] quantidades;

    public Pedido(double[] precosUnitarios, int[] quantidades) {
        this.precosUnitarios = precosUnitarios;
        this.quantidades = quantidades;
    }

    public double calcularSubtotal() {
        double subtotal = 0;
        for (int i = 0; i < precosUnitarios.length; i++) {
            subtotal += calcularSubtotalDoItem(i);
        }
        return subtotal;
    }

    public double calcularImpostos() {
        double impostos = 0;
        for (int i = 0; i < precosUnitarios.length; i++) {
            impostos += calcularImpostoDoItem(i);
        }
        return impostos;
    }

    private double calcularSubtotalDoItem(int indice) {
        return precosUnitarios[indice] * quantidades[indice];
    }

    private double calcularImpostoDoItem(int indice) {
        return calcularSubtotalDoItem(indice) * obterAliquota(precosUnitarios[indice]);
    }

    private double obterAliquota(double precoUnitario) {
        if (precoUnitario > LIMITE_PRODUTO_CARO) {
            return ALIQUOTA_PRODUTO_CARO;
        }
        return ALIQUOTA_PRODUTO_BARATO;
    }
}

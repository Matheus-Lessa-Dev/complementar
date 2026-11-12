public class Item {
    private double valor;
    private int quantidade;

    public Item(double valor, int quantidade) {
        this.valor = valor;
        this.quantidade = quantidade;
    }

    public double getValor() { return valor; }
    public int getQuantidade() { return quantidade; }
    public double getSubtotal() { return valor * quantidade; }
}
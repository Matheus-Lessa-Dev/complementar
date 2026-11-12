public class FreteNacionalStrategy implements EstrategiaFrete {
    @Override
    public double calcularFrete(Cliente cliente) {
        return 50.0;
    }
}

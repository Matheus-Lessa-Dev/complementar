public class FreteSudesteStrategy implements EstrategiaFrete {
    @Override
    public double calcularFrete(Cliente cliente) {
        return 15.0;
    }
}

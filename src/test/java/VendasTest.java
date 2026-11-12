import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

class VendasTest {
    private static final Cliente CLIENTE = new Cliente("Ana", "12345678901", 1, "AM");

    @Test
    void preservaDadosDoCliente() {
        assertEquals("Ana", CLIENTE.getNome());
        assertEquals("12345678901", CLIENTE.getDocumento());
        assertEquals(1, CLIENTE.getTipoCliente());
        assertEquals("AM", CLIENTE.getEstado());
        assertTrue(CLIENTE.isDocumentoValido());
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "1234567890", "123456789012"})
    void rejeitaDocumentoComComprimentoInvalido(String documento) {
        Cliente cliente = new Cliente("Ana", documento, 1, "AM");
        assertFalse(cliente.isDocumentoValido());
        String recibo = capturarSaida(() -> new ProcessadorDeVendas(new FreteVipStrategy())
                .processar(cliente, List.of(new Item(10, 1))));
        assertTrue(recibo.contains("Erro: Documento invalido."));
        assertFalse(recibo.contains("RECIBO DE VENDA"));
    }

    @Test
    void preservaValidacaoOriginalPorComprimentoSemAdicionarRegraDeCpf() {
        assertTrue(new Cliente("Ana", "abcdefghijk", 1, "AM").isDocumentoValido());
    }

    @Test
    void preservaValoresDasEstrategias() {
        assertEquals(0.0, new FreteVipStrategy().calcularFrete(CLIENTE));
        assertEquals(15.0, new FreteSudesteStrategy().calcularFrete(CLIENTE));
        assertEquals(50.0, new FreteNacionalStrategy().calcularFrete(CLIENTE));
    }

    @Test
    void calculaItemComQuantidade() {
        Item item = new Item(30.0, 3);
        assertEquals(30.0, item.getValor());
        assertEquals(3, item.getQuantidade());
        assertEquals(90.0, item.getSubtotal());
    }

    @Test
    void calculaPedidoComAliquotasEmAmbosOsLadosDoLimite() {
        Pedido pedido = new Pedido(new double[] {100, 150, 30}, new int[] {2, 1, 3});
        assertEquals(440.0, pedido.calcularSubtotal());
        assertEquals(37.0, pedido.calcularImpostos());
    }

    @Test
    void calculaPedidoVazio() {
        Pedido pedido = new Pedido(new double[0], new int[0]);
        assertEquals(0.0, pedido.calcularSubtotal());
        assertEquals(0.0, pedido.calcularImpostos());
    }

    @Test
    void processadorAplicaCincoPorCentoNoPrecoExatamenteCem() {
        String recibo = capturarSaida(() -> new ProcessadorDeVendas(new FreteVipStrategy())
                .processar(CLIENTE, List.of(new Item(100.0, 2))));
        assertTrue(recibo.contains("Subtotal Itens: R$ 200.0"));
        assertTrue(recibo.contains("Total Impostos: R$ 10.0"));
        assertTrue(recibo.contains("TOTAL A PAGAR: R$ 210.0"));
    }

    @Test
    void processadorAceitaOutraImplementacaoDeFrete() {
        EstrategiaFrete fretePersonalizado = cliente -> 7.5;
        String recibo = capturarSaida(() -> new ProcessadorDeVendas(fretePersonalizado)
                .processar(CLIENTE, List.of()));
        assertTrue(recibo.contains("Subtotal Itens: R$ 0.0"));
        assertTrue(recibo.contains("Total Impostos: R$ 0.0"));
        assertTrue(recibo.contains("TOTAL A PAGAR: R$ 7.5"));
    }

    @Test
    void impressoraPreservaConteudoDoRecibo() {
        String recibo = capturarSaida(() -> new ImpressoraDeRecibo()
                .imprimir(CLIENTE, 340, 32, 15, 387));
        assertTrue(recibo.contains("RECIBO DE VENDA"));
        assertTrue(recibo.contains("Data: "));
        assertTrue(recibo.contains("Cliente: Ana"));
        assertTrue(recibo.contains("Documento: 12345678901"));
        assertTrue(recibo.contains("Subtotal Itens: R$ 340.0"));
        assertTrue(recibo.contains("Total Impostos: R$ 32.0"));
        assertTrue(recibo.contains("Frete: R$ 15.0"));
        assertTrue(recibo.contains("TOTAL A PAGAR: R$ 387.0"));
    }

    @Test
    void mainPreservaOsTresExemplosOriginais() {
        String recibos = capturarSaida(() -> ProcessadorDeVendas.main(new String[0]));
        assertEquals(3, recibos.split("RECIBO DE VENDA", -1).length - 1);
        assertTrue(recibos.contains("Cliente: Ana Souza"));
        assertTrue(recibos.contains("Cliente: João Silva"));
        assertTrue(recibos.contains("Cliente: Maria Santos"));
        assertTrue(recibos.contains("TOTAL A PAGAR: R$ 372.0"));
        assertTrue(recibos.contains("TOTAL A PAGAR: R$ 387.0"));
        assertTrue(recibos.contains("TOTAL A PAGAR: R$ 422.0"));
    }

    private static String capturarSaida(Runnable operacao) {
        PrintStream original = System.out;
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        try (PrintStream captura = new PrintStream(buffer, true, StandardCharsets.UTF_8)) {
            System.setOut(captura);
            operacao.run();
        } finally {
            System.setOut(original);
        }
        return buffer.toString(StandardCharsets.UTF_8);
    }
}

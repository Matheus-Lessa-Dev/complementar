public class Cliente {
    private String nome;
    private String documento;
    private int tipoCliente; // 1 = VIP, 2 = Comum
    private String estado;

    public Cliente(String nome, String documento, int tipoCliente, String estado) {
        this.nome = nome;
        this.documento = documento;
        this.tipoCliente = tipoCliente;
        this.estado = estado;
    }

    public String getNome() { return nome; }
    public String getDocumento() { return documento; }
    public int getTipoCliente() { return tipoCliente; }
    public String getEstado() { return estado; }
    
    public boolean isDocumentoValido() {
        return documento != null && documento.length() == 11;
    }
}
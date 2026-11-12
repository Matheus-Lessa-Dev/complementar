# Sistema reengenheirado de vendas

Aplicação de console em **Java 21**, com estratégias de frete VIP, Sudeste e nacional. O programa imprime três recibos e termina; não é um servidor HTTP e não expõe portas.

O código original consistia em nove arquivos Java na raiz, sem gerenciador de build, testes ou bibliotecas externas. As classes foram movidas, sem alterações de conteúdo, para `src/main/java`. Maven agora gera o JAR executável, executa os testes JUnit e produz cobertura JaCoCo. Não há dependências de produção; JUnit e suas dependências transitivas são usados apenas nos testes.

## Executar localmente

Requisitos: JDK 21 e Maven 3.9.11 ou compatível.

```sh
mvn --batch-mode --no-transfer-progress clean verify
java -jar target/app.jar
```

O build também rejeita métodos com complexidade ciclomática acima de 10. Os relatórios ficam em `target/surefire-reports`, `target/site/jacoco/index.html` e `target/checkstyle-result.xml`.

Com Docker Desktop em modo Linux ou Docker Engine:

```sh
docker build --pull -t sistema-reengenheirado:local .
docker run --rm sistema-reengenheirado:local
```

O Dockerfile tem uma etapa de build com JDK/Maven e uma imagem final com JRE 21, executada pelo usuário sem privilégios `10001:10001`. Os totais esperados são R$ 372,00, R$ 387,00 e R$ 422,00. A data do recibo varia conforme a execução.

## Publicar uma versão

O workflow [ci-cd.yml](.github/workflows/ci-cd.yml) executa **somente no push de tags `v*`**. Commits, pull requests, branches chamadas `v*` e execução manual não o disparam.

```text
Tag v* → build/testes/complexidade → Sonar com Quality Gate aprovado
       → Trivy de dependências → build Docker sem push → smoke test
       → Trivy da imagem → transferência da imagem aprovada → Docker Hub
```

Os dois scans Trivy bloqueiam vulnerabilidades HIGH e CRITICAL, inclusive sem correção disponível. A publicação depende do sucesso dos jobs anteriores e carrega a imagem já analisada, conferindo checksum e ID, sem novo build.

Antes da primeira tag, configure SonarCloud, Docker Hub, Secrets e Variables conforme [o guia de CI/CD](docs/ci-cd.md). O guia também explica a primeira versão, as evidências reais e a resolução de falhas. Use [o rascunho de relatório](docs/relatorio-entrega.md) para preparar o PDF da entrega após a execução real.

O relatório de arquitetura anterior permanece em [relatorio-arquitetura.md](relatorio-arquitetura.md).

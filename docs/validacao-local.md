# Validação local

Verificações executadas em **12/11/2026**, no Windows com JDK 21.0.11, Maven 3.9.11 e Docker Desktop com contêineres Linux. Não representam uma execução do GitHub Actions.

| Verificação | Resultado observado |
| --- | --- |
| Compilação e execução antes das alterações | Sucesso; recibos com totais 372.0, 387.0 e 422.0. |
| Integridade das fontes de produção | Os hashes Git das nove classes em `src/main/java` correspondem aos arquivos originais de `HEAD`, anteriormente na raiz. |
| `mvn verify` | Sucesso; 14 testes, zero falhas, zero erros e nenhum teste ignorado. |
| JaCoCo | 109/109 linhas e 18/18 ramificações cobertas no código de produção instrumentado. Oito classes concretas; a interface não contém código executável. |
| Checkstyle no código real | Zero violações do limite de complexidade ciclomática 10. |
| Checkstyle, controle negativo | Uma classe temporária fora das fontes do projeto, com método de complexidade 12, foi rejeitada com erro `CyclomaticComplexity` e falha do build. |
| actionlint 1.7.7 | Workflow validado sem diagnósticos. |
| `docker build --pull --platform linux/amd64` | Sucesso; os 14 testes também passaram no estágio Linux do Dockerfile. |
| `docker run --rm --network none sistema-reengenheirado:local` | Sucesso; três recibos e os três totais originais, sem dependência de rede. |
| Inspeção da imagem final | Usuário `10001:10001`; entrypoint `java -jar /app/app.jar`. |
| Trivy de dependências | Inconclusivo: falhou na obtenção do banco de CVEs, antes da análise; detalhes abaixo. |
| Trivy da imagem | Inconclusivo: tentativa sobre a imagem exportada também falhou no download do banco de CVEs, com `context deadline exceeded`. |
| `git diff --check` | Sem erros de whitespace. |

O Maven e o actionlint usados na validação foram baixados de fontes oficiais e tiveram seus checksums conferidos. O download do executável Windows do Trivy não concluiu e foi interrompido; foi utilizada a imagem oficial `aquasec/trivy:0.70.0`, que confirmou a versão 0.70.0. Ferramentas, caches e a classe de controle negativo ficam apenas em `.work/`, ignorado pelo Git e fora do contexto Docker.

O scan de dependências terminou com código 1 antes de consultar CVEs: o download de `mirror.gcr.io/aquasec/trivy-db:2` falhou com `unexpected EOF`. Uma segunda tentativa diretamente em `ghcr.io/aquasecurity/trivy-db:2`, com prazo de cinco minutos, terminou com `context deadline exceeded`. O scan separado da imagem exportada, usando `trivy image --input` e prazo de dois minutos, também terminou com código 1 e `context deadline exceeded` ao baixar o banco. Isso não demonstra ausência nem presença de vulnerabilidades. O workflow mantém a falha do scanner como bloqueio de publicação; é necessário repetir os scans quando o acesso ao banco estiver funcionando.

Identificadores observados no build local:

```text
Imagem da aplicação:
sha256:1016c5923434d66ff27d1012ba72e18afc9eb50b6e972f935251ae9a5ec79976

Base de execução eclipse-temurin:21-jre-noble:
sha256:0d57a8e90c2bb844c5a8e1b2b71e11eff06dad7edcfd803cd21ed4ae203b2515

Scanner aquasec/trivy:0.70.0:
sha256:be1190afcb28352bfddc4ddeb71470835d16462af68d310f9f4bca710961a41e
```

Esses IDs são registros locais, não digests de uma imagem publicada da aplicação no Docker Hub. As tags de base podem receber atualizações; o workflow baixa a base e analisa a imagem final de cada release antes de publicá-la.

## Verificações externas pendentes

- Autenticação e análise no projeto SonarCloud da equipe, inclusive o resultado real do Quality Gate, a complexidade ciclomática, os Code Smells e a duplicação apresentados pelo servidor.
- Execução do workflow por push de tag `v*` no GitHub, após cadastrar Variables e Secrets.
- Autenticação, push e confirmação da tag/digest no repositório público Docker Hub da equipe.
- Capturas reais descritas em [ci-cd.md](ci-cd.md), para finalizar o relatório e exportá-lo em PDF.

O JaCoCo local não substitui o relatório Sonar, e o build local não comprova sucesso remoto. Nenhuma tag foi criada ou enviada, nenhum código foi enviado ao Sonar e nenhuma imagem da aplicação foi publicada durante esta implementação.

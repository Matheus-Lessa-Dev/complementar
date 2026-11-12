# Configuração e operação do CI/CD

## Estrutura e critérios de aprovação

O workflow `.github/workflows/ci-cd.yml` aceita apenas `push.tags: ['v*']`. Não possui `workflow_dispatch`, `pull_request`, agendamento ou gatilho para branches. As Actions são fixadas em SHAs de commits. A imagem de build fixa Maven 3.9.11; plugins, dependências e o scanner Trivy também têm versões explícitas. O primeiro job usa o Maven disponível no runner Ubuntu.

1. **Análise de código e dependências:** Java 21, `mvn clean verify`, testes, cobertura, limite de complexidade e SonarScanner for Maven. O scanner usa `sonar.qualitygate.wait=true`: aguarda o processamento no servidor e falha se o Quality Gate for reprovado, houver erro ou o prazo de 600 segundos se esgotar. Enviar o relatório com sucesso não libera a publicação. Em seguida, Trivy examina o projeto e os JARs de dependências materializados em `target/dependency`, incluindo dependências transitivas de teste.
2. **Build e análise da imagem:** depende do primeiro job. Faz build com `push: false`, carrega a imagem no Docker do runner, executa os três exemplos e verifica os totais. Trivy analisa sistema operacional, JRE e bibliotecas identificáveis na imagem final. Somente depois do sucesso exporta `image.tar`, checksum SHA-256 e ID da imagem.
3. **Publicação:** depende dos dois jobs anteriores. Baixa o artefato desta execução, confere checksum e ID, faz login e executa `docker push`. Não reconstrói a imagem. O token Docker só é fornecido ao passo de login deste job.

Os dois scans usam `severity: HIGH,CRITICAL`, `exit-code: '1'` e `ignore-unfixed: 'false'`. Não há `continue-on-error` nos controles. Erros de download do banco de CVEs ou do scanner também impedem o push. Não se usa `latest` para a imagem publicada; a referência recebe exatamente a tag Git. A plataforma configurada é `linux/amd64`.

O Sonar é configurado no `pom.xml` e por argumentos do workflow. O SonarScanner for Maven identifica fontes, testes, bytecode e bibliotecas a partir do build; lê `target/site/jacoco/jacoco.xml`. Não é necessário `sonar-project.properties`, que não é a configuração usada por esse scanner.

## Configuração externa obrigatória

### GitHub e Docker Hub

1. Confirme que o repositório `https://github.com/Matheus-Lessa-Dev/complementar` é público e habilite GitHub Actions nas configurações. Se o código for enviado a outro repositório, atualize o link do relatório.
2. Crie um repositório **público** no Docker Hub, por exemplo `SEU_USUARIO/sistema-reengenheirado`, usando seu usuário real em minúsculas.
3. Gere um Personal Access Token do Docker Hub com permissão de escrita nesse repositório. Use esse token em `DOCKER_PASSWORD`; não coloque credenciais nos arquivos do projeto.
4. Em **Settings → Secrets and variables → Actions**, configure os valores abaixo antes da primeira tag.

Secrets do repositório:

| Nome | Valor |
| --- | --- |
| `DOCKER_USERNAME` | Usuário do Docker Hub autorizado a publicar a imagem. |
| `DOCKER_PASSWORD` | Token de acesso do Docker Hub com escrita. |
| `SONAR_TOKEN` | Token SonarCloud com permissão **Execute Analysis** no projeto. |

Variables do repositório, aba **Variables**, não Secrets:

| Nome | Valor |
| --- | --- |
| `DOCKER_IMAGE` | Nome completo `usuario/sistema-reengenheirado`, sem `https://`, sem `docker.io/` e sem tag. |
| `SONAR_PROJECT_KEY` | Project Key exato fornecido pelo SonarCloud, sem adivinhá-lo pelo nome do GitHub. |
| `SONAR_ORGANIZATION` | Organization Key exata da organização SonarCloud. |

`GITHUB_TOKEN` é fornecido pelo próprio GitHub; não é necessário criar um Secret com esse nome. O workflow solicita apenas `contents: read`. Em organizações, permita as Actions usadas pelo arquivo. Para manter a regra de release, recomenda-se restringir a criação/alteração de tags `v*` aos responsáveis e não sobrescrever versões já publicadas.

### SonarCloud / SonarQube Cloud

1. Entre em `https://sonarcloud.io` com GitHub e importe o repositório público para uma organização. Esta implementação usa a instância europeia (`sonarcloud.io`); projetos na região US exigem adaptação de região/endpoint.
2. Copie as chaves de organização e projeto para as Variables. Gere um token e registre-o como `SONAR_TOKEN`.
3. Em **Administration → Analysis Method**, desabilite **Automatic Analysis** e use análise pelo CI. Evite outro workflow Sonar gerado pelo assistente, pois ele poderia executar em commits comuns.
4. Configure a branch principal do projeto como a branch principal do GitHub (`main` neste repositório). A versão enviada é `github.ref_name`; a tag não é configurada como uma branch Sonar separada.
5. Confirme em **Quality Gate** que um gate está efetivamente associado ao projeto. No plano gratuito, utilize **Sonar way**. Verifique suas condições na interface e mantenha o gate ativo; `sonar.qualitygate.wait` aguarda o resultado desse gate, não cria condições no servidor.
6. Confira o perfil Java e consulte os resultados em **Measures → Complexity**, **Duplications** e **Issues/Maintainability**. A métrica `complexity` é a **complexidade ciclomática**; `cognitive_complexity` é outra métrica e não substitui a exigida pelo trabalho. Os Code Smells podem aparecer na interface atual como problemas de manutenibilidade.
7. Registre a definição de **New Code** usada pelo projeto. O Sonar way prioriza código novo; um gate aprovado não significa ausência de problemas no código total. Capture também as métricas de **Overall Code** para a avaliação acadêmica.

Não basta exibir a complexidade no dashboard para bloqueá-la. Por isso `config/checkstyle.xml` impõe **complexidade ciclomática máxima de 10 por método/construtor** em todo o código de produção durante `validate`, inclusive na primeira versão. Esse controle complementa o Sonar way. Perfis e gates personalizados no SonarCloud dependem do plano; se disponíveis, a equipe pode habilitar também a regra Java `S1541` com limite 10 e um gate que reprove os problemas correspondentes. A implementação não depende desses recursos pagos.

O código foi preservado: duplicações ou Code Smells existentes devem ser observados no Sonar, e podem exigir outra rodada de refatoração se o gate associado os reprovar. Não se adicionaram exclusões para ocultar as classes originais ou remover a análise de duplicações.

## Primeira versão

Execute primeiro:

```sh
mvn --batch-mode --no-transfer-progress clean verify
docker build --pull -t sistema-reengenheirado:local .
docker run --rm sistema-reengenheirado:local
```

Após configurar os serviços, conferir `git remote -v` e revisar os arquivos:

```sh
git add .
git commit -m "Configura CI/CD com Sonar, Trivy e Docker Hub"
git push origin main
git tag -a v1.0.0 -m "Primeira versao com controles de qualidade e seguranca"
git push origin v1.0.0
```

O push de `main` não dispara este workflow. O push de `v1.0.0` inicia a esteira. Use uma tag Docker válida com até 128 caracteres; prefira `vMAJOR.MINOR.PATCH`, como `v1.0.0` ou `v1.1.2`. A versão do JAR está em `pom.xml`; mantenha-a alinhada à próxima versão estável quando fizer novas entregas.

Após o sucesso, abra `https://hub.docker.com/r/SEU_USUARIO/sistema-reengenheirado/tags` com o usuário real e execute:

```sh
docker pull SEU_USUARIO/sistema-reengenheirado:v1.0.0
docker run --rm SEU_USUARIO/sistema-reengenheirado:v1.0.0
```

Se houver falha, não publique manualmente nem remova o bloqueio. Corrija o código, as dependências ou a base da imagem, crie um novo commit e uma nova tag, por exemplo `v1.0.1`. Falhas transitórias de rede ou configuração podem ser reexecutadas na mesma execução pelo GitHub Actions depois da correção externa, sem alterar a tag. Um novo CVE pode bloquear uma imagem que antes passava; a base deve ser atualizada e novamente analisada.

## Reproduzir o Trivy localmente

Instale a versão oficial **0.70.0** do Trivy. Execute depois de `mvn verify` e do build Docker:

```sh
trivy fs --scanners vuln --pkg-types library --severity HIGH,CRITICAL --ignore-unfixed=false --exit-code 1 --format table --skip-dirs .git,.cache,.work .
trivy image --scanners vuln --pkg-types os,library --severity HIGH,CRITICAL --ignore-unfixed=false --exit-code 1 --format table sistema-reengenheirado:local
```

Os scans dependem de acesso ao banco atualizado de vulnerabilidades e, para Java, ao banco de identificação de JARs. A ausência de bibliotecas de produção não dispensa o scan da imagem: o JRE e a distribuição Linux também podem conter CVEs. A imagem de build Maven/JDK não é publicada; o scan de imagem cobre o estágio final de execução.

## Evidências reais para o PDF

Nenhuma captura de sucesso remoto foi criada nesta implementação. Após uma execução real aprovada, capture:

| Captura | Conteúdo que deve ficar visível |
| --- | --- |
| Actions, visão geral | Nome do workflow, tag `v1.0.0` (ou a versão real), commit e os três jobs aprovados, incluindo publicação. |
| Sonar, dashboard | Projeto correto, versão analisada e **Quality Gate: Passed**. |
| Sonar, métricas | **Cyclomatic Complexity / `complexity`** com valor numérico; se não couber no dashboard, faça captura adicional em Measures. Inclua Code Smells/manutenibilidade e duplicação, mesmo quando forem zero. |
| Actions, Trivy dependências | Etapa **Exibir tabela Trivy de dependencias nos logs**, com a tabela/resumo gerado e as severidades HIGH/CRITICAL. |
| Actions, Trivy imagem | Etapa **Exibir tabela Trivy da imagem nos logs**, com os alvos analisados e o resultado. |
| Docker Hub | Repositório público, tag publicada e digest; complemente com o log de `docker push` no Actions. |

Quando o scanner não encontrar vulnerabilidades, capture o resumo com total zero; não invente uma tabela de CVEs nem introduza falhas para fabricar evidência. Preserve os links da execução, do projeto Sonar e da tag no Docker Hub. Não exponha tokens nas capturas.

Os artefatos `analise-codigo-e-dependencias` e `relatorios-imagem` ficam disponíveis por 14 dias. `imagem-aprovada` expira em um dia para reduzir armazenamento; se expirar antes de uma reexecução parcial, reexecute todos os jobs para gerar e verificar outra imagem.

Consulte [validacao-local.md](validacao-local.md) para distinguir verificações locais de resultados externos pendentes. Preencha [relatorio-entrega.md](relatorio-entrega.md), insira as capturas reais e exporte esse relatório para PDF antes de enviar ao D2L.

## Referências oficiais

- [SonarScanner for Maven](https://docs.sonarsource.com/sonarqube-cloud/analyzing-source-code/scanners/sonarscanner-for-maven)
- [SonarCloud no GitHub Actions e espera do Quality Gate](https://docs.sonarsource.com/sonarqube-cloud/analyzing-source-code/ci-based-analysis/github-actions-for-sonarcloud)
- [Planos e perfis de qualidade SonarCloud](https://docs.sonarsource.com/sonarqube-cloud/standards/managing-quality-profiles/understanding-quality-profiles)
- [Trivy Action: entradas e códigos de saída](https://github.com/aquasecurity/trivy-action/tree/v0.36.0)
- [Checkstyle CyclomaticComplexity](https://checkstyle.org/checks/metrics/cyclomaticcomplexity.html)

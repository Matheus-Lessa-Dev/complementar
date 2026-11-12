# Relatório de CI/CD — rascunho para entrega

**Status:** aguarda execução real no GitHub Actions e inclusão das evidências. Este documento não comprova publicação nem aprovação remota. Exporte para PDF somente depois de preencher os campos e inserir as capturas reais.

**Disciplina / turma:** [preencher]

**Integrantes e líder:** [preencher]

**Data da execução:** [preencher]

**Versão e commit:** [preencher com tag e SHA reais]

## Links verificáveis

- Repositório GitHub: https://github.com/Matheus-Lessa-Dev/complementar — confirmar acesso público.
- Execução GitHub Actions: [inserir URL da execução real].
- Projeto SonarCloud: [inserir URL real].
- Imagem pública Docker Hub: [inserir URL com usuário e repositório reais].
- Tag publicada e digest: [preencher a partir do push e do Docker Hub].

## Implementação

O sistema usa Java 21 e o padrão Strategy para fretes. A entrega adiciona Maven, JUnit, JaCoCo e um Dockerfile com build em múltiplos estágios. As nove classes de produção mantêm o conteúdo original. O programa continua imprimindo três recibos e encerrando, com totais de R$ 372,00, R$ 387,00 e R$ 422,00.

O workflow é acionado exclusivamente por push de tags `v*`. O primeiro job compila, testa, bloqueia complexidade ciclomática acima de 10 por método/construtor, envia a análise ao Sonar e espera a aprovação efetiva do Quality Gate. Depois verifica dependências diretas e transitivas com Trivy. O segundo job constrói a imagem sem publicar, executa um smoke test e analisa a imagem final com Trivy. Ambos os scans bloqueiam HIGH e CRITICAL, inclusive vulnerabilidades sem correção disponível.

O último job só executa após todos esses controles passarem. Ele verifica a integridade e a identidade da imagem aprovada e publica essa mesma imagem no Docker Hub, sem reconstrução. Os Secrets de acesso são armazenados no GitHub e não no repositório.

## Evidências a inserir após a execução

1. **GitHub Actions:** inserir captura da execução com tag, commit e os três jobs concluídos com sucesso.
2. **Sonar:** inserir captura do Quality Gate aprovado e da métrica de **Complexidade Ciclomática**. Registrar valor: [preencher]. Incluir Code Smells/manutenibilidade: [preencher], duplicação: [preencher] e definição de código novo: [preencher]. Não usar complexidade cognitiva no lugar de ciclomática.
3. **Trivy de dependências:** inserir captura da tabela/resumo real dos logs. Resultado HIGH/CRITICAL: [preencher].
4. **Trivy da imagem:** inserir captura da tabela/resumo real dos logs. Resultado HIGH/CRITICAL: [preencher]. Se não houver CVEs, manter o resumo de total zero.
5. **Docker Hub:** inserir captura da tag pública e do digest, junto do log de push, se necessário.

## Ensaio crítico final

A entropia de software aparece quando sucessivas mudanças tornam o sistema mais difícil de compreender, testar e manter. O diagnóstico de Code Smells, a refatoração e a adoção de Strategy ajudaram a separar responsabilidades e reduzir decisões concentradas em uma única classe. Entretanto, essas melhorias podem se perder se cada nova entrega depender apenas de uma revisão manual. A esteira transforma parte desse cuidado em um processo repetível: os testes verificam comportamentos existentes, o limite de complexidade restringe o crescimento dos métodos, e o Sonar mede manutenibilidade, duplicação e complexidade, condicionando a entrega ao Quality Gate configurado.

O débito técnico também inclui o custo de manter componentes vulneráveis e entregas difíceis de reproduzir. O Trivy torna esse risco visível nas dependências e na imagem de execução, enquanto o Docker e as tags associam cada versão publicada a um código e a uma imagem verificáveis. Esses controles não eliminam a necessidade de decisões de arquitetura, atualização de dependências ou revisão humana: eles impedem que problemas detectados sejam simplesmente ignorados no lançamento e fornecem retorno para uma nova correção. Dessa forma, a equipe passa a tratar a evolução segura como parte de cada entrega, reduzindo o acúmulo de dívida que encarece mudanças futuras.

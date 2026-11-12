Como a aplicação do Padrão Strategy reduziu a Complexidade Ciclomática da classe original?


Antes, a classe CalculadoraFrete usava vários ifs para verificar o tipo do cliente e o estado, decidindo qual valor de frete retornar. Cada condição criava caminhos diferentes para o código executar. Com o Strategy, essa classe foi substituída por três estratégias separadas, cada uma retornando seu valor diretamente. O ProcessadorDeVendas só chama calcularFrete(cliente) na estratégia recebida, sem precisar de condições para escolher o frete. Assim, a lógica de frete ficou com menos decisões e mais fácil de entender e testar. Os ifs de validação e impostos continuam, pois não fazem parte dessa mudança.

Como essa nova estrutura respeita rigorosamente o OCP (Open/Closed Principle) e o DIP (Dependency Inversion Principle)?

O OCP é respeitado porque podemos adicionar um novo tipo de frete criando outra classe que implemente EstrategiaFrete. Por exemplo, para adicionar FreteInternacionalStrategy, não precisamos alterar a lógica de processamento das vendas. Basta criar a estratégia e passá-la ao montar o processador. A escolha da estratégia fica no main, que configura os exemplos.

O DIP é respeitado porque o ProcessadorDeVendas passou a depender da interface EstrategiaFrete, em vez da classe concreta CalculadoraFrete. As estratégias também seguem essa interface e são recebidas pelo construtor. Dessa forma, o processador consegue trabalhar com qualquer implementação de EstrategiaFrete sem conhecer seus detalhes.

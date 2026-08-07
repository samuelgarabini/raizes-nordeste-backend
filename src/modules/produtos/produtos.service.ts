import { ProdutoRepository, Produto } from './produtos.repository';

export class ProdutoService {
  private produtoRepository: ProdutoRepository;

  constructor(produtoRepository: ProdutoRepository) {
    this.produtoRepository = produtoRepository;
  }

  async listarTodos() {
    return await this.produtoRepository.listarTodos();
  }

  async buscarPorId(id: number) {
    const produto = await this.produtoRepository.buscarPorId(id);
    if (!produto) {
      throw new Error('Produto não encontrado');
    }
    return produto;
  }

  async criar(dados: Produto) {
    if (!dados.nome || !dados.preco) {
      throw new Error('Nome e preço são obrigatórios');
    }
    return await this.produtoRepository.criar(dados);
  }

  async atualizar(id: number, dados: Partial<Produto>) {
    const produtoExistente = await this.produtoRepository.buscarPorId(id);
    if (!produtoExistente) {
      throw new Error('Produto não encontrado');
    }
    return await this.produtoRepository.atualizar(id, dados);
  }

  async deletar(id: number) {
    const produtoExistente = await this.produtoRepository.buscarPorId(id);
    if (!produtoExistente) {
      throw new Error('Produto não encontrado');
    }
    return await this.produtoRepository.deletar(id);
  }
}
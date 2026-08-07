import { EstoqueRepository } from './estoque.repository';

export class EstoqueService {
  private estoqueRepository: EstoqueRepository;

  constructor(estoqueRepository: EstoqueRepository) {
    this.estoqueRepository = estoqueRepository;
  }

  async consultarSaldo(produto_id: number, unidade_id: number) {
    const saldo = await this.estoqueRepository.consultarSaldo(produto_id, unidade_id);
    if (!saldo) {
      return { produto_id, unidade_id, quantidade: 0 };
    }
    return saldo;
  }

  async listarPorUnidade(unidade_id: number) {
    return await this.estoqueRepository.listarPorUnidade(unidade_id);
  }

  async movimentarEstoque(produto_id: number, unidade_id: number, quantidade: number, tipo: 'ENTRADA' | 'SAIDA') {
    if (!produto_id || !unidade_id || !quantidade || quantidade <= 0) {
      throw new Error('Produto, unidade e quantidade válida são obrigatórios');
    }

    if (tipo === 'SAIDA') {
      const saldoAtual = await this.consultarSaldo(produto_id, unidade_id);
      if (saldoAtual.quantidade < quantidade) {
        throw new Error('Saldo insuficiente em estoque para esta unidade');
      }
      return await this.estoqueRepository.atualizarOuCriarSaldo(produto_id, unidade_id, -quantidade);
    }

    return await this.estoqueRepository.atualizarOuCriarSaldo(produto_id, unidade_id, quantidade);
  }
}
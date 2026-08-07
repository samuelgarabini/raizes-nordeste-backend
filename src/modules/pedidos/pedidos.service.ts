import { PedidoRepository, ItemPedidoInput } from './pedidos.repository';
import { CampanhaRepository } from '../campanhas/campanhas.repository';

export class PedidoService {
  private pedidoRepository: PedidoRepository;
  private campanhaRepository: CampanhaRepository;

  constructor(pedidoRepository: PedidoRepository, campanhaRepository: CampanhaRepository) {
    this.pedidoRepository = pedidoRepository;
    this.campanhaRepository = campanhaRepository;
  }

  async criarPedido(dados: {
    usuario_id: number;
    unidade_id: number;
    itens: ItemPedidoInput[];
    desconto_manual_percentual?: number;
  }) {
    const { usuario_id, unidade_id, itens, desconto_manual_percentual = 0 } = dados;

    const unidadeExiste = await this.pedidoRepository.verificarUnidadeExiste(unidade_id);
    if (!unidadeExiste) {
      const err: any = new Error('Unidade não encontrada');
      err.statusCode = 404;
      throw err;
    }

    if (!itens || itens.length === 0) {
      throw new Error('O pedido deve conter ao menos um item');
    }

    let valorBruto = 0;
    const itensProcessados = [];

    for (const item of itens) {
      const produto = await this.pedidoRepository.verificarProdutoExiste(item.produto_id);
      if (!produto) {
        const err: any = new Error(`Produto com ID ${item.produto_id} não encontrado`);
        err.statusCode = 404;
        throw err;
      }

      const precoUnitario = Number(produto.preco);
      valorBruto += precoUnitario * item.quantidade;
      itensProcessados.push({
        produto_id: item.produto_id,
        quantidade: item.quantidade,
        preco_unitario: precoUnitario
      });
    }

    const campanhasAtivas = await this.campanhaRepository.listarAtivas();
    let maiorDescontoCampanhaPercentual = 0;
    
    campanhasAtivas.forEach(c => {
      if (Number(c.desconto_percentual) > maiorDescontoCampanhaPercentual) {
        maiorDescontoCampanhaPercentual = Number(c.desconto_percentual);
      }
    });

    const requerAuditoria = desconto_manual_percentual > 15;
    const percentualDescontoTotal = maiorDescontoCampanhaPercentual + desconto_manual_percentual;
    
    const valorDesconto = (valorBruto * percentualDescontoTotal) / 100;
    const valorFinal = Math.max(0, valorBruto - valorDesconto);

    return await this.pedidoRepository.criarPedido({
      usuario_id,
      unidade_id,
      valor_bruto: valorBruto,
      desconto: valorDesconto,
      valor_final: valorFinal,
      desconto_manual_percentual,
      requer_auditoria: requerAuditoria,
      itens: itensProcessados
    });
  }

  async buscarPorId(id: string) {
    const pedido = await this.pedidoRepository.buscarPorId(id);
    if (!pedido) {
      const err: any = new Error('Pedido não encontrado');
      err.statusCode = 404;
      throw err;
    }
    return pedido;
  }
}
import { Pool } from 'pg';

export interface ItemPedidoInput {
  produto_id: number;
  quantidade: number;
}

export class PedidoRepository {
  private pool: Pool;

  constructor(pool: Pool) {
    this.pool = pool;
  }

  async verificarProdutoExiste(produtoId: number): Promise<any> {
    const res = await this.pool.query('SELECT * FROM produtos WHERE id = $1', [produtoId]);
    return res.rows[0] || null;
  }

  async verificarUnidadeExiste(unidadeId: number): Promise<boolean> {
    return unidadeId > 0 && unidadeId <= 100;
  }

  async criarPedido(dados: {
    usuario_id: number;
    unidade_id: number;
    valor_bruto: number;
    desconto: number;
    valor_final: number;
    desconto_manual_percentual: number;
    requer_auditoria: boolean;
    itens: Array<{ produto_id: number; quantidade: number; preco_unitario: number }>;
  }) {
    const client = await this.pool.connect();
    try {
      await client.query('BEGIN');

      const pedRes = await client.query(
        `INSERT INTO pedidos 
         (usuario_id, unidade_id, valor_bruto, desconto, valor_final, desconto_manual_percentual, requer_auditoria)
         VALUES ($1, $2, $3, $4, $5, $6, $7) RETURNING *`,
        [
          dados.usuario_id,
          dados.unidade_id,
          dados.valor_bruto,
          dados.desconto,
          dados.valor_final,
          dados.desconto_manual_percentual,
          dados.requer_auditoria
        ]
      );

      const pedidoId = pedRes.rows[0].id; // id retornado como string UUID

      for (const item of dados.itens) {
        await client.query(
          'INSERT INTO pedido_itens (pedido_id, produto_id, quantidade, preco_unitario) VALUES ($1, $2, $3, $4)',
          [pedidoId, item.produto_id, item.quantidade, item.preco_unitario]
        );
      }

      await client.query('COMMIT');
      return { ...pedRes.rows[0], itens: dados.itens };
    } catch (error) {
      await client.query('ROLLBACK');
      throw error;
    } finally {
      client.release();
    }
  }

  async buscarPorId(id: string) {
    const res = await this.pool.query('SELECT * FROM pedidos WHERE id = $1', [id]);
    return res.rows[0] || null;
  }
}
import { Pool } from 'pg';

export interface EstoqueItem {
  id?: number;
  produto_id: number;
  unidade_id: number;
  quantidade: number;
  atualizado_em?: Date;
}

export class EstoqueRepository {
  private pool: Pool;

  constructor(pool: Pool) {
    this.pool = pool;
  }

  async consultarSaldo(produto_id: number, unidade_id: number): Promise<EstoqueItem | null> {
    const result = await this.pool.query(
      'SELECT * FROM estoque WHERE produto_id = $1 AND unidade_id = $2',
      [produto_id, unidade_id]
    );
    return result.rows[0] || null;
  }

  async listarPorUnidade(unidade_id: number): Promise<EstoqueItem[]> {
    const result = await this.pool.query(
      'SELECT e.*, p.nome as produto_nome FROM estoque e JOIN produtos p ON e.produto_id = p.id WHERE e.unidade_id = $1',
      [unidade_id]
    );
    return result.rows;
  }

  async atualizarOuCriarSaldo(produto_id: number, unidade_id: number, quantidade: number): Promise<EstoqueItem> {
    const result = await this.pool.query(
      `INSERT INTO estoque (produto_id, unidade_id, quantidade)
       VALUES ($1, $2, $3)
       ON CONFLICT (produto_id, unidade_id) 
       DO UPDATE SET quantidade = estoque.quantidade + EXCLUDED.quantidade, atualizado_em = CURRENT_TIMESTAMP
       RETURNING *`,
      [produto_id, unidade_id, quantidade]
    );
    return result.rows[0];
  }
}
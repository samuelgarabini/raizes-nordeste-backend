import { Pool } from 'pg';

export interface Produto {
  id?: number;
  nome: string;
  descricao: string;
  preco: number;
  categoria: string;
  ativo?: boolean;
}

export class ProdutoRepository {
  private pool: Pool;

  constructor(pool: Pool) {
    this.pool = pool;
  }

  async listarTodos(): Promise<Produto[]> {
    const result = await this.pool.query('SELECT * FROM produtos ORDER BY id ASC');
    return result.rows;
  }

  async buscarPorId(id: number): Promise<Produto | null> {
    const result = await this.pool.query('SELECT * FROM produtos WHERE id = $1', [id]);
    return result.rows[0] || null;
  }

  async criar(produto: Produto): Promise<Produto> {
    const { nome, descricao, preco, categoria } = produto;
    const result = await this.pool.query(
      'INSERT INTO produtos (nome, descricao, preco, categoria) VALUES ($1, $2, $3, $4) RETURNING *',
      [nome, descricao, preco, categoria]
    );
    return result.rows[0];
  }

  async atualizar(id: number, produto: Partial<Produto>): Promise<Produto | null> {
    const { nome, descricao, preco, categoria, ativo } = produto;
    const result = await this.pool.query(
      `UPDATE produtos 
       SET nome = COALESCE($1, nome), 
           descricao = COALESCE($2, descricao), 
           preco = COALESCE($3, preco), 
           categoria = COALESCE($4, categoria),
           ativo = COALESCE($5, ativo)
       WHERE id = $6 RETURNING *`,
      [nome, descricao, preco, categoria, ativo, id]
    );
    return result.rows[0] || null;
  }

  async deletar(id: number): Promise<boolean> {
    const result = await this.pool.query('DELETE FROM produtos WHERE id = $1', [id]);
    return (result.rowCount ?? 0) > 0;
  }
}
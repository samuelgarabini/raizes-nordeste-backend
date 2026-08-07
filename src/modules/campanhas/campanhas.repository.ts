import { Pool } from 'pg';

export interface Campanha {
  id?: number;
  nome: string;
  desconto_percentual: number;
  ativa?: boolean;
}

export class CampanhaRepository {
  private pool: Pool;

  constructor(pool: Pool) {
    this.pool = pool;
  }

  async criar(campanha: Campanha): Promise<Campanha> {
    const { nome, desconto_percentual } = campanha;
    const res = await this.pool.query(
      'INSERT INTO campanhas (nome, desconto_percentual) VALUES ($1, $2) RETURNING *',
      [nome, desconto_percentual]
    );
    return res.rows[0];
  }

  async listarAtivas(): Promise<Campanha[]> {
    const res = await this.pool.query('SELECT * FROM campanhas WHERE ativa = TRUE ORDER BY id DESC');
    return res.rows;
  }
}
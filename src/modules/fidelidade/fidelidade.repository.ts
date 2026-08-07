import { Pool } from 'pg';

export interface ExtratoItem {
  id?: number;
  usuario_id: number;
  pontos: number;
  tipo: 'ACUMULO' | 'RESGATE';
  descricao: string;
  criado_em?: Date;
}

export class FidelidadeRepository {
  private pool: Pool;

  constructor(pool: Pool) {
    this.pool = pool;
  }

  async obterSaldo(usuarioId: number): Promise<number> {
    const result = await this.pool.query('SELECT pontos FROM fidelidade WHERE usuario_id = $1', [usuarioId]);
    return result.rows[0]?.pontos || 0;
  }

  async obterExtrato(usuarioId: number): Promise<ExtratoItem[]> {
    const result = await this.pool.query(
      'SELECT * FROM fidelidade_extrato WHERE usuario_id = $1 ORDER BY criado_em DESC',
      [usuarioId]
    );
    return result.rows;
  }

  async adicionarPontos(usuarioId: number, pontos: number, descricao: string): Promise<number> {
    const client = await this.pool.connect();
    try {
      await client.query('BEGIN');

      const res = await client.query(
        `INSERT INTO fidelidade (usuario_id, pontos)
         VALUES ($1, $2)
         ON CONFLICT (usuario_id)
         DO UPDATE SET pontos = fidelidade.pontos + EXCLUDED.pontos, atualizado_em = CURRENT_TIMESTAMP
         RETURNING pontos`,
        [usuarioId, pontos]
      );

      await client.query(
        'INSERT INTO fidelidade_extrato (usuario_id, pontos, tipo, descricao) VALUES ($1, $2, $3, $4)',
        [usuarioId, pontos, 'ACUMULO', descricao]
      );

      await client.query('COMMIT');
      return res.rows[0].pontos;
    } catch (error) {
      await client.query('ROLLBACK');
      throw error;
    } finally {
      client.release();
    }
  }

  async resgatarPontos(usuarioId: number, pontos: number, descricao: string): Promise<number> {
    const client = await this.pool.connect();
    try {
      await client.query('BEGIN');

      const res = await client.query(
        'UPDATE fidelidade SET pontos = pontos - $1, atualizado_em = CURRENT_TIMESTAMP WHERE usuario_id = $2 RETURNING pontos',
        [pontos, usuarioId]
      );

      await client.query(
        'INSERT INTO fidelidade_extrato (usuario_id, pontos, tipo, descricao) VALUES ($1, $2, $3, $4)',
        [usuarioId, pontos, 'RESGATE', descricao]
      );

      await client.query('COMMIT');
      return res.rows[0].pontos;
    } catch (error) {
      await client.query('ROLLBACK');
      throw error;
    } finally {
      client.release();
    }
  }
}
// src/modules/usuarios/usuarios.repository.ts
import { Pool } from 'pg';

export class UsuarioRepository {
  private db: Pool;

  constructor(dbPool: Pool) {
    this.db = dbPool;
  }

  async criar(dados: { nome: string; email: string; senhaHash: string; telefone: string; tipo: string }) {
    const query = `
      INSERT INTO usuarios (nome, email, senha, telefone, tipo, criado_em)
      VALUES ($1, $2, $3, $4, $5, NOW())
      RETURNING id, nome, email, telefone, tipo, criado_em;
    `;
    const values = [dados.nome, dados.email, dados.senhaHash, dados.telefone, dados.tipo];
    const resultado = await this.db.query(query, values);
    return resultado.rows[0];
  }

  async buscarPorEmail(email: string) {
    const query = `SELECT * FROM usuarios WHERE email = $1;`;
    const resultado = await this.db.query(query, [email]);
    return resultado.rows[0];
  }

  async buscarPorId(id: number) {
    const query = `SELECT id, nome, email, telefone, tipo, criado_em FROM usuarios WHERE id = $1;`;
    const resultado = await this.db.query(query, [id]);
    return resultado.rows[0];
  }

  async atualizar(id: number, dados: { nome?: string; telefone?: string }) {
    const query = `
      UPDATE usuarios 
      SET nome = COALESCE($1, nome), telefone = COALESCE($2, telefone)
      WHERE id = $3
      RETURNING id, nome, email, telefone, tipo;
    `;
    const values = [dados.nome, dados.telefone, id];
    const resultado = await this.db.query(query, values);
    return resultado.rows[0];
  }
}
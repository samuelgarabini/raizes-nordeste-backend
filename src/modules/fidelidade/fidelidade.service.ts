import { FidelidadeRepository } from './fidelidade.repository';

export class FidelidadeService {
  private fidelidadeRepository: FidelidadeRepository;

  constructor(fidelidadeRepository: FidelidadeRepository) {
    this.fidelidadeRepository = fidelidadeRepository;
  }

  async consultarSaldo(usuarioId: number) {
    if (!usuarioId) throw new Error('ID do usuário é obrigatório');
    const saldo = await this.fidelidadeRepository.obterSaldo(usuarioId);
    return { usuario_id: usuarioId, saldo_pontos: saldo };
  }

  async consultarExtrato(usuarioId: number) {
    if (!usuarioId) throw new Error('ID do usuário é obrigatório');
    return await this.fidelidadeRepository.obterExtrato(usuarioId);
  }

  async acumularPontos(usuarioId: number, pontos: number, descricao: string = 'Pontos por compra') {
    if (!usuarioId || !pontos || pontos <= 0) {
      throw new Error('Usuário e quantidade válida de pontos são obrigatórios');
    }
    const novoSaldo = await this.fidelidadeRepository.adicionarPontos(usuarioId, pontos, descricao);
    return { usuario_id: usuarioId, saldo_atualizado: novoSaldo };
  }

  async resgatarPontos(usuarioId: number, pontos: number, descricao: string = 'Resgate de recompensa') {
    if (!usuarioId || !pontos || pontos <= 0) {
      throw new Error('Usuário e quantidade válida de pontos são obrigatórios');
    }

    const saldoAtual = await this.fidelidadeRepository.obterSaldo(usuarioId);
    if (saldoAtual < pontos) {
      throw new Error('Saldo de pontos insuficiente para resgate');
    }

    const novoSaldo = await this.fidelidadeRepository.resgatarPontos(usuarioId, pontos, descricao);
    return { usuario_id: usuarioId, saldo_atualizado: novoSaldo };
  }
}
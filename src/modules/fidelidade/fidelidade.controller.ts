import { Request, Response } from 'express';
import { FidelidadeService } from './fidelidade.service';

export class FidelidadeController {
  private fidelidadeService: FidelidadeService;

  constructor(fidelidadeService: FidelidadeService) {
    this.fidelidadeService = fidelidadeService;
  }

  async consultarSaldo(req: Request, res: Response) {
    try {
      const { usuarioId } = req.params;
      const saldo = await this.fidelidadeService.consultarSaldo(Number(usuarioId));
      return res.status(200).json(saldo);
    } catch (error: any) {
      return res.status(400).json({ error: error.message });
    }
  }

  async consultarExtrato(req: Request, res: Response) {
    try {
      const { usuarioId } = req.params;
      const extrato = await this.fidelidadeService.consultarExtrato(Number(usuarioId));
      return res.status(200).json(extrato);
    } catch (error: any) {
      return res.status(400).json({ error: error.message });
    }
  }

  async acumular(req: Request, res: Response) {
    try {
      const { usuario_id, pontos, descricao } = req.body;
      const resultado = await this.fidelidadeService.acumularPontos(
        Number(usuario_id),
        Number(pontos),
        descricao
      );
      return res.status(200).json(resultado);
    } catch (error: any) {
      return res.status(400).json({ error: error.message });
    }
  }

  async resgatar(req: Request, res: Response) {
    try {
      const { usuario_id, pontos, descricao } = req.body;
      const resultado = await this.fidelidadeService.resgatarPontos(
        Number(usuario_id),
        Number(pontos),
        descricao
      );
      return res.status(200).json(resultado);
    } catch (error: any) {
      return res.status(400).json({ error: error.message });
    }
  }
}
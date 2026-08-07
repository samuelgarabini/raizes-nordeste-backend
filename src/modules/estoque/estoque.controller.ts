import { Request, Response } from 'express';
import { EstoqueService } from './estoque.service';

export class EstoqueController {
  private estoqueService: EstoqueService;

  constructor(estoqueService: EstoqueService) {
    this.estoqueService = estoqueService;
  }

  async consultarSaldo(req: Request, res: Response) {
    try {
      const { produtoId, unidadeId } = req.params;
      const saldo = await this.estoqueService.consultarSaldo(Number(produtoId), Number(unidadeId));
      return res.status(200).json(saldo);
    } catch (error: any) {
      return res.status(400).json({ error: error.message });
    }
  }

  async listarPorUnidade(req: Request, res: Response) {
    try {
      const { unidadeId } = req.params;
      const estoque = await this.estoqueService.listarPorUnidade(Number(unidadeId));
      return res.status(200).json(estoque);
    } catch (error: any) {
      return res.status(400).json({ error: error.message });
    }
  }

  async movimentar(req: Request, res: Response) {
    try {
      const { produto_id, unidade_id, quantidade, tipo } = req.body;
      const resultado = await this.estoqueService.movimentarEstoque(
        Number(produto_id),
        Number(unidade_id),
        Number(quantidade),
        tipo
      );
      return res.status(200).json(resultado);
    } catch (error: any) {
      return res.status(400).json({ error: error.message });
    }
  }
}
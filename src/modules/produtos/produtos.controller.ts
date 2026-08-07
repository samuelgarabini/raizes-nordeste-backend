import { Request, Response } from 'express';
import { ProdutoService } from './produtos.service';

export class ProdutoController {
  private produtoService: ProdutoService;

  constructor(produtoService: ProdutoService) {
    this.produtoService = produtoService;
  }

  async listarTodos(req: Request, res: Response) {
    try {
      const produtos = await this.produtoService.listarTodos();
      return res.status(200).json(produtos);
    } catch (error: any) {
      return res.status(500).json({ error: error.message });
    }
  }

  async buscarPorId(req: Request, res: Response) {
    try {
      const { id } = req.params;
      const produto = await this.produtoService.buscarPorId(Number(id));
      return res.status(200).json(produto);
    } catch (error: any) {
      return res.status(404).json({ error: error.message });
    }
  }

  async criar(req: Request, res: Response) {
    try {
      const produto = await this.produtoService.criar(req.body);
      return res.status(201).json(produto);
    } catch (error: any) {
      return res.status(400).json({ error: error.message });
    }
  }

  async atualizar(req: Request, res: Response) {
    try {
      const { id } = req.params;
      const produto = await this.produtoService.atualizar(Number(id), req.body);
      return res.status(200).json(produto);
    } catch (error: any) {
      return res.status(404).json({ error: error.message });
    }
  }

  async deletar(req: Request, res: Response) {
    try {
      const { id } = req.params;
      await this.produtoService.deletar(Number(id));
      return res.status(204).send();
    } catch (error: any) {
      return res.status(404).json({ error: error.message });
    }
  }
}
import { Request, Response } from 'express';
import { PedidoService } from './pedidos.service';

export class PedidoController {
  private pedidoService: PedidoService;

  constructor(pedidoService: PedidoService) {
    this.pedidoService = pedidoService;
  }

  async criar(req: Request, res: Response) {
    try {
      const pedido = await this.pedidoService.criarPedido(req.body);
      return res.status(201).json(pedido);
    } catch (error: any) {
      const statusCode = error.statusCode || 400;
      return res.status(statusCode).json({ error: error.message });
    }
  }

  async buscarPorId(req: Request, res: Response) {
    try {
      const { id } = req.params;
      const pedido = await this.pedidoService.buscarPorId(String(id));
      return res.status(200).json(pedido);
    } catch (error: any) {
      const statusCode = error.statusCode || 400;
      return res.status(statusCode).json({ error: error.message });
    }
  }
}
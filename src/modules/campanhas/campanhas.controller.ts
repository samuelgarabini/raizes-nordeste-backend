import { Request, Response } from 'express';
import { CampanhaService } from './campanhas.service';

export class CampanhaController {
  private campanhaService: CampanhaService;

  constructor(campanhaService: CampanhaService) {
    this.campanhaService = campanhaService;
  }

  async criar(req: Request, res: Response) {
    try {
      const campanha = await this.campanhaService.criar(req.body);
      return res.status(201).json(campanha);
    } catch (error: any) {
      return res.status(400).json({ error: error.message });
    }
  }

  async listarAtivas(req: Request, res: Response) {
    try {
      const campanhas = await this.campanhaService.listarAtivas();
      return res.status(200).json(campanhas);
    } catch (error: any) {
      return res.status(500).json({ error: error.message });
    }
  }
}
// src/modules/usuarios/usuarios.controller.ts
import { Request, Response } from 'express';
import { UsuarioService } from './usuarios.service';

export class UsuarioController {
  private usuarioService: UsuarioService;

  constructor(usuarioService: UsuarioService) {
    this.usuarioService = usuarioService;
  }

  async criar(req: Request, res: Response): Promise<Response> {
    try {
      const novoUsuario = await this.usuarioService.criarUsuario(req.body);
      return res.status(201).json(novoUsuario);
    } catch (error: any) {
      return res.status(400).json({ erro: error.message });
    }
  }

  async obterPerfil(req: Request, res: Response): Promise<Response> {
    try {
      const usuarioId = (req as any).user.id;
      const perfil = await this.usuarioService.buscarPorId(usuarioId);
      return res.status(200).json(perfil);
    } catch (error: any) {
      return res.status(404).json({ erro: error.message });
    }
  }

  async atualizarPerfil(req: Request, res: Response): Promise<Response> {
    try {
      const usuarioId = (req as any).user.id;
      const atualizado = await this.usuarioService.atualizar(usuarioId, req.body);
      return res.status(200).json(atualizado);
    } catch (error: any) {
      return res.status(400).json({ erro: error.message });
    }
  }
}
// src/modules/usuarios/usuarios.service.ts
import { UsuarioRepository } from './usuarios.repository';
import bcrypt from 'bcrypt';

export class UsuarioService {
  private usuarioRepository: UsuarioRepository;

  constructor(usuarioRepository: UsuarioRepository) {
    this.usuarioRepository = usuarioRepository;
  }

  async criarUsuario(dados: any) {
    const usuarioExistente = await this.usuarioRepository.buscarPorEmail(dados.email);
    if (usuarioExistente) {
      throw new Error('E-mail já cadastrado no sistema.');
    }

    const senhaHash = await bcrypt.hash(dados.senha, 10);

    const novoUsuario = await this.usuarioRepository.criar({
      nome: dados.nome,
      email: dados.email,
      senhaHash,
      telefone: dados.telefone,
      tipo: dados.tipo || 'CLIENTE'
    });

    return novoUsuario;
  }

  async buscarPorId(id: number) {
    const usuario = await this.usuarioRepository.buscarPorId(id);
    if (!usuario) {
      throw new Error('Usuário não encontrado.');
    }
    return usuario;
  }

  async atualizar(id: number, dados: { nome?: string; telefone?: string }) {
    const usuarioAtualizado = await this.usuarioRepository.atualizar(id, dados);
    if (!usuarioAtualizado) {
      throw new Error('Usuário não encontrado para atualização.');
    }
    return usuarioAtualizado;
  }
}
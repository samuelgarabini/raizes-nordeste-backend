// src/modules/usuarios/usuarios.routes.ts
import { Router } from 'express';
import { UsuarioController } from './usuarios.controller';
import { UsuarioService } from './usuarios.service';
import { UsuarioRepository } from './usuarios.repository';
import { pool } from '../../config/database'; // Ajuste conforme a sua conexão de banco
import { authMiddleware } from '../../middlewares/auth.middleware'; // Ajuste conforme seu middleware JWT

const router = Router();

const usuarioRepository = new UsuarioRepository(pool);
const usuarioService = new UsuarioService(usuarioRepository);
const usuarioController = new UsuarioController(usuarioService);

router.post('/usuarios', (req, res) => usuarioController.criar(req, res));
router.get('/usuarios/perfil', authMiddleware, (req, res) => usuarioController.obterPerfil(req, res));
router.put('/usuarios/perfil', authMiddleware, (req, res) => usuarioController.atualizarPerfil(req, res));

export default router;
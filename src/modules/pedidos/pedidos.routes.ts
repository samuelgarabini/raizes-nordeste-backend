// src/modules/pedidos/pedidos.routes.ts
import { Router } from 'express';
import { PedidoController } from './pedidos.controller';
import { PedidoService } from './pedidos.service';
import { PedidoRepository } from './pedidos.repository';
import { CampanhaRepository } from '../campanhas/campanhas.repository';
import { pool } from '../../config/database';

const router = Router();

const pedidoRepository = new PedidoRepository(pool);
const campanhaRepository = new CampanhaRepository(pool);
const pedidoService = new PedidoService(pedidoRepository, campanhaRepository);
const pedidoController = new PedidoController(pedidoService);

router.post('/pedidos', (req, res) => pedidoController.criar(req, res));
router.get('/pedidos/:id', (req, res) => pedidoController.buscarPorId(req, res));

export default router;
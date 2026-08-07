import { Router } from 'express';
import { EstoqueController } from './estoque.controller';
import { EstoqueService } from './estoque.service';
import { EstoqueRepository } from './estoque.repository';
import { pool } from '../../config/database';

const router = Router();

const estoqueRepository = new EstoqueRepository(pool);
const estoqueService = new EstoqueService(estoqueRepository);
const estoqueController = new EstoqueController(estoqueService);

router.get('/estoque/unidade/:unidadeId', (req, res) => estoqueController.listarPorUnidade(req, res));
router.get('/estoque/produto/:produtoId/unidade/:unidadeId', (req, res) => estoqueController.consultarSaldo(req, res));
router.post('/estoque/movimentacao', (req, res) => estoqueController.movimentar(req, res));

export default router;
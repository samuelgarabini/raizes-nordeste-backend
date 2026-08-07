import { Router } from 'express';
import { ProdutoController } from './produtos.controller';
import { ProdutoService } from './produtos.service';
import { ProdutoRepository } from './produtos.repository';
import { pool } from '../../config/database'; 
// Se precisar proteger a criação de produtos, descomente a linha abaixo e adicione nas rotas de POST/PUT/DELETE
// import { authMiddleware } from '../../middlewares/auth.middleware';

const router = Router();

const produtoRepository = new ProdutoRepository(pool);
const produtoService = new ProdutoService(produtoRepository);
const produtoController = new ProdutoController(produtoService);

router.get('/produtos', (req, res) => produtoController.listarTodos(req, res));
router.get('/produtos/:id', (req, res) => produtoController.buscarPorId(req, res));
router.post('/produtos', (req, res) => produtoController.criar(req, res));
router.put('/produtos/:id', (req, res) => produtoController.atualizar(req, res));
router.delete('/produtos/:id', (req, res) => produtoController.deletar(req, res));

export default router;
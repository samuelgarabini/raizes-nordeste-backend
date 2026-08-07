import { Router } from 'express';
import { CampanhaController } from './campanhas.controller';
import { CampanhaService } from './campanhas.service';
import { CampanhaRepository } from './campanhas.repository';
import { pool } from '../../config/database';

const router = Router();

const campanhaRepository = new CampanhaRepository(pool);
const campanhaService = new CampanhaService(campanhaRepository);
const campanhaController = new CampanhaController(campanhaService);

router.post('/campanhas', (req, res) => campanhaController.criar(req, res));
router.get('/campanhas/ativas', (req, res) => campanhaController.listarAtivas(req, res));

export default router;
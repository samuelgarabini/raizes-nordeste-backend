import { Router } from 'express';
import { FidelidadeController } from './fidelidade.controller';
import { FidelidadeService } from './fidelidade.service';
import { FidelidadeRepository } from './fidelidade.repository';
import { pool } from '../../config/database';

const router = Router();

const fidelidadeRepository = new FidelidadeRepository(pool);
const fidelidadeService = new FidelidadeService(fidelidadeRepository);
const fidelidadeController = new FidelidadeController(fidelidadeService);

router.get('/fidelidade/saldo/:usuarioId', (req, res) => fidelidadeController.consultarSaldo(req, res));
router.get('/fidelidade/extrato/:usuarioId', (req, res) => fidelidadeController.consultarExtrato(req, res));
router.post('/fidelidade/acumular', (req, res) => fidelidadeController.acumular(req, res));
router.post('/fidelidade/resgatar', (req, res) => fidelidadeController.resgatar(req, res));

export default router;
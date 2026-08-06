// src/index.ts
import express from 'express';
import cors from 'cors';
import dotenv from 'dotenv';
import usuarioRoutes from './modules/usuarios/usuarios.routes';

dotenv.config();

const app = express();
const PORT = process.env.PORT || 3333;

app.use(cors());
app.use(express.json());

// Rota de boas-vindas
app.get('/', (req, res) => {
  res.json({ mensagem: 'API Raízes Nordeste rodando com sucesso!' });
});

// Registrar rotas dos módulos
app.use(usuarioRoutes);

app.listen(PORT, () => {
  console.log(`Servidor rodando na porta ${PORT}`);
});
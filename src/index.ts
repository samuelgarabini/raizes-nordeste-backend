// src/index.ts
import express from 'express';
import cors from 'cors';
import dotenv from 'dotenv';
import usuarioRoutes from './modules/usuarios/usuarios.routes';
import produtoRoutes from './modules/produtos/produtos.routes';
import estoqueRoutes from './modules/estoque/estoque.routes';
import fidelidadeRoutes from './modules/fidelidade/fidelidade.routes';
import campanhaRoutes from './modules/campanhas/campanhas.routes'; // <-- Adicionado
import pedidoRoutes from './modules/pedidos/pedidos.routes';     // <-- Adicionado

dotenv.config();

const app = express();
const PORT = process.env.PORT || 3333;

app.use(cors());
app.use(express.json());

app.get('/', (req, res) => {
  res.json({ mensagem: 'API Raízes Nordeste rodando com sucesso!' });
});

app.use(usuarioRoutes);
app.use(produtoRoutes);
app.use(estoqueRoutes);
app.use(fidelidadeRoutes);
app.use(campanhaRoutes); // <-- Adicionado
app.use(pedidoRoutes);   // <-- Adicionado

app.listen(PORT, () => {
  console.log(`Servidor rodando na porta ${PORT}`);
});
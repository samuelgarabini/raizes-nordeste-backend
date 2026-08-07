import { CampanhaRepository, Campanha } from './campanhas.repository';

export class CampanhaService {
  private campanhaRepository: CampanhaRepository;

  constructor(campanhaRepository: CampanhaRepository) {
    this.campanhaRepository = campanhaRepository;
  }

  async criar(dados: Campanha) {
    if (!dados.nome || !dados.desconto_percentual) {
      throw new Error('Nome e desconto percentual são obrigatórios');
    }
    return await this.campanhaRepository.criar(dados);
  }

  async listarAtivas() {
    return await this.campanhaRepository.listarAtivas();
  }
}
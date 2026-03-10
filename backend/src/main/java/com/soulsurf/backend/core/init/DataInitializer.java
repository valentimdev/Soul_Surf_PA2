package com.soulsurf.backend.core.init;

import com.soulsurf.backend.modules.beach.entity.Beach;
import com.soulsurf.backend.modules.beach.repository.BeachRepository;
import com.soulsurf.backend.modules.poi.entity.PointOfInterest;
import com.soulsurf.backend.modules.poi.entity.PoiCategory;
import com.soulsurf.backend.modules.poi.repository.PointOfInterestRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@org.springframework.context.annotation.Profile("!test")
public class DataInitializer implements CommandLineRunner {

        private final BeachRepository beachRepository;
        private final PointOfInterestRepository poiRepository;

        public DataInitializer(BeachRepository beachRepository, PointOfInterestRepository poiRepository) {
                this.beachRepository = beachRepository;
                this.poiRepository = poiRepository;
        }

        @Override
        public void run(String... args) throws Exception {
                if (beachRepository.count() == 0) {
                        // Seed Beaches in Ceara
                        Beach iracema = createBeach("Praia de Iracema",
                                        "Berço da boemia e do surf clássico em Fortaleza.",
                                        "Fortaleza, CE", "Iniciante", -3.72, -38.52);
                        Beach beiraMar = createBeach("Beira Mar",
                                        "Ponto turístico central com águas tranquilas e calçadão integrado.",
                                        "Fortaleza, CE", "Iniciante",
                                        -3.725, -38.50);
                        Beach lesteOeste = createBeach("Leste Oeste",
                                        "Pico tradicional de surf urbano com boas esquerdas.",
                                        "Fortaleza, CE", "Intermediário", -3.71, -38.54);
                        Beach praiaDoFuturo = createBeach("Praia do Futuro",
                                        "Melhor infraestrutura de barracas e ondas constantes.", "Fortaleza, CE",
                                        "Avançado", -3.74,
                                        -38.45);

                        // Seed POIs
                        createPoi("Escola de Surf Iracema", "Aulas para iniciantes e aluguel de pranchas.",
                                        PoiCategory.SURF_SCHOOL,
                                        -3.721, -38.521, "(85) 9999-0001", iracema);
                        createPoi("Surf Shop Leste", "Venda de pranchas e acessórios.", PoiCategory.SURF_SHOP, -3.711,
                                        -38.541,
                                        "(85) 9999-0002", lesteOeste);
                        createPoi("Reparos do Leste", "Concerto rápido de fibras e quilhas.", PoiCategory.BOARD_REPAIR,
                                        -3.712,
                                        -38.542, "(85) 9999-0003", lesteOeste);
                        createPoi("Natação P. Futuro", "Treinos de águas abertas.", PoiCategory.SWIMMING_SCHOOL, -3.741,
                                        -38.451,
                                        "(85) 9999-0004", praiaDoFuturo);
                        createPoi("Ponte dos Ingleses", "Cartão postal e mirante histórico.", PoiCategory.TOURIST_SPOT,
                                        -3.720,
                                        -38.517, null, iracema);
                }
        }

        private Beach createBeach(String nome, String descricao, String localizacao, String nivel, Double lat,
                        Double lon) {
                Beach beach = new Beach();
                beach.setNome(nome);
                beach.setDescricao(descricao);
                beach.setLocalizacao(localizacao);
                beach.setNivelExperiencia(nivel);
                beach.setLatitude(lat);
                beach.setLongitude(lon);
                return beachRepository.save(beach);
        }

        private void createPoi(String nome, String descricao, PoiCategory cat, Double lat, Double lon, String tel,
                        Beach beach) {
                PointOfInterest poi = new PointOfInterest();
                poi.setNome(nome);
                poi.setDescricao(descricao);
                poi.setCategoria(cat);
                poi.setLatitude(lat);
                poi.setLongitude(lon);
                poi.setTelefone(tel);
                poi.setBeach(beach);
                poiRepository.save(poi);
        }
}

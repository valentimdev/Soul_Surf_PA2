package com.soulsurf.backend.modules.weather.service;

import com.soulsurf.backend.modules.weather.dto.SurfConditionsDTO;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SurfConditionsServiceTest {

    private static final String TIDE_HTML = """
            <table id="tab_mare">
              <tr>
                <td><font class=mare_data>13/05/2026 <br> </font></td>
                <td><font class=sol><b>Nascente</b><BR>  5:31  </font><br><font class=sol><b>Poente</b><BR>  17:28  </font></td>
                <td><font class=mare_nome>ALTA</font><br><font class=mare>  1h55 </font><br><font class=mare>   2.26m</font></td>
                <td><font class=mare_nome>BAIXA</font><br><font class=mare>  8h05 </font><br><font class=mare>   0.28m</font></td>
                <td><font class=mare_nome>ALTA</font><br><font class=mare> 14h21 </font><br><font class=mare>   2.28m</font></td>
                <td><font class=mare_nome>BAIXA</font><br><font class=mare> 20h26 </font><br><font class=mare>   0.27m</font></td>
              </tr>
              <tr>
                <td><font class=mare_data>14/05/2026 <br> </font></td>
                <td><font class=sol><b>Nascente</b><BR>  5:31  </font><br><font class=sol><b>Poente</b><BR>  17:28  </font></td>
                <td><font class=mare_nome>ALTA</font><br><font class=mare>  2h41 </font><br><font class=mare>   2.42m</font></td>
                <td><font class=mare_nome>BAIXA</font><br><font class=mare>  8h53 </font><br><font class=mare>   0.11m</font></td>
                <td><font class=mare_nome>ALTA</font><br><font class=mare> 15h09 </font><br><font class=mare>   2.40m</font></td>
                <td><font class=mare_nome>BAIXA</font><br><font class=mare> 21h11 </font><br><font class=mare>   0.18m</font></td>
              </tr>
            </table>
            """;

    private final SurfConditionsService service = new SurfConditionsService(
            "http://localhost/marine",
            "http://localhost/forecast",
            "http://localhost/semace",
            "http://localhost/mare",
            "America/Fortaleza",
            WebClient.builder()
    );

    @Test
    void identifiesIncomingMidTideAsGoodSurfWindow() {
        ZonedDateTime now = ZonedDateTime.of(2026, 5, 13, 11, 0, 0, 0, ZoneId.of("America/Fortaleza"));

        SurfConditionsDTO.TideDTO tide = service.buildTideDtoFromHtml(TIDE_HTML, now);

        assertEquals("ENCHENDO", tide.getCurrentStatus());
        assertEquals("Boa para surfar agora", tide.getRecommendationLabel());
        assertTrue(tide.getFillPercent() >= 35 && tide.getFillPercent() <= 75);
        assertNotNull(tide.getNextTurnLabel());
        assertNotNull(tide.getUpdatedAt());
        assertNotNull(tide.getExpiresAt());
        assertFalse(tide.getBestSurfWindows().isEmpty());
        assertTrue(tide.getBestSurfWindows().get(0).getActiveNow());
    }

    @Test
    void identifiesOutgoingTideAsDryingSea() {
        ZonedDateTime now = ZonedDateTime.of(2026, 5, 13, 18, 0, 0, 0, ZoneId.of("America/Fortaleza"));

        SurfConditionsDTO.TideDTO tide = service.buildTideDtoFromHtml(TIDE_HTML, now);

        assertEquals("SECANDO", tide.getCurrentStatus());
        assertEquals("Mar secando", tide.getCurrentLabel());
    }
}

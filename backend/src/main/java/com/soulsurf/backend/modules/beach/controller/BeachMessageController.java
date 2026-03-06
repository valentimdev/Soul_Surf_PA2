package com.soulsurf.backend.modules.beach.controller;

import com.soulsurf.backend.modules.beach.dto.BeachMessageDTO;
import com.soulsurf.backend.modules.beach.service.BeachMessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/beaches/{beachesId}/mensagens")
public class BeachMessageController {

    private final BeachMessageService BeachMessageService;

    public BeachMessageController(BeachMessageService BeachMessageService) {
        this.BeachMessageService = BeachMessageService;
    }

    @Operation(summary = "Lista mensagens do mural da praia", description = "Retorna todas as mensagens de um mural específico, ordenadas da mais recente para a mais antiga.")
    @ApiResponse(responseCode = "200", description = "Mensagens listadas com sucesso")
    @ApiResponse(responseCode = "404", description = "Praia não encontrada")
    @GetMapping
    public ResponseEntity<List<BeachMessageDTO>> listarMensagens(
            @Parameter(description = "ID da praia cujo mural será listado") @PathVariable Long beachesId) {
        List<BeachMessageDTO> mensagens = BeachMessageService.listarMensagensPorPraia(beachesId);
        return ResponseEntity.ok(mensagens);
    }

    @Operation(summary = "Posta uma nova BeachMessage no mural da praia", description = "Cria e salva uma nova BeachMessage, associada ao usuário logado e à praia. Requer autenticação.", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "201", description = "BeachMessage criada com sucesso")
    @ApiResponse(responseCode = "401", description = "Não autenticado")
    @ApiResponse(responseCode = "404", description = "Praia não encontrada")
    @PostMapping
    public ResponseEntity<BeachMessageDTO> postarBeachMessage(
            @Parameter(description = "ID da praia onde a BeachMessage será postada") @PathVariable Long beachesId,
            @Parameter(description = "Corpo da requisição contendo o texto da BeachMessage") @RequestParam("texto") String texto,
            @AuthenticationPrincipal UserDetails userDetails) {
        BeachMessageDTO BeachMessage = BeachMessageService.criarBeachMessage(beachesId, texto, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(BeachMessage);
    }
}


package com.soulsurf.backend.controllers;

import com.soulsurf.backend.dto.MensagemDTO;
import com.soulsurf.backend.dto.MessageResponse;
import com.soulsurf.backend.services.MensagemService;
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

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/beaches/{beachesId}/mensagens")
public class MensagemController {

    private final MensagemService mensagemService;

    public MensagemController(MensagemService mensagemService) {
        this.mensagemService = mensagemService;
    }

    @Operation(summary = "Lista mensagens do mural da praia", description = "Retorna todas as mensagens de um mural específico, ordenadas da mais recente para a mais antiga.")
    @ApiResponse(responseCode = "200", description = "Mensagens listadas com sucesso")
    @ApiResponse(responseCode = "404", description = "Praia não encontrada")
    @GetMapping
    public ResponseEntity<?> listarMensagens(
            @Parameter(description = "ID da praia cujo mural será listado") @PathVariable Long beachId) {
        try {
            List<MensagemDTO> mensagens = mensagemService.listarMensagensPorPraia(beachId);
            return ResponseEntity.ok(mensagens);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new MessageResponse(e.getMessage()));
        }
    }

    @Operation(summary = "Posta uma nova mensagem no mural da praia", description = "Cria e salva uma nova mensagem, associada ao usuário logado e à praia. Requer autenticação.", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "201", description = "Mensagem criada com sucesso")
    @ApiResponse(responseCode = "401", description = "Não autenticado")
    @ApiResponse(responseCode = "404", description = "Praia não encontrada")
    @PostMapping
    public ResponseEntity<?> postarMensagem(
            @Parameter(description = "ID da praia onde a mensagem será postada") @PathVariable Long beachId,
            @Parameter(description = "Corpo da requisição contendo o texto da mensagem") @RequestParam("texto") String texto,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {

            MensagemDTO mensagem = mensagemService.criarMensagem(beachId, texto, userDetails.getUsername());


            return ResponseEntity.status(HttpStatus.CREATED).body(mensagem);
        } catch (RuntimeException e) {

            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new MessageResponse(e.getMessage()));
        }
    }
}
// src/main/java/com/soulsurf/backend/services/EmailService.java

package com.soulsurf.backend.services;

import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class EmailService {

    private static final String RESEND_API_URL = "https://api.resend.com/emails";
    private final OkHttpClient client = new OkHttpClient();

    @Value("${resend.api.key}")
    private String apiKey;

    public void sendPasswordResetEmail(String toEmail, String token) {
        String frontendUrl = System.getenv("FRONTEND_URL");
        if (frontendUrl == null || frontendUrl.isEmpty()) {
            frontendUrl = "http://localhost:5173"; // ou 3000, dependendo do seu vite/react
        }

        String resetLink = frontendUrl + "/reset-password?token=" + token;

        // REMETENTE OBRIGATÓRIO: use um domínio verificado no Resend!
        // Opção 1 (recomendado): seu domínio verificado → ex: no-reply@soulsurf.app
        // Opção 2 (rápido pra testar): use o domínio grátis do Resend → @resend.dev

        String jsonBody = """
            {
              "from": "Soul Surf <no-reply@valentimdev.resend.dev>",
              "to": "%s",
              "subject": "Redefinição de Senha - Soul Surf",
              "html": "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: auto; padding: 30px; background:#f8f9fa; border-radius:12px; text-align:center;'>
                         <h2 style='color:#007bff;'>Ola, surfista!</h2>
                         <p>Recebemos uma solicitação para redefinir sua senha no <strong>Soul Surf</strong>.</p>
                         <p>Clique no botão abaixo para criar uma nova senha:</p>
                         <br>
                         <a href='%s' style='background:#007bff; color:white; padding:14px 32px; text-decoration:none; border-radius:8px; font-size:16px; font-weight:bold;'>Redefinir Senha</a>
                         <br><br>
                         <small style='color:#666;'>Este link expira em 24 horas.</small>
                         <p style='color:#666; margin-top:30px;'>Se você não solicitou isso, ignore este e-mail.</p>
                         <p><strong>Boa onda!</strong><br>Equipe Soul Surf</p>
                       </div>"
            }
            """.formatted(toEmail, resetLink);

        RequestBody body = RequestBody.create(jsonBody, MediaType.get("application/json; charset=utf-8"));

        Request request = new Request.Builder()
                .url(RESEND_API_URL)
                .post(body)
                .addHeader("Authorization", "Bearer " + apiKey)
                .addHeader("Content-Type", "application/json")
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String error = response.body() != null ? response.body().string() : "sem detalhes";
                throw new RuntimeException("Erro Resend: " + response.code() + " → " + error);
            }
            // Sucesso!
        } catch (IOException e) {
            throw new RuntimeException("Erro de conexão com Resend: " + e.getMessage());
        }
    }
}
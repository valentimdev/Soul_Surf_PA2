package com.soulsurf.backend.modules.user.service;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class EmailService {

    private static final String RESEND_API_URL = "https://api.resend.com/emails";
    private final OkHttpClient client = new OkHttpClient();

    @Value("${resend.api.key}")
    private String apiKey;

    @Value("${resend.from.email:no-reply@valentimdev.resend.dev}")
    private String fromEmail;

    @Value("${frontend.url:http://localhost:5173}")
    private String frontendUrl;

    public void sendPasswordResetEmail(String toEmail, String token, String code) {
        String resetLink = frontendUrl + "/reset-password?token=" + token;
        String htmlContent = """
                <div style='font-family:Arial,sans-serif;max-width:600px;margin:auto;padding:30px;background:#f8f9fa;border-radius:12px;text-align:center;'>
                  <h2 style='color:#007bff;'>Ola, surfista!</h2>
                  <p>Recebemos uma solicitacao para redefinir sua senha no <strong>Soul Surf</strong>.</p>
                  <p>Seu codigo de verificacao e:</p>
                  <div style='font-size:30px;font-weight:700;letter-spacing:4px;margin:14px 0;color:#0f172a;'>%s</div>
                  <p>Esse codigo expira em 15 minutos.</p>
                  <p style='margin-top:18px;'>Se estiver no navegador, tambem pode usar o link abaixo:</p>
                  <a href='%s' style='background:#007bff;color:white;padding:12px 28px;text-decoration:none;border-radius:8px;font-size:15px;font-weight:bold;'>Redefinir senha</a>
                  <p style='color:#666;margin-top:24px;'>Se voce nao solicitou isso, ignore este email.</p>
                </div>
                """.formatted(code, resetLink);

        String textContent = "Seu codigo Soul Surf: " + code
                + ". Ele expira em 15 minutos. Link alternativo: " + resetLink;

        String jsonBody = """
                {
                  "from": %s,
                  "to": [%s],
                  "subject": %s,
                  "html": %s,
                  "text": %s
                }
                """.formatted(
                escapeJson("Soul Surf <" + fromEmail + ">"),
                escapeJson(toEmail),
                escapeJson("Codigo para redefinir senha - Soul Surf"),
                escapeJson(htmlContent),
                escapeJson(textContent)
        );

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
                throw new RuntimeException("Erro Resend: " + response.code() + " -> " + error);
            }
        } catch (IOException e) {
            throw new RuntimeException("Erro de conexao com Resend: " + e.getMessage());
        }
    }

    private String escapeJson(String value) {
        if (value == null) {
            return "null";
        }

        String escaped = value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");

        return "\"" + escaped + "\"";
    }
}

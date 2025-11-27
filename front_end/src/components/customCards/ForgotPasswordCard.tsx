// src/components/customCards/ForgotPasswordCard.tsx

import { useState } from "react";
import { Link } from "react-router-dom";
import { AuthService } from "@/api/services/authService";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Loader2, CheckCircle } from "lucide-react";

export default function ForgotPasswordCard() {
  const [email, setEmail] = useState("");
  const [isSubmitted, setIsSubmitted] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    setLoading(true);

    try {
      await AuthService.forgotPassword({ email });
      setIsSubmitted(true);
    } catch (err: any) {
      setError(err?.response?.data?.message || "E-mail não encontrado ou erro no servidor.");
    } finally {
      setLoading(false);
    }
  };

  // TELA DE SUCESSO
  if (isSubmitted) {
    return (
      <Card className="w-full max-w-md border-0 shadow-2xl bg-white/95 backdrop-blur-md">
        <CardHeader className="text-center pb-8 pt-10">
          <div className="mx-auto w-20 h-20 bg-green-100 rounded-full flex items-center justify-center mb-6">
            <CheckCircle className="w-12 h-12 text-green-600" />
          </div>
          <CardTitle className="text-2xl font-bold">Link Enviado!</CardTitle>
          <CardDescription className="text-base leading-relaxed">
            Verifique seu e-mail: <span className="font-semibold text-primary">{email}</span>
            <br />
            O link foi enviado! Confira também a pasta de <strong>spam</strong>.
          </CardDescription>
        </CardHeader>
        <CardFooter className="flex justify-center pb-8">
          <Button asChild size="lg" className="w-full max-w-xs">
            <Link to="/login">Voltar para o Login</Link>
          </Button>
        </CardFooter>
      </Card>
    );
  }

  // FORMULÁRIO
  return (
    <Card className="w-full max-w-md border-0 shadow-2xl bg-white/95 backdrop-blur-md">
      <CardHeader className="space-y-3 text-center pb-8 pt-10">
        <CardTitle className="text-3xl font-bold">Esqueceu a senha?</CardTitle>
        <CardDescription className="text-base">
          Sem problemas! Digite seu e-mail que enviaremos o link para redefinir sua senha.
        </CardDescription>
      </CardHeader>

      <CardContent className="space-y-6">
        <form onSubmit={handleSubmit} className="space-y-5">
          <div className="space-y-2">
            <Label htmlFor="email" className="text-base font-medium">E-mail</Label>
            <Input
              id="email"
              type="email"
              placeholder="seu@email.com"
              required
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              disabled={loading}
              className="h-12 text-base"
            />
          </div>

          {error && (
            <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-lg text-sm font-medium text-center">
              {error}
            </div>
          )}

          <Button
            type="submit"
            size="lg"
            className="w-full h-12 text-base font-semibold"
            disabled={loading || !email.trim()}
          >
            {loading ? (
              <>
                <Loader2 className="mr-2 h-5 w-5 animate-spin" />
                Enviando link...
              </>
            ) : (
              "Enviar Link de Recuperação"
            )}
          </Button>
        </form>
      </CardContent>

      <CardFooter className="flex justify-center pt-4 pb-10">
        <Button variant="link" asChild className="text-base">
          <Link to="/login" className="flex items-center gap-1 hover:gap-2 transition-all">
            Voltar para o Login
          </Link>
        </Button>
      </CardFooter>
    </Card>
  );
}
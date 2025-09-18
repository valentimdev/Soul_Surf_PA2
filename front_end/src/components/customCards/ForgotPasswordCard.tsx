import { useState } from "react";
import { Link } from "react-router-dom";
import { AuthService } from "@/api/services/authService";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";

function ForgotPasswordCard() {
  const [email, setEmail] = useState("");
  const [isSubmitted, setIsSubmitted] = useState(false);

  const handleSubmit = async (event: React.FormEvent) => {
    event.preventDefault();
    try {
      const response = await AuthService.forgotPassword({ email });
      console.log(response.message); // pode mostrar no UI
      setIsSubmitted(true);
    } catch (err: unknown) {
      console.error(err);
    }
  };

  // Se o formulário foi enviado, mostra a mensagem de confirmação
  if (isSubmitted) {
    return (
      <div className="h-full flex items-center justify-center">
        <Card className="w-full max-w-sm border">
          <CardHeader className="text-center">
            <CardTitle>Link Enviado!</CardTitle>
            <CardDescription>
              Verifique a sua caixa de entrada e a pasta de spam para encontrar o link de redefinição.
            </CardDescription>
          </CardHeader>
          <CardFooter>
            <Button asChild className="w-full">
              <Link to="/login">Voltar para o Login</Link>
            </Button>
          </CardFooter>
        </Card>
      </div>
    );
  }

  // Vista inicial do formulário
  return (
    <div className="h-full flex items-center justify-center">
      <Card className="w-full max-w-sm border">
        <CardHeader>
          <CardTitle>Esqueceu a senha?</CardTitle>
          <CardDescription>
            Insira o seu e-mail para receber um link e redefinir a sua senha.
          </CardDescription>
        </CardHeader>
        <CardContent>
          <form onSubmit={handleSubmit}>
            <div className="grid gap-2">
              <Label htmlFor="email">Email</Label>
              <Input
                id="email"
                type="email"
                placeholder="exemplo@exemplo.com"
                required
                value={email}
                onChange={(e) => setEmail(e.target.value)}
              />
            </div>
            <CardFooter className="flex-col gap-4 pt-6">
              <Button type="submit" className="w-full">
                Enviar Link
              </Button>
               <Button variant="link" asChild>
                  <Link to="/login">Voltar para o Login</Link>
              </Button>
            </CardFooter>
          </form>
        </CardContent>
      </Card>
    </div>
  );
}

export default ForgotPasswordCard;


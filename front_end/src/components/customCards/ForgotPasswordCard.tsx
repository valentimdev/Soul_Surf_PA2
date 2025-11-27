import { useState } from "react";
import { Link } from "react-router-dom";
import { AuthService } from "@/api/services/authService";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Loader2 } from "lucide-react";

function ForgotPasswordCard() {
    const [email, setEmail] = useState("");
    const [isSubmitted, setIsSubmitted] = useState(false);
    const [error, setError] = useState<string | null>(null);
    const [loading, setLoading] = useState(false);

    const handleSubmit = async (event: React.FormEvent) => {
        event.preventDefault();
        setError(null);
        setLoading(true);

        try {
            await AuthService.forgotPassword({ email });
            setIsSubmitted(true);
        } catch (err: any) {
            setError(
                err?.response?.data?.message ||
                "Não foi possível enviar o link. Tente novamente."
            );
        } finally {
            setLoading(false);
        }
    };

    if (isSubmitted) {
        return (
            <div className="h-full flex items-center justify-center">
                <Card className="w-full max-w-sm border">
                    <CardHeader className="text-center">
                        <CardTitle>Link Enviado!</CardTitle>
                        <CardDescription>
                            Verifique sua caixa de entrada (e a pasta de spam) para encontrar o
                            link de redefinição.
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

    return (
        <div className="h-full flex items-center justify-center">
            <Card className="w-full max-w-sm border">
                <CardHeader>
                    <CardTitle>Esqueceu a senha?</CardTitle>
                    <CardDescription>
                        Insira seu e-mail para receber um link e redefinir sua senha.
                    </CardDescription>
                </CardHeader>
                <form onSubmit={handleSubmit}>
                    <CardContent className="space-y-4">
                        <div className="grid gap-2">
                            <Label htmlFor="email">Email</Label>
                            <Input
                                id="email"
                                type="email"
                                placeholder="exemplo@exemplo.com"
                                required
                                value={email}
                                onChange={(e) => setEmail(e.target.value)}
                                disabled={loading}
                            />
                        </div>
                        {error && (
                            <p className="text-sm text-red-500 mt-2">{error}</p>
                        )}
                    </CardContent>
                    <CardFooter className="flex-col gap-4">
                        <Button type="submit" className="w-full" disabled={loading}>
                            {loading ? (
                                <>
                                    <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                                    Enviando...
                                </>
                            ) : (
                                "Enviar Link"
                            )}
                        </Button>
                        <Button variant="link" asChild>
                            <Link to="/login">Voltar para o Login</Link>
                        </Button>
                    </CardFooter>
                </form>
            </Card>
        </div>
    );
}

export default ForgotPasswordCard;
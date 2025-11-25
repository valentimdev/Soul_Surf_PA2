import React, { useState } from "react";
import { Button } from "@/components/ui/button";
import {
    Card,
    CardContent,
    CardDescription,
    CardFooter,
    CardHeader,
    CardTitle,
} from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import {AuthService} from "@/api/services/authService";
import {useNavigate} from "react-router-dom";
import { useAuth } from "@/contexts/AuthContext.tsx";
import { Loader2 } from "lucide-react";

function LoginCard() {
    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const [error, setError] = useState<string | null>(null);
    const [loading, setLoading] = useState(false);
    const navigate = useNavigate();
    const { login } =useAuth();

    const handleLogin = async (e: React.FormEvent) => {
        e.preventDefault();
        setError(null);
        setLoading(true);

        try {
            const response = await AuthService.login({ email, password });
            login(response.token);

            setTimeout(() => {
                navigate("/home", { replace: true });
            }, 3000);

        } catch (err: any) {
            if (!err.response) {
                setError("Não foi possível conectar ao servidor. Verifique sua conexão ou tente mais tarde.");
            } else {
                setError(err.response?.data?.message || "Email ou senha incorretos");
            }
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="h-full flex items-center justify-center">
            <Card className="w-full max-w-sm border">
                <CardHeader>
                    <CardTitle>Entre na sua conta</CardTitle>
                    <CardDescription>
                        Insira seu email e senha para acessar sua conta.
                    </CardDescription>
                </CardHeader>
                <CardContent>
                    <form onSubmit={handleLogin} className="flex flex-col gap-6">
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
                        <div className="grid gap-2">
                            <div className="flex items-center">
                                <Label htmlFor="password">Senha</Label>
                                <a
                                    href="/forgot-password"
                                    className="ml-auto inline-block text-sm underline-offset-4 hover:underline"
                                >
                                    Esqueceu a senha?
                                </a>
                            </div>
                            <Input
                                id="password"
                                type="password"
                                required
                                value={password}
                                onChange={(e) => setPassword(e.target.value)}
                                disabled={loading}
                            />
                        </div>
                        {error && <p className="text-red-500 text-sm">{error}</p>}
                        <Button type="submit" className="w-full" disabled={loading}>
                            {loading ? (
                                <>
                                    <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                                    Entrando...
                                </>
                            ) : (
                                "Login"
                            )}
                        </Button>
                    </form>
                </CardContent>
                <CardFooter className="flex-col gap-2">
                    <div className="flex items-center">
                        <a
                            href="/cadastro"
                            className="ml-auto inline-block underline-offset-4 hover:underline text-sm"
                        >
                            Não possui conta ainda? Registre-se Agora!
                        </a>
                    </div>
                </CardFooter>
            </Card>
        </div>
    );
}

export default LoginCard;
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
import { AuthService } from "@/api/services/authService";
import {useNavigate} from "react-router-dom";

function CadastroCard() {
    const [email, setEmail] = useState("");
    const [username, setUsername] = useState("");
    const [password, setPassword] = useState("");
    const [confirmPassword, setConfirmPassword] = useState("");
    const [error, setError] = useState<string | null>(null);
    const [success, setSuccess] = useState<string | null>(null);
    const navigate = useNavigate();

    const handleSignup = async (e: React.FormEvent) => {
        e.preventDefault();
        setError(null);
        setSuccess(null);

        if (password !== confirmPassword) {
            setError("As senhas não coincidem.");
            return;
        }

        try {
            const response = await AuthService.signup({ email, password, username });
            localStorage.setItem("token", response.token);
            setSuccess("Conta criada com sucesso! Faça login.");
            navigate("/home");
        } catch (err: any) {
            setError(err.response?.data?.message || "Erro ao cadastrar");
        }
    };

    return (
        <Card className="w-full max-w-sm">
            <CardHeader>
                <CardTitle>Cadastro</CardTitle>
                <CardDescription>
                    Preencha todos os campos abaixo para criar sua conta.
                </CardDescription>
            </CardHeader>
            <CardContent>
                <form onSubmit={handleSignup} className="flex flex-col gap-4">
                    <div className="grid gap-2">
                        <Label htmlFor="username">Username</Label>
                        <Input
                            id="username"
                            type="text"
                            placeholder="Digite seu username"
                            required
                            value={username}
                            onChange={(e) => setUsername(e.target.value)}
                        />
                    </div>
                    <div className="grid gap-2">
                        <Label htmlFor="email">Email</Label>
                        <Input
                            id="email"
                            type="email"
                            placeholder="m@example.com"
                            required
                            value={email}
                            onChange={(e) => setEmail(e.target.value)}
                        />
                    </div>
                    <div className="grid gap-2">
                        <Label htmlFor="password">Senha</Label>
                        <Input
                            id="password"
                            type="password"
                            required
                            value={password}
                            onChange={(e) => setPassword(e.target.value)}
                        />
                    </div>
                    <div className="grid gap-2">
                        <Label htmlFor="confirm-password">Confirmar Senha</Label>
                        <Input
                            id="confirm-password"
                            type="password"
                            required
                            value={confirmPassword}
                            onChange={(e) => setConfirmPassword(e.target.value)}
                        />
                    </div>
                    {error && <p className="text-red-500 text-sm">{error}</p>}
                    {success && <p className="text-green-500 text-sm">{success}</p>}
                </form>
            </CardContent>
            <CardFooter className="flex-col gap-2 pt-6">
                <Button onClick={handleSignup} type="submit" className="w-full">
                    Confirmar Cadastro
                </Button>
                <div className="flex items-center">
                    <a
                        href="/login"
                        className="ml-auto inline-block underline-offset-4 hover:underline"
                    >
                        Já possui conta? Entre agora!
                    </a>
                </div>
            </CardFooter>
        </Card>
    );
}

export default CadastroCard;
import React, { useState } from 'react';
import axios from 'axios';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';

// URL base do seu backend, ajustada para o seu endpoint de login.
const BACKEND_URL = 'http://localhost:8080/api/auth/login';

// Componente para o cartão de login
function LoginCard() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const handleLogin = async (e) => {
    e.preventDefault(); // Impede o recarregamento da página
    setLoading(true);
    setError(null);

    try {
      // Envia uma requisição POST com os dados do formulário
      const response = await axios.post(BACKEND_URL, {
        email: email,
        password: password
      });

      // Se o login for bem-sucedido, você pode armazenar o token
      console.log('Login bem-sucedido!', response.data);
      // Exemplo: Armazenar o token em localStorage (melhor usar React Context ou Zustand para apps maiores)
      localStorage.setItem('token', response.data.token);

      // Limpa os campos do formulário e redireciona o usuário
      setEmail('');
      setPassword('');

      // Ação de sucesso: pode ser um redirecionamento ou uma mensagem de sucesso
      alert('Login bem-sucedido!');
      // window.location.href = "/dashboard"; // Exemplo de redirecionamento
    } catch (err) {
      // Se houver um erro, exibe a mensagem de erro
      const errorMessage = err.response?.data?.message || 'Erro inesperado no login. Tente novamente.';
      setError(errorMessage);
      console.error('Erro no login:', err);
      alert(errorMessage);
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
          <form onSubmit={handleLogin}>
            <div className="flex flex-col gap-6">
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
              <div className="grid gap-2">
                <div className="flex items-center">
                  <Label htmlFor="password">Senha</Label>
                  <a
                    href="#"
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
                />
              </div>
            </div>
            {error && <p className="text-red-500 text-sm mt-4">{error}</p>}
            <CardFooter className="flex-col gap-2 mt-6 p-0">
              <Button type="submit" className="w-full" disabled={loading}>
                {loading ? 'Entrando...' : 'Login'}
              </Button>
              <Button variant="outline" className="w-full" disabled={loading}>
                Login com o Google
              </Button>
            </CardFooter>
          </form>
        </CardContent>
      </Card>
    </div>
  );
}

export default LoginCard;

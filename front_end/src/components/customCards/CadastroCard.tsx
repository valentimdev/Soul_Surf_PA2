import { useState } from 'react';
import { Link } from 'react-router-dom';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';

function CadastroCard() {
  // Estados para gerenciar os dados do formulário e o estado da requisição
  const [nome, setNome] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  const handleCadastro = async (e) => {
    e.preventDefault();

    // Limpa mensagens de erro/sucesso anteriores
    setError('');
    setSuccess('');

    // Validação básica do lado do cliente
    if (password !== confirmPassword) {
      setError('As senhas não coincidem!');
      return;
    }

    setLoading(true);

    try {
      // Envia a requisição para o seu endpoint de cadastro no backend
      const response = await fetch('http://localhost:8080/api/auth/signup', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ nome, email, password }),
      });

      // Verifica se a resposta foi bem-sucedida
      if (response.ok) {
        setSuccess('Cadastro realizado com sucesso! Você já pode fazer login.');
        // Opcional: Limpar o formulário após o sucesso
        setNome('');
        setEmail('');
        setPassword('');
        setConfirmPassword('');
      } else {
        // Se a resposta não foi ok, tenta ler a mensagem de erro do backend
        const errorData = await response.json();
        setError(errorData.message || 'Erro no cadastro. Tente novamente.');
      }
    } catch (e) {
      // Trata erros de rede
      setError('Não foi possível conectar ao servidor. Verifique se o backend está rodando.');
      console.error('Erro de conexão:', e);
    } finally {
      setLoading(false);
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
        <form onSubmit={handleCadastro}>
          <div className="flex flex-col gap-4">
            {/* Mensagens de feedback */}
            {loading && <p className="text-gray-500">A carregar...</p>}
            {error && <p className="text-red-500">{error}</p>}
            {success && <p className="text-green-500">{success}</p>}

            <div className="grid gap-2">
              <Label htmlFor="nome">Nome</Label>
              <Input
                id="nome"
                type="text"
                placeholder="Seu nome completo"
                value={nome}
                onChange={(e) => setNome(e.target.value)}
                required
              />
            </div>
            <div className="grid gap-2">
              <Label htmlFor="email">Email</Label>
              <Input
                id="email"
                type="email"
                placeholder="m@example.com"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                required
              />
            </div>
            <div className="grid gap-2">
              <Label htmlFor="password">Senha</Label>
              <Input
                id="password"
                type="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                required
              />
            </div>
            <div className="grid gap-2">
              <Label htmlFor="confirm-password">Confirmar Senha</Label>
              <Input
                id="confirm-password"
                type="password"
                value={confirmPassword}
                onChange={(e) => setConfirmPassword(e.target.value)}
                required
              />
            </div>
          </div>
          <CardFooter className="flex-col gap-2 pt-6">
            <Button type="submit" className="w-full" disabled={loading}>
              Confirmar Cadastro
            </Button>
            <Button variant="outline" className="w-full" asChild>
                <Link to="/home">Cancelar</Link>
            </Button>
          </CardFooter>
        </form>
      </CardContent>
    </Card>
  );
}

export default CadastroCard;

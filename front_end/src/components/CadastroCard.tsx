import React from 'react';
import {
  Button,
  Card,
  CardContent,
  CardDescription,
  CardFooter,
  CardHeader,
  CardTitle,
  Input,
  Label,
} from './ui/application';

function CadastroCard() {
  return (
    <Card className="w-full max-w-sm">
      <CardHeader>
        <CardTitle>Cadastro</CardTitle>
        <CardDescription>
          Preencha todos os campos abaixo para criar sua conta.
        </CardDescription>
      </CardHeader>
      <CardContent>
        <form>
          <div className="flex flex-col gap-4">
            <div className="grid gap-2">
              <Label htmlFor="username">Nome</Label>
              <Input
                id="username"
                type="text"
                placeholder="Seu nome completo"
                required
              />
            </div>
            <div className="grid gap-2">
              <Label htmlFor="email">Email</Label>
              <Input
                id="email"
                type="email"
                placeholder="m@example.com"
                required
              />
            </div>
            <div className="grid gap-2">
              <Label htmlFor="password">Senha</Label>
              <Input id="password" type="password" required />
            </div>
            <div className="grid gap-2">
              <Label htmlFor="confirm-password">Confirmar Senha</Label>
              <Input id="confirm-password" type="password" required />
            </div>
          </div>
        </form>
      </CardContent>
      <CardFooter className="flex-col gap-2 pt-6">
        <Button type="submit" className="w-full">
          Confirmar Cadastro
        </Button>
        <Button variant="outline" className="w-full">
          Cancelar
        </Button>
      </CardFooter>
    </Card>
  );
}

export default CadastroCard;

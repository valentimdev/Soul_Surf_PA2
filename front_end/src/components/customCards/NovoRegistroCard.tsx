import { useState } from "react";
import { Link } from "react-router-dom";

// Importando os componentes de UI do seu projeto
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
import { Textarea } from "@/components/ui/textarea"; // Manter o Textarea

function NovoRegistroCard() {
  // Estados para guardar os dados do formulário
  // Removendo apenas o estado de 'titulo'
  const [descricao, setDescricao] = useState(""); // Manter estado da descrição
  const [foto, setFoto] = useState<File | null>(null);

  const handleFileChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    if (event.target.files) {
      setFoto(event.target.files[0]);
    }
  };

  const handleSubmit = (event: React.FormEvent) => {
    event.preventDefault();
    // A integração com a API virá na próxima issue
    console.log({ descricao, foto }); // Ajustado para não incluir 'titulo'
    alert("Registo salvo (simulação)!");
  };

  return (
    <Card className="w-full max-w-2xl border">
      <CardHeader>
        <CardTitle>Criar um Novo Registo</CardTitle>
        <CardDescription>
          Partilhe a sua mais recente sessão de surf com a comunidade.
        </CardDescription>
      </CardHeader>
      <form onSubmit={handleSubmit}>
        <CardContent className="space-y-6">
          {/* O bloco do Título foi removido daqui */}
          {/* <div className="space-y-2">
            <Label htmlFor="titulo">Título</Label>
            <Input
              id="titulo"
              placeholder="Ex: Manhã épica na Praia do Futuro"
              value={titulo}
              onChange={(e) => setTitulo(e.target.value)}
              required
            />
          </div> */}
          
          {/* Bloco da Descrição - MANTIDO */}
          <div className="space-y-2">
            <Label htmlFor="descricao">Descrição</Label>
            <Textarea
              id="descricao"
              placeholder="Descreva como estava o mar, as suas melhores manobras, etc."
              value={descricao}
              onChange={(e) => setDescricao(e.target.value)}
              className="min-h-[120px]"
            />
          </div>
          
          {/* Bloco da Foto - MANTIDO */}
          <div className="space-y-2">
            <Label htmlFor="foto">Foto</Label>
            <Input
              id="foto"
              type="file"
              accept="image/*" // Aceita apenas ficheiros de imagem
              onChange={handleFileChange}
              required
            />
          </div>
        </CardContent>
        <CardFooter className="flex justify-end gap-4">
           <Button variant="outline" asChild>
              <Link to="/home">Cancelar</Link>
          </Button>
          <Button type="submit">Publicar Registo</Button>
        </CardFooter>
      </form>
    </Card>
  );
}

export default NovoRegistroCard;
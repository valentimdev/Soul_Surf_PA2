import { useState, useEffect } from "react";
import { Link } from "react-router-dom";
import { Loader2 } from "lucide-react";

// Componentes de UI
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
import { Textarea } from "@/components/ui/textarea";

import { BeachService, type BeachDTO } from "@/api/services/BeachService";
import { PostService } from "@/api/services/postService.ts";

interface NovoRegistroCardProps {
    onSuccess?: () => void; // função chamada quando criar o post
}

function NovoRegistroCard({ onSuccess }: NovoRegistroCardProps) {
    const [descricao, setDescricao] = useState("");
    const [foto, setFoto] = useState<File | null>(null);
    const [praias, setPraias] = useState<BeachDTO[]>([]);
    const [selectedBeachId, setSelectedBeachId] = useState<number | null>(null);
    const [loading, setLoading] = useState(false);

    // Buscar praias ao montar o componente
    useEffect(() => {
        BeachService.getAllBeaches()
            .then(setPraias)
            .catch((err) => console.error("Erro ao buscar praias:", err));
    }, []);

    const handleFileChange = (event: React.ChangeEvent<HTMLInputElement>) => {
        if (event.target.files) setFoto(event.target.files[0]);
    };

    const handleSubmit = async (event: React.FormEvent) => {
        event.preventDefault();

        if (!selectedBeachId) return;

        try {
            setLoading(true);

            await PostService.create({
                descricao,
                publico: true,
                beachId: selectedBeachId,
                foto: foto ?? undefined,
            });

            // Resetar os campos
            setDescricao("");
            setFoto(null);
            setSelectedBeachId(null);

            // Fechar o dialog
            if (onSuccess) onSuccess();
        } catch (error: unknown) {
            console.error("Erro ao criar post:", error);
        } finally {
            setLoading(false);
        }
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
                    {/* Seleção da Praia */}
                    <div className="space-y-2">
                        <Label htmlFor="praia">Praia</Label>
                        <select
                            id="praia"
                            value={selectedBeachId ?? ""}
                            onChange={(e) => setSelectedBeachId(Number(e.target.value))}
                            className="w-full border rounded p-2"
                            required
                        >
                            <option value="" disabled>
                                Selecione uma praia
                            </option>
                            {praias.map((beach) => (
                                <option key={beach.id} value={beach.id}>
                                    {beach.nome}
                                </option>
                            ))}
                        </select>
                    </div>

                    {/* Descrição */}
                    <div className="space-y-2">
                        <Label htmlFor="descricao">Descrição</Label>
                        <Textarea
                            id="descricao"
                            placeholder="Descreva como estava o mar, as suas melhores manobras, etc."
                            value={descricao}
                            onChange={(e) => setDescricao(e.target.value)}
                            className="min-h-[120px]"
                            required
                        />
                    </div>

                    {/* Foto */}
                    <div className="space-y-2">
                        <Label htmlFor="foto">Foto</Label>
                        <Input
                            id="foto"
                            type="file"
                            accept="image/*"
                            onChange={handleFileChange}
                            required
                        />
                    </div>
                </CardContent>
                <CardFooter className="flex justify-end gap-4">
                    <Button variant="outline" asChild>
                        <Link to="/home">Cancelar</Link>
                    </Button>
                    <Button type="submit" disabled={loading}>
                        {loading ? (
                            <>
                                <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                                Publicando...
                            </>
                        ) : (
                            "Publicar Registo"
                        )}
                    </Button>
                </CardFooter>
            </form>
        </Card>
    );
}

export default NovoRegistroCard;

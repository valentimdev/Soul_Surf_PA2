import { useEffect, useState } from "react";
import { Loader2 } from "lucide-react";
import { Button } from "@/components/ui/button";
import {
    Card,
    CardContent,
    CardFooter,
    CardHeader,
    CardTitle,
    CardDescription,
} from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import { DialogClose } from "@radix-ui/react-dialog";

import { BeachService, type BeachDTO } from "@/api/services/beachService";

interface NovoPraiaCardProps {
    beach?: BeachDTO; // se existir, estamos editando
    onSuccess?: () => void;
}

export default function NovoPraiaCard({ beach, onSuccess }: NovoPraiaCardProps) {
    const [nome, setNome] = useState("");
    const [descricao, setDescricao] = useState("");
    const [localizacao, setLocalizacao] = useState("");
    const [nivelExperiencia, setNivelExperiencia] = useState("INICIANTE");
    const [foto, setFoto] = useState<File | null>(null);
    const [loading, setLoading] = useState(false);

    // Preenche campos se estivermos editando
    useEffect(() => {
        if (beach) {
            setNome(beach.nome);
            setDescricao(beach.descricao);
            setLocalizacao(beach.localizacao);
            setNivelExperiencia(beach.nivelExperiencia || "INICIANTE");
            // foto não é preenchida, só upload de nova imagem
        }
    }, [beach]);

    const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        if (e.target.files) setFoto(e.target.files[0]);
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        try {
            setLoading(true);

            const formData = new FormData();
            formData.append("nome", nome);
            formData.append("descricao", descricao);
            formData.append("localizacao", localizacao);
            formData.append("nivelExperiencia", nivelExperiencia);
            if (foto) formData.append("foto", foto);

            if (beach) {
                // Editar
                /* await BeachService.updateBeach(beach.id, formData); */
            } else {
                // Criar nova praia
                await BeachService.createBeach(formData);
            }

            if (onSuccess) onSuccess();
        } catch (error) {
            console.error("Erro ao salvar praia:", error);
        } finally {
            setLoading(false);
        }
    };

    return (
        <Card className="w-full max-w-2xl border relative">
            <CardHeader>
                <CardTitle>{beach ? "Editar Praia" : "Adicionar Nova Praia"}</CardTitle>
                <CardDescription>
                    {beach
                        ? "Atualize as informações da praia."
                        : "Cadastre uma nova praia para a comunidade."}
                </CardDescription>
            </CardHeader>

            <form onSubmit={handleSubmit}>
                <CardContent className="space-y-6">
                    <div className="space-y-2">
                        <Label htmlFor="nome">Nome</Label>
                        <Input
                            id="nome"
                            value={nome}
                            onChange={(e) => setNome(e.target.value)}
                            placeholder="Ex: Praia de Itacaré"
                            required
                        />
                    </div>

                    <div className="space-y-2">
                        <Label htmlFor="descricao">Descrição</Label>
                        <Textarea
                            id="descricao"
                            value={descricao}
                            onChange={(e) => setDescricao(e.target.value)}
                            placeholder="Descreva as características da praia..."
                            required
                        />
                    </div>

                    <div className="space-y-2">
                        <Label htmlFor="localizacao">Localização</Label>
                        <Input
                            id="localizacao"
                            value={localizacao}
                            onChange={(e) => setLocalizacao(e.target.value)}
                            placeholder="Cidade ou coordenadas"
                            required
                        />
                    </div>

                    <div className="space-y-2">
                        <Label htmlFor="nivelExperiencia">Nível de Experiência</Label>
                        <select
                            id="nivelExperiencia"
                            value={nivelExperiencia}
                            onChange={(e) => setNivelExperiencia(e.target.value)}
                            className="w-full border rounded px-2 py-1"
                            required
                        >
                            <option value="INICIANTE">INICIANTE</option>
                            <option value="INTERMEDIARIO">INTERMEDIARIO</option>
                            <option value="AVANÇADO">AVANÇADO</option>
                        </select>
                    </div>

                    <div className="space-y-2">
                        <Label htmlFor="foto">Foto</Label>
                        <Input
                            id="foto"
                            type="file"
                            accept="image/*"
                            onChange={handleFileChange}
                        />
                    </div>
                </CardContent>

                <CardFooter className="flex justify-end gap-4 mt-5">
                    <DialogClose asChild>
                        <Button type="button" variant="outline">Cancelar</Button>
                    </DialogClose>
                    <Button type="submit" disabled={loading}>
                        {loading ? (
                            <>
                                <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                                Salvando...
                            </>
                        ) : beach ? "Atualizar Praia" : "Salvar Praia"}
                    </Button>
                </CardFooter>
            </form>
        </Card>
    );
}

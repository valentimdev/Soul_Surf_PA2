import { Card, CardHeader, CardTitle } from "@/components/ui/card";
import { Link } from "react-router-dom";

interface Beach {
    id: number;
    nome: string;
    descricao: string;
    localizacao: string;
    caminhoFoto: string;
}

interface BeachCardProps {
    beach: Beach;
}

export function BeachCard({ beach }: BeachCardProps) {
    return (
        <Link to={`/praias/${beach.id}`}>
        <Card className="w-[90%] rounded-2xl shadow-sm h-84">
            <CardHeader>
                <CardTitle className="text-xl font-bold">{beach.nome}</CardTitle>
                <span className="font-semibold">{beach.localizacao}</span>
            </CardHeader>
            <img
                src={beach.caminhoFoto}
                alt={`Foto da praia ${beach.nome}`}
                className="w-full aspect-video object-cover"
            />
        </Card>
        </Link>
    );
}

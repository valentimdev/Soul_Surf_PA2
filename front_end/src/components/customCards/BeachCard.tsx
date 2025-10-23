import { Card, CardHeader, CardTitle } from "@/components/ui/card";
import { Link } from "react-router-dom";
import { Pencil, Trash2 } from "lucide-react";

interface Beach {
    id: number;
    nome: string;
    descricao: string;
    localizacao: string;
    caminhoFoto: string;
}

interface BeachCardProps {
    beach: Beach;
    isAdmin?: boolean;
    onEdit?: (beach: Beach) => void;
    onDelete?: (beachId: number) => void;
}

export function BeachCard({ beach, isAdmin = false, onEdit, onDelete }: BeachCardProps) {
    return (
        <div className="relative w-[90%] rounded-2xl shadow-sm h-84">
            {isAdmin && (
                <div className="absolute top-2 right-2 flex gap-2 z-10">
                    <button
                        onClick={() => onEdit && onEdit(beach)}
                        className="p-1 rounded hover:bg-gray-200 hover:cursor-pointer"
                    >
                        <Pencil className="w-5 h-5 text-gray-600" />
                    </button>
                    <button
                        onClick={() => onDelete && onDelete(beach.id)}
                        className="p-1 rounded hover:bg-red-200 hover:cursor-pointer"
                    >
                        <Trash2 className="w-5 h-5 text-red-600" />
                    </button>
                </div>
            )}

            <Link to={`/praias/${beach.id}`}>
                <Card className="h-full rounded-2xl overflow-hidden">
                    <CardHeader>
                        <CardTitle className="text-xl font-bold">{beach.nome}</CardTitle>
                        <p className="text-sm italic font-semibold my-1">{beach.descricao}</p>
                        <span className="text-xs font-normal">{beach.localizacao}</span>
                    </CardHeader>
                    <img
                        src={beach.caminhoFoto}
                        alt={`Foto da praia ${beach.nome}`}
                        className="w-full aspect-video object-cover"
                    />
                </Card>
            </Link>
        </div>
    );
}

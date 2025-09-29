import { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import { BeachService, type BeachDTO, type PostDTO } from "@/api/services/beachService";
import { Card } from "@/components/ui/card";
import {PostCard} from "@/components/customCards/PostCard.tsx";

function BeachDetailPage() {
    const { id } = useParams<{ id: string }>();
    const [beach, setBeach] = useState<BeachDTO | null>(null);
    const [posts, setPosts] = useState<PostDTO[]>([]);
    const [followers, setFollowers] = useState<number>(0);

    useEffect(() => {
        if (!id) return;

        BeachService.getBeachById(id).then(setBeach);
        BeachService.getBeachPosts(id).then(setPosts);

        // TODO: endpoint real de seguidores
        setFollowers(42);
    }, [id]);

    if (!beach) return <div className="p-5 mt-5">Carregando...</div>;

    return (
        <div className="max-w-4xl mx-auto p-5 space-y-6">
        <div className="max-w-4xl mx-auto">
            <Card className="overflow-hidden shadow-lg rounded-2xl relative">
                <img
                    src={beach.caminhoFoto}
                    alt={beach.nome}
                    className="w-full h-64 object-cover"
                />
                <div className="absolute bottom-0 w-full bg-[#5899c2] text-white p-3 flex flex-col space-y-1">
                    <h1 className="text-xl font-bold">{beach.nome}</h1>
                    {beach.descricao && <p className="text-sm">{beach.descricao}</p>}
                    <p className="text-xs">📍 {beach.localizacao}</p>
                    <p className="text-xs">👥 {followers} seguidores</p>
                </div>
            </Card>
        </div>
            <div className="space-y-4">
                {posts.length === 0 && <p className="text-gray-500">Nenhum post ainda.</p>}
                {posts.map((post) => (
                    <PostCard
                        key={post.id}
                        username={post.usuario.username}
                        userAvatarUrl={post.usuario.fotoPerfil || ""}
                        imageUrl={post.caminhoFoto || ""}
                        description={post.descricao}
                        praia={beach.nome}
                    />
                ))}
            </div>
        </div>
    );
}

export default BeachDetailPage;

import { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import { Card, CardHeader, CardContent } from "@/components/ui/card";
import { Textarea } from "@/components/ui/textarea";
import { Button } from "@/components/ui/button";
import { PostCard } from "@/components/customCards/PostCard";
import { Avatar, AvatarImage, AvatarFallback } from "@/components/ui/avatar";
import api from "@/api/axios.ts";

interface Comment {
    id: number;
    texto: string;
    data: string;
    usuario: {
        id: number;
        username: string;
        fotoPerfil?: string | null;
    };
}

interface Post {
    id: number;
    descricao: string;
    usuario: {
        id: number;
        username: string;
        fotoPerfil?: string | null;
    };
    caminhoFoto?: string | null;
    beach?: { id: number; nome: string };
    publico: boolean;
    postOwnerId: number;
    loggedUserId: number;
    isFollowing: boolean;
    comments: Comment[];
}

export default function PostCommentsPage() {
    const { id } = useParams(); // vem da URL: /posts/:id/comments
    const [post, setPost] = useState<Post | null>(null);
    const [comments, setComments] = useState<Comment[]>([]);
    const [newComment, setNewComment] = useState("");
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const fetchData = async () => {
            try {
                // Buscar post
                const postRes = await api.get(`/posts/${id}`);
                const postData = postRes.data;

                setPost({
                    ...postData,
                    descricao: postData.descricao,
                    usuario: postData.usuario,
                    caminhoFoto: postData.caminhoFoto,
                    beach: postData.beach,
                    publico: postData.publico,
                    postOwnerId: postData.usuario.id,
                    loggedUserId: postData.loggedUserId || 0,
                    isFollowing: postData.isFollowing || false,
                    comments: postData.comments || [],
                });

                // Mapear os comentários
                setComments(
                    (postData.comments || []).map((c: any) => ({
                        id: c.id,
                        texto: c.texto,
                        data: c.data,
                        usuario: {
                            id: c.usuario.id,
                            username: c.usuario.username,
                            fotoPerfil: c.usuario.fotoPerfil,
                        },
                    }))
                );
            } catch (e) {
                console.error("Erro ao carregar post/comentários", e);
            } finally {
                setLoading(false);
            }
        };

        fetchData();
    }, [id]);

    const handleAddComment = async () => {
        if (!newComment.trim()) return;

        try {
            const res = await api.post(`/posts/${id}/comments/`, new URLSearchParams({ texto: newComment }));
            const created = res.data;

            // Adicionar comentário no estado já mapeado
            setComments((prev) => [
                ...prev,
                {
                    id: created.id,
                    texto: created.texto,
                    data: created.data,
                    usuario: {
                        id: created.usuario.id,
                        username: created.usuario.username,
                        fotoPerfil: created.usuario.fotoPerfil,
                    },
                },
            ]);

            setNewComment("");
        } catch (err) {
            console.error("Erro ao enviar comentário:", err);
        }
    };

    if (loading) return <p className="text-center mt-6">Carregando...</p>;
    if (!post) return <p className="text-center mt-6">Post não encontrado</p>;

    return (
        <div className="max-w-2xl mx-auto py-6 space-y-6">
            <PostCard
                postId={post.id}
                description={post.descricao}
                username={post.usuario.username}
                userAvatarUrl={post.usuario.fotoPerfil || undefined}
                imageUrl={post.caminhoFoto || undefined}
                praia={post.beach?.nome}
                postOwnerId={post.postOwnerId}
                loggedUserId={post.loggedUserId}
                isFollowing={post.isFollowing}
                onToggleFollow={() => {}}
            />

            <Card className="p-4">
                <h3 className="text-lg font-semibold mb-2">Adicionar comentário</h3>
                <Textarea
                    placeholder="Escreva algo..."
                    value={newComment}
                    onChange={(e) => setNewComment(e.target.value)}
                />
                <Button onClick={handleAddComment} className="mt-2">
                    Enviar
                </Button>
            </Card>

            <div className="space-y-3">
                {
                    comments.map((c) => (
                        <Card key={c.id} className="p-3">
                            <CardHeader className="flex flex-row items-center gap-3 p-0 mb-2">
                                <Avatar>
                                    <AvatarImage src={c.usuario.fotoPerfil || undefined} alt={c.usuario.username} />
                                    <AvatarFallback>{c.usuario.username.charAt(0).toUpperCase()}</AvatarFallback>
                                </Avatar>
                                <span className="font-semibold">{c.usuario.username}</span>
                            </CardHeader>
                            <CardContent className="p-0">
                                <p className="text-sm">{c.texto}</p>
                                <p className="text-xs text-gray-400 mt-1">
                                    {new Date(c.data).toLocaleString()}
                                </p>
                            </CardContent>
                        </Card>
                    ))
                }
            </div>
        </div>
    );
}

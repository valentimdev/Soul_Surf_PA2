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
    parentId?: number | null;
    replies?: Comment[];
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

function CommentItem({
                         comment,
                         loggedUserId,
                         onReplySubmit,
                         onDelete,
                         onUpdate,
                     }: {
    comment: Comment;
    loggedUserId: number;
    onReplySubmit: (parentId: number, text: string) => void;
    onDelete: (commentId: number) => void;
    onUpdate: (commentId: number, newText: string) => void;
}) {
    const [showReplyBox, setShowReplyBox] = useState(false);
    const [replyText, setReplyText] = useState("");
    const [isEditing, setIsEditing] = useState(false);
    const [editText, setEditText] = useState(comment.texto);

    const handleReply = () => {
        if (!replyText.trim()) return;
        onReplySubmit(comment.id, replyText);
        setReplyText("");
        setShowReplyBox(false);
    };

    const handleUpdate = () => {
        if (!editText.trim()) return;
        onUpdate(comment.id, editText);
        setIsEditing(false);
    };

    const isOwner = comment.usuario.id === loggedUserId;

    return (
        <div className="pl-2 border-l border-gray-200">
            <Card className="p-3 mb-2">
                <CardHeader className="flex items-center gap-3 p-0 mb-2">
                    <Avatar>
                        <AvatarImage src={comment.usuario.fotoPerfil || undefined} alt={comment.usuario.username} />
                        <AvatarFallback>{comment.usuario.username.charAt(0).toUpperCase()}</AvatarFallback>
                    </Avatar>
                    <span className="font-semibold">{comment.usuario.username}</span>
                </CardHeader>
                <CardContent className="p-0">
                    {isEditing ? (
                        <>
                            <Textarea value={editText} onChange={(e) => setEditText(e.target.value)} />
                            <div className="mt-2 flex gap-2">
                                <Button size="sm" onClick={handleUpdate}>Salvar</Button>
                                <Button size="sm" variant="outline" onClick={() => setIsEditing(false)}>Cancelar</Button>
                            </div>
                        </>
                    ) : (
                        <p className="text-sm">{comment.texto}</p>
                    )}

                    <p className="text-xs text-gray-400 mt-1">{new Date(comment.data).toLocaleString()}</p>

                    {!comment.parentId && !isEditing && (
                        <Button
                            variant="link"
                            size="sm"
                            className="p-0 mt-1"
                            onClick={() => setShowReplyBox((prev) => !prev)}
                        >
                            {showReplyBox ? "Cancelar" : "Responder"}
                        </Button>
                    )}

                    {showReplyBox && (
                        <div className="mt-2">
                            <Textarea placeholder={`Responder a ${comment.usuario.username}...`} value={replyText} onChange={(e) => setReplyText(e.target.value)} />
                            <Button className="mt-2" size="sm" onClick={handleReply}>Enviar resposta</Button>
                        </div>
                    )}

                    {isOwner && !isEditing && (
                        <div className="mt-2 flex gap-2">
                            <Button size="sm" variant="outline" onClick={() => setIsEditing(true)}>Editar</Button>
                            <Button size="sm" variant="destructive" onClick={() => onDelete(comment.id)}>Apagar</Button>
                        </div>
                    )}
                </CardContent>
            </Card>

            {comment.replies && comment.replies.length > 0 && (
                <div className="ml-4 mt-2 space-y-2">
                    {comment.replies.map((resp: Comment) => (
                        <Card key={resp.id} className="p-3 mb-2">
                            <CardHeader className="flex items-center gap-3 p-0 mb-2">
                                <Avatar>
                                    <AvatarImage src={resp.usuario.fotoPerfil || undefined} alt={resp.usuario.username} />
                                    <AvatarFallback>{resp.usuario.username.charAt(0).toUpperCase()}</AvatarFallback>
                                </Avatar>
                                <span className="font-semibold">{resp.usuario.username}</span>
                            </CardHeader>
                            <CardContent className="p-0">
                                <p className="text-sm">{resp.texto}</p>
                                <p className="text-xs text-gray-400 mt-1">{new Date(resp.data).toLocaleString()}</p>
                            </CardContent>
                        </Card>
                    ))}
                </div>
            )}
        </div>
    );
}

export default function PostCommentsPage() {
    const { id } = useParams();
    const [post, setPost] = useState<Post | null>(null);
    const [comments, setComments] = useState<Comment[]>([]);
    const [newComment, setNewComment] = useState("");
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const fetchData = async () => {
            try {
                const postRes = await api.get(`/posts/${id}`, {
                    headers: { "Content-Type": "application/json" },
                });
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
                    comments: [],
                });

                const commentsRes = await api.get(`/posts/${id}/comments/`, {
                    headers: { "Content-Type": "application/json" },
                });
                setComments(commentsRes.data);
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
            const params = new URLSearchParams({ texto: newComment });
            const res = await api.post(`/posts/${id}/comments/?${params.toString()}`);
            const created = res.data;

            const newC: Comment = {
                id: created.id,
                texto: created.texto,
                data: created.data,
                parentId: created.parentId,
                usuario: {
                    id: created.usuario.id,
                    username: created.usuario.username,
                    fotoPerfil: created.usuario.fotoPerfil,
                },
                replies: [],
            };

            setComments((prev) => [newC, ...prev]);
            setNewComment("");
        } catch (err) {
            console.error("Erro ao enviar comentário:", err);
        }
    };

    const handleReplySubmit = async (parentId: number, text: string) => {
        if (!text.trim()) return;
        try {
            const params = new URLSearchParams({ texto: text, parentId: parentId.toString() });
            const res = await api.post(`/posts/${id}/comments/?${params.toString()}`);
            const created = res.data;

            const newReply: Comment = {
                id: created.id,
                texto: created.texto,
                data: created.data,
                parentId: created.parentId,
                usuario: {
                    id: created.usuario.id,
                    username: created.usuario.username,
                    fotoPerfil: created.usuario.fotoPerfil,
                },
                replies: [],
            };

            setComments((prev) =>
                prev.map((c) =>
                    c.id === parentId
                        ? { ...c, replies: [...(c.replies || []), newReply] }
                        : c
                )
            );
        } catch (err) {
            console.error("Erro ao enviar resposta:", err);
        }
    };

    const handleDeleteComment = async (commentId: number) => {
        try {
            await api.delete(`/posts/${id}/comments/${commentId}`);
            setComments((prev) => prev.filter((c) => c.id !== commentId));
        } catch (err) {
            console.error("Erro ao apagar comentário:", err);
        }
    };

    const handleUpdateComment = async (commentId: number, newText: string) => {
        try {
            const res = await api.put(`/posts/${id}/comments/${commentId}?texto=${encodeURIComponent(newText)}`);
            const updated = res.data;
            setComments((prev) =>
                prev.map((c) => (c.id === commentId ? { ...c, texto: updated.texto } : c))
            );
        } catch (err) {
            console.error("Erro ao atualizar comentário:", err);
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
                <Button onClick={handleAddComment} className="mt-2">Enviar</Button>
            </Card>

            <div className="space-y-3">
                {comments.map((c) => (
                    <CommentItem
                        key={c.id}
                        comment={c}
                        loggedUserId={post.loggedUserId}
                        onReplySubmit={handleReplySubmit}
                        onDelete={handleDeleteComment}
                        onUpdate={handleUpdateComment}
                    />
                ))}
            </div>
        </div>
    );
}

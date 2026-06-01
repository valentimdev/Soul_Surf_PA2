import { useCallback, useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import { BeachService, type BeachDTO, type PostDTO } from "@/api/services/beachService";
import { UserService, type UserDTO } from "@/api/services/userService";
import { Card } from "@/components/ui/card";
import { PostCard } from "@/components/customCards/PostCard.tsx";
import LoadingSpinner from "@/components/LoadingSpinner.tsx";

const BEACH_IMAGE_FALLBACK =
    "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?q=80&w=1200";

function BeachDetailPage() {
    const { id } = useParams<{ id: string }>();
    const [beach, setBeach] = useState<BeachDTO | null>(null);
    const [posts, setPosts] = useState<PostDTO[]>([]);
    const [me, setMe] = useState<UserDTO | null>(null);
    const [followingIds, setFollowingIds] = useState<number[]>([]);

    const loadBeach = useCallback(() => {
        if (!id) return;

        BeachService.getBeachById(id).then(setBeach);
        BeachService.getBeachPosts(id).then(setPosts);
    }, [id]);

    useEffect(() => {
        loadBeach();
    }, [loadBeach]);

    useEffect(() => {
        UserService.getMe()
            .then(async (user) => {
                setMe(user);
                const following = await UserService.getFollowing(user.id);
                setFollowingIds(following.map((u) => u.id));
            })
            .catch(() => {
                setMe(null);
                setFollowingIds([]);
            });
    }, []);

    useEffect(() => {
        const handlePostDeleted = (event: Event) => {
            const postId = (event as CustomEvent<{ id: number }>).detail?.id;
            if (postId) setPosts((current) => current.filter((post) => post.id !== postId));
        };

        const handlePostUpdated = (event: Event) => {
            const detail = (event as CustomEvent<Partial<PostDTO> & { id: number }>).detail;
            if (!detail?.id) return;

            setPosts((current) =>
                current.map((post) => (post.id === detail.id ? { ...post, ...detail } : post))
            );
        };

        window.addEventListener("postDeleted", handlePostDeleted);
        window.addEventListener("postUpdated", handlePostUpdated);
        window.addEventListener("newPost", loadBeach);

        return () => {
            window.removeEventListener("postDeleted", handlePostDeleted);
            window.removeEventListener("postUpdated", handlePostUpdated);
            window.removeEventListener("newPost", loadBeach);
        };
    }, [loadBeach]);

    if (!beach) return <LoadingSpinner />;

    const handleDeletePostFromList = (postId: number) => {
        setPosts((prev) => prev.filter((p) => p.id !== postId));
    };

    const handleToggleFollow = (userId: number, isNowFollowing: boolean) => {
        setFollowingIds((prev) =>
            isNowFollowing ? [...prev, userId] : prev.filter((followedId) => followedId !== userId)
        );
    };

    return (
        <div className="max-w-4xl mx-auto p-5 space-y-6">
            <div className="max-w-4xl mx-auto">
                <Card className="overflow-hidden shadow-lg rounded-2xl relative">
                    <img
                        src={beach.caminhoFoto || BEACH_IMAGE_FALLBACK}
                        alt={beach.nome}
                        className="w-full h-64 object-cover"
                    />
                    <div className="absolute bottom-0 w-full bg-[#5899c2] text-white p-3 flex flex-col space-y-1">
                        <h1 className="text-xl font-bold">{beach.nome}</h1>
                        {beach.descricao && <p className="text-sm">{beach.descricao}</p>}
                        <p className="text-xs">Localizacao: {beach.localizacao}</p>
                        <p className="text-xs">Nivel: {beach.nivelExperiencia}</p>
                    </div>
                </Card>
            </div>

            <div className="space-y-4">
                {posts.length === 0 && <p className="text-gray-500">Nenhum post ainda.</p>}
                {posts.map((post) => (
                    <PostCard
                        key={post.id}
                        postId={post.id}
                        username={post.usuario.username}
                        fotoPerfil={post.usuario.fotoPerfil || ""}
                        imageUrl={post.caminhoFoto || ""}
                        description={post.descricao}
                        praia={beach.nome}
                        postOwnerId={post.usuario.id}
                        loggedUserId={me?.id || 0}
                        isFollowing={followingIds.includes(post.usuario.id)}
                        onToggleFollow={handleToggleFollow}
                        onPostDeleted={handleDeletePostFromList}
                        likesCount={post.likesCount}
                        commentsCount={post.commentsCount}
                        likedByCurrentUser={post.likedByCurrentUser}
                    />
                ))}
            </div>
        </div>
    );
}

export default BeachDetailPage;

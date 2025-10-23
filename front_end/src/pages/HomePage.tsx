import { useEffect, useState } from "react";
import api from "@/api/axios";
import { PostService, type PostDTO } from "@/api/services/postService";
import { UserService, type UserDTO } from "@/api/services/userService";
import { PostCard } from "@/components/customCards/PostCard";

function HomePage() {
    const [posts, setPosts] = useState<PostDTO[]>([]);
    const [me, setMe] = useState<UserDTO | null>(null);
    const [followingIds, setFollowingIds] = useState<number[]>([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const fetchData = async () => {
            try {
                const loggedUser = await UserService.getMe();
                setMe(loggedUser);

                // pega followings usando o id
                const followingRes = await api.get<UserDTO[]>(`/users/${loggedUser.id}/following`);
                setFollowingIds(followingRes.data.map((u) => u.id));

                const postsData = await PostService.list();
                setPosts(postsData);
            } catch (error) {
                console.error("Erro ao buscar dados:", error);
            } finally {
                setLoading(false);
            }
        };

        fetchData();
        const handleNewPost = (e: Event) => {
            const customEvent = e as CustomEvent<PostDTO>;
            setPosts((prev) => [customEvent.detail, ...prev]);
        };

        window.addEventListener("newPost", handleNewPost);
        return () => window.removeEventListener("newPost", handleNewPost);
    }, []);

    const handleToggleFollow = (userId: number, isNowFollowing: boolean) => {
        setFollowingIds((prev) =>
            isNowFollowing
                ? [...prev, userId] // adiciona
                : prev.filter((id) => id !== userId) // remove
        );
    };

    if (loading || !me) {
        return <div className="w-full text-center py-10">Carregando posts...</div>;
    }

    return (
        <div className="flex w-full min-h-screen gap-3 px-5 sm:p-0 ">
            <div className="hidden md:block w-[20%]"></div>
            <div className="w-full md:w-[60%] border-green-400 py-4 space-y-4">
                {posts.map((post) => (
                    <PostCard
                        key={post.id}
                        postId={post.id}
                        username={post.usuario.username}
                        userAvatarUrl={post.usuario.fotoPerfil || ""}
                        imageUrl={post.caminhoFoto || ""}
                        description={post.descricao}
                        praia={post.beach?.nome || "Praia desconhecida"}
                        postOwnerId={post.usuario.id}
                        loggedUserId={me.id}
                        isFollowing={followingIds.includes(post.usuario.id)}
                        onToggleFollow={handleToggleFollow}
                    />
                ))}
            </div>
            <div className="hidden md:block w-[20%]"></div>
        </div>
    );
}

export default HomePage;

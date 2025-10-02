import { useEffect, useState } from "react";
import { PostService, type PostDTO } from "@/api/services/postService";
import { UserService, type UserDTO } from "@/api/services/userService";
import { PostCard } from "@/components/customCards/PostCard";

function HomePage() {
    const [posts, setPosts] = useState<PostDTO[]>([]);
    const [me, setMe] = useState<UserDTO | null>(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const fetchPosts = async () => {
            try {
                const loggedUser = await UserService.getMe();
                setMe(loggedUser);
                const data = await PostService.list();
                setPosts(data);
            } catch (error) {
                console.error("Erro ao buscar dados:", error);
            } finally {
                setLoading(false);
            }
        };

        fetchPosts();
    }, []);

    if (loading || !me) {
        return <div className="w-full text-center py-10">Carregando posts...</div>;
    }

    return (
        <div className="flex w-full min-h-screen gap-3">
            <div className="hidden md:block w-[20%]"></div>
            <div className="w-full md:w-[60%] border-green-400 py-4 space-y-4">
                {posts.map((post) => (
                    <PostCard
                        key={post.id}
                        username={post.usuario.username}
                        userAvatarUrl={post.usuario.fotoPerfil || ""}
                        imageUrl={post.caminhoFoto || ""}
                        description={post.descricao}
                        praia={post.beach?.nome || "Praia desconhecida"}
                        postOwnerId={post.usuario.id}
                        loggedUserId={me.id}
                    />
                ))}
            </div>
            <div className="hidden md:block w-[20%]"></div>
        </div>
    );
}

export default HomePage;

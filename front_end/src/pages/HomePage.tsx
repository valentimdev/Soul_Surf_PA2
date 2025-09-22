import { useEffect, useState } from "react";
import { PostService, type PostDTO } from "@/api/services/postService";
import { PostCard } from "@/components/customCards/PostCard";

function HomePage() {
    const [posts, setPosts] = useState<PostDTO[]>([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const fetchPosts = async () => {
            try {
                const data = await PostService.list(); // lista todos os posts
                setPosts(data);
            } catch (error) {
                console.error("Erro ao buscar posts:", error);
            } finally {
                setLoading(false);
            }
        };

        fetchPosts();
    }, []);

    if (loading) {
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
                    />
                ))}
            </div>
            <div className="hidden md:block w-[20%]"></div>
        </div>
    );
}

export default HomePage;

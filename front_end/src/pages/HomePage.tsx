import { useEffect, useRef, useState } from "react";
import { Client } from "@stomp/stompjs";
import api from "@/api/axios";
import { type PostDTO } from "@/api/services/postService";
import { UserService, type UserDTO } from "@/api/services/userService";
import { PostCard } from "@/components/customCards/PostCard";
import LoadingSpinner from "@/components/LoadingSpinner.tsx";
import { Button } from "@/components/ui/button";
import { usePagination } from "@/hooks/usePagination";

function HomePage() {
    const [me, setMe] = useState<UserDTO | null>(null);
    const [followingIds, setFollowingIds] = useState<number[]>([]);
    const [feedType, setFeedType] = useState<"public" | "following">("public");

    const {
        data: posts,
        loading,
        setLoading,
        page,
        size,
        hasNext,
        updatePaginationData,
        loadMore,
        reset,
        setData: setPosts,
    } = usePagination<PostDTO>({ initialSize: 20 });

    // 🔥 WebSocket para o feed
    const wsClientRef = useRef<Client | null>(null);
    const subscribedPostIdsRef = useRef<Set<number>>(new Set());
    const latestPostsRef = useRef<PostDTO[]>([]);

    // sempre guarda a versão mais recente da lista de posts
    useEffect(() => {
        latestPostsRef.current = posts;
    }, [posts]);

    // Busca dados do usuário logado
    useEffect(() => {
        const fetchUserData = async () => {
            try {
                const loggedUser = await UserService.getMe();
                setMe(loggedUser);

                const followingRes = await api.get<UserDTO[]>(`/users/${loggedUser.id}/following`);
                setFollowingIds(followingRes.data.map((u) => u.id));
            } catch (error) {
                console.error("Erro ao buscar dados do usuário:", error);
            }
        };

        fetchUserData();
    }, []);

    // Busca posts baseado no tipo de feed e página
    useEffect(() => {
        const fetchPosts = async () => {
            setLoading(true);
            try {
                const endpoint = feedType === "public" ? "/posts/home" : "/posts/following";
                const response = await api.get(`${endpoint}?page=${page}&size=${size}`);

                const isFirstLoad = page === 0;
                updatePaginationData(response.data, !isFirstLoad);
            } catch (error) {
                console.error("Erro ao buscar posts:", error);
            } finally {
                setLoading(false);
            }
        };

        if (me) {
            fetchPosts();
        }
    }, [me, feedType, page, size, updatePaginationData, setLoading]);

    // 🔥 Abre o WebSocket uma vez para o feed
    useEffect(() => {
        const token = localStorage.getItem("token");
        if (!token) {
            return;
        }

        if (wsClientRef.current) {
            return; // já existe client
        }

        const isLocalhost = window.location.hostname === "localhost";
        const baseWsUrl = isLocalhost
            ? "ws://localhost:8080/ws"
            : "wss://soulsurfpa2-production.up.railway.app/ws";

        const socketUrl = `${baseWsUrl}?access_token=${encodeURIComponent(token)}`;

        let reconnectAttempts = 0;
        const maxReconnectAttempts = 3;

        const client = new Client({
            webSocketFactory: () => new WebSocket(socketUrl),
            reconnectDelay: 5000,
            heartbeatIncoming: 4000,
            heartbeatOutgoing: 4000,
            debug: () => {}, // Desabilitar logs de debug
            onConnect: () => {
                reconnectAttempts = 0; // Reset contador ao conectar
                // quando conectar, assina todos os posts que já estão carregados
                subscribeForPosts(latestPostsRef.current, client);
            },
            onStompError: (frame) => {
                console.error("[HOME] Erro STOMP:", frame.headers["message"]);
            },
            onWebSocketError: (_error) => {
                reconnectAttempts++;
                if (reconnectAttempts >= maxReconnectAttempts) {
                    console.warn("[HOME] Muitas tentativas de reconexão. Desabilitando WebSocket.");
                    client.deactivate();
                    wsClientRef.current = null;
                }
            },
            onDisconnect: () => {
                if (reconnectAttempts >= maxReconnectAttempts) {
                    wsClientRef.current = null;
                }
            },
        });

        wsClientRef.current = client;
        client.activate();

        return () => {
            if (client.connected) {
                client.deactivate();
            }
            wsClientRef.current = null;
            subscribedPostIdsRef.current.clear();
        };
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, []);

    // Função auxiliar para assinar likes dos posts
    const subscribeForPosts = (postsToSubscribe: PostDTO[], client: Client) => {
        postsToSubscribe.forEach((post) => {
            if (!subscribedPostIdsRef.current.has(post.id)) {
                subscribedPostIdsRef.current.add(post.id);

                const dest = `/topic/posts/${post.id}/likes`;
                console.log("[HOME] Subscribing to", dest);

                client.subscribe(dest, (message) => {
                    try {
                        const event = JSON.parse(message.body) as {
                            postId: number;
                            likesCount: number;
                            username: string;
                            liked: boolean;
                        };

                        setPosts((prev) =>
                            prev.map((p) =>
                                p.id === event.postId
                                    ? {
                                        ...p,
                                        likesCount: event.likesCount,
                                        likedByCurrentUser:
                                            me && event.username === me.username
                                                ? event.liked
                                                : p.likedByCurrentUser,
                                    }
                                    : p
                            )
                        );
                    } catch (e) {
                        console.error("Erro ao parsear like no feed:", e);
                    }
                });
            }
        });
    };

    // 🔥 Sempre que a lista de posts mudar E o WS estiver conectado, assina os novos posts
    useEffect(() => {
        const client = wsClientRef.current;
        if (!client || !client.connected) {
            return;
        }

        if (!posts || posts.length === 0) return;

        subscribeForPosts(posts, client);
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [posts, me, setPosts]);

    // Reset quando muda o tipo de feed
    const handleFeedTypeChange = (newFeedType: "public" | "following") => {
        if (newFeedType !== feedType) {
            setFeedType(newFeedType);
            reset();
        }
    };

    const handleToggleFollow = (userId: number, isNowFollowing: boolean) => {
        setFollowingIds((prev) => {
            if (isNowFollowing) return [...prev, userId];
            return prev.filter((id) => id !== userId);
        });
    };

    useEffect(() => {
        const handleNewPost = (e: Event) => {
            const customEvent = e as CustomEvent<PostDTO>;
            setPosts((prev) => [customEvent.detail, ...prev]);
        };

        window.addEventListener("newPost", handleNewPost);
        return () => window.removeEventListener("newPost", handleNewPost);
    }, [setPosts]);

    const handleDeletePostFromList = (postId: number) => {
        setPosts((prev) => prev.filter((p) => p.id !== postId));
    };

    if (!me || (loading && posts.length === 0)) {
        return (
            <div className="w-full flex justify-center py-10">
                <LoadingSpinner />
            </div>
        );
    }

    return (
        <div className="w-full max-w-2xl mx-auto p-4 flex flex-col gap-6">
            <div className="flex gap-4 justify-center mb-6 sticky top-20 bg-background z-1 py-2 border-b border-gray-200">
                <Button
                    variant={feedType === "public" ? "default" : "outline"}
                    onClick={() => handleFeedTypeChange("public")}
                >
                    Feed Público
                </Button>
                <Button
                    variant={feedType === "following" ? "default" : "outline"}
                    onClick={() => handleFeedTypeChange("following")}
                >
                    Seguindo
                </Button>
            </div>

            {/* Lista de posts */}
            <div className="space-y-6">
                {posts.map((post) => (
                    <PostCard
                        key={post.id}
                        postId={post.id}
                        username={post.usuario.username}
                        fotoPerfil={post.usuario.fotoPerfil || ""}
                        imageUrl={post.caminhoFoto || ""}
                        description={post.descricao}
                        praia={post.beach?.nome || "Praia do Futuro"}
                        postOwnerId={post.usuario.id}
                        loggedUserId={me?.id || 0}
                        isFollowing={followingIds.includes(post.usuario.id)}
                        onPostDeleted={handleDeletePostFromList}
                        onToggleFollow={handleToggleFollow}
                        likesCount={post.likesCount}
                        commentsCount={post.commentsCount}
                        likedByCurrentUser={post.likedByCurrentUser}
                    />
                ))}
            </div>

            {/* Botão carregar mais */}
            {hasNext && (
                <div className="flex justify-center mt-6">
                    <Button onClick={loadMore} disabled={loading} variant="outline">
                        {loading ? "Carregando..." : "Carregar mais"}
                    </Button>
                </div>
            )}

            {/* Estado vazio */}
            {posts.length === 0 && !loading && (
                <div className="text-center py-8">
                    <p className="text-gray-500">
                        {feedType === "following"
                            ? "Nenhum post de usuários que você segue"
                            : "Nenhum post encontrado"}
                    </p>
                </div>
            )}
        </div>
    );
}

export default HomePage;

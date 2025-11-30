import { useEffect, useState } from "react";
import { useSearchParams, useNavigate } from "react-router-dom";
import api from "@/api/axios";
import { Button } from "@/components/ui/button";
import { BeachCard } from "@/components/customCards/BeachCard";
import { UserService, type UserDTO } from "@/api/services/userService";

export default function SearchResultsPage() {
    const [params] = useSearchParams();
    const navigate = useNavigate();
    const query = params.get("query") || "";

    const [me, setMe] = useState<UserDTO | null>(null);
    const [followingIds, setFollowingIds] = useState<number[]>([]);
    const [users, setUsers] = useState<any[]>([]);
    const [beaches, setBeaches] = useState<any[]>([]);
    const [loading, setLoading] = useState(true);

    // ✅ busca usuário logado + quem ele segue
    useEffect(() => {
        const fetchMe = async () => {
            try {
                const loggedUser = await UserService.getMe();
                setMe(loggedUser);

                const followingRes = await api.get<UserDTO[]>(
                    `/users/${loggedUser.id}/following`
                );
                setFollowingIds(followingRes.data.map((u) => u.id));
            } catch (err) {
                console.error("Erro ao buscar dados do usuário:", err);
            }
        };

        fetchMe();
    }, []);

    async function fetchUsers() {
        try {
            const res = await api.get(`/users/search?query=${query}`);
            const result = res.data || [];

            // ✅ injeta isFollowing corretamente
            const usersWithFollow = result.map((user: any) => ({
                ...user,
                isFollowing: followingIds.includes(user.id),
            }));

            setUsers(usersWithFollow);
        } catch {
            setUsers([]);
        }
    }

    async function fetchBeaches() {
        try {
            const res = await api.get(`/beaches`);
            const all = res.data || [];

            const filtered = all.filter(
                (b: any) =>
                    b.nome?.toLowerCase().includes(query.toLowerCase()) ||
                    b.descricao?.toLowerCase().includes(query.toLowerCase()) ||
                    b.localizacao?.toLowerCase().includes(query.toLowerCase())
            );

            setBeaches(filtered);
        } catch {
            setBeaches([]);
        }
    }

    useEffect(() => {
        if (!me) return;

        setLoading(true);
        Promise.all([fetchUsers(), fetchBeaches()]).finally(() =>
            setLoading(false)
        );
    }, [query, me, followingIds]);

    const handleFollowUser = async (userId: number) => {
        try {
            const isCurrentlyFollowing = followingIds.includes(userId);

            await api.post(`/users/${userId}/follow`);

            setFollowingIds((prev) =>
                isCurrentlyFollowing
                    ? prev.filter((id) => id !== userId)
                    : [...prev, userId]
            );

            setUsers((prev) =>
                prev.map((user) =>
                    user.id === userId
                        ? { ...user, isFollowing: !user.isFollowing }
                        : user
                )
            );
        } catch (err) {
            console.error("Erro ao seguir/desseguir usuário:", err);
        }
    };

    if (loading) {
        return (
            <div className="p-6 text-center text-gray-500">
                Carregando resultados...
            </div>
        );
    }

    return (
        <div className="max-w-4xl mx-auto p-6 space-y-10">
            {/* USERS */}
            <section>
                <h2 className="text-xl font-bold mb-4">Usuários</h2>

                {users.length === 0 ? (
                    <p className="text-gray-500">
                        Não foram encontrados resultados
                    </p>
                ) : (
                    <div className="space-y-4">
                        {users.map((user: any) => (
                            <div
                                key={user.id}
                                className="flex items-center justify-between p-4 border rounded-xl shadow-sm"
                            >
                                <div className="flex items-center space-x-4">
                                    {user.fotoPerfil ? (
                                        <img
                                            src={user.fotoPerfil}
                                            alt={user.username}
                                            className="w-14 h-14 rounded-full object-cover cursor-pointer hover:opacity-80 transition-opacity"
                                            onClick={() => navigate(`/perfil/${user.username}`)}
                                        />
                                    ) : (
                                        <div
                                            className="w-14 h-14 rounded-full bg-gray-300 flex items-center justify-center text-white font-bold text-xl cursor-pointer hover:opacity-80 transition-opacity"
                                            onClick={() => navigate(`/perfil/${user.username}`)}
                                        >
                                            {user.username
                                                ?.charAt(0)
                                                .toUpperCase()}
                                        </div>
                                    )}
                                    <div>
                                        <p
                                            className="font-semibold cursor-pointer hover:text-primary transition-colors"
                                            onClick={() => navigate(`/perfil/${user.username}`)}
                                        >
                                            {user.username}
                                        </p>
                                        <p className="text-sm text-gray-500">
                                            {user.bio || "Sem bio"}
                                        </p>
                                    </div>
                                </div>

                                <Button
                                    variant={
                                        user.isFollowing ? "secondary" : "default"
                                    }
                                    onClick={() =>
                                        handleFollowUser(user.id)
                                    }
                                >
                                    {user.isFollowing ? "Seguindo" : "Seguir"}
                                </Button>
                            </div>
                        ))}
                    </div>
                )}
            </section>

            {/* PRAIAS */}
            <section>
                <h2 className="text-xl font-bold mb-4">Praias</h2>

                {beaches.length === 0 ? (
                    <p className="text-gray-500">
                        Não foram encontrados resultados
                    </p>
                ) : (
                    <div className="grid grid-cols-1 sm:grid-cols-2 gap-6">
                        {beaches.map((beach: any) => (
                            <BeachCard key={beach.id} beach={beach} />
                        ))}
                    </div>
                )}
            </section>
        </div>
    );
}

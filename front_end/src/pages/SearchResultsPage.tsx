import { useEffect, useState } from "react";
import { useSearchParams } from "react-router-dom";
import api from "@/api/axios";
import { Button } from "@/components/ui/button";
import {BeachCard} from "@/components/customCards/BeachCard";

export default function SearchResultsPage() {
    const [params] = useSearchParams();
    const query = params.get("query") || "";

    const [users, setUsers] = useState([]);
    const [beaches, setBeaches] = useState([]);
    const [loading, setLoading] = useState(true);

    // busca users pela API
    async function fetchUsers() {
        try {
            const res = await api.get(`/users/search?query=${query}`);
            setUsers(res.data || []);
        } catch {
            setUsers([]);
        }
    }

    // busca praias (todas) e filtra no front
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
        setLoading(true);
        Promise.all([fetchUsers(), fetchBeaches()]).finally(() =>
            setLoading(false)
        );
    }, [query]);

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
                    <p className="text-gray-500">Não foram encontrados resultados</p>
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
                                            className="w-14 h-14 rounded-full object-cover"
                                        />
                                    ) : (
                                        <div
                                            className="w-14 h-14 rounded-full bg-gray-300 flex items-center justify-center text-white font-bold text-xl"
                                        >
                                            {user.username?.charAt(0).toUpperCase()}
                                        </div>
                                    )}
                                    <div>
                                        <p className="font-semibold">{user.username}</p>
                                        <p className="text-sm text-gray-500">
                                            {user.bio || "Sem bio"}
                                        </p>
                                    </div>
                                </div>

                                <Button
                                    variant={user.isFollowing ? "secondary" : "default"}
                                    onClick={() => handleFollowUser(user.id)}
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
                    <p className="text-gray-500">Não foram encontrados resultados</p>
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

async function handleFollowUser(id: number) {
    try {
        await api.post(`/users/${id}/follow`);
        window.location.reload();
    } catch (err) {
        console.error(err);
    }
}

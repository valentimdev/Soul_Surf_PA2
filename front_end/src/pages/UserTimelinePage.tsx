import React, { useEffect, useState, useCallback } from "react";
import { useNavigate } from "react-router-dom";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import api from "@/api/axios";

interface User {
    id: number;
    username: string;
    bio: string;
    fotoPerfil: string | "";
    isFollowing: boolean;
}

export default function UsersTimelinePage() {
    const navigate = useNavigate();
    const [users, setUsers] = useState<User[]>([]);
    const [offset, setOffset] = useState(0);
    const [hasMore, setHasMore] = useState(true);
    const [searchTerm, setSearchTerm] = useState("");
    const [searchQuery, setSearchQuery] = useState("");

    const fetchUsers = useCallback(async (reset = false) => {
        try {
            const res = await api.get(`/users?offset=${reset ? 0 : offset}`);
            setUsers((prev) =>
                reset ? res.data : [...prev, ...res.data.filter((u: { id: number; }) => !prev.some(p => p.id === u.id))]
            );
            if (res.data.length < 10) setHasMore(false);
        } catch (err) {
            console.error(err);
        }
    }, [offset]);

    const handleSearch = useCallback(async (term: string) => {
        if (!term.trim()) {
            await fetchUsers(true);
            return;
        }

        try {
            const res = await api.get(`/users/search?query=${term}`);
            setUsers(res.data);
            setHasMore(false);
        } catch (err) {
            console.error(err);
        }
    }, [fetchUsers]);

    // ================================
    // 🔹 Buscar usuários iniciais
    // ================================
    useEffect(() => {
        fetchUsers(true);
    }, []);

    // ================================
    // 🔹 Dispara busca quando muda o searchQuery
    // ================================
    useEffect(() => {
        handleSearch(searchQuery);
    }, [searchQuery]);

    // ================================
    // 🔹 Enter confirma busca
    // ================================
    const handleKeyDown = (e: React.KeyboardEvent<HTMLInputElement>) => {
        if (e.key === "Enter") {
            e.preventDefault();
            setSearchQuery(searchTerm.trim());
        }
    };

    const handleFollow = async (userId: number) => {
        try {
            await api.post(`/follow/${userId}`);
            setUsers((prev) =>
                prev.map((u) =>
                    u.id === userId ? { ...u, isFollowing: !u.isFollowing } : u
                )
            );
        } catch (err) {
            console.error(err);
        }
    };

    return (
        <div className="max-w-2xl mx-auto mt-6">
            <Input
                type="text"
                placeholder="Buscar usuários..."
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                onKeyDown={handleKeyDown}
                className="mb-4"
            />
            <div className="space-y-4">
                {users.map((user) => (
                    <div
                        key={user.id}
                        className="flex items-center justify-between p-4 border rounded-xl shadow-sm"
                    >
                        <div className="flex items-center space-x-4">
                            <img
                                src={user.fotoPerfil || "/default-avatar.png"}
                                alt={user.username}
                                className="w-14 h-14 rounded-full object-cover cursor-pointer hover:opacity-80 transition-opacity"
                                onClick={() => navigate(`/perfil/${user.username}`)}
                            />
                            <div>
                                <p
                                    className="font-semibold cursor-pointer hover:text-primary transition-colors"
                                    onClick={() => navigate(`/perfil/${user.username}`)}
                                >
                                    {user.username}
                                </p>
                                <p className="text-sm text-gray-500">{user.bio || "Sem bio"}</p>
                            </div>
                        </div>
                        <Button
                            variant={user.isFollowing ? "secondary" : "default"}
                            onClick={() => handleFollow(user.id)}
                        >
                            {user.isFollowing ? "Seguindo" : "Seguir"}
                        </Button>
                    </div>
                ))}
            </div>
            {hasMore && (
                <div className="flex justify-center mt-4">
                    <Button onClick={() => setOffset((prev) => prev + 10)}>
                        Carregar mais
                    </Button>
                </div>
            )}
        </div>
    );
}

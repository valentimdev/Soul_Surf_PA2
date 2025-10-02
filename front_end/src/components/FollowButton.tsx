import { useState, useEffect } from "react";
import api from "@/api/axios"; // seu axios configurado com baseURL e token
import type { UserDTO } from "@/api/services/userService.ts";

interface FollowButtonProps {
    postOwnerId: number;
}

export default function FollowButton({ postOwnerId }: FollowButtonProps) {
    const [isFollowing, setIsFollowing] = useState<boolean | null>(null);
    const [loading, setLoading] = useState(false);

    useEffect(() => {
        // Pega o usuário logado
        const fetchFollowing = async () => {
            try {
                const res = await api.get<UserDTO[]>("/api/users/me/following");
                const followingIds = res.data.map((u) => u.id);
                setIsFollowing(followingIds.includes(postOwnerId));
            } catch (err) {
                console.error("Erro ao buscar lista de seguindo:", err);
            }
        };

        fetchFollowing();
    }, [postOwnerId]);

    const handleFollowToggle = async () => {
        if (loading || isFollowing === null) return;

        setLoading(true);
        try {
            if (isFollowing) {
                // Unfollow
                await api.delete(`/api/users/${postOwnerId}/follow`);
                setIsFollowing(false);
            } else {
                // Follow
                await api.post(`/api/users/${postOwnerId}/follow`);
                setIsFollowing(true);
            }
        } catch (err) {
            console.error("Erro ao seguir/deixar de seguir:", err);
        } finally {
            setLoading(false);
        }
    };

    if (isFollowing === null) return null; // ainda carregando

    return (
        <button
            onClick={handleFollowToggle}
            disabled={loading}
            className={`px-4 py-2 rounded ${
                isFollowing ? "bg-red-500 text-white" : "bg-blue-500 text-white"
            }`}
        >
            {isFollowing ? "Remover seguindo" : "Seguir"}
        </button>
    );
}
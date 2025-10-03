import { useState } from "react";
import { Button } from "@/components/ui/button";
import { Loader2 } from "lucide-react";
import api from "@/api/axios";

interface FollowButtonProps {
    postOwnerId: number;
    isFollowing: boolean;
    onToggleFollow: (userId: number, isNowFollowing: boolean) => void;
}

export default function FollowButton({ postOwnerId, isFollowing, onToggleFollow }: FollowButtonProps) {
    const [loading, setLoading] = useState(false);

    const handleClick = async () => {
        if (loading) return;
        setLoading(true);

        try {
            if (isFollowing) {
                await api.delete(`/users/${postOwnerId}/follow`);
                onToggleFollow(postOwnerId, false);
            } else {
                await api.post(`/users/${postOwnerId}/follow`);
                onToggleFollow(postOwnerId, true);
            }
        } catch (err) {
            console.error("Erro ao seguir/deseguir:", err);
        } finally {
            setLoading(false);
        }
    };

    return (
        <Button
            size="sm"
            onClick={handleClick}
            disabled={loading}
            className={`
                px-2 py-1 text-xs rounded-md
                ${isFollowing
                ? "bg-blue-500 text-white hover:bg-red-500" // já seguindo: hover vermelho
                : "bg-blue-500 text-white hover:bg-blue-600"} // ainda não seguindo: hover azul
            `}
        >
            {loading ? <Loader2 className="h-3 w-3 animate-spin" /> : isFollowing ? "Seguindo" : "Seguir"}
        </Button>
    );
}

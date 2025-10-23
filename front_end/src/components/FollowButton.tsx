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
    const [isHovering, setIsHovering] = useState(false);

    const handleClick = async () => {
        // ... (your existing handleClick logic)
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

    const commonClasses = `
        px-4 py-1 text-xs rounded-md font-semibold
        focus:outline-none focus:ring-2 focus:ring-ring focus:ring-offset-2
        transition-all duration-300 ease-in-out
        min-w-[120px] 
    `;

    if (isFollowing) {
        return (
            <Button
                size="sm"
                variant={isHovering ? "destructive" : "outline"}
                onClick={handleClick}
                disabled={loading}
                onMouseEnter={() => setIsHovering(true)}
                onMouseLeave={() => setIsHovering(false)}
                className={commonClasses} // Use the common classes
            >
                {loading ? (
                    <Loader2 className="h-4 w-4 animate-spin" />
                ) : isHovering ? (
                    "Deixar de Seguir"
                ) : (
                    "Seguindo"
                )}
            </Button>
        );
    }

    return (
        <Button
            size="sm"
            variant="default"
            onClick={handleClick}
            disabled={loading}
            className={commonClasses} // Use the common classes
        >
            {loading ? <Loader2 className="h-4 w-4 animate-spin" /> : "Seguir"}
        </Button>
    );
}
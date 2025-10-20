import { useNavigate } from "react-router-dom";
import { Avatar, AvatarImage, AvatarFallback } from "@/components/ui/avatar";
import { Button } from "@/components/ui/button";
import {
    Card,
    CardContent,
    CardFooter,
    CardHeader,
} from "@/components/ui/card";
import { Heart, MessageCircle } from "lucide-react";
import FollowButton from "@/components/FollowButton.tsx";

interface PostCardProps {
    postId: number;
    username: string;
    userAvatarUrl?: string;
    imageUrl?: string;
    description: string;
    praia?: string;
    postOwnerId: number;
    loggedUserId: number;
    isFollowing: boolean;
    onToggleFollow: (userId: number, isNowFollowing: boolean) => void;
}

export function PostCard({
                             postId,
                             username,
                             userAvatarUrl,
                             imageUrl,
                             description,
                             praia,
                             postOwnerId,
                             loggedUserId,
                             isFollowing,
                             onToggleFollow,
                         }: PostCardProps) {
    const isOwner =
        postOwnerId && loggedUserId
            ? Number(postOwnerId) === Number(loggedUserId)
            : false;

    const navigate = useNavigate();
    
    const highlightMentions = (text?: string) => {
        if (!text) return "";
        const escapedText = text.replace(/</g, "&lt;").replace(/>/g, "&gt;");
        return escapedText.replace(
            /@([\w]+)/g,
            `<span style="color:#2563eb; font-weight:500; cursor:pointer;">@$1</span>`
        );
    };

    return (
        <Card className="w-80% max-w-xl mx-auto gap-0">
            <CardHeader className="flex flex-row items-center justify-between p-2">
                <div className="flex items-center gap-3">
                    <Avatar>
                        <AvatarImage src={userAvatarUrl} alt={username} />
                        <AvatarFallback>
                            {username?.charAt(0).toUpperCase() ?? "?"}
                        </AvatarFallback>
                    </Avatar>
                    <div className="flex flex-col">
                        <span className="font-semibold">{username}</span>
                        <span className="text-sm text-muted-foreground">{praia}</span>
                    </div>
                </div>

                {!isOwner && (
                    <FollowButton
                        postOwnerId={postOwnerId}
                        isFollowing={isFollowing}
                        onToggleFollow={onToggleFollow}
                    />
                )}
            </CardHeader>

            {/* Se tiver imagem, mostra a imagem */}
            {imageUrl ? (
                <CardContent className="p-0">
                    <img
                        src={imageUrl}
                        alt="Post"
                        className="w-full max-h-[500px] object-cover"
                    />
                    <div className="mt-2 w-full p-2">
                        <p
                            className="text-sm whitespace-pre-wrap break-words"
                            dangerouslySetInnerHTML={{
                                __html: highlightMentions(description),
                            }}
                        />
                    </div>
                </CardContent>
            ) : (
                // Post apenas de texto, compacto
                <CardContent className="p-2">
                    <p
                        className="text-sm whitespace-pre-wrap break-words leading-relaxed"
                        dangerouslySetInnerHTML={{
                            __html: highlightMentions(description),
                        }}
                    />
                </CardContent>
            )}

            <CardFooter className="flex flex-col items-start p-2">
                <div className="flex gap-4">
                    <Button variant="ghost" size="icon">
                        <Heart className="h-6 w-6" />
                    </Button>
                    <Button
                        variant="ghost"
                        size="icon"
                        onClick={() => navigate(`/posts/${postId}/comments`)}
                    >
                        <MessageCircle className="h-6 w-6" />
                    </Button>
                </div>

                <div className="mt-2 text-xs text-gray-500">
                    Ver todos os comentários
                </div>
            </CardFooter>
        </Card>
    );
}

import { Avatar, AvatarImage, AvatarFallback } from '@/components/ui/avatar';
import { Button } from '@/components/ui/button';
import {
    Card,
    CardContent,
    CardFooter,
    CardHeader,
} from '@/components/ui/card';
import { Heart, MessageCircle } from 'lucide-react';
import FollowButton from "@/components/FollowButton.tsx";

interface PostCardProps {
    username: string;
    userAvatarUrl?: string;
    imageUrl?: string;
    description: string;
    praia?: string;
    postOwnerId: number;
    loggedUserId: number;
}

export function PostCard({
                             username,
                             userAvatarUrl,
                             imageUrl,
                             description,
                             praia,
                             postOwnerId,
                             loggedUserId,
                         }: PostCardProps) {
    const isOwner = postOwnerId && loggedUserId ? Number(postOwnerId) === Number(loggedUserId) : false;

    return (
        <Card className="w-80% max-w-xl mx-auto">
            <CardHeader className="flex flex-row items-center justify-between p-2">
                <div className="flex items-center gap-3">
                    <Avatar>
                        <AvatarImage src={userAvatarUrl} alt={username} />
                        <AvatarFallback>{username?.charAt(0).toUpperCase() ?? "?"}</AvatarFallback>
                    </Avatar>
                    <div className="flex flex-col">
                        <span className="font-semibold">{username}</span>
                        <span className="text-sm text-muted-foreground">{praia}</span>
                    </div>
                </div>

                {!isOwner && (
                    <FollowButton postOwnerId={postOwnerId} />
                )}
            </CardHeader>

            <CardContent className="p-0">
                {imageUrl && (
                    <img
                        src={imageUrl}
                        alt="Post"
                        className="w-full max-h-[500px] object-cover"
                    />
                )}
            </CardContent>

            <CardFooter className="flex flex-col items-start p-2">
                <div className="flex gap-4">
                    <Button variant="ghost" size="icon">
                        <Heart className="h-6 w-6" />
                    </Button>
                    <Button variant="ghost" size="icon">
                        <MessageCircle className="h-6 w-6" />
                    </Button>
                </div>
                <div className="mt-2">
                    <p className="text-sm">
                        <span className="font-semibold">{username}</span> {description}
                    </p>
                </div>
                <div className="mt-2 text-xs text-gray-500">
                    Ver todos os comentários
                </div>
            </CardFooter>
        </Card>
    );
}

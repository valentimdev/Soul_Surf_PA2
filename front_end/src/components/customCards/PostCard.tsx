import { useNavigate } from 'react-router-dom';
import { Avatar, AvatarImage, AvatarFallback } from '@/components/ui/avatar';
import { Button } from '@/components/ui/button';
import {
  Card,
  CardContent,
  CardFooter,
  CardHeader,
} from '@/components/ui/card';
import { MessageCircle, MapPin, Share2 } from 'lucide-react';
import FollowButton from '@/components/FollowButton.tsx';
import { HanglooseIcon } from '@/assets/icons/HanglooseIcon';
import { useState } from 'react';
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
  const [liked, setLiked] = useState(false);

  const highlightMentions = (text?: string) => {
    if (!text) return '';
    const escapedText = text.replace(/</g, '&lt;').replace(/>/g, '&gt;');
    return escapedText.replace(
      /@([\w]+)/g,
      `<span style="color:#2A4B7C; font-weight:600; cursor:pointer;">@$1</span>` // Mudança de cor aqui
    );
  };

 return (
    <Card className="w-full mx-auto bg-card rounded-lg overflow-hidden shadow-sm mb-4 border border-none">
      <CardHeader className="flex flex-row items-center justify-between p-4 pb-2">
        <div className="flex items-center gap-3">
          <Avatar className="w-10 h-10 border-2 border-transparent hover:border-primary">
            <AvatarImage src={userAvatarUrl} alt={username} />
            <AvatarFallback>
              {username?.charAt(0).toUpperCase() ?? '?'}
            </AvatarFallback>
          </Avatar>
          <div className="flex flex-col">
            <span className="font-bold text-card-foreground text-base">
              {username}
            </span>
            {praia && (
              <div className="flex items-center text-sm text-muted-foreground mt-0.5">

                <MapPin className="h-3 w-3 mr-1 text-primary" />
                <span>{praia}</span>
              </div>
            )}
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

      {imageUrl ? (
        <CardContent className="p-0">
          <img
            src={imageUrl}
            alt="Post"
            className="w-full max-h-[500px] object-cover bg-muted"
          />
          <div className="mt-3 px-4 pt-2">
            <p
              className="text-foreground whitespace-pre-wrap break-words leading-relaxed text-sm"
              dangerouslySetInnerHTML={{
                __html: highlightMentions(description),
              }}
            />
          </div>
        </CardContent>
      ) : (
        <CardContent className="p-4">
          <p
            className="text-foreground whitespace-pre-wrap break-words leading-relaxed text-sm"
            dangerouslySetInnerHTML={{
              __html: highlightMentions(description),
            }}
          />
        </CardContent>
      )}

     <CardFooter className="flex flex-col items-start p-4 pt-2">
    <div className=" flex gap-6 w-full items-center">
        {/* Botão Hangloose */}
        <Button
            variant="ghost"
            className="group hover:bg-primary/20 rounded-full h-12 w-12 p-0"
            onClick={() => setLiked(!liked)}
        >
            <HanglooseIcon
                // MUDANÇA: Usando size-7 diretamente no ícone
                className={`size-7 transition-colors ${
                    liked ? 'text-primary' : 'text-muted-foreground'
                }`}
            />
        </Button>

        {/* Botão de Comentários */}
        <Button
            variant="ghost"
            className="group hover:bg-primary/20 rounded-full h-12 w-12 p-0"
            onClick={() => navigate(`/posts/${postId}/comments`)}
        >
            <MessageCircle className="size-7 text-muted-foreground group-hover:text-primary" />
        </Button>

        {/* Botão de Compartilhar */}
        <Button
            variant="ghost"
            className="group hover:bg-primary/20 rounded-full h-12 w-12 p-0"
        >
            <Share2 className="size-7 text-muted-foreground group-hover:text-primary" />
        </Button>
    </div>

    <div
        className="ml-3 mt-3 text-sm font-semibold text-foreground cursor-pointer hover:underline"
        onClick={() => navigate(`/posts/${postId}/comments`)}
    >
        Ver todos os comentários
    </div>
</CardFooter>
    </Card>
  );
}
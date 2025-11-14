import { useNavigate } from "react-router-dom";
import { AdminService } from "@/api/services/adminService";
import { useEffect, useRef, useState } from "react";
import { Avatar, AvatarImage, AvatarFallback } from "@/components/ui/avatar";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardFooter, CardHeader } from "@/components/ui/card";
import {
    MessageCircle,
    MapPin,
    Share2,
    MoreVertical,
    Pencil,
    Trash2,
} from "lucide-react";
import FollowButton from "@/components/FollowButton";
import { HanglooseIcon } from "@/assets/icons/HanglooseIcon";
import {
    DropdownMenu,
    DropdownMenuContent,
    DropdownMenuItem,
    DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import {
    Dialog,
    DialogContent,
    DialogHeader,
    DialogTitle,
    DialogFooter,
} from "@/components/ui/dialog";
import { Textarea } from "@/components/ui/textarea";
import { postRoutes } from "@/api/routes/post";
import api from "@/api/axios";
import { LikeService } from "@/api/services/likeService";
import { UserService } from "@/api/services/userService";

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
    likesCount?: number;
    likedByCurrentUser?: boolean;
    onPostDeleted: (postId: number) => void;
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
                             likesCount: initialLikesCount = 0,
                             likedByCurrentUser: initialLiked = false,
                             onPostDeleted
                         }: PostCardProps) {
    const navigate = useNavigate();
    const [liked, setLiked] = useState(initialLiked);
    const [likesCount, setLikesCount] = useState(initialLikesCount);
    const [isLiking, setIsLiking] = useState(false);
    const [editDialogOpen, setEditDialogOpen] = useState(false);
    const [editedContent, setEditedContent] = useState(description);
    const [isDeleting, setIsDeleting] = useState(false);
    const [isAdmin, setIsAdmin] = useState(false);
    const contentRef = useRef<HTMLParagraphElement>(null);

    useEffect(() => {
        setLiked(initialLiked);
        setLikesCount(initialLikesCount);
    }, [initialLiked, initialLikesCount]);

    useEffect(() => {
        UserService.getMe()
            .then((user) => setIsAdmin(user.admin === true))
            .catch(() => setIsAdmin(false));
    }, []);

    const isOwner =
        postOwnerId && loggedUserId
            ? Number(postOwnerId) === Number(loggedUserId)
            : false;

    useEffect(() => {
        if (!contentRef.current) return;
        const spans = contentRef.current.querySelectorAll("span[data-mention]");
        spans.forEach((span) => {
            span.addEventListener("click", () => {
                const mention = span.getAttribute("data-mention");
                if (mention) navigate(`/perfil/${mention}`);
            });
        });
        return () => {
            spans.forEach((span) => span.replaceWith(span.cloneNode(true)));
        };
    }, [description, navigate]);

    const highlightMentions = (text?: string) => {
        if (!text) return "";
        const escapedText = text.replace(/</g, "&lt;").replace(/>/g, "&gt;");
        return escapedText.replace(
            /@([\w]+)/g,
            `<span data-mention="$1" style="color:#2A4B7C; font-weight:600; cursor:pointer;">@$1</span>`
        );
    };

    const handleUpdate = async () => {
        try {
            const formData = new FormData();
            formData.append("descricao", editedContent);

            await api.put(postRoutes.update(postId), formData, {
                headers: { "Content-Type": "multipart/form-data" },
            });

            setEditDialogOpen(false);
        } catch (err) {
            console.error("Erro ao editar post:", err);
            alert("Não foi possível atualizar o post.");
        }
    };

    const handleDelete = async () => {
        if (!confirm("Tem certeza que deseja excluir este post?")) return;
        setIsDeleting(true);

        try {
            if (isAdmin && !isOwner) {
                await AdminService.deletePost(postId);
            } else {
                await api.delete(postRoutes.delete(postId));
            }

            onPostDeleted(postId);
        } catch (err) {
            console.error("Erro ao excluir post:", err);
            alert("Não foi possível excluir o post.");
        } finally {
            setIsDeleting(false);
        }
    };

    const handleToggleLike = async () => {
        if (isLiking) return;
        setIsLiking(true);

        const previousLiked = liked;
        const previousCount = likesCount;
        setLiked(!liked);
        setLikesCount((prev) => (liked ? prev - 1 : prev + 1));

        try {
            const response = await LikeService.toggleLike(postId);
            setLiked(response.liked);
            setLikesCount(response.likesCount);
        } catch (err) {
            setLiked(previousLiked);
            setLikesCount(previousCount);
            console.error("Erro ao alternar like:", err);
        } finally {
            setIsLiking(false);
        }
    };

    const shouldShowFollowButton = !isOwner;
    const shouldShowMenu = isOwner || isAdmin;
    const canEdit = isOwner;
    const canDelete = isOwner || isAdmin;

    return (
        <>
            <Card className="w-full mx-auto bg-card rounded-lg overflow-hidden shadow-sm mb-4 border border-none">
                <CardHeader className="flex flex-row items-center justify-between p-4 pb-2">
                    <div className="flex items-center gap-3">
                        <Avatar className="w-10 h-10 border-2 border-transparent hover:border-primary">
                            <AvatarImage src={userAvatarUrl} alt={username} />
                            <AvatarFallback>{username?.charAt(0).toUpperCase() ?? "?"}</AvatarFallback>
                        </Avatar>
                        <div className="flex flex-col">
                            <span className="font-bold text-card-foreground text-base">{username}</span>
                            {praia && (
                                <div className="flex items-center text-sm text-muted-foreground mt-0.5">
                                    <MapPin className="h-3 w-3 mr-1 text-primary" />
                                    <span>{praia}</span>
                                </div>
                            )}
                        </div>
                    </div>

                    <div className="flex items-center gap-2">
                        {shouldShowFollowButton && (
                            <FollowButton
                                postOwnerId={postOwnerId}
                                isFollowing={isFollowing}
                                onToggleFollow={onToggleFollow}
                            />
                        )}

                        {shouldShowMenu && (
                            <DropdownMenu>
                                <DropdownMenuTrigger asChild>
                                    <Button variant="ghost" className="rounded-full h-10 w-10 p-0">
                                        <MoreVertical className="w-5 h-5 text-muted-foreground" />
                                    </Button>
                                </DropdownMenuTrigger>

                                <DropdownMenuContent align="end">
                                    {canEdit && (
                                        <DropdownMenuItem onClick={() => setEditDialogOpen(true)}>
                                            <Pencil className="w-4 h-4 mr-2" /> Editar post
                                        </DropdownMenuItem>
                                    )}
                                    {canDelete && (
                                        <DropdownMenuItem
                                            onClick={handleDelete}
                                            className="text-red-500 focus:text-red-600"
                                            disabled={isDeleting}
                                        >
                                            <Trash2 className="w-4 h-4 mr-2" />
                                            {isDeleting ? "Excluindo..." : "Excluir post"}
                                        </DropdownMenuItem>
                                    )}
                                </DropdownMenuContent>
                            </DropdownMenu>
                        )}
                    </div>
                </CardHeader>

                {imageUrl ? (
                    <CardContent className="p-0">
                        <img src={imageUrl} alt="Post" className="w-full max-h-[500px] object-cover bg-muted" />
                        <div className="mt-3 px-4 pt-2">
                            <p
                                ref={contentRef}
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
                            ref={contentRef}
                            className="text-foreground whitespace-pre-wrap break-words leading-relaxed text-sm"
                            dangerouslySetInnerHTML={{
                                __html: highlightMentions(description),
                            }}
                        />
                    </CardContent>
                )}

                <CardFooter className="flex flex-col items-start p-4 pt-2">
                    <div className="flex gap-6 w-full items-center">
                        <div className="flex items-center gap-2">
                            <Button
                                variant="ghost"
                                className={`group hover:bg-primary/20 rounded-full h-12 w-12 p-0 transition-transform ${
                                    liked ? "scale-110" : ""
                                }`}
                                onClick={handleToggleLike}
                                disabled={isLiking}
                            >
                                <HanglooseIcon
                                    className={`size-7 transition-all duration-200 ${
                                        liked
                                            ? "text-primary scale-110"
                                            : "text-muted-foreground group-hover:text-primary"
                                    }`}
                                />
                            </Button>
                            {likesCount > 0 && (
                                <span className="text-sm font-semibold text-foreground min-w-[20px]">
                                    {likesCount}
                                </span>
                            )}
                        </div>

                        <Button
                            variant="ghost"
                            className="group hover:bg-primary/20 rounded-full h-12 w-12 p-0"
                            onClick={() => navigate(`/posts/${postId}/comments`)}
                        >
                            <MessageCircle className="size-7 text-muted-foreground group-hover:text-primary" />
                        </Button>

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

            <Dialog open={editDialogOpen} onOpenChange={setEditDialogOpen}>
                <DialogContent className="sm:max-w-md">
                    <DialogHeader>
                        <DialogTitle>Editar publicação</DialogTitle>
                    </DialogHeader>
                    <Textarea
                        value={editedContent}
                        onChange={(e) => setEditedContent(e.target.value)}
                        placeholder="Edite seu post..."
                        className="min-h-[120px]"
                    />
                    <DialogFooter>
                        <Button variant="outline" onClick={() => setEditDialogOpen(false)}>
                            Cancelar
                        </Button>
                        <Button onClick={handleUpdate}>Confirmar</Button>
                    </DialogFooter>
                </DialogContent>
            </Dialog>
        </>
    );
}

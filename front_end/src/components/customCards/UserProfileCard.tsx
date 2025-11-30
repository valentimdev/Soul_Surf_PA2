import { useState, type ChangeEvent, type FormEvent, useEffect } from 'react';
import { Avatar, AvatarFallback, AvatarImage } from '@/components/ui/avatar';
import { Card } from '@/components/ui/card';
import { Pencil } from 'lucide-react';
import { Tabs, TabsContent } from '@/components/ui/tabs';
import {
  Dialog,
  DialogTrigger,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogClose,
} from '@/components/ui/dialog';
import { Input } from '@/components/ui/input';
import { type UserDTO, UserService } from '@/api/services/userService';
import { toast } from 'sonner';
import { PostCard } from '@/components/customCards/PostCard.tsx';
import { Button } from '../ui/button';
import LoadingSpinner from "@/components/LoadingSpinner.tsx";
type UserProfileCardProps = {
  user: UserDTO;
};

export function UserProfileCard({ user }: UserProfileCardProps) {
  const [username, setUsername] = useState(user.username);
  const [bio, setBio] = useState(user.bio || '');
  const [fotoPerfil, setFotoPerfil] = useState<File | null>(null);
  const [fotoCapa, setFotoCapa] = useState<File | null>(null);
  const [previewPerfil, setPreviewPerfil] = useState(user.fotoPerfil);
  const [previewCapa, setPreviewCapa] = useState(user.fotoCapa);
  const [isDialogOpen, setIsDialogOpen] = useState(false);
  const [me, setMe] = useState<UserDTO | null>(null);
  const [followingIds, setFollowingIds] = useState<number[]>([]);
  const [showFollowers, setShowFollowers] = useState(false);
  const [showFollowing, setShowFollowing] = useState(false);
  const [followersList, setFollowersList] = useState<UserDTO[]>([]);
  const [followingList, setFollowingList] = useState<UserDTO[]>([]);
  const [posts, setPosts] = useState(user.posts || []);

    const handleDeletePostFromList = (postId: number) => {
        setPosts(prev => prev.filter(p => p.id !== postId));
    };

    useEffect(() => {
    UserService.getMe().then((user) => {
      setMe(user);
      UserService.getFollowing(user.id).then((list) => {
        setFollowingIds(list.map((u) => u.id));
      });
    });
  }, []);
  const [editData, setEditData] = useState({
    username: user.username,
    bio: user.bio || '',
    previewPerfil: user.fotoPerfil,
    previewCapa: user.fotoCapa,
  });
  const [usernameError, setUsernameError] = useState<string | null>(null);

  // Função para remover acentos
  const removeAccents = (str: string) => {
    return str.normalize("NFD").replace(/[\u0300-\u036f]/g, "");
  };

  // Função para validar e normalizar username
  const handleUsernameChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const value = e.target.value;
    // Remove espaços e acentos
    const normalized = removeAccents(value.replace(/\s/g, ""));
    setEditData({ ...editData, username: normalized });

    // Mostra erro se o usuário tentou digitar espaço ou acento
    if (value !== normalized) {
      setUsernameError("Username não pode conter espaços ou acentos.");
    } else {
      setUsernameError(null);
    }
  };

  const handleFotoPerfilChange = (e: ChangeEvent<HTMLInputElement>) => {
    if (e.target.files?.[0]) {
      const file = e.target.files[0];
      setFotoPerfil(file);
      setEditData((prevData) => ({
        ...prevData,
        previewPerfil: URL.createObjectURL(file),
      }));
    }
  };

  const handleFotoCapaChange = (e: ChangeEvent<HTMLInputElement>) => {
    if (e.target.files?.[0]) {
      const file = e.target.files[0];
      setFotoCapa(file);
      setEditData((prevData) => ({
        ...prevData,
        previewCapa: URL.createObjectURL(file),
      }));
    }
  };

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();

    // Validação adicional antes de enviar
    if (editData.username.includes(" ") || /[àáâãäèéêëìíîïòóôõöùúûüçñ]/.test(editData.username)) {
      setUsernameError("Username não pode conter espaços ou acentos.");
      return;
    }

    const formData = new FormData();
    formData.append('username', editData.username);
    formData.append('bio', editData.bio);
    if (fotoPerfil) formData.append('fotoPerfil', fotoPerfil);
    if (fotoCapa) formData.append('fotoCapa', fotoCapa);

    try {
      const updatedUser = await UserService.updateProfile(formData);
      setUsername(updatedUser.username);
      setBio(updatedUser.bio || '');
      setPreviewPerfil(updatedUser.fotoPerfil);
      setPreviewCapa(updatedUser.fotoCapa);
      setIsDialogOpen(false);
      setUsernameError(null);
      toast.success('Perfil atualizado com sucesso!');
    } catch (error) {
      console.error('Erro ao atualizar perfil:', error);
      toast.error('Erro ao atualizar perfil. Tente novamente.');
    }
  };

  const openFollowers = async () => {
    const list = await UserService.getFollowers(user.id);
    setFollowersList(list);
    setShowFollowers(true);
  };

  const openFollowing = async () => {
    const list = await UserService.getFollowing(user.id);
    setFollowingList(list);
    setShowFollowing(true);
  };

  const handleToggleFollow = (userId: number, isNowFollowing: boolean) => {
    setFollowingIds((prev) => {
      if (isNowFollowing) return [...prev, userId];
      return prev.filter((id) => id !== userId);
    });
  };

  return (
    <Card className="pt-0 w-full bg-white rounded-lg shadow-sm overflow-hidden border border-gray-200">
      <div
        className="w-full h-50 bg-cover bg-center"
        style={{
          backgroundImage: `url(${
            previewCapa ||
            'https://via.placeholder.com/800x200?text=Capa+do+Perfil'
          })`,
        }}
      ></div>

      <div className="p-4">
        <div className="flex items-center -mt-16 ml-4 justify-between">
          <div className="flex items-start gap-6">
            <div className="flex flex-col items-center">
              <Avatar className="h-24 w-24 border-4 border-white shadow-md">
                {previewPerfil ? (
                  <AvatarImage
                    src={previewPerfil}
                    alt={`${username}'s avatar`}
                  />
                ) : (
                  <AvatarFallback>
                    {username ? username.charAt(0) : 'U'}
                  </AvatarFallback>
                )}
              </Avatar>
              <div className="flex gap-6 mt-4 mr-2">
                <button
                  onClick={openFollowers}
                  className="flex flex-col items-center cursor-pointer"
                >
                  <span className="font-bold">{user.seguidoresCount}</span>
                  <span className="text-xs text-gray-500">Seguidores</span>
                </button>
                <button
                  onClick={openFollowing}
                  className="flex flex-col items-center cursor-pointer"
                >
                  <span className="font-bold">{user.seguindoCount}</span>
                  <span className="text-xs text-gray-500">Seguindo</span>
                </button>
              </div>
            </div>
            <div className="mt-8 flex flex-col justify-center">
              <h2 className="text-2xl font-bold text-gray-800 ">{username}</h2>
              <p className="text-gray-600 text-sm mt-1">{bio}</p>
            </div>
          </div>

          <Dialog open={isDialogOpen} onOpenChange={setIsDialogOpen}>
            {me?.id === user.id && (
              <DialogTrigger asChild>
                <button
                  onClick={() => {
                    setEditData({
                      username: username,
                      bio: bio,
                      previewPerfil: previewPerfil,
                      previewCapa: previewCapa,
                    });

                    setFotoPerfil(null);
                    setFotoCapa(null);
                  }}
                  className="mr-4 p-2 rounded-full hover:bg-gray-100 transition cursor-pointer"
                >
                  <Pencil size={20} />
                </button>
              </DialogTrigger>
            )}
            <DialogContent>
              <DialogHeader>
                <DialogTitle>Editar Perfil</DialogTitle>
              </DialogHeader>

              <form onSubmit={handleSubmit} className="mt-4 space-y-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700">
                    Nome de usuário
                  </label>
                  <Input
                    type="text"
                    value={editData.username}
                    onChange={handleUsernameChange}
                    placeholder="Digite seu username (sem espaços ou acentos)"
                  />
                  {usernameError && (
                    <p className="text-red-500 text-sm mt-1">{usernameError}</p>
                  )}
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1 ">
                    Foto de Perfil
                  </label>
                  <Input
                    type="file"
                    accept="image/*"
                    onChange={handleFotoPerfilChange}
                    className="cursor-pointer "
                  />
                  {editData.previewPerfil &&
                    editData.previewPerfil !== previewPerfil && (
                      <div className="mt-2 flex flex-col">
                        <p className="text-sm text-gray-500 mb-1">Nova foto:</p>
                        <Avatar className="h-30 w-30 ">
                          <AvatarImage
                            src={editData.previewPerfil}
                            alt="Pré-visualização"
                          />
                        </Avatar>
                      </div>
                    )}
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    Foto de Capa
                  </label>
                  <Input
                    type="file"
                    accept="image/*"
                    onChange={handleFotoCapaChange}
                    className="cursor-pointer"
                  />
                  {editData.previewCapa &&
                    editData.previewCapa !== previewCapa && (
                      <div className="mt-2">
                        <p className="text-sm text-gray-500 mb-1">
                          Nova foto de capa:
                        </p>
                        <img
                          src={editData.previewCapa}
                          alt="Pré-visualização da capa"
                          className="w-full h-32 rounded-md object-cover mt-1"
                        />
                      </div>
                    )}
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700">
                    Bio
                  </label>
                  <textarea
                    placeholder="Escreva sobre você..."
                    value={editData.bio}
                    onChange={(e) =>
                      setEditData({ ...editData, bio: e.target.value })
                    }
                    className="w-full border border-gray-300 rounded p-2 resize-none"
                  />
                </div>

                <div className="flex justify-end gap-2">
                  <DialogClose asChild>
                    <Button type="button" variant="secondary">
                      Cancelar
                    </Button>
                  </DialogClose>
                  <Button type="submit" className="">
                    Salvar
                  </Button>
                </div>
              </form>
            </DialogContent>
          </Dialog>
        </div>

        <div className="mt-6 border-b border-gray-200">
          <Tabs
            defaultValue="overview"
            onValueChange={(value) => {
              if (value === 'following' && followingList.length === 0) {
                UserService.getFollowing(user.id).then((list) =>
                  setFollowingList(list)
                );
              }
              if (value === 'followers' && followersList.length === 0) {
                UserService.getFollowers(user.id).then((list) =>
                  setFollowersList(list)
                );
              }
            }}
          >
            <TabsContent
              value="overview"
              className="mt-4 space-y-4"
            >
              {!me ? (
                  <LoadingSpinner />
              ) : user.posts.length === 0 ? (
                <div className="bg-gray-50 w-full p-6 rounded-md text-center text-gray-600 border border-dashed border-gray-300">
                  Este usuário ainda não postou
                </div>
              ) : (
                  posts.map((post) => (
                  <PostCard
                    key={post.id}
                    postId={post.id}
                    username={user.username}
                    fotoPerfil={user.fotoPerfil || ''}
                    imageUrl={post.caminhoFoto || ''}
                    description={post.descricao}
                    praia={'Praia do Futuro'}
                    postOwnerId={post.usuario.id}
                    loggedUserId={me.id}
                    isFollowing={followingIds.includes(post.usuario.id)}
                    onToggleFollow={handleToggleFollow}
                    onPostDeleted={handleDeletePostFromList}
                  />
                ))
              )}
            </TabsContent>

            <TabsContent value="followers" className="mt-4 space-y-2">
              {followersList.length === 0
                ? 'Nenhum seguidor ainda'
                : followersList.map((u) => (
                    <div
                      key={u.id}
                      className="flex items-center gap-2 p-2 border-b border-gray-200"
                    >
                      <Avatar className="h-10 w-10 border border-gray-300">
                        {u.fotoPerfil ? (
                          <AvatarImage src={u.fotoPerfil} />
                        ) : (
                          <AvatarFallback>
                            {u.username.charAt(0)}
                          </AvatarFallback>
                        )}
                      </Avatar>
                      <span>{u.username}</span>
                    </div>
                  ))}
            </TabsContent>

            <TabsContent value="following" className="mt-4 space-y-2">
              {followingList.length === 0
                ? 'Não está seguindo ninguém'
                : followingList.map((u) => (
                    <div
                      key={u.id}
                      className="flex items-center gap-2 p-2 border-b border-gray-200"
                    >
                      <Avatar className="h-10 w-10 border border-gray-300">
                        {u.fotoPerfil ? (
                          <AvatarImage src={u.fotoPerfil} />
                        ) : (
                          <AvatarFallback>
                            {u.username.charAt(0)}
                          </AvatarFallback>
                        )}
                      </Avatar>
                      <span>{u.username}</span>
                    </div>
                  ))}
            </TabsContent>
          </Tabs>
        </div>

        <Dialog
          open={showFollowers}
          onOpenChange={() => setShowFollowers(false)}
        >
          <DialogContent>
            <DialogHeader>
              <DialogTitle>Seguidores</DialogTitle>
            </DialogHeader>
            <div className="space-y-2 mt-4">
              {followersList.length === 0
                ? 'Nenhum seguidor ainda'
                : followersList.map((u) => (
                    <div
                      key={u.id}
                      className="flex items-center gap-2 p-2 border-b border-gray-200"
                    >
                      <Avatar className="h-10 w-10 border border-gray-300">
                        {u.fotoPerfil ? (
                          <AvatarImage src={u.fotoPerfil} />
                        ) : (
                          <AvatarFallback>
                            {u.username.charAt(0)}
                          </AvatarFallback>
                        )}
                      </Avatar>
                      <span>{u.username}</span>
                    </div>
                  ))}
            </div>
            <DialogClose className="mt-4 px-4 py-2 bg-gray-300 rounded hover:bg-gray-400 cursor-pointer">
              Fechar
            </DialogClose>
          </DialogContent>
        </Dialog>

        <Dialog
          open={showFollowing}
          onOpenChange={() => setShowFollowing(false)}
        >
          <DialogContent>
            <DialogHeader>
              <DialogTitle>Seguindo</DialogTitle>
            </DialogHeader>
            <div className="space-y-2 mt-4">
              {followingList.length === 0
                ? 'Não está seguindo ninguém'
                : followingList.map((u) => (
                    <div
                      key={u.id}
                      className="flex items-center gap-2 p-2 border-b border-gray-200"
                    >
                      <Avatar className="h-10 w-10 border border-gray-300">
                        {u.fotoPerfil ? (
                          <AvatarImage src={u.fotoPerfil} />
                        ) : (
                          <AvatarFallback>
                            {u.username.charAt(0)}
                          </AvatarFallback>
                        )}
                      </Avatar>
                      <span>{u.username}</span>
                    </div>
                  ))}
            </div>
            <DialogClose className="mt-4 px-4 py-2 bg-gray-300 rounded hover:bg-gray-400 cursor-pointer">
              Fechar
            </DialogClose>
          </DialogContent>
        </Dialog>
      </div>
    </Card>
  );
}

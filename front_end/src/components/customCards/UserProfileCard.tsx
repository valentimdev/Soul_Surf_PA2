import { useState, type ChangeEvent, type FormEvent } from "react";
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar";
import { Card } from "@/components/ui/card";
import { Pencil } from "lucide-react";
import { Tabs, TabsList, TabsTrigger, TabsContent } from "@/components/ui/tabs";
import {
    Dialog,
    DialogTrigger,
    DialogContent,
    DialogHeader,
    DialogTitle,
    DialogClose,
} from "@/components/ui/dialog";
import { Input } from "@/components/ui/input";
import { type UserDTO, type UpdateProfileRequest, UserService } from "@/api/services/userService";

type UserProfileCardProps = {
    user: UserDTO;
};

export function UserProfileCard({ user }: UserProfileCardProps) {
    const [username, setUsername] = useState(user.username);
    const [fotoPerfil, setFotoPerfil] = useState<File | null>(null);
    const [fotoCapa, setFotoCapa] = useState<File | null>(null);
    const [previewPerfil, setPreviewPerfil] = useState(user.fotoPerfil);
    const [previewCapa, setPreviewCapa] = useState(user.fotoCapa);

    const handleFotoPerfilChange = (e: ChangeEvent<HTMLInputElement>) => {
        if (e.target.files?.[0]) {
            setFotoPerfil(e.target.files[0]);
            setPreviewPerfil(URL.createObjectURL(e.target.files[0]));
        }
    };

    const handleFotoCapaChange = (e: ChangeEvent<HTMLInputElement>) => {
        if (e.target.files?.[0]) {
            setFotoCapa(e.target.files[0]);
            setPreviewCapa(URL.createObjectURL(e.target.files[0]));
        }
    };

    const handleSubmit = async (e: FormEvent) => {
        e.preventDefault();

        const payload: UpdateProfileRequest = {
            fotoPerfil: fotoPerfil || undefined,
            fotoCapa: fotoCapa || undefined,
        };

        try {
            await UserService.updateProfile(payload);
            alert("Perfil atualizado com sucesso!");
            // opcional: atualizar o estado local do usuário
        } catch (error) {
            console.error("Erro ao atualizar perfil:", error);
            alert("Erro ao atualizar perfil");
        }
    };

    return (
        <Card className="pt-0 w-full bg-white rounded-lg shadow-sm overflow-hidden border border-gray-200">
            <div
                className="w-full h-50 bg-cover bg-center"
                style={{
                    backgroundImage: `url(${previewCapa || "https://via.placeholder.com/800x200?text=Capa+do+Perfil"})`,
                }}
            ></div>

            <div className="p-4">
                <div className="flex items-center -mt-16 ml-4 justify-between">
                    <div className="flex items-center gap-4">
                        <Avatar className="h-24 w-24 border-4 border-white shadow-md">
                            {previewPerfil ? (
                                <AvatarImage src={previewPerfil} alt={`${username}'s avatar`} />
                            ) : (
                                <AvatarFallback>{username ? username.charAt(0) : "U"}</AvatarFallback>
                            )}
                        </Avatar>

                        <div className="mt-12">
                            <h2 className="text-2xl font-bold text-gray-800">{username}</h2>
                            {/* <p className="text-gray-600 text-sm mt-1">{user.bio}</p> */} {/* futuro campo bio */}
                        </div>
                    </div>

                    {/* Botão de editar perfil */}
                    <Dialog>
                        <DialogTrigger asChild>
                            <button className="mt-12 mr-4 p-2 rounded-full hover:bg-gray-100 transition">
                                <Pencil size={20} />
                            </button>
                        </DialogTrigger>
                        <DialogContent>
                            <DialogHeader>
                                <DialogTitle>Editar Perfil</DialogTitle>
                            </DialogHeader>

                            <form onSubmit={handleSubmit} className="mt-4 space-y-4">
                                {/* Username */}
                                <div>
                                    <label className="block text-sm font-medium text-gray-700">Nome de usuário</label>
                                    <Input
                                        type="text"
                                        value={username}
                                        onChange={(e) => setUsername(e.target.value)}
                                        placeholder="Digite seu nome"
                                    />
                                </div>

                                <div>
                                    <label className="block text-sm font-medium text-gray-700 mb-1">Foto de Perfil</label>
                                    <input
                                        type="file"
                                        accept="image/*"
                                        onChange={handleFotoPerfilChange}
                                        className="cursor-pointer border border-gray-300 rounded px-3 py-2 bg-white hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                                    />
                                </div>

                                <div>
                                    <label className="block text-sm font-medium text-gray-700 mb-1">Foto de Capa</label>
                                    <input
                                        type="file"
                                        accept="image/*"
                                        onChange={handleFotoCapaChange}
                                        className="cursor-pointer border border-gray-300 rounded px-3 py-2 bg-white hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                                    />
                                </div>

                                {/* <div>
                                  <label className="block text-sm font-medium text-gray-700">Bio</label>
                                  <textarea
                                    placeholder="Escreva sobre você..."
                                    value={bio}
                                    onChange={(e) => setBio(e.target.value)}
                                    className="w-full border border-gray-300 rounded p-2"
                                  />
                                </div> */}

                                <div className="flex justify-end gap-2">
                                    <DialogClose className="px-4 py-2 bg-gray-300 text-black rounded hover:bg-gray-400">
                                        Cancelar
                                    </DialogClose>
                                    <button
                                        type="submit"
                                        className="px-4 py-2 bg-blue-500 text-white rounded hover:bg-blue-600"
                                    >
                                        Salvar
                                    </button>
                                </div>
                            </form>
                        </DialogContent>
                    </Dialog>
                </div>

                <div className="mt-6 border-b border-gray-200">
                    <Tabs defaultValue="overview">
                        <TabsList className="grid w-full grid-cols-5 h-auto">
                            <TabsTrigger value="overview">Visão Geral</TabsTrigger>
                            <TabsTrigger value="posts">Registros</TabsTrigger>
                            <TabsTrigger value="comments">Comentários</TabsTrigger>
                            <TabsTrigger value="likes">Curtidas</TabsTrigger>
                            <TabsTrigger value="followers">Marcado</TabsTrigger>
                        </TabsList>

                        <TabsContent value="overview" className="mt-4">
                            <div className="bg-gray-50 p-6 rounded-md text-center text-gray-600 border border-dashed border-gray-300">
                                Este usuário ainda não postou
                            </div>
                        </TabsContent>
                    </Tabs>
                </div>
            </div>
        </Card>
    );
}

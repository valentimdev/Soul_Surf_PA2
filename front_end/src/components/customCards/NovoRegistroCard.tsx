import {useEffect, useRef, useState} from "react";
import {Link} from "react-router-dom";
import {Loader2} from "lucide-react";
import {Button} from "@/components/ui/button";
import {Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle,} from "@/components/ui/card";
import {Input} from "@/components/ui/input";
import {Label} from "@/components/ui/label";
import {Textarea} from "@/components/ui/textarea";

import {type BeachDTO, BeachService} from "@/api/services/beachService";
import {PostService} from "@/api/services/postService";
import {MencoesService} from "@/api/services/mencoesService";
import {type PostDTO, type UserDTO, UserService} from "@/api/services/userService";

interface NovoRegistroCardProps {
    onSuccess?: (newPost: PostDTO) => void;
}

function NovoRegistroCard({ onSuccess }: NovoRegistroCardProps) {
    const [descricao, setDescricao] = useState("");
    const [foto, setFoto] = useState<File | null>(null);
    const [praias, setPraias] = useState<BeachDTO[]>([]);
    const [selectedBeachId, setSelectedBeachId] = useState<number | null>(null);
    const [loading, setLoading] = useState(false);

    const [mentionQuery, setMentionQuery] = useState("");
    const [mentionSuggestions, setMentionSuggestions] = useState<UserDTO[]>([]);
    const [showMentionBox, setShowMentionBox] = useState(false);
    const [selectedIndex, setSelectedIndex] = useState(0);
    const textareaRef = useRef<HTMLTextAreaElement>(null);

    useEffect(() => {
        BeachService.getAllBeaches()
            .then(setPraias)
            .catch((err) => console.error("Erro ao buscar praias:", err));
    }, []);

    useEffect(() => {
        if (mentionQuery.length >= 3) {
            MencoesService.getSuggestions(mentionQuery)
                .then((res) => {
                    setMentionSuggestions(res);
                    setSelectedIndex(0);
                })
                .catch(() => setMentionSuggestions([]));
        } else {
            setMentionSuggestions([]);
        }
    }, [mentionQuery]);

    const handleDescricaoChange = (e: React.ChangeEvent<HTMLTextAreaElement>) => {
        const value = e.target.value;
        setDescricao(value);

        const cursorPos = e.target.selectionStart;
        const textUntilCursor = value.slice(0, cursorPos);
        const match = /@([a-zA-Z0-9_]*)$/.exec(textUntilCursor);

        if (match) {
            const query = match[1];
            setMentionQuery(query);
            setShowMentionBox(true);
        } else {
            setShowMentionBox(false);
            setMentionQuery("");
        }
    };

    const insertMention = (username: string) => {
        if (!textareaRef.current) return;
        const cursorPos = textareaRef.current.selectionStart;
        const textBefore = descricao.slice(0, cursorPos).replace(/@[\w]*$/, `@${username} `);
        const textAfter = descricao.slice(cursorPos);
        const newText = textBefore + textAfter;
        setDescricao(newText);
        setShowMentionBox(false);
        setMentionQuery("");
        setMentionSuggestions([]);
        textareaRef.current.focus();
    };

    const handleKeyDown = (e: React.KeyboardEvent<HTMLTextAreaElement>) => {
        if (showMentionBox) {
            if (e.key === "Enter") {
                e.preventDefault();
                if (mentionSuggestions[selectedIndex]) {
                    insertMention(
                        mentionSuggestions[selectedIndex].username || mentionSuggestions[selectedIndex].email
                    );
                }
            } else if (e.key === "ArrowDown") {
                e.preventDefault();
                setSelectedIndex((prev) =>
                    prev < mentionSuggestions.length - 1 ? prev + 1 : 0
                );
            } else if (e.key === "ArrowUp") {
                e.preventDefault();
                setSelectedIndex((prev) =>
                    prev > 0 ? prev - 1 : mentionSuggestions.length - 1
                );
            }
        }
    };

    const handleSubmit = async (event: React.FormEvent) => {
        event.preventDefault();
        if (!selectedBeachId) return;

        try {
            setLoading(true);

            const createdPost = await PostService.create({
                descricao,
                publico: true,
                beachId: selectedBeachId,
                foto: foto ?? undefined,
            });

            const me = await UserService.getMe();

            const selectedBeach =
                praias.find((b) => b.id === selectedBeachId) ?? {
                    id: 0,
                    nome: "Praia desconhecida",
                    descricao: "",
                    localizacao: "",
                    caminhoFoto: "",
                };

            const tempPost: PostDTO = {
                id: createdPost.id ?? Math.random(), // temporário
                descricao,
                caminhoFoto: foto ? URL.createObjectURL(foto) : "", // nunca null
                data: new Date().toISOString(),
                usuario: createdPost.usuario ?? me,
                publico: true,
                beach: selectedBeach,
                comments: [], // inicializa vazio
            };

            setDescricao("");
            setFoto(null);
            setSelectedBeachId(null);
            window.dispatchEvent(new CustomEvent("newPost", { detail: tempPost }));
            if (onSuccess) onSuccess(tempPost);
        } catch (error) {
            console.error("Erro ao criar post:", error);
        } finally {
            setLoading(false);
        }
    };


    const handleFileChange = (event: React.ChangeEvent<HTMLInputElement>) => {
        if (event.target.files) setFoto(event.target.files[0]);
    };

    return (
        <Card className="w-full max-w-2xl border relative">
            <CardHeader>
                <CardTitle>Criar um Novo Registo</CardTitle>
                <CardDescription>
                    Partilhe a sua mais recente sessão de surf com a comunidade.
                </CardDescription>
            </CardHeader>

            <form onSubmit={handleSubmit}>
                <CardContent className="space-y-6">
                    {/* Praia */}
                    <div className="space-y-2">
                        <Label htmlFor="praia">Praia</Label>
                        <select
                            id="praia"
                            value={selectedBeachId ?? ""}
                            onChange={(e) => setSelectedBeachId(Number(e.target.value))}
                            className="w-full border rounded p-2"
                        >
                            <option value="" disabled>
                                Selecione uma praia
                            </option>
                            {praias.map((beach) => (
                                <option key={beach.id} value={beach.id}>
                                    {beach.nome}
                                </option>
                            ))}
                        </select>
                    </div>

                    {/* Descrição + menções */}
                    <div className="space-y-2 relative">
                        <Label htmlFor="descricao">Descrição</Label>
                        <Textarea
                            ref={textareaRef}
                            id="descricao"
                            placeholder="Descreva e mencione outros surfistas com @nome..."
                            value={descricao}
                            onChange={handleDescricaoChange}
                            onKeyDown={handleKeyDown}
                            className="min-h-[120px]"
                            required
                        />

                        {showMentionBox && (
                            <div
                                className="absolute bg-white border shadow-lg rounded-md w-64 p-2 z-50"
                                style={{
                                    bottom: "100%",
                                    left: "0",
                                    marginBottom: "0.5rem",
                                }}
                            >
                                {mentionQuery.length < 3 ? (
                                    <p className="text-sm text-gray-500">
                                        Digite mais {3 - mentionQuery.length} letra
                                        {3 - mentionQuery.length > 1 ? "s" : ""} para buscar...
                                    </p>
                                ) : mentionSuggestions.length > 0 ? (
                                    <ul>
                                        {mentionSuggestions.map((user, index) => (
                                            <li
                                                key={user.id}
                                                onClick={() =>
                                                    insertMention(user.username || user.email)
                                                }
                                                className={`cursor-pointer px-2 py-1 rounded text-sm transition-colors
                                                    ${
                                                    index === selectedIndex
                                                        ? "bg-blue-100 text-blue-700"
                                                        : "hover:bg-gray-100"
                                                }`}
                                            >
                                                @{user.username || user.email}
                                            </li>
                                        ))}
                                    </ul>
                                ) : (
                                    <p className="text-sm text-gray-500">
                                        Nenhum usuário encontrado.
                                    </p>
                                )}
                            </div>
                        )}
                    </div>

                    {/* Foto */}
                    <div className="space-y-2">
                        <Label htmlFor="foto">Foto</Label>
                        <Input
                            id="foto"
                            type="file"
                            accept="image/*"
                            onChange={handleFileChange}
                        />
                    </div>
                </CardContent>

                <CardFooter className="flex justify-end gap-4">
                    <Button variant="outline" asChild>
                        <Link to="/home">Cancelar</Link>
                    </Button>
                    <Button type="submit" disabled={loading}>
                        {loading ? (
                            <>
                                <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                                Publicando...
                            </>
                        ) : (
                            "Publicar Registo"
                        )}
                    </Button>
                </CardFooter>
            </form>
        </Card>
    );
}

export default NovoRegistroCard;

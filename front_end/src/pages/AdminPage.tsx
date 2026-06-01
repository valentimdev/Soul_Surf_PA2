import { useEffect, useState } from "react";
import { toast } from "sonner";
import { RefreshCw, ShieldCheck, Trash2 } from "lucide-react";
import { AdminService } from "@/api/services/adminService";
import { UserService } from "@/api/services/userService";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";

type AdminMetrics = {
    totalUsers: number;
    totalAdmins: number;
    totalBannedUsers: number;
    totalPosts: number;
    totalComments: number;
    activeAuthors: number;
};

type AuditLog = {
    id: number;
    actorEmail: string;
    action: string;
    targetType: string;
    targetId: number;
    createdAt: string;
};

function readContent<T>(raw: any): T[] {
    if (Array.isArray(raw)) return raw;
    if (Array.isArray(raw?.content)) return raw.content;
    if (Array.isArray(raw?.data)) return raw.data;
    return [];
}

export default function AdminPage() {
    const [allowed, setAllowed] = useState<boolean | null>(null);
    const [metrics, setMetrics] = useState<AdminMetrics | null>(null);
    const [audits, setAudits] = useState<AuditLog[]>([]);
    const [postId, setPostId] = useState("");
    const [commentId, setCommentId] = useState("");
    const [loading, setLoading] = useState(false);

    const loadAdminData = async () => {
        setLoading(true);
        try {
            const [metricsRes, auditsRes] = await Promise.all([
                AdminService.getMetrics(),
                AdminService.listAudits(0, 12),
            ]);
            setMetrics(metricsRes.data);
            setAudits(readContent<AuditLog>(auditsRes.data));
        } catch (error) {
            console.error("Erro ao carregar gestao:", error);
            toast.error("Nao foi possivel carregar a gestao.");
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        UserService.getMe()
            .then((user) => {
                const isAdmin = user.admin === true;
                setAllowed(isAdmin);
                if (isAdmin) void loadAdminData();
            })
            .catch(() => setAllowed(false));
    }, []);

    const clearCache = async () => {
        setLoading(true);
        try {
            await AdminService.clearCache();
            toast.success("Cache limpo.");
            await loadAdminData();
        } catch (error) {
            console.error("Erro ao limpar cache:", error);
            toast.error("Nao foi possivel limpar o cache.");
        } finally {
            setLoading(false);
        }
    };

    const deletePost = async () => {
        const id = Number(postId);
        if (!Number.isFinite(id) || id <= 0) return toast.warning("Informe um ID de post valido.");

        setLoading(true);
        try {
            await AdminService.deletePost(id);
            window.dispatchEvent(new CustomEvent("postDeleted", { detail: { id } }));
            setPostId("");
            toast.success("Post removido.");
            await loadAdminData();
        } catch (error) {
            console.error("Erro ao apagar post:", error);
            toast.error("Nao foi possivel apagar o post.");
        } finally {
            setLoading(false);
        }
    };

    const deleteComment = async () => {
        const id = Number(commentId);
        if (!Number.isFinite(id) || id <= 0) return toast.warning("Informe um ID de comentario valido.");

        setLoading(true);
        try {
            await AdminService.deleteComment(id);
            setCommentId("");
            toast.success("Comentario removido.");
            await loadAdminData();
        } catch (error) {
            console.error("Erro ao apagar comentario:", error);
            toast.error("Nao foi possivel apagar o comentario.");
        } finally {
            setLoading(false);
        }
    };

    if (allowed === null) {
        return <div className="p-6 text-gray-500">Carregando gestao...</div>;
    }

    if (!allowed) {
        return <div className="p-6 text-gray-500">Acesso restrito.</div>;
    }

    const statItems = [
        ["Usuarios", metrics?.totalUsers ?? 0],
        ["Admins", metrics?.totalAdmins ?? 0],
        ["Banidos", metrics?.totalBannedUsers ?? 0],
        ["Posts", metrics?.totalPosts ?? 0],
        ["Comentarios", metrics?.totalComments ?? 0],
        ["Autores", metrics?.activeAuthors ?? 0],
    ];

    return (
        <div className="mx-auto max-w-5xl p-6 space-y-6">
            <div className="flex flex-wrap items-center justify-between gap-3">
                <div>
                    <h1 className="text-2xl font-bold text-slate-900">Gestao</h1>
                    <p className="text-sm text-slate-500">Moderacao, cache e auditoria</p>
                </div>
                <div className="flex gap-2">
                    <Button variant="outline" onClick={loadAdminData} disabled={loading}>
                        <RefreshCw className="h-4 w-4" />
                        Atualizar
                    </Button>
                    <Button onClick={clearCache} disabled={loading}>
                        <ShieldCheck className="h-4 w-4" />
                        Limpar cache
                    </Button>
                </div>
            </div>

            <section className="grid grid-cols-2 gap-3 md:grid-cols-3">
                {statItems.map(([label, value]) => (
                    <div key={label} className="rounded-lg border bg-white p-4">
                        <p className="text-2xl font-bold text-slate-900">{value}</p>
                        <p className="text-sm text-slate-500">{label}</p>
                    </div>
                ))}
            </section>

            <section className="grid gap-4 md:grid-cols-2">
                <div className="rounded-lg border bg-white p-4 space-y-3">
                    <h2 className="font-semibold text-slate-900">Posts</h2>
                    <div className="flex gap-2">
                        <Input
                            value={postId}
                            onChange={(event) => setPostId(event.target.value)}
                            placeholder="ID do post"
                            inputMode="numeric"
                        />
                        <Button variant="destructive" onClick={deletePost} disabled={loading}>
                            <Trash2 className="h-4 w-4" />
                            Apagar
                        </Button>
                    </div>
                </div>

                <div className="rounded-lg border bg-white p-4 space-y-3">
                    <h2 className="font-semibold text-slate-900">Comentarios</h2>
                    <div className="flex gap-2">
                        <Input
                            value={commentId}
                            onChange={(event) => setCommentId(event.target.value)}
                            placeholder="ID do comentario"
                            inputMode="numeric"
                        />
                        <Button variant="destructive" onClick={deleteComment} disabled={loading}>
                            <Trash2 className="h-4 w-4" />
                            Apagar
                        </Button>
                    </div>
                </div>
            </section>

            <section className="rounded-lg border bg-white p-4">
                <h2 className="mb-3 font-semibold text-slate-900">Auditoria</h2>
                {audits.length === 0 ? (
                    <p className="text-sm text-slate-500">Nenhum registro encontrado.</p>
                ) : (
                    <div className="divide-y">
                        {audits.map((audit) => (
                            <div key={audit.id} className="flex items-center justify-between gap-4 py-3 text-sm">
                                <div>
                                    <p className="font-medium text-slate-900">{audit.action}</p>
                                    <p className="text-slate-500">
                                        {audit.targetType} #{audit.targetId} - {audit.actorEmail}
                                    </p>
                                </div>
                                <p className="shrink-0 text-xs text-slate-500">
                                    {audit.createdAt ? new Date(audit.createdAt).toLocaleString("pt-BR") : ""}
                                </p>
                            </div>
                        ))}
                    </div>
                )}
            </section>
        </div>
    );
}

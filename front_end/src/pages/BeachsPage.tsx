import { useCallback, useEffect, useState } from "react";
import { BeachService, type BeachDTO } from "@/api/services/beachService";
import { BeachCard } from "@/components/customCards/BeachCard";
import NovoPraiaCard from "@/components/customCards/NovaPraiaCard";

function BeachsPage() {
    const [beaches, setBeaches] = useState<BeachDTO[]>([]);
    const [open, setOpen] = useState(false);
    const [loading, setLoading] = useState(true);

    const loadBeaches = useCallback(() => {
        setLoading(true);
        BeachService.getAllBeaches()
            .then(setBeaches)
            .catch((err) => console.error("Erro ao carregar praias:", err))
            .finally(() => setLoading(false));
    }, []);

    useEffect(() => {
        loadBeaches();
        window.addEventListener("beachCreated", loadBeaches);
        window.addEventListener("focus", loadBeaches);

        return () => {
            window.removeEventListener("beachCreated", loadBeaches);
            window.removeEventListener("focus", loadBeaches);
        };
    }, [loadBeaches]);

    return (
        <div className="flex flex-col ml-[10%] mr-[10%] mt-5">
            <div className="flex justify-between items-center">
                <h1 className="ml-5 text-2xl font-bold">Praias</h1>
            </div>

            {open && (
                <NovoPraiaCard
                    onSuccess={() => {
                        loadBeaches();
                        setOpen(false);
                    }}
                />
            )}

            {loading ? (
                <p className="ml-5 mt-5 text-gray-500">Carregando praias...</p>
            ) : beaches.length === 0 ? (
                <p className="ml-5 mt-5 text-gray-500">Nenhuma praia cadastrada.</p>
            ) : (
                <div className="ml-5 mt-5 grid grid-cols-1 md:grid-cols-2 gap-5">
                    {beaches.map((beach) => (
                        <BeachCard key={beach.id} beach={beach} />
                    ))}
                </div>
            )}
        </div>
    );
}

export default BeachsPage;

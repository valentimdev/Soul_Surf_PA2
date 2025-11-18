import { useEffect, useState } from "react";
import { BeachService, type BeachDTO } from "@/api/services/beachService";
import { BeachCard } from "@/components/customCards/BeachCard";
import NovoPraiaCard from "@/components/customCards/NovaPraiaCard";
import {Button} from "@/components/ui/button.tsx";

function BeachsPage() {
    const [beaches, setBeaches] = useState<BeachDTO[]>([]);
    const [open, setOpen] = useState(false);

    const loadBeaches = () => {
        BeachService.getAllBeaches()
            .then(setBeaches)
            .catch((err) => console.error("Erro ao carregar praias:", err));
    };

    useEffect(() => {
        loadBeaches();
    }, []);

    return (
        <div className="flex flex-col ml-[10%] mr-[10%] mt-5">
            <div className="flex justify-between items-center">
                <h1 className="ml-5 text-2xl font-bold">Praias</h1>

                <Button onClick={() => setOpen(true)}>Nova Praia</Button>
            </div>

            {open && (
                <NovoPraiaCard
                    onSuccess={() => {
                        loadBeaches();
                        setOpen(false);
                    }}
                />
            )}

            <div className="ml-5 mt-5 grid grid-cols-1 md:grid-cols-2 gap-5">
                {beaches.map((beach) => (
                    <BeachCard key={beach.id} beach={beach} />
                ))}
            </div>
        </div>
    );
}

export default BeachsPage;

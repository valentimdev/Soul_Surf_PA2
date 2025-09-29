import { useEffect, useState } from "react";
import { BeachService, type BeachDTO } from "@/api/services/beachService";
import {BeachCard} from "@/components/customCards/BeachCard.tsx";

function BeachsPage() {
    const [beaches, setBeaches] = useState<BeachDTO[]>([]);

    useEffect(() => {
        BeachService.getAllBeaches()
            .then(setBeaches)
            .catch((err) => console.error("Erro ao carregar praias:", err));
    }, []);

    return (
        <div className="flex flex-col ml-[10%] mr-[10%] mt-5">
            <div>
                <h1 className="ml-5 text-2xl font-bold">Praias</h1>
            </div>
            <div className="ml-5 mt-5 grid grid-cols-1 md:grid-cols-2 gap-5">
                {beaches.map((beach) => (
                    <BeachCard key={beach.id} beach={beach} />
                ))}
            </div>
        </div>
    );
}

export default BeachsPage;

import { useNavigate } from "react-router-dom";
import { ArrowLeft } from "lucide-react";

export function BackArrow() {
    const navigate = useNavigate();

    return (
        <button
            onClick={() => navigate(-1)}
            className="flex items-center gap-1 text-white bg-gray-700 bg-opacity-50 p-2 rounded-full hover:bg-opacity-70 transition z-10"
        >
            <ArrowLeft className="w-5 h-5" />
            <span className="hidden sm:inline">Voltar</span>
        </button>
    );
}
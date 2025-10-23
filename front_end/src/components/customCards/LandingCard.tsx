import soulSurfIcon from "@/assets/header/SoulSurfIconAzul.png";
import { Button, Card, CardContent } from "@/components/ui/application";
import { CheckCircle2 } from "lucide-react";
import { useNavigate } from "react-router-dom";

function LandingCard() {
    const navigate = useNavigate();
    return (
        <div className="h-full flex items-center justify-center py-10">
            <Card className="w-full max-w-md shadow-none border-0 mt-[-50px]">
                <CardContent className="flex flex-col items-center gap-6 p-6">
                    <img src={soulSurfIcon} alt="Soul Surf Logo" className="h-75 w-auto" />

                    <p className="text-center text-lg italic">
                        Vem pegar essa onda!
                    </p>

                    <div className="flex flex-col gap-4 w-full">
                        <div className="flex items-center gap-3">
                            <CheckCircle2 className="text-blue-500" size={28} />
                            <span className="text-lg font-semibold">Acompanhe seu progresso nas ondas</span>
                        </div>
                        <div className="flex items-center gap-3">
                            <CheckCircle2 className="text-blue-500" size={28} />
                            <span className="text-lg font-semibold">Comunidades Ativas de Surf em Fortaleza!</span>
                        </div>
                        <div className="flex items-center gap-3">
                            <CheckCircle2 className="text-blue-500" size={28} />
                            <span className="text-lg font-semibold">Fortaleça sua comunidade no esporte</span>
                        </div>
                    </div>

                    <Button
                        className="w-full py-8 text-lg font-bold hover:cursor-pointer"
                        onClick={() => navigate("/login")}
                    >
                        Entrar
                    </Button>
                </CardContent>
            </Card>
        </div>
    );
}

export default LandingCard;

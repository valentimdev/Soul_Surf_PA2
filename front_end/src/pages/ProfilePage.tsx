import { useEffect, useState } from "react";
import { UserService, type UserDTO } from "@/api/services/userService";
import { UserProfileCard } from "@/components/customCards/UserProfileCard";

function ProfilePage() {
    const [userData, setUserData] = useState<UserDTO | null>(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const fetchUser = async () => {
            try {
                const user = await UserService.getMe();
                setUserData(user);
            } catch (error) {
                console.error("Erro ao carregar perfil:", error);
            } finally {
                setLoading(false);
            }
        };

        fetchUser();
    }, []);

    if (loading) {
        return <div className="text-center py-10">Carregando perfil...</div>;
    }

    if (!userData) {
        return <div className="text-center py-10">Usuário não encontrado</div>;
    }

    return (
        <div className="p-4 space-y-6">
            <UserProfileCard user={userData} />
        </div>
    );
}

export default ProfilePage;

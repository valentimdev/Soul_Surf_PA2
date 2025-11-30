import { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import { UserService, type UserDTO } from "@/api/services/userService";
import { UserProfileCard } from "@/components/customCards/UserProfileCard";

function ProfilePage() {
    const { username } = useParams();
    const [userData, setUserData] = useState<UserDTO | null>(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const loadUser = async () => {
            try {
                const user = username
                    ? await UserService.getUserByUsername(username)
                    : await UserService.getMe();
                setUserData(user);
            } catch {
                setUserData(null);
            } finally {
                setLoading(false);
            }
        };
        loadUser();
    }, [username]);

    if (loading) return <div className="text-center py-10">Carregando perfil...</div>;
    if (!userData) return <div className="text-center py-10">Usuário não encontrado</div>;

    return (
      <div className="w-full max-w-2xl mx-auto p-4 space-y-6">
            <UserProfileCard user={userData} />
        </div>
    );
}

export default ProfilePage;

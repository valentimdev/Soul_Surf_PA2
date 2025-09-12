import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar";
import { Tabs, TabsList, TabsTrigger, TabsContent } from "@/components/ui/tabs";
import { Card } from "@/components/ui/card";

export function UserProfileCard({ username, avatarSrc, coverImageSrc }) {
  return (
    <Card className="pt-0 w-full bg-white rounded-lg shadow-sm overflow-hidden border border-gray-200">
      {/* Imagem de Capa */}
      <div 
        className="w-full h-50 bg-cover bg-center" 
        style={{ backgroundImage: `url(${coverImageSrc || 'https://via.placeholder.com/800x200?text=Capa+do+Perfil'})` }}
      ></div>

      <div className="p-4">
        <div className="flex items-center -mt-16 ml-4">
          {/* Avatar do Usuário */}
          <Avatar className="h-24 w-24 border-4 border-white shadow-md">
            <AvatarImage src={avatarSrc || 'https://via.placeholder.com/150'} alt={`${username}'s avatar`} />
            <AvatarFallback>{username ? username.charAt(0) : 'U'}</AvatarFallback>
          </Avatar>

          {/* Nome e Handle do Usuário */}
          <div className="ml-4 mt-12">
            <h2 className="text-2xl font-bold text-gray-800">{username || "nome_do_usuario"}</h2>
          </div>
        </div>

        {/* Abas de Navegação (Visão Geral, Comentários, etc.) */}
        <div className="mt-6 border-b border-gray-200">
          <Tabs defaultValue="overview">
            <TabsList className="grid w-full grid-cols-5 h-auto">
              <TabsTrigger value="overview" className="flex-grow">Visão Geral</TabsTrigger>
              <TabsTrigger value="posts" className="flex-grow">Registros</TabsTrigger>
              <TabsTrigger value="comments" className="flex-grow">Comentários</TabsTrigger>
              <TabsTrigger value="likes" className="flex-grow">Curtidas</TabsTrigger>
              <TabsTrigger value="followers" className="flex-grow">Marcado</TabsTrigger>
            </TabsList>
            <TabsContent value="overview" className="mt-4">
              <div className="bg-gray-50 p-6 rounded-md text-center text-gray-600 border border-dashed border-gray-300">
                Este usuário ainda não postou
              </div>
            </TabsContent>
            {/* Você pode adicionar mais TabsContent para os outros valores conforme necessário */}
          </Tabs>
        </div>
      </div>
    </Card>
  );
}
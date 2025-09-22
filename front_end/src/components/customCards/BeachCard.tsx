import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Avatar, AvatarImage, AvatarFallback } from "@/components/ui/avatar";

const beaches = [
  {
    name: "Praia do Futuro",
    img: "https://upload.wikimedia.org/wikipedia/commons/0/09/Praia_do_Futuro%2C_Fortaleza.jpg",
  },
  {
    name: "Iracema",
    img: "https://upload.wikimedia.org/wikipedia/commons/4/4a/Praia_de_Iracema_-_Fortaleza.jpg",
  },
  {
    name: "Meireles",
    img: "https://upload.wikimedia.org/wikipedia/commons/e/eb/Praia_de_Meireles_-_Fortaleza.jpg",
  },
];

export function BeachCard() {
  return (
    <Card className="w-full rounded-2xl shadow-sm">
      <CardHeader>
        <CardTitle className="text-lg font-semibold">🌊 Praias de Fortaleza</CardTitle>
      </CardHeader>
      <CardContent className="space-y-4">
        {beaches.map((beach) => (
          <div key={beach.name} className="flex items-center justify-between">
            <div className="flex items-center gap-3">
              <Avatar className="w-10 h-10">
                <AvatarImage src={beach.img} className="rounded-full object-cover" />
                <AvatarFallback>{beach.name[0]}</AvatarFallback>
              </Avatar>
              <span className="font-medium">{beach.name}</span>
            </div>
            <Button variant="secondary" size="sm">
              Seguir
            </Button>
          </div>
        ))}
      </CardContent>
    </Card>
  );
}

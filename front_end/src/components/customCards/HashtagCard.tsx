import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";

const communities = [
  "#pflovers",
  "#acaipossurf",
  "#sereias",
  "#pedalPreSurf",
  "#crowdeado",
  "#fotografia",
  "#longboard",
  "#bodyboard",
  "#standup",
];

export function HashtagCard() {
  return (
    <Card className="w-full h-64 rounded-2xl shadow-sm">
      <CardHeader>
        <CardTitle className="text-lg font-semibold">Hashtags em alta</CardTitle>
      </CardHeader>
      <CardContent className="space-y-3 overflow-y-auto h-[180px] pr-2">
        {communities.map((community) => (
          <div
            key={community}
            className="flex items-center justify-between"
          >
            <span className="font-medium text-sm">{community}</span>
            <Button variant="secondary" size="sm">
              Seguir
            </Button>
          </div>
        ))}
      </CardContent>
    </Card>
  );
}

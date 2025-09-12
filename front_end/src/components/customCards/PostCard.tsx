import { Avatar } from '@/components/ui/avatar';
import { Button } from '@/components/ui/button';
import {
  Card,
  CardContent,
  CardFooter,
  CardHeader,
} from '@/components/ui/card';
import { Heart, MessageCircle, Send } from 'lucide-react';

export function PostCard() {
  return (
    <Card className="w-80% max-w-xl mx-auto">
      <CardHeader className="flex flex-row items-center gap-3 p-4">
        <Avatar>
          {/* Placeholder for user avatar */}
          <div className="bg-gray-300 rounded-full w-10 h-10" />
        </Avatar>
        <span className="font-semibold">nome_do_usuario</span>
      </CardHeader>
      <CardContent className="p-0">
        {/* Placeholder for post image */}
        <div className="bg-gray-200 w-80% aspect-[4/3]" />
      </CardContent>
      <CardFooter className="flex flex-col items-start p-4">
        <div className="flex gap-4">
          <Button variant="ghost" size="icon">
            <Heart className="h-6 w-6" />
          </Button>
          <Button variant="ghost" size="icon">
            <MessageCircle className="h-6 w-6" />
          </Button>
          <Button variant="ghost" size="icon">
            <Send className="h-6 w-6" />
          </Button>
        </div>
        <div className="mt-2">
          <p className="text-sm">
            <span className="font-semibold">nome_do_usuario</span> Descrição do
            post aqui...
          </p>
        </div>
        <div className="mt-2 text-xs text-gray-500">
          Ver todos os comentários
        </div>
      </CardFooter>
    </Card>
  );
}

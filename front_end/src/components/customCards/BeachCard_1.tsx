import React from 'react';
import { Card, CardContent, CardHeader, CardTitle, CardFooter } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Avatar, AvatarImage, AvatarFallback } from '@/components/ui/avatar';

const beaches = [
  {
    name: 'Praia do Futuro',
    img: 'https://upload.wikimedia.org/wikipedia/commons/0/09/Praia_do_Futuro%2C_Fortaleza.jpg',
  },
  {
    name: 'Iracema',
    img: 'https://upload.wikimedia.org/wikipedia/commons/4/4a/Praia_de_Iracema_-_Fortaleza.jpg',
  },
  {
    name: 'Meireles',
    img: 'https://upload.wikimedia.org/wikipedia/commons/e/eb/Praia_de_Meireles_-_Fortaleza.jpg',
  },
];

export function BeachCardTeste() {
  return (
    <Card className="w-[90%] rounded-2xl shadow-sm h-84">
      <CardHeader>
        <CardTitle className="text-lg font-semibold">
          Praia de Iracema
        </CardTitle>
      </CardHeader>
      <img
        src="https://dicasdefortalezaejeri.com.br/wp-content/uploads/sites/29/2021/03/praia-iracema-fortaleza-jpg.webp"
        alt="Foto da Praia de Iracema"
        className="w-full aspect-video object-cover" // Classes para ajustar a imagem
      />
      <CardFooter>
        <h1>Av. Historiador Raimundo Girão, 800</h1>
      </CardFooter>
    </Card>
  );
}

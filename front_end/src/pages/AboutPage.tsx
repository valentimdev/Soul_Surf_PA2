// src/pages/AboutPage.tsx
import React from 'react';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Separator } from '@/components/ui/separator';
import { Avatar, AvatarFallback, AvatarImage } from '@/components/ui/avatar';
import { Globe, Instagram, Mail } from 'lucide-react'; // Ícones, se você usa lucide-react

function AboutPage() {
    return (
        <div className="container mx-auto py-8 px-4 sm:px-6 lg:px-8">
            <h1 className="text-4xl font-extrabold text-center text-primary mb-10 animate-fade-in-down">
                Sobre o Soul Surf
            </h1>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-8 mb-12">
                {/* Seção 1: Nossa Missão */}
                <Card className="shadow-lg hover:shadow-xl transition-shadow duration-300">
                    <CardHeader>
                        <CardTitle className="text-2xl font-bold text-secondary">Nossa Missão</CardTitle>
                    </CardHeader>
                    <CardContent>
                        <p className="text-gray-700 dark:text-gray-300 leading-relaxed">
                            No Soul Surf, nossa missão é conectar entusiastas do surf de todo o Brasil, criando uma comunidade vibrante e inspiradora. Acreditamos que o surf é mais do que um esporte; é um estilo de vida que une as pessoas à natureza e entre si.
                            Queremos ser a plataforma onde você pode compartilhar suas ondas, descobrir novos picos, conectar-se com outros surfistas e organizar seus registros de surf, tudo em um só lugar.
                        </p>
                    </CardContent>
                </Card>

                {/* Seção 2: O Que Oferecemos */}
                <Card className="shadow-lg hover:shadow-xl transition-shadow duration-300">
                    <CardHeader>
                        <CardTitle className="text-2xl font-bold text-secondary">O Que Oferecemos</CardTitle>
                    </CardHeader>
                    <CardContent>
                        <ul className="list-disc list-inside text-gray-700 dark:text-gray-300 space-y-2">
                            <li>
                                <span className="font-semibold">Comunidade:</span> Conecte-se com surfistas, compartilhe experiências e encontre parceiros para sua próxima sessão.
                            </li>
                            <li>
                                <span className="font-semibold">Registros de Surf:</span> Organize e visualize seus registros de surf com detalhes como data, local, condições e desempenho.
                            </li>
                            <li>
                                <span className="font-semibold">Descoberta de Praias:</span> Explore informações sobre praias, suas ondas e dicas da comunidade.
                            </li>
                            <li>
                                <span className="font-semibold">Compartilhamento de Posts:</span> Publique fotos e vídeos de suas ondas e interaja com os posts de outros.
                            </li>
                        </ul>
                    </CardContent>
                </Card>
            </div>

            <Separator className="my-12 bg-accent" />

            <h2 className="text-3xl font-bold text-center text-secondary mb-8 animate-fade-in-up">
                Nossa Equipe (Exemplo)
            </h2>

            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-8 justify-items-center">
                {/* Membro da Equipe 1 */}
                <Card className="flex flex-col items-center p-6 shadow-md hover:shadow-lg transition-shadow duration-300 w-full max-w-xs text-center">
                    <Avatar className="w-24 h-24 mb-4">
                        {/* Você pode substituir esta imagem por uma real */}
                        <AvatarImage src="https://example.com/thiago_avatar.jpg" alt="Thiago Dev" />
                        <AvatarFallback className="bg-primary text-primary-foreground text-xl">TD</AvatarFallback>
                    </Avatar>
                    <h3 className="text-xl font-semibold mb-2">Thiago Silva</h3>
                    <p className="text-sm text-gray-600 dark:text-gray-400 mb-3">Desenvolvedor Líder & Visionário</p>
                    <p className="text-xs text-gray-500 dark:text-gray-400 mb-4">
                        Apaixonado por surf e tecnologia, Thiago idealizou o Soul Surf para unir suas duas grandes paixões.
                    </p>
                    <div className="flex gap-3">
                        <a href="https://www.instagram.com/seuinstagram" target="_blank" rel="noopener noreferrer" className="text-gray-500 hover:text-pink-500 transition-colors">
                            <Instagram size={20} />
                        </a>
                        <a href="mailto:thiago.dev@example.com" className="text-gray-500 hover:text-blue-500 transition-colors">
                            <Mail size={20} />
                        </a>
                    </div>
                </Card>

                {/* Membro da Equipe 2 (Exemplo) */}
                <Card className="flex flex-col items-center p-6 shadow-md hover:shadow-lg transition-shadow duration-300 w-full max-w-xs text-center">
                    <Avatar className="w-24 h-24 mb-4">
                        <AvatarImage src="https://example.com/ana_avatar.jpg" alt="Ana Marketing" />
                        <AvatarFallback className="bg-green-500 text-white text-xl">AA</AvatarFallback>
                    </Avatar>
                    <h3 className="text-xl font-semibold mb-2">Ana Almeida</h3>
                    <p className="text-sm text-gray-600 dark:text-gray-400 mb-3">Estrategista de Comunidade</p>
                    <p className="text-xs text-gray-500 dark:text-gray-400 mb-4">
                        Responsável por engajar nossa comunidade e espalhar a paixão pelo surf online e offline.
                    </p>
                    <div className="flex gap-3">
                        <a href="https://www.instagram.com/ana.soul" target="_blank" rel="noopener noreferrer" className="text-gray-500 hover:text-pink-500 transition-colors">
                            <Instagram size={20} />
                        </a>
                        <a href="mailto:ana.almeida@example.com" className="text-gray-500 hover:text-blue-500 transition-colors">
                            <Mail size={20} />
                        </a>
                    </div>
                </Card>

                {/* Membro da Equipe 3 (Exemplo) */}
                <Card className="flex flex-col items-center p-6 shadow-md hover:shadow-lg transition-shadow duration-300 w-full max-w-xs text-center">
                    <Avatar className="w-24 h-24 mb-4">
                        <AvatarImage src="https://example.com/carlos_avatar.jpg" alt="Carlos Designer" />
                        <AvatarFallback className="bg-blue-500 text-white text-xl">CS</AvatarFallback>
                    </Avatar>
                    <h3 className="text-xl font-semibold mb-2">Carlos Souza</h3>
                    <p className="text-sm text-gray-600 dark:text-gray-400 mb-3">Designer de UX/UI</p>
                    <p className="text-xs text-gray-500 dark:text-gray-400 mb-4">
                        O gênio por trás da interface intuitiva e visualmente deslumbrante do Soul Surf.
                    </p>
                    <div className="flex gap-3">
                        <a href="https://www.instagram.com/carlos.ux" target="_blank" rel="noopener noreferrer" className="text-gray-500 hover:text-pink-500 transition-colors">
                            <Instagram size={20} />
                        </a>
                        <a href="mailto:carlos.souza@example.com" className="text-gray-500 hover:text-blue-500 transition-colors">
                            <Mail size={20} />
                        </a>
                    </div>
                </Card>
            </div>

            <Separator className="my-12 bg-accent" />

            <Card className="p-6 text-center shadow-lg hover:shadow-xl transition-shadow duration-300">
                <CardTitle className="text-2xl font-bold text-secondary mb-4">Entre em Contato</CardTitle>
                <CardContent className="flex flex-col items-center justify-center p-0">
                    <p className="text-gray-700 dark:text-gray-300 mb-4">
                        Tem alguma dúvida, sugestão ou quer saber mais? Adoraríamos ouvir você!
                    </p>
                    <a
                        href="mailto:contato@soulsurf.com.br"
                        className="inline-flex items-center gap-2 text-primary hover:text-primary/80 transition-colors text-lg font-semibold"
                    >
                        <Mail size={20} /> contato@soulsurf.com.br
                    </a>
                    <p className="mt-4 text-sm text-gray-500 dark:text-gray-400">
                        Siga-nos nas redes sociais para ficar por dentro das últimas ondas!
                    </p>
                    <div className="flex gap-4 mt-2">
                        <a href="https://www.instagram.com/soulsurf" target="_blank" rel="noopener noreferrer" className="text-gray-500 hover:text-pink-500 transition-colors">
                            <Instagram size={28} />
                        </a>
                        <a href="https://www.soulsurf.com.br" target="_blank" rel="noopener noreferrer" className="text-gray-500 hover:text-blue-600 transition-colors">
                            <Globe size={28} />
                        </a>
                    </div>
                </CardContent>
            </Card>
        </div>
    );
}

export default AboutPage;
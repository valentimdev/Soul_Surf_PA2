import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Waves, Users, MapPin, Camera, Heart, ArrowLeft } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import soulSurfIcon from '@/assets/header/SoulSurfIconAzul.png';

function AboutPage() {
  const navigate = useNavigate();

  return (
    <div className="min-h-screen bg-gradient-to-b from-blue-50 to-white dark:from-gray-900 dark:to-gray-800">
      {/* Header */}
      <div className="container mx-auto px-4 py-8">
        <Button
          variant="ghost"
          onClick={() => navigate('/')}
          className="mb-6 hover:bg-blue-100 dark:hover:bg-gray-700"
        >
          <ArrowLeft className="mr-2 h-4 w-4" />
          Voltar para Home
        </Button>

        {/* Hero Section */}
        <div className="text-center mb-16">
          <div className="flex justify-center mb-6">
            <img
              src={soulSurfIcon}
              alt="Soul Surf Logo"
              className="h-20 w-auto"
            />
          </div>
          <h1 className="text-5xl font-bold text-gray-900 dark:text-white mb-4">
            Soul Surf
          </h1>
          <p className="text-xl text-gray-600 dark:text-gray-300 max-w-2xl mx-auto">
            A rede social definitiva para surfistas apaixonados. Conecte-se,
            compartilhe e viva o surf como nunca antes.
          </p>
        </div>

        {/* Features Grid */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-16">
          <Card className="text-center p-6 hover:shadow-lg transition-shadow border-blue-100 dark:border-gray-700">
            <div className="bg-blue-100 dark:bg-blue-900 w-16 h-16 rounded-full flex items-center justify-center mx-auto mb-4">
              <Users className="h-8 w-8 text-blue-600 dark:text-blue-300" />
            </div>
            <h3 className="font-semibold text-lg mb-2 text-gray-900 dark:text-white">
              Comunidade
            </h3>
            <p className="text-gray-600 dark:text-gray-300 text-sm">
              Conecte-se com surfistas locais e encontre parceiros para suas
              sessões
            </p>
          </Card>

          <Card className="text-center p-6 hover:shadow-lg transition-shadow border-blue-100 dark:border-gray-700">
            <div className="bg-green-100 dark:bg-green-900 w-16 h-16 rounded-full flex items-center justify-center mx-auto mb-4">
              <Waves className="h-8 w-8 text-green-600 dark:text-green-300" />
            </div>
            <h3 className="font-semibold text-lg mb-2 text-gray-900 dark:text-white">
              Registros
            </h3>
            <p className="text-gray-600 dark:text-gray-300 text-sm">
              Organize suas sessões de surf e acompanhe seu progresso
            </p>
          </Card>

          <Card className="text-center p-6 hover:shadow-lg transition-shadow border-blue-100 dark:border-gray-700">
            <div className="bg-purple-100 dark:bg-purple-900 w-16 h-16 rounded-full flex items-center justify-center mx-auto mb-4">
              <MapPin className="h-8 w-8 text-purple-600 dark:text-purple-300" />
            </div>
            <h3 className="font-semibold text-lg mb-2 text-gray-900 dark:text-white">
              Praias
            </h3>
            <p className="text-gray-600 dark:text-gray-300 text-sm">
              Descubra novos picos e compartilhe informações sobre ondas
            </p>
          </Card>

          <Card className="text-center p-6 hover:shadow-lg transition-shadow border-blue-100 dark:border-gray-700">
            <div className="bg-orange-100 dark:bg-orange-900 w-16 h-16 rounded-full flex items-center justify-center mx-auto mb-4">
              <Camera className="h-8 w-8 text-orange-600 dark:text-orange-300" />
            </div>
            <h3 className="font-semibold text-lg mb-2 text-gray-900 dark:text-white">
              Compartilhe
            </h3>
            <p className="text-gray-600 dark:text-gray-300 text-sm">
              Publique fotos e vídeos das suas melhores ondas
            </p>
          </Card>
        </div>

        {/* Main Content */}
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8 mb-16">
          {/* Nossa História */}
          <Card className="lg:col-span-2 p-8 border-blue-100 dark:border-gray-700">
            <CardHeader className="p-0 mb-6">
              <CardTitle className="text-3xl font-bold text-gray-900 dark:text-white flex items-center">
                <Heart className="mr-3 h-8 w-8 text-red-500" />
                Nossa História
              </CardTitle>
            </CardHeader>
            <CardContent className="p-0">
              <div className="space-y-4 text-gray-600 dark:text-gray-300 leading-relaxed">
                <p>
                  O Soul Surf nasceu da paixão de surfistas que queriam algo
                  mais que apenas redes sociais comuns. Queríamos um lugar onde
                  a cultura do surf pudesse florescer, onde cada onda contasse
                  uma história e cada surfista pudesse encontrar sua tribo.
                </p>
                <p>
                  Desenvolvido especialmente para a vibrante cena do surf em
                  Fortaleza e região, o Soul Surf se tornou o ponto de encontro
                  digital dos amantes das ondas. Aqui, você não apenas
                  compartilha fotos - você constrói memórias, planeja sessões e
                  fortalece os laços da comunidade surfista.
                </p>
                <p>
                  Nossa missão é simples: manter vivo o espírito do surf,
                  conectar pessoas que compartilham a mesma paixão e criar um
                  espaço onde cada onda é celebrada.
                </p>
              </div>
            </CardContent>
          </Card>

          {/* Estatísticas */}
          <Card className="p-8 bg-gradient-to-br from-blue-500 to-blue-600 text-white border-0">
            <CardHeader className="p-0 mb-6">
              <CardTitle className="text-2xl font-bold">
                Por que escolher o Soul Surf?
              </CardTitle>
            </CardHeader>
            <CardContent className="p-0">
              <div className="space-y-6">
                <div className="text-center">
                  <div className="text-3xl font-bold">100%</div>
                  <div className="text-blue-100">Focado no Surf</div>
                </div>
                <div className="text-center">
                  <div className="text-3xl font-bold">24/7</div>
                  <div className="text-blue-100">Comunidade Ativa</div>
                </div>
                <div className="text-center">
                  <div className="text-3xl font-bold">Local</div>
                  <div className="text-blue-100">Feito para Fortaleza</div>
                </div>
                <div className="text-center">
                  <div className="text-3xl font-bold">Gratuito</div>
                  <div className="text-blue-100">Sempre será</div>
                </div>
              </div>
            </CardContent>
          </Card>
        </div>

        {/* Call to Action */}
        <Card className="text-center p-12 bg-gradient-to-r from-blue-600 to-purple-600 text-white border-0">
          <CardContent className="p-0">
            <h2 className="text-3xl font-bold mb-4">
              Pronto para entrar nessa onda?
            </h2>
            <p className="text-xl mb-8 text-blue-100">
              Junte-se à maior comunidade de surfistas do Ceará e comece a
              compartilhar suas ondas hoje mesmo!
            </p>
            <div className="flex flex-col sm:flex-row gap-4 justify-center">
              <Button
                size="lg"
                variant="secondary"
                onClick={() => navigate('/cadastro')}
                className="bg-white text-blue-600 hover:bg-gray-100"
              >
                Criar Conta Gratuita
              </Button>
              <Button
                size="lg"
                variant="secondary"
                onClick={() => navigate('/login')}
               className="bg-white text-blue-600 hover:bg-gray-100"
              >
                Já tenho conta
              </Button>
            </div>
          </CardContent>
        </Card>
      </div>
    </div>
  );
}

export default AboutPage;

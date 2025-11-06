import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { FileText, X } from 'lucide-react';

interface TermsOfServiceCardProps {
  onClose?: () => void;
}

function TermsOfServiceCard({ onClose }: TermsOfServiceCardProps) {
  return (
    <Card className="w-full max-w-4xl mx-auto border-0 shadow-none">
      <CardHeader className="flex flex-row items-center justify-between p-6 border-b">
        <div className="flex items-center gap-3">
          <div className="bg-blue-100 dark:bg-blue-900 p-2 rounded-full">
            <FileText className="h-6 w-6 text-blue-600 dark:text-blue-300" />
          </div>
          <CardTitle className="text-2xl font-bold text-gray-900 dark:text-white">
            Termos de Uso - Soul Surf
          </CardTitle>
        </div>
        {onClose && (
          <Button variant="ghost" size="icon" onClick={onClose}>
            <X className="h-5 w-5" />
          </Button>
        )}
      </CardHeader>

      <CardContent className="p-0">
        <div className="h-[500px] overflow-y-auto p-6">
          <div className="space-y-6 text-gray-700 dark:text-gray-300">
            <div className="text-sm text-gray-500 dark:text-gray-400 mb-6">
              <strong>Última atualização:</strong> 6 de novembro de 2025
            </div>

            <section>
              <h3 className="text-xl font-semibold text-gray-900 dark:text-white mb-3">
                1. Aceitação dos Termos
              </h3>
              <p className="leading-relaxed">
                Ao acessar e usar o Soul Surf, você concorda em cumprir e estar
                vinculado a estes Termos de Uso. Se você não concordar com
                qualquer parte destes termos, não deve usar nosso serviço.
              </p>
            </section>

            <section>
              <h3 className="text-xl font-semibold text-gray-900 dark:text-white mb-3">
                2. Descrição do Serviço
              </h3>
              <p className="leading-relaxed mb-3">
                O Soul Surf é uma rede social dedicada à comunidade de surf,
                permitindo que usuários:
              </p>
              <ul className="list-disc pl-6 space-y-1">
                <li>Conectem-se com outros surfistas</li>
                <li>Compartilhem registros de suas sessões de surf</li>
                <li>Descubram e avaliem praias e picos de surf</li>
                <li>Publiquem fotos e vídeos relacionados ao surf</li>
                <li>Participem de discussões da comunidade</li>
              </ul>
            </section>

            <section>
              <h3 className="text-xl font-semibold text-gray-900 dark:text-white mb-3">
                3. Registro e Conta de Usuário
              </h3>
              <p className="leading-relaxed mb-3">
                Para usar o Soul Surf, você deve:
              </p>
              <ul className="list-disc pl-6 space-y-1">
                <li>
                  Fornecer informações precisas e completas durante o registro
                </li>
                <li>Manter suas informações de conta atualizadas</li>
                <li>Ser responsável pela segurança de sua senha</li>
                <li>
                  Notificar-nos imediatamente sobre qualquer uso não autorizado
                  de sua conta
                </li>
                <li>Ter pelo menos 13 anos de idade</li>
              </ul>
            </section>

            <section>
              <h3 className="text-xl font-semibold text-gray-900 dark:text-white mb-3">
                4. Código de Conduta
              </h3>
              <p className="leading-relaxed mb-3">
                Ao usar o Soul Surf, você concorda em NÃO:
              </p>
              <ul className="list-disc pl-6 space-y-1">
                <li>Postar conteúdo ofensivo, difamatório ou ilegal</li>
                <li>Assediar ou intimidar outros usuários</li>
                <li>
                  Compartilhar informações falsas sobre condições de surf ou
                  segurança
                </li>
                <li>Usar a plataforma para fins comerciais não autorizados</li>
                <li>Violar direitos autorais ou propriedade intelectual</li>
                <li>Tentar hackear ou comprometer a segurança da plataforma</li>
              </ul>
            </section>

            <section>
              <h3 className="text-xl font-semibold text-gray-900 dark:text-white mb-3">
                5. Conteúdo do Usuário
              </h3>
              <p className="leading-relaxed mb-3">
                Você mantém os direitos sobre o conteúdo que publica, mas
                concede ao Soul Surf uma licença não exclusiva para usar, exibir
                e distribuir seu conteúdo na plataforma. Você é responsável por
                garantir que possui os direitos necessários para o conteúdo
                compartilhado.
              </p>
            </section>

            <section>
              <h3 className="text-xl font-semibold text-gray-900 dark:text-white mb-3">
                6. Segurança e Responsabilidade
              </h3>
              <p className="leading-relaxed mb-3">
                <strong className="text-orange-600 dark:text-orange-400">
                  IMPORTANTE:
                </strong>
                O Soul Surf é uma plataforma de compartilhamento de informações.
                Sempre pratique surf com segurança e responsabilidade:
              </p>
              <ul className="list-disc pl-6 space-y-1">
                <li>
                  Verifique as condições locais e previsões meteorológicas
                </li>
                <li>Conheça seus limites e habilidades</li>
                <li>Use equipamentos de segurança adequados</li>
                <li>Respeite as regras locais e outros surfistas</li>
                <li>Nunca pratique surf sozinho em condições perigosas</li>
              </ul>
            </section>

            <section>
              <h3 className="text-xl font-semibold text-gray-900 dark:text-white mb-3">
                7. Privacidade
              </h3>
              <p className="leading-relaxed">
                Sua privacidade é importante para nós. Coletamos apenas as
                informações necessárias para operar o serviço e nunca vendemos
                seus dados pessoais para terceiros. Para mais detalhes, consulte
                nossa Política de Privacidade.
              </p>
            </section>

            <section>
              <h3 className="text-xl font-semibold text-gray-900 dark:text-white mb-3">
                8. Moderação e Suspensão
              </h3>
              <p className="leading-relaxed">
                Reservamos o direito de moderar conteúdo e suspender ou encerrar
                contas que violem estes termos. As decisões de moderação são
                tomadas para manter um ambiente positivo e seguro para toda a
                comunidade.
              </p>
            </section>

            <section>
              <h3 className="text-xl font-semibold text-gray-900 dark:text-white mb-3">
                9. Limitação de Responsabilidade
              </h3>
              <p className="leading-relaxed">
                O Soul Surf é fornecido "como está". Não nos responsabilizamos
                por danos diretos ou indiretos resultantes do uso da plataforma,
                incluindo mas não limitado a acidentes relacionados ao surf
                baseados em informações da plataforma.
              </p>
            </section>

            <section>
              <h3 className="text-xl font-semibold text-gray-900 dark:text-white mb-3">
                10. Alterações nos Termos
              </h3>
              <p className="leading-relaxed">
                Podemos atualizar estes termos ocasionalmente. Alterações
                significativas serão comunicadas aos usuários. O uso continuado
                da plataforma após alterações constitui aceitação dos novos
                termos.
              </p>
            </section>

            <section>
              <h3 className="text-xl font-semibold text-gray-900 dark:text-white mb-3">
                11. Contato
              </h3>
              <p className="leading-relaxed">
                Para questões sobre estes termos, entre em contato conosco em:
                <br />
                <strong>Email:</strong> legal@soulsurf.com.br
                <br />
                <strong>Endereço:</strong> Fortaleza, Ceará, Brasil
              </p>
            </section>

            <div className="mt-8 p-4 bg-blue-50 dark:bg-blue-900/20 rounded-lg">
              <p className="text-sm text-blue-800 dark:text-blue-200">
                <strong>Lembre-se:</strong> O surf é uma atividade que envolve
                riscos. Sempre pratique com consciência, respeite o oceano e
                cuide da sua segurança e da dos outros. O Soul Surf é uma
                ferramenta para conectar a comunidade, não um substituto para
                bom senso e experiência no mar. 🏄‍♂️🌊
              </p>
            </div>
          </div>
        </div>

        <div className="p-6 border-t bg-gray-50 dark:bg-gray-800">
          <div className="flex flex-col sm:flex-row gap-3 justify-end">
            <Button
              variant="outline"
              onClick={onClose}
              className="w-full sm:w-auto"
            >
              Fechar
            </Button>
            <Button onClick={onClose} className="w-full sm:w-auto">
              Aceito os Termos
            </Button>
          </div>
        </div>
      </CardContent>
    </Card>
  );
}

export default TermsOfServiceCard;

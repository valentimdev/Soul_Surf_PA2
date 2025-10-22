import {
    useEffect,
    useState,
    useCallback
} from "react";

import {
    BeachService,
    type BeachDTO
} from "@/api/services/beachService";

import {
    BeachCard
} from "@/components/customCards/BeachCard.tsx";

// =========================================================================================================
// DEFINIÇÃO DAS PRAIAS MOCKADAS (DADOS DE TESTE OU PADRÃO)
// Estas informações de praias são criadas diretamente no frontend para serem exibidas.
// Elas são usadas para preencher a lista de praias inicial, ou como um fallback
// caso haja algum problema na comunicação com o backend (servidor).
// Cada praia é definida como um objeto que segue a estrutura esperada pelo tipo 'BeachDTO'.
// Os IDs são definidos com valores altos para minimizar a chance de conflito com IDs reais
// que poderiam vir de um banco de dados do backend.
// =========================================================================================================
const MOCKED_BEACHES: BeachDTO[] = [
    {
        id: 9999,
        nome: "Praia do Futuro",
        descricao: "A mais famosa de Fortaleza, com muitas barracas e coqueiros! É um local ideal para passar um dia inteiro de lazer e desfrutar do sol cearense. Conhecida pelas suas águas quentes e ondas propícias para o surf em alguns pontos.",
        localizacao: "Fortaleza, Ceará",
        caminhoFoto: "https://i.ibb.co/L5Q4m9W/praia-do-futuro-fortaleza.jpg", // <--- SUBSTITUA PELA SUA IMAGEM REAL DA PRAIA DO FUTURO
    },
    {
        id: 9998,
        nome: "Praia de Iracema",
        descricao: "Um ponto turístico icônico e histórico de Fortaleza, famoso pela sua ponte metálica, bares, restaurantes e uma vibrante vida noturna. É perfeita para caminhadas ao entardecer e para apreciar a arquitetura local.",
        localizacao: "Fortaleza, Ceará",
        caminhoFoto: "https://i.ibb.co/XXXYYY/praia-de-iracema.jpg", // <--- SUBSTITUA PELA SUA IMAGEM REAL DA PRAIA DE IRACEMA
    },
    {
        id: 9997,
        nome: "Praia do Iguape",
        descricao: "Uma praia mais afastada e tranquila, ideal para relaxar longe do burburinho da cidade. É muito procurada por praticantes de kitesurf devido aos ventos favoráveis e também oferece belas paisagens com suas dunas.",
        localizacao: "Aquiraz, Ceará",
        caminhoFoto: "https://i.ibb.co/AAABBB/praia-do-iguape.jpg", // <--- SUBSTITUA PELA SUA IMAGEM REAL DA PRAIA DO IGUAPE
    },
    {
        id: 9996,
        nome: "Praia Leste-Oeste",
        descricao: "Esta área da praia é mais urbana e tem uma conexão direta com a atividade portuária da cidade. Oferece uma vista diferente e é um local de movimento constante, ligando o centro da cidade a outras áreas costeiras.",
        localizacao: "Fortaleza, Ceará",
        caminhoFoto: "https://i.ibb.co/CCCDDD/praia-leste-oeste.jpg", // <--- SUBSTITUA PELA SUA IMAGEM REAL DA PRAIA LESTE-OESTE
    },
    {
        id: 9995,
        nome: "Praia do Titãnzinho",
        descricao: "Famosa pelas suas boas ondas e por ser um dos principais pontos de encontro para surfistas em Fortaleza. É um local com uma cultura de surf forte, onde muitos praticantes locais se reúnem para pegar as melhores ondas.",
        localizacao: "Fortaleza, Ceará",
        caminhoFoto: "https://i.ibb.co/EEEFFF/praia-do-titanzinho.jpg", // <--- SUBSTITUA PELA SUA IMAGEM REAL DA PRAIA DO TITÃNZINHO
    },
    // Você pode continuar adicionando mais objetos BeachDTO aqui para expandir a lista de praias mockadas.
];

// =========================================================================================================
// COMPONENTE PRINCIPAL: BeachsPage
// Este componente é responsável por renderizar a página que exibe a lista de praias.
// Ele gerencia o estado das praias, o carregamento e possíveis erros na busca de dados.
// =========================================================================================================
function BeachsPage() {

    const [beaches, setBeaches] = useState<BeachDTO[]>([]);
    const [isLoading, setIsLoading] = useState<boolean>(true);
    const [error, setError] = useState<string | null>(null);

    const retrieveBeachesData = useCallback(async () => {
        setIsLoading(true);
        setError(null);

        try {
            const backendResponseForBeaches = await BeachService.getAllBeaches();
            const beachesFromBackend = backendResponseForBeaches;

            const filteredBackendBeaches = beachesFromBackend.filter((backendBeachItem) => {
                const isBeachIdMocked = MOCKED_BEACHES.some((mockedBeachItem) => {
                    return mockedBeachItem.id === backendBeachItem.id;
                });
                return !isBeachIdMocked;
            });

            const finalBeachesListToDisplay = [
                ...MOCKED_BEACHES,
                ...filteredBackendBeaches
            ];

            setBeaches(finalBeachesListToDisplay);

        } catch (operationError) {
            console.error("Um erro ocorreu ao carregar as praias do serviço de backend:", operationError);
            setError("Houve uma falha ao tentar carregar as praias do servidor. Estamos exibindo praias de demonstração como alternativa.");
            setBeaches(MOCKED_BEACHES);

        } finally {
            setIsLoading(false);
        }
    }, []);

    useEffect(() => {
        retrieveBeachesData();
    }, [retrieveBeachesData]);

    if (isLoading) {
        return (
            <div
                className="flex justify-center items-center h-screen"
                role="status"
                aria-live="polite"
            >
                <p className="text-xl font-medium text-foreground">
                    Estamos carregando as praias para você. Por favor, aguarde um momento...
                </p>
            </div>
        );
    }

    if (!isLoading && beaches.length === 0 && !error) {
         return (
            <div className="flex flex-col ml-[10%] mr-[10%] mt-5">
                <div className="ml-5">
                    <h1 className="text-2xl font-bold text-foreground">Praias</h1>
                </div>
                <div className="ml-5 mt-5 p-4 bg-yellow-100 border border-yellow-200 text-yellow-800 rounded-md">
                    <p>
                        Não foi possível encontrar nenhuma praia para exibir no momento.
                        Verifique sua conexão ou tente novamente mais tarde.
                    </p>
                </div>
            </div>
        );
    }

    return (
        <div
            className="flex flex-col ml-[10%] mr-[10%] mt-5 p-4 bg-background rounded-lg shadow-md"
            aria-label="Seção de Praias Disponíveis"
        >
            <div className="mb-6">
                <h1 className="ml-5 text-3xl font-extrabold text-primary border-b-2 pb-2 border-border">
                    Nossas Praias para Surf
                </h1>
                <p className="ml-5 mt-2 text-foreground text-lg">
                    Explore os melhores picos de surf e as praias mais acolhedoras.
                </p>
            </div>

            {error && (
                <div
                    className="ml-5 mt-4 p-4 bg-destructive/10 text-destructive border border-destructive rounded-lg shadow-sm"
                    role="alert"
                    aria-atomic="true"
                >
                    <p className="font-semibold mb-1">Atenção:</p>
                    <p>{error}</p>
                </div>
            )}

            {/* AJUSTE AQUI: Para duas colunas em telas médias e maiores */}
            <div className="ml-5 mt-6 grid grid-cols-1 md:grid-cols-2 gap-6">
                {
                    beaches.map((currentBeachItem) => {
                        return (
                            <BeachCard key={currentBeachItem.id} beach={currentBeachItem} />
                        );
                    })
                }
            </div>

            <div className="mt-8 pt-4 border-t-2 border-border text-center text-muted-foreground text-sm">
                <p>&copy; {new Date().getFullYear()} Soul Surf. Todos os direitos reservados.</p>
                <p>Desenvolvido com paixão em Fortaleza, Ceará.</p>
            </div>
        </div>
    );
}

export default BeachsPage;
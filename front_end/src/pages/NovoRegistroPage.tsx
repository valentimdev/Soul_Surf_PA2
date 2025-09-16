import NovoRegistroCard from "@/components/customCards/NovoRegistroCard.tsx";

function NovoRegistroPage() {
  // Este componente serve como um "container" para centralizar o formulário
  return (
    <div className="w-full flex justify-center py-8 px-4 bg-background">
      <NovoRegistroCard />
    </div>
  );
}

export default NovoRegistroPage;
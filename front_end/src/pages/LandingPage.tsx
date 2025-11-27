import LandingCard from "../components/customCards/LandingCard";




const MainContent = () => (
  <main className="flex-1 flex items-center justify-center animate-fade-in-up">
    <LandingCard />
  </main>
);




function LandingPage() {

  return (
    <div className="relative h-100vh w-full flex flex-col bg-background text-foreground">
 
      <MainContent />

    </div>
  );
}

export default LandingPage;
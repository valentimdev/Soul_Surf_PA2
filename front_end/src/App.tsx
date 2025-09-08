import CadastroCard from './components/CadastroCard.tsx';
import Header from './components/Header.tsx';
import LoginCard from './components/LoginCard.tsx';

function App() {
  return (
    <div className="bg-[var(--card)] w-full h-screen flex flex-col ">
      <div className="flex-1 flex justify-center items-center my-8">
        <CadastroCard />
      </div>
    </div>
  );
}

export default App;

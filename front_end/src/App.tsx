import Header from './components/Header.tsx'
import LoginCard from './components/LoginCard.tsx'

function App() {
  return (
    <div className="bg-[var(--card)] w-full h-screen justify-center items-center ">
      <Header></Header>
      <div className="flex justify-center items-center h-[calc(100vh-96px)]">
        <LoginCard></LoginCard>
      </div>
    </div>
  )
}

export default App
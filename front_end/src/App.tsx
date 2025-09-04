import Header from './components/Header.tsx'
import LoginPage from '../../components/pages/LoginPage.tsx'

function App() {
  return (
    <div className="bg-[var(--card)] w-full h-screen justify-center items-center ">
      <Header></Header>
      <div className="flex justify-center items-center h-[calc(100vh-96px)]">
        <LoginPage></LoginPage>
      </div>
    </div>
  )
}

export default App
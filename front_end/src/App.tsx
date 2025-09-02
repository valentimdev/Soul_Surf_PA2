import React from 'react'
import Header from './components/Header.tsx'
import LoginPage from './components/pages/LoginPage.tsx'

function App() {
  return (
    <div className="bg-[var(--card)] w-full h-screen justify-center items-center ">
      <Header></Header>
      <LoginPage></LoginPage>
    </div>
  )
}

export default App
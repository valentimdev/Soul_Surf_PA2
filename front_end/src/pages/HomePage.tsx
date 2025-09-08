import React from 'react'

function HomePage() {
  return (
    <div className="flex w-full min-h-screen">
      <div className="hidden md:flex w-[30%] border red-400 ">
        {/* Left column content */}
      </div>
      <div className="w-full md:w-[40%] border green-400">
        {/* Middle column (feed) content */}
      </div>
      <div className="hidden md:flex w-[30%] border  blue-400 ">
        {/* Right column content */}
      </div>
    </div>
  )
}

export default HomePage
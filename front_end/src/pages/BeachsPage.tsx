import { BeachCard } from '@/components/customCards/BeachCard';
import { BeachCardTeste } from '@/components/customCards/BeachCard_1';
import React from 'react';

function BeachsPage() {
  // mock
  const beachData = [1, 2, 3, 4, 5, 6];

  return (
    <div className="flex flex-col ml-[10%] mr-[10%] border border-black mt-5">
      <div>
        <h1 className="ml-5 text-2xl font-bold">Praias</h1>
      </div>
      <div className="ml-5 mt-5 grid grid-cols-1 md:grid-cols-2 gap-5">
        {beachData.map((id) => (
          <BeachCardTeste key={id} />
        ))}
      </div>
    </div>
  );
}

export default BeachsPage;

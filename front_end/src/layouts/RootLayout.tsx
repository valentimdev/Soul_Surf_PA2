// src/layouts/RootLayout.tsx
import Header from '@/layouts/Header';
import React from 'react';
import { Outlet } from 'react-router-dom';

const RootLayout: React.FC = () => {
  return (
    <main>
    <Header></Header>
      <Outlet /> 
    </main>
  );
};

export default RootLayout;
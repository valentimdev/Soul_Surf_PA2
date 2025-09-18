// src/layouts/RootLayout.tsx
import Header from '@/layouts/Header';
import React from 'react';
import { Outlet, useLocation } from 'react-router-dom';
import SideBarLeft from '@/layouts/SideBarLeft';
import SideBarRight from '@/layouts/SideBarRight';
const RootLayout: React.FC = () => {
  const location = useLocation();
  const noSidebarRoutes = [
    '/login',
    '/cadastro',
    '/forgot-password',
    '/landing',
  ];
  const showSidebars = !noSidebarRoutes.includes(location.pathname);

  return (
    <main>
      {showSidebars ? (
        <>
          <div className="fixed top-0 left-0 right-0 z-50">
            <Header />
          </div>
          <div className="flex pt-20">
            <div className="hidden md:block w-[20%]">
              <div className="fixed w-[20%] h-screen">
                <SideBarLeft />
              </div>
            </div>

            <div className="w-full md:w-[60%]">
              <Outlet />
            </div>
            <div className="hidden md:block w-[20%]">
              {/* <SideBarRight /> */}
            </div>
          </div>
        </>
      ) : (
        <div className="w-full">
          <Outlet />
        </div>
      )}
    </main>
  );
};

export default RootLayout;

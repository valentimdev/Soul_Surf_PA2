import LandingCard from "../components/customCards/LandingCard";
import { FaInstagram, FaFacebook, FaTwitter } from "react-icons/fa";
import React from "react";


interface HeaderProps {
  title: string;
  subtitle: string;
}

const Header: React.FC<HeaderProps> = ({ title, subtitle }) => (
  <header className="absolute top-0 left-0 w-full p-6">
    {/*
    */}
    <h1 className="text-2xl font-bold text-white tracking-wider">
      {title}
      <span className="font-light">{subtitle}</span>
    </h1>
  </header>
);

const MainContent = () => (
  <main className="flex-1 flex items-center justify-center animate-fade-in-up">
    <LandingCard />
  </main>
);

interface SocialLinkProps {
  href: string;
  ariaLabel: string;
  IconComponent: React.ElementType;
}



function LandingPage() {

  return (
    <div className="relative h-100vh w-full flex flex-col bg-background text-foreground">
 
      <MainContent />

    </div>
  );
}

export default LandingPage;
// src/pages/ResetPasswordPage.tsx

import surfzada from "@/assets/surfzada.jpg";
import ResetPasswordCard from "@/components/customCards/ResetPasswordCard";

export default function ResetPasswordPage() {
  return (
    <div
      className="h-screen w-full flex items-center justify-center bg-cover bg-center bg-no-repeat"
      style={{ backgroundImage: `url(${surfzada})` }}
    >
      <div className="w-full max-w-md px-6">
        <div className="bg-white/95 backdrop-blur-sm rounded-2xl shadow-2xl p-8 border border-white/20">
          <ResetPasswordCard />
        </div>
      </div>
    </div>
  );
}
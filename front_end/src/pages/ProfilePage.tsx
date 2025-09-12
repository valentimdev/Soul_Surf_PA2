import { UserProfileCard } from '@/components/customCards/UserProfileCard';
import React from 'react';

function ProfilePage() {
  // mock
  const userData = {
    username: 'Kelly Slater',
    avatarSrc:
      'https://img.olympics.com/images/image/private/t_1-1_300/f_auto/primary/rousxeo5xvuqj3qvrxzq',
    coverImageSrc:'https://blogger.googleusercontent.com/img/b/R29vZ2xl/AVvXsEjKOd-OXzWMhz_Ve3eVE_kX8k1aEQeTRcsvYKGXAJD6Va_aJ436xpjuJqLbRFB4Kul14N95Mh-3dpvRQz8dQVJOyslgWu81QTANudK5eWoQiOTJFzyzlwZlosmz0ngM-ZR3voQhU50F9aUl/s1600/135-i-love-surf.jpg',
  };

  return (
    <div className="p-4">
      <UserProfileCard
        username={userData.username}
        userHandle={userData.userHandle}
        avatarSrc={userData.avatarSrc}
        coverImageSrc={userData.coverImageSrc}
      />
    </div>
  );
}

export default ProfilePage;
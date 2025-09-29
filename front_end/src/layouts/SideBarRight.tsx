import { BeachCard_backup } from '../components/customCards/BeachCard_backup.tsx';
import { HashtagCard } from '../components/customCards/HashtagCard';

function SideBarRight() {
  return (
    <div className="h-full flex flex-col items-center border border-amber-400">
      <div className="border border pink h-1/2 w-full">
        <BeachCard_backup />
      </div>
      <div className="border border ambar h-1/2 w-full">
        <HashtagCard></HashtagCard>
      </div>
    </div>
  );
}

export default SideBarRight;

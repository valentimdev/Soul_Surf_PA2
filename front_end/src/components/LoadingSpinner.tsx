export default function LoadingSpinner() {
    return (
        <div className="fixed inset-0 flex items-center justify-center bg-black/10 backdrop-blur-sm z-[9999]">
            <div className="w-12 h-12 border-4 border-gray-300 border-t-blue-600 rounded-full animate-spin"></div>
        </div>
    );
}

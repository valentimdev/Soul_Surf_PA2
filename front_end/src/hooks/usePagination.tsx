import { useState, useCallback } from 'react';

interface UsePaginationOptions {
    initialPage?: number;
    initialSize?: number;
}

interface PaginatedResponse<T> {
    content: T[];
    totalElements: number;
    totalPages: number;
    size: number;
    number: number;
    first: boolean;
    last: boolean;
}

export function usePagination<T>({ initialPage = 0, initialSize = 20 }: UsePaginationOptions = {}) {
    const [data, setData] = useState<T[]>([]);
    const [loading, setLoading] = useState(false);
    const [page, setPage] = useState(initialPage);
    const [size] = useState(initialSize);
    const [totalPages, setTotalPages] = useState(0);
    const [totalElements, setTotalElements] = useState(0);
    const [hasNext, setHasNext] = useState(false);
    const [hasPrevious, setHasPrevious] = useState(false);

    const updatePaginationData = useCallback((response: PaginatedResponse<T>, append = false) => {
        setData(prevData => append ? [...prevData, ...response.content] : response.content);
        setTotalPages(response.totalPages);
        setTotalElements(response.totalElements);
        setHasNext(!response.last);
        setHasPrevious(!response.first);
        setPage(response.number);
    }, []);

    const loadMore = useCallback(() => {
        if (hasNext) {
            setPage(prev => prev + 1);
        }
    }, [hasNext]);

    const reset = useCallback(() => {
        setData([]);
        setPage(0);
        setTotalPages(0);
        setTotalElements(0);
        setHasNext(false);
        setHasPrevious(false);
    }, []);

    return {
        data,
        setData,
        loading,
        setLoading,
        page,
        setPage,
        size,
        totalPages,
        totalElements,
        hasNext,
        hasPrevious,
        updatePaginationData,
        loadMore,
        reset,
    };
}
import React, { useState, useEffect } from 'react';
import { useSearchParams, Link } from 'react-router-dom';
import { searchThreads, ThreadSummary } from '../api/threads';
import ThreadCard from '../components/ThreadCard';
import LoadingSpinner from '../components/LoadingSpinner';

const SearchPage: React.FC = () => {
  const [searchParams] = useSearchParams();
  const query = searchParams.get('q') || '';

  const [threads, setThreads] = useState<ThreadSummary[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [page, setPage] = useState(0);
  const [hasMore, setHasMore] = useState(false);

  const PAGE_SIZE = 20;

  useEffect(() => {
    if (!query.trim()) return;
    const fetchResults = async () => {
      setLoading(true);
      setError(null);
      try {
        const res = await searchThreads(query, page, PAGE_SIZE);
        const data = Array.isArray(res.data) ? res.data : [];
        setThreads(data);
        setHasMore(data.length === PAGE_SIZE);
      } catch (err: unknown) {
        const message = err instanceof Error ? err.message : 'Search failed. Please try again.';
        setError(message);
      } finally {
        setLoading(false);
      }
    };
    fetchResults();
  }, [query, page]);

  return (
    <div className="page-container">
      <div className="page-header">
        <Link to="/" className="breadcrumb-link">← Home</Link>
        <h1 className="page-title">
          Search Results {query && <span className="search-query">for "{query}"</span>}
        </h1>
      </div>

      {!query.trim() && (
        <div className="empty-state">
          <p>Enter a search term to find threads.</p>
        </div>
      )}

      {loading && <LoadingSpinner />}

      {error && (
        <div className="alert alert-error">{error}</div>
      )}

      {!loading && !error && query.trim() && threads.length === 0 && (
        <div className="empty-state">
          <p>No threads found for "<strong>{query}</strong>".</p>
          <p>Try different keywords or <Link to="/" className="auth-link">browse categories</Link>.</p>
        </div>
      )}

      {!loading && threads.length > 0 && (
        <>
          <p className="search-result-count">
            Showing {threads.length} result{threads.length !== 1 ? 's' : ''}
          </p>
          <div className="threads-list">
            {threads.map(thread => (
              <ThreadCard key={thread.id} thread={thread} />
            ))}
          </div>

          {(page > 0 || hasMore) && (
            <div className="pagination">
              <button
                className="btn btn-secondary"
                onClick={() => setPage(p => p - 1)}
                disabled={page === 0}
              >
                ← Previous
              </button>
              <span className="pagination-info">Page {page + 1}</span>
              <button
                className="btn btn-secondary"
                onClick={() => setPage(p => p + 1)}
                disabled={!hasMore}
              >
                Next →
              </button>
            </div>
          )}
        </>
      )}
    </div>
  );
};

export default SearchPage;

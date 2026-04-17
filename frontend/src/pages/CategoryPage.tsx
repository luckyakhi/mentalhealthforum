import React, { useState, useEffect } from 'react';
import { useParams, Link } from 'react-router-dom';
import { getCategory, Category } from '../api/categories';
import { getThreadsByCategory, ThreadSummary } from '../api/threads';
import { useAuth } from '../context/AuthContext';
import ThreadCard from '../components/ThreadCard';
import LoadingSpinner from '../components/LoadingSpinner';

const CategoryPage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const { user } = useAuth();

  const [category, setCategory] = useState<Category | null>(null);
  const [threads, setThreads] = useState<ThreadSummary[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [page, setPage] = useState(0);
  const [hasMore, setHasMore] = useState(true);

  const PAGE_SIZE = 20;

  useEffect(() => {
    if (!id) return;
    const fetchData = async () => {
      setLoading(true);
      setError(null);
      try {
        const [catRes, threadsRes] = await Promise.all([
          getCategory(id),
          getThreadsByCategory(id, page, PAGE_SIZE),
        ]);
        setCategory(catRes.data);
        const threadData = Array.isArray(threadsRes.data) ? threadsRes.data : [];
        setThreads(threadData);
        setHasMore(threadData.length === PAGE_SIZE);
      } catch (err: unknown) {
        const message = err instanceof Error ? err.message : 'Failed to load category.';
        setError(message);
      } finally {
        setLoading(false);
      }
    };
    fetchData();
  }, [id, page]);

  const handlePrev = () => {
    if (page > 0) setPage(p => p - 1);
  };

  const handleNext = () => {
    if (hasMore) setPage(p => p + 1);
  };

  if (loading) return <LoadingSpinner />;

  if (error) {
    return (
      <div className="page-container">
        <div className="alert alert-error">{error}</div>
        <Link to="/" className="btn btn-secondary">Back to Home</Link>
      </div>
    );
  }

  return (
    <div className="page-container">
      <div className="page-header">
        <Link to="/" className="breadcrumb-link">← Home</Link>
        {category && (
          <>
            <h1 className="page-title">{category.name}</h1>
            <p className="page-description">{category.description}</p>
          </>
        )}
        {user && (
          <Link
            to="/thread/create"
            state={{ categoryId: id }}
            className="btn btn-primary"
          >
            + New Thread
          </Link>
        )}
      </div>

      <div className="threads-list">
        {threads.length === 0 ? (
          <div className="empty-state">
            <p>No threads in this category yet.</p>
            {user ? (
              <Link to="/thread/create" state={{ categoryId: id }} className="btn btn-primary">
                Start the first thread
              </Link>
            ) : (
              <p>
                <Link to="/login" className="auth-link">Sign in</Link> to create the first thread.
              </p>
            )}
          </div>
        ) : (
          threads.map(thread => (
            <ThreadCard key={thread.id} thread={thread} />
          ))
        )}
      </div>

      {(page > 0 || hasMore) && (
        <div className="pagination">
          <button
            className="btn btn-secondary"
            onClick={handlePrev}
            disabled={page === 0}
          >
            ← Previous
          </button>
          <span className="pagination-info">Page {page + 1}</span>
          <button
            className="btn btn-secondary"
            onClick={handleNext}
            disabled={!hasMore}
          >
            Next →
          </button>
        </div>
      )}
    </div>
  );
};

export default CategoryPage;

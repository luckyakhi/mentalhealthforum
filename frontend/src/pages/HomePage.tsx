import React, { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { getAllThreads, ThreadSummary } from '../api/threads';
import { useAuth } from '../context/AuthContext';
import ThreadCard from '../components/ThreadCard';
import LoadingSpinner from '../components/LoadingSpinner';

const PAGE_SIZE = 20;

const HomePage: React.FC = () => {
  const { user } = useAuth();
  const navigate = useNavigate();

  const [threads, setThreads] = useState<ThreadSummary[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [page, setPage] = useState(0);
  const [hasMore, setHasMore] = useState(true);
  const [searchQuery, setSearchQuery] = useState('');

  useEffect(() => {
    const fetchThreads = async () => {
      setLoading(true);
      setError(null);
      try {
        const res = await getAllThreads(page, PAGE_SIZE);
        const data = Array.isArray(res.data) ? res.data : [];
        setThreads(data);
        setHasMore(data.length === PAGE_SIZE);
      } catch {
        setError('Failed to load threads. Please try again later.');
      } finally {
        setLoading(false);
      }
    };
    fetchThreads();
  }, [page]);

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault();
    if (searchQuery.trim()) {
      navigate(`/search?q=${encodeURIComponent(searchQuery.trim())}`);
    }
  };

  return (
    <div className="page-container">
      <section className="hero">
        <div className="hero-content">
          <h1 className="hero-title">MindSpace</h1>
          <p className="hero-subtitle">A Safe Space to Talk</p>
          <p className="hero-description">
            Connect with others, share your experiences, and find support in our compassionate community.
            You are not alone.
          </p>
          <form className="hero-search" onSubmit={handleSearch}>
            <input
              type="text"
              className="hero-search-input"
              placeholder="Search threads..."
              value={searchQuery}
              onChange={e => setSearchQuery(e.target.value)}
            />
            <button type="submit" className="btn btn-primary">
              Search
            </button>
          </form>
        </div>
      </section>

      <section className="forum-section">
        <div className="forum-section-header">
          <div>
            <h2 className="section-title">All Discussions</h2>
            <p className="section-subtitle">Browse threads from the community</p>
          </div>
          {user ? (
            <Link to="/thread/create" className="btn btn-primary">
              + New Thread
            </Link>
          ) : (
            <Link to="/login" className="btn btn-secondary">
              Sign in to post
            </Link>
          )}
        </div>

        {loading && <LoadingSpinner />}

        {error && (
          <div className="alert alert-error">{error}</div>
        )}

        {!loading && !error && threads.length === 0 && (
          <div className="empty-state">
            <p>No threads yet. Be the first to start a discussion!</p>
            {user ? (
              <Link to="/thread/create" className="btn btn-primary">
                Start a Thread
              </Link>
            ) : (
              <p>
                <Link to="/login" className="auth-link">Sign in</Link> to create the first thread.
              </p>
            )}
          </div>
        )}

        {!loading && !error && threads.length > 0 && (
          <div className="threads-list">
            {threads.map(thread => (
              <ThreadCard key={thread.id} thread={thread} />
            ))}
          </div>
        )}

        {!loading && (page > 0 || hasMore) && (
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
      </section>

      <section className="info-section">
        <div className="info-grid">
          <div className="info-card">
            <div className="info-icon">🤝</div>
            <h3>Community Support</h3>
            <p>Connect with people who understand what you're going through.</p>
          </div>
          <div className="info-card">
            <div className="info-icon">🔒</div>
            <h3>Safe & Respectful</h3>
            <p>Our community guidelines ensure a respectful, supportive environment.</p>
          </div>
          <div className="info-card">
            <div className="info-icon">💚</div>
            <h3>You're Not Alone</h3>
            <p>Thousands of people share their journeys here every day.</p>
          </div>
        </div>
      </section>
    </div>
  );
};

export default HomePage;

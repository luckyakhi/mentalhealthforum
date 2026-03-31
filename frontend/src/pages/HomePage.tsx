import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { getCategories, Category } from '../api/categories';
import LoadingSpinner from '../components/LoadingSpinner';

const HomePage: React.FC = () => {
  const [categories, setCategories] = useState<Category[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [searchQuery, setSearchQuery] = useState('');
  const navigate = useNavigate();

  useEffect(() => {
    const fetchCategories = async () => {
      try {
        const response = await getCategories();
        const data = response.data;
        const sorted = Array.isArray(data)
          ? [...data].sort((a, b) => a.displayOrder - b.displayOrder)
          : [];
        setCategories(sorted);
      } catch (err: unknown) {
        const message = err instanceof Error ? err.message : 'Failed to load categories. Please try again later.';
        setError(message);
      } finally {
        setLoading(false);
      }
    };
    fetchCategories();
  }, []);

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
              placeholder="Search for topics, threads, or discussions..."
              value={searchQuery}
              onChange={e => setSearchQuery(e.target.value)}
            />
            <button type="submit" className="btn btn-primary">
              Search
            </button>
          </form>
        </div>
      </section>

      <section className="categories-section">
        <h2 className="section-title">Discussion Categories</h2>
        <p className="section-subtitle">Choose a topic to explore and join the conversation</p>

        {loading && <LoadingSpinner />}

        {error && (
          <div className="alert alert-error">
            <strong>Unable to load categories.</strong> {error}
          </div>
        )}

        {!loading && !error && categories.length === 0 && (
          <div className="empty-state">
            <p>No categories available yet. Check back soon!</p>
          </div>
        )}

        {!loading && !error && categories.length > 0 && (
          <div className="categories-grid">
            {categories.map(category => (
              <div
                key={category.id}
                className="category-card"
                onClick={() => navigate(`/category/${category.id}`)}
                role="button"
                tabIndex={0}
                onKeyDown={e => e.key === 'Enter' && navigate(`/category/${category.id}`)}
              >
                <div className="category-card-body">
                  <h3 className="category-card-title">{category.name}</h3>
                  <p className="category-card-description">{category.description}</p>
                </div>
                <div className="category-card-footer">
                  <span className="category-thread-count">
                    <span className="meta-icon">💬</span>
                    {category.threadCount} {category.threadCount === 1 ? 'thread' : 'threads'}
                  </span>
                  <span className="category-arrow">→</span>
                </div>
              </div>
            ))}
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

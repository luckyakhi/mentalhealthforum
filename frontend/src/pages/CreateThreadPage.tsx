import React, { useState, useEffect } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { getCategories, Category } from '../api/categories';
import { createThread } from '../api/threads';
import LoadingSpinner from '../components/LoadingSpinner';

interface LocationState {
  categoryId?: string;
}

const CreateThreadPage: React.FC = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const locationState = location.state as LocationState | null;

  const [categories, setCategories] = useState<Category[]>([]);
  const [loadingCategories, setLoadingCategories] = useState(true);

  const [categoryId, setCategoryId] = useState(locationState?.categoryId || '');
  const [title, setTitle] = useState('');
  const [content, setContent] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    const fetchCategories = async () => {
      try {
        const res = await getCategories();
        const data = Array.isArray(res.data) ? res.data : [];
        const sorted = [...data].sort((a, b) => a.displayOrder - b.displayOrder);
        setCategories(sorted);
        if (!categoryId && sorted.length > 0) {
          setCategoryId(sorted[0].id);
        }
      } catch {
        setError('Failed to load categories.');
      } finally {
        setLoadingCategories(false);
      }
    };
    fetchCategories();
  }, []); // eslint-disable-line react-hooks/exhaustive-deps

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);

    if (!categoryId) {
      setError('Please select a category.');
      return;
    }
    if (!title.trim()) {
      setError('Please enter a title.');
      return;
    }
    if (title.trim().length < 5) {
      setError('Title must be at least 5 characters.');
      return;
    }
    if (!content.trim()) {
      setError('Please enter the thread content.');
      return;
    }
    if (content.trim().length < 20) {
      setError('Content must be at least 20 characters.');
      return;
    }

    setSubmitting(true);
    try {
      const res = await createThread({
        categoryId,
        title: title.trim(),
        content: content.trim(),
      });
      navigate(`/thread/${res.data.id}`);
    } catch (err: unknown) {
      const axiosError = err as { response?: { data?: { message?: string } } };
      const serverMessage = axiosError?.response?.data?.message;
      setError(serverMessage || 'Failed to create thread. Please try again.');
    } finally {
      setSubmitting(false);
    }
  };

  if (loadingCategories) return <LoadingSpinner />;

  return (
    <div className="page-container">
      <div className="form-page-card">
        <h1 className="page-title">Create New Thread</h1>
        <p className="page-description">
          Share your thoughts, ask for support, or start a discussion.
        </p>

        {error && (
          <div className="alert alert-error">{error}</div>
        )}

        <form className="thread-form" onSubmit={handleSubmit}>
          <div className="form-group">
            <label htmlFor="category" className="form-label">Category</label>
            <select
              id="category"
              className="form-select"
              value={categoryId}
              onChange={e => setCategoryId(e.target.value)}
              required
            >
              <option value="">Select a category...</option>
              {categories.map(cat => (
                <option key={cat.id} value={cat.id}>{cat.name}</option>
              ))}
            </select>
          </div>

          <div className="form-group">
            <label htmlFor="title" className="form-label">Thread Title</label>
            <input
              id="title"
              type="text"
              className="form-input"
              placeholder="Enter a descriptive title..."
              value={title}
              onChange={e => setTitle(e.target.value)}
              required
              minLength={5}
              maxLength={200}
            />
            <span className="form-hint">{title.length}/200 characters</span>
          </div>

          <div className="form-group">
            <label htmlFor="content" className="form-label">Content</label>
            <textarea
              id="content"
              className="form-textarea"
              placeholder="Share your thoughts here. This is a safe space..."
              value={content}
              onChange={e => setContent(e.target.value)}
              required
              minLength={20}
              rows={10}
            />
            <span className="form-hint">Minimum 20 characters. Be kind and respectful.</span>
          </div>

          <div className="form-actions">
            <button
              type="submit"
              className="btn btn-primary"
              disabled={submitting}
            >
              {submitting ? 'Creating...' : 'Create Thread'}
            </button>
            <button
              type="button"
              className="btn btn-secondary"
              onClick={() => navigate(-1)}
            >
              Cancel
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default CreateThreadPage;

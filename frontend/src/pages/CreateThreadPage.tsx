import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { createThread } from '../api/threads';

const CreateThreadPage: React.FC = () => {
  const navigate = useNavigate();

  const [title, setTitle] = useState('');
  const [content, setContent] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);

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
    if (content.trim().length < 10) {
      setError('Content must be at least 10 characters.');
      return;
    }

    setSubmitting(true);
    try {
      const res = await createThread({ title: title.trim(), content: content.trim() });
      navigate(`/thread/${res.data.id}`);
    } catch (err: unknown) {
      const axiosError = err as { response?: { data?: { message?: string } } };
      setError(axiosError?.response?.data?.message || 'Failed to create thread. Please try again.');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="page-container">
      <div className="form-page-card">
        <h1 className="page-title">Start a New Thread</h1>
        <p className="page-description">
          Share your thoughts, ask for support, or start a discussion with the community.
        </p>

        {error && (
          <div className="alert alert-error">{error}</div>
        )}

        <form className="thread-form" onSubmit={handleSubmit}>
          <div className="form-group">
            <label htmlFor="title" className="form-label">Title</label>
            <input
              id="title"
              type="text"
              className="form-input"
              placeholder="Enter a clear, descriptive title..."
              value={title}
              onChange={e => setTitle(e.target.value)}
              required
              minLength={5}
              maxLength={200}
              autoFocus
            />
            <span className="form-hint">{title.length}/200 characters</span>
          </div>

          <div className="form-group">
            <label htmlFor="content" className="form-label">Content</label>
            <textarea
              id="content"
              className="form-textarea"
              placeholder="Share your thoughts here. This is a safe space — be kind and respectful..."
              value={content}
              onChange={e => setContent(e.target.value)}
              required
              minLength={10}
              rows={10}
            />
            <span className="form-hint">Minimum 10 characters.</span>
          </div>

          <div className="form-actions">
            <button
              type="submit"
              className="btn btn-primary"
              disabled={submitting}
            >
              {submitting ? 'Posting...' : 'Post Thread'}
            </button>
            <button
              type="button"
              className="btn btn-secondary"
              onClick={() => navigate('/')}
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
